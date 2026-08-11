const fs=require('fs'),path=require('path');
const {encodePNG}=require('./png');
const {MARBLE,V,C,A,S,marble,edges,slab,faces,front,side,top,plate}=require('./marble');
const {Canvas,rng}=require('./draw');
const TIER_OF={resonance_stabilizer:'t1',component_assembler:'t1',resonance_whetstone:'t1',
 crystal_crusher:'t1',flux_purifier:'t2',crystal_roost:'t2',field_emitter:'t2',
 tunable_field_emitter:'t2',damping_array:'t3',veskorian_alloy_forge:'t3',resonance_relay:'t3',
 flux_compressor:'t3',reclaimer:'t3',advanced_assembler:'t3',structural_synthesizer:'t3',deep_crystal_driller:'t3',slag_vent:'t3',
 deep_synthesis_chamber:'t3',harmonic_amplifier:'t3',
 automated_extraction_array:'t3',resonance_network_hub:'t3',convergence_core:'t3',rift_anchor:'t3',
 rift_core_extractor:'t3',rift_ward_emitter:'t3'};
const ACCENT={damping_array:C,rift_ward_emitter:C,harmonic_amplifier:C,resonance_network_hub:C,
 automated_extraction_array:A,
 deep_synthesis_chamber:{deep:'#5E7A8E',mid:'#8FB3C6',lite:'#BBD9E6',hot:'#FFFFFF'},deep_crystal_driller:A,slag_vent:{deep:'#6B3A12',mid:'#A8641F',lite:'#D08A3A',hot:'#EFC58A'},veskorian_alloy_forge:{deep:'#8E5A15',mid:'#D8922A',lite:'#F0B863',hot:'#FBE0B0'}};
const tex={};
for(const k of ['t1','t2','t3']){
  const nm={t1:'fractured',t2:'attuned',t3:'veskorian'}[k];
  tex[nm+'_chassis_side']=side(k,0x100+k.charCodeAt(1));
  tex[nm+'_chassis_top']=top(k,0x200+k.charCodeAt(1));
  // La plaque, c'est le flanc SANS son cadre. Rien d'autre ne change entre les deux : c'est
  // ce qui fait qu'un caisson connecté et un caisson isolé sont la même tôle.
  // PAS de texture de cadre séparée : les baguettes de géométrie reprennent `_side` et son
  // UV automatique les fait tomber pile sur la bordure qu'elles représentent. Une texture
  // de métal à part donnait un bloc posé qui ne ressemblait pas au bloc en main.
  tex[nm+'_chassis_plate']=plate(k);}
let seed=0x300;
for(const[n,k]of Object.entries(TIER_OF)){
  tex[n+'_front']=front(k,seed,faces[n],false,ACCENT[n]);
  tex[n+'_front_on']=front(k,seed,faces[n],true,ACCENT[n]);seed+=13;}
tex.attunement_console_front=front('t1',0x424,faces.attunement_console,true);

// Roche : pierre sombre (pas du marbre) pour trancher avec les machines.
const ROCK={tones:['#2E333C','#373D48','#414854','#4B5260'],w:[3,4,3,2],
 line:'#22262E',dark:'#1A1E24',metal:'#414854',metalHi:'#5A6270',crack:null};
function vein(c){
  const p=[[[6,0],[7,4],[5,8],[8,12],[6,15]],[[8,12],[12,10],[15,11]],[[0,11],[3,9],[5,8]]];
  for(const path of p)for(let i=0;i+1<path.length;i++){const[x1,y1]=path[i],[x2,y2]=path[i+1];
    c.line(x1,y1,x2,y2,V.mid);c.line(x1+1,y1,x2+1,y2,V.deep);}
  for(const[x,y]of[[7,4],[5,8],[8,12]])c.set(x,y,V.hot);return c;}
const v=marble(ROCK,0x711);vein(v);edges(v,ROCK);tex.resonance_veined_stone=v;
const sp=marble(ROCK,0x711);vein(sp);
{const r=rng(0x991);for(let n=0;n<4;n++){const x=2+Math.floor(r()*12),y=2+Math.floor(r()*12);
  sp.rect(x,y,2,2,'#9E8ABE');sp.set(x,y,'#E4DBF7');}}
edges(sp,ROCK);tex.resonance_veined_stone_spored=sp;
// Amas : de GROS cristaux qui percent, pas des pastilles.
const cl=marble(ROCK,0x722);
{for(const s of[[[8,0],[13,7],[8,13],[3,7]],[[3,8],[6,12],[3,16],[0,12]],[[13,8],[16,12],[13,16],[10,12]]]){
  const cx=s.reduce((a,p)=>a+p[0],0)/4,cy=s.reduce((a,p)=>a+p[1],0)/4;
  cl.poly(s,V.deep);cl.poly(s.map(([x,y])=>[x+(x<cx?1:-0.6),y]),V.mid);
  cl.poly(s.map(([x,y])=>[x+(x<cx?2:-1.6),y]),V.lite);
  cl.set(Math.round(cx),Math.round(cy),V.hot);}}
edges(cl,ROCK);tex.resonance_crystal_cluster=cl;
const fx=marble(ROCK,0x733);
{const r=rng(0x123);for(let n=0;n<16;n++){const x=Math.floor(r()*S),y=Math.floor(r()*S);
  fx.set(x,y,V.mid);if(r()>.5)fx.set(x+1,y,V.deep);}}
edges(fx,ROCK);tex.raw_flux_deposit=fx;

// --- Architecture de donjon (17-Dungeons.md) ------------------------------
// MÊME roche que la pierre veinée — le donjon et la poche de cristal doivent
// appartenir au même monde — mais APPAREILLÉE. C'est l'appareillage seul qui
// dit « bâti » plutôt que « naturel », et il se lit à un bloc de distance là où
// une différence de teinte ne se lirait pas du tout.
const {DISC12,DISC8,DISC4,fill,outline,ring}=require('./shapes');
const DEAD={deep:'#33303A',mid:'#403C48',lite:'#4D4856',hot:'#5C5668'}; // le violet ÉTEINT
function bricks(seed){
  const c=marble(ROCK,seed);
  // Assises de 4 px : joint sombre, puis une ligne claire dessous (la lumière
  // vient d'en haut, donc c'est le dessous du joint qui accroche).
  for(let y=0;y<S;y+=4)for(let x=0;x<S;x++){c.set(x,y,ROCK.line);c.set(x,y+1,ROCK.tones[3]);}
  // Joints verticaux décalés d'une assise à l'autre : sans le décalage, on lit
  // une grille de carrelage, pas un mur. Une assise sur deux n'en porte qu'un —
  // c'est l'appareil vanilla des stone bricks, mesuré plutôt que deviné.
  for(let r=0;r<4;r++)for(const jx of(r%2?[7]:[3,11]))
    for(let y=r*4+1;y<r*4+4;y++){c.set(jx,y,ROCK.line);if(jx+1<S)c.set(jx+1,y,ROCK.tones[3]);}
  return c;}
{const b=bricks(0x741);
 // Une veine de Résonance court dans un joint : la pierre a été taillée DANS le
 // filon, elle n'a pas été apportée d'ailleurs.
 for(let x=4;x<12;x++)b.set(x,8,V.deep);b.set(7,8,V.mid);b.set(8,8,V.mid);
 edges(b,ROCK);tex.veined_stone_bricks=b;}
{const b=bricks(0x742);
 for(const p of[[[2,1],[5,5],[3,9]],[[10,3],[13,8],[11,14]]])
   for(let i=0;i+1<p.length;i++)b.line(p[i][0],p[i][1],p[i+1][0],p[i+1][1],ROCK.dark);
 edges(b,ROCK);tex.cracked_veined_stone_bricks=b;}
// Pierre gravée : le seul bloc « écrit » du vocabulaire. Sert de borne — une
// salle qui en porte est une salle qui comptait pour eux.
{const {DIAMOND,DIAMOND_IN,DIAMOND_CORE}=require('./shapes');
 const g=marble(ROCK,0x743);
 slab(g,1,1,14,14,ROCK.tones[1],ROCK.tones[3],ROCK.line);
 outline(g,DIAMOND,ROCK.dark);fill(g,DIAMOND,ROCK.tones[0]);
 ring(g,DIAMOND,DIAMOND_IN,V.deep);fill(g,DIAMOND_IN,ROCK.tones[0]);
 fill(g,DIAMOND_CORE,V.mid);
 edges(g,ROCK);tex.chiseled_veined_stone=g;}
// Lampe : deux états. Éteinte elle reste une LENTILLE (on reconnaît l'objet même
// mort), allumée elle brûle — c'est ce contraste qui fait lire l'état du réseau
// d'un bout à l'autre d'un couloir (R2).
for(const on of[false,true]){
  const c=marble(ROCK,0x744);const a=on?V:DEAD;
  slab(c,1,1,14,14,ROCK.tones[0],ROCK.tones[3],ROCK.line);
  outline(c,DISC12,ROCK.line);fill(c,DISC12,a.deep);
  ring(c,DISC12,DISC8,on?a.mid:ROCK.dark);fill(c,DISC8,a.mid);fill(c,DISC4,a.hot);
  edges(c,ROCK);tex['resonance_lamp'+(on?'_on':'')]=c;}
// Conduit : une gouttière prise dans le mur. Le fil d'Ariane du donjon — il ne
// décore pas, il indique où l'énergie passe encore. Le bloc porte un AXE, donc il
// lui faut aussi une COUPE (la face du bout) : sans elle, un conduit vertical
// affiche une gouttière horizontale sur son sommet, et le tuyau a l'air coupé en
// travers.
for(const on of[false,true]){
  const c=bricks(0x745);const a=on?V:DEAD;
  c.rect(0,6,S,4,ROCK.dark);
  for(let x=0;x<S;x++){c.set(x,6,ROCK.line);c.set(x,9,ROCK.tones[3]);
    c.set(x,7,a.deep);c.set(x,8,a.mid);}
  for(let x=1;x<S;x+=5){c.set(x,7,a.hot);c.set(x,8,a.lite);}
  edges(c,ROCK);tex['conduit_line'+(on?'_on':'')]=c;}
for(const on of[false,true]){
  const c=marble(ROCK,0x749);const a=on?V:DEAD;
  slab(c,0,0,S,S,ROCK.tones[1],ROCK.tones[3],ROCK.line);
  c.rect(6,6,4,4,ROCK.dark);c.rect(7,7,2,2,a.mid);c.set(7,7,a.hot);
  edges(c,ROCK);tex['conduit_line_end'+(on?'_on':'')]=c;}
// Colonne cannelée : le bloc qui fait les colonnades, donc celui qui fait les
// monuments. Trois cannelures verticales et deux bagues — c'est la verticalité
// répétée qui donne l'impression de hauteur, bien plus que la hauteur réelle.
{const c=marble(ROCK,0x74A);
 slab(c,1,0,14,S,ROCK.tones[1],ROCK.tones[3],ROCK.line);
 for(const x of[4,7,10]){for(let y=0;y<S;y++){c.set(x,y,ROCK.line);c.set(x+1,y,ROCK.tones[3]);}}
 for(const y of[1,14]){for(let x=1;x<15;x++)c.set(x,y,ROCK.tones[3]);}
 for(const y of[2,13]){for(let x=1;x<15;x++)c.set(x,y,ROCK.line);}
 c.set(7,7,V.deep);c.set(8,7,V.mid);c.set(7,8,V.mid);c.set(8,8,V.deep);
 edges(c,ROCK);tex.veined_stone_column=c;}
{const c=marble(ROCK,0x74B);
 slab(c,1,1,14,14,ROCK.tones[1],ROCK.tones[3],ROCK.line);
 outline(c,DISC12,ROCK.line);fill(c,DISC12,ROCK.tones[2]);
 ring(c,DISC12,DISC8,ROCK.dark);fill(c,DISC8,ROCK.tones[0]);
 fill(c,DISC4,V.deep);
 edges(c,ROCK);tex.veined_stone_column_top=c;}
// Efflorescence de dissonance : ni un minerai ni une mousse — une CROÛTE grise
// et violacée, terne. La couleur doit dire « déchet », pas « trésor », sinon le
// joueur la mine au lieu de s'en méfier.
{const c=marble(ROCK,0x746);const r=rng(0x746);
 for(let n=0;n<11;n++){const x=1+Math.floor(r()*13),y=1+Math.floor(r()*13),w=1+Math.floor(r()*3);
   c.rect(x,y,w,w,'#5A5266');c.set(x,y,'#7C7290');if(r()>.6)c.set(x+w-1,y+w-1,'#3E384A');}
 edges(c,ROCK);tex.dissonance_bloom=c;}
// Sas : une plaque pleine, sans poignée ni serrure. Elle ne s'ouvre pas avec un
// objet, donc elle ne doit rien montrer qui ressemble à un mécanisme (R1).
for(const open of[false,true]){
  const c=marble(ROCK,0x747);const a=open?V:DEAD;
  slab(c,0,0,S,S,ROCK.tones[0],ROCK.tones[3],ROCK.line);
  for(const x of[3,12])for(let y=1;y<S-1;y++){c.set(x,y,ROCK.metalHi);c.set(x+1,y,ROCK.dark);}
  c.rect(6,1,4,14,ROCK.tones[2]);
  for(let y=2;y<14;y+=3){c.set(7,y,a.mid);c.set(8,y,a.deep);}
  c.rect(6,7,4,2,a.deep);c.set(7,7,a.hot);c.set(8,8,a.hot);
  edges(c,ROCK);tex['resonance_bulkhead'+(open?'_open':'')]=c;}
// Émetteur ancien : la façade de l'émetteur T2, montée sur le châssis FRACTURÉ.
// C'est volontaire — le joueur doit reconnaître l'objet qu'il fabriquera plus
// tard, dans un état antérieur au sien. Le T2 s'apprend ici, avant d'être craft.
// Bloc d'alliage veskorien : de la tôle rivetée, pas du marbre. C'est le seul bloc
// du mod qui doit se lire « métal » avant de se lire « veskorien ».
{const ALLOY={tones:['#5E5A70','#6E6A80','#7C7890','#8A86A0'],w:[3,4,3,2],
  line:'#4A4658',dark:'#3A3648',metal:'#A8A4BA',metalHi:'#D2CEE0',crack:null};
 const c=marble(ALLOY,0x74E);
 slab(c,0,0,S,S,ALLOY.tones[1],ALLOY.tones[3],ALLOY.line);
 for(const y of[3,12]){for(let x=1;x<15;x++)c.set(x,y,ALLOY.line);}
 for(const[x,y]of[[3,3],[12,3],[3,12],[12,12]]){c.rect(x,y,2,2,ALLOY.metal);c.set(x,y,ALLOY.metalHi);}
 c.rect(6,6,4,4,ALLOY.dark);c.rect(7,7,2,2,V.deep);c.set(7,7,V.mid);
 edges(c,ALLOY);tex.veskorian_alloy_block=c;}
tex.ancient_emitter_front=front('t1',0x748,faces.field_emitter,false);
// Sigma : la haute époque. Marbre sombre poli et ambre — on doit voir au premier
// coup d'oeil qu'on a changé d'âge, pas seulement de salle.
tex.sigma_console_front=front('t3',0x74C,faces.attunement_console,true);
// Console de l'Archive : même pupitre, accent BLANC-BLEU. Le geste qui ouvre un
// palier doit se reconnaître d'un coup d'œil aux trois consoles ; seule la couleur
// dit à quel âge on est arrivé.
tex.archive_console_front=front('t3',0x750,faces.attunement_console,true,{deep:'#5E7A8E',mid:'#8FB3C6',lite:'#BBD9E6',hot:'#FFFFFF'});
tex.damaged_relay_front=front('t3',0x74D,faces.field_emitter,false);
tex.damaged_relay_front_on=front('t3',0x74D,faces.field_emitter,true);
tex.ancient_emitter_front_on=front('t1',0x748,faces.field_emitter,true);

// --- La Faille : la seule matière du mod qui ne soit ni bâtie ni minée ------
// La pierre déformée doit se lire « quelque chose a mal tourné ici » AVANT qu'on
// sache ce qu'est une Faille : c'est le seul indice de repérage de l'endgame.
{const RIFT={deep:'#3A1D57',mid:'#5C2C86',lite:'#8A47B8',hot:'#B57CE0'};
 const d=marble(ROCK,0x760);const r=rng(0x760);
 // Des fractures qui PARTENT DU BORD vers le centre : la pierre est tirée, pas fissurée.
 for(let n=0;n<7;n++){let x=Math.floor(r()*S),y=r()>.5?0:S-1;
  if(r()>.5){y=Math.floor(r()*S);x=r()>.5?0:S-1;}
  for(let i=0;i<5+r()*5;i++){d.set(x,y,i<2?ROCK.dark:RIFT.deep);
   x+=x<8?1:-1;y+=y<8?1:-1;}}
 for(const[x,y]of[[7,7],[8,8],[5,10],[11,4]])d.set(x,y,RIFT.mid);
 edges(d,ROCK);tex.deformed_stone=d;}
// Le noyau : PAS un cristal. Un trou qui rayonne — le seul bloc du mod dont le
// centre soit plus sombre que ses bords.
{const c=marble(ROCK,0x761);const RIFT={deep:'#3A1D57',mid:'#5C2C86',lite:'#8A47B8',hot:'#B57CE0'};
 slab(c,0,0,S,S,RIFT.deep,RIFT.mid,ROCK.dark);
 outline(c,DISC12,RIFT.lite);fill(c,DISC12,RIFT.mid);
 ring(c,DISC12,DISC8,RIFT.hot);fill(c,DISC8,RIFT.deep);
 fill(c,DISC4,'#0A0410');
 edges(c,ROCK);tex.rift_core=c;}
// Résidu compressé : le bloc le plus TERNE du mod, et c'est le propos. Le dossier en
// fait la contre-preuve de la scorie — tous les sous-produits ne sont pas des
// nuisances — mais « utile » ne veut pas dire « précieux ». Pas de veine, pas d'éclat,
// pas une seule touche de violet : de la matière tassée, grise, qui a servi. Un joueur
// doit pouvoir en faire un mur entier sans que le mur réclame l'attention.
{const DULL={tones:['#4A4A4E','#54545A','#5E5E64','#68686E'],w:[3,4,3,2],
  line:'#3C3C40',dark:'#323236',metal:'#76767C',metalHi:'#8A8A90',crack:null};
 const c=marble(DULL,0x762);const r=rng(0x762);
 // Trois strates de compression, décalées : on voit que ça a été pressé en couches,
 // pas coulé. C'est le seul relief du bloc.
 for(const y of[4,8,12]){for(let x=0;x<S;x++)c.set(x,y,DULL.line);}
 for(let n=0;n<26;n++){const x=Math.floor(r()*S),y=Math.floor(r()*S);
  c.set(x,y,r()>.6?DULL.metal:DULL.dark);}
 edges(c,DULL);tex.synthesis_residue_block=c;}

// Sable de Résonance : du GRAIN, pas des cristaux. Un joueur doit le reconnaître comme
// du sable avant de le reconnaître comme veskorien — sinon il ne pensera jamais à le
// mettre au four. La teinte violette suffit à dire lequel.
{const SAND={tones:['#6E5A80','#7C6890','#8A76A0','#9884AE'],w:[3,4,3,2],
  line:'#5C4A6E',dark:'#4A3A5A',metal:'#A896BE',metalHi:'#C4B4D6',crack:null};
 const c=marble(SAND,0x763);const r=rng(0x763);
 // Grain fin et DENSE : c'est la densité qui lit « meuble » plutôt que « taillé ».
 for(let n=0;n<90;n++){const x=Math.floor(r()*S),y=Math.floor(r()*S);
  c.set(x,y,r()>.5?SAND.tones[0]:SAND.metal);}
 for(let n=0;n<12;n++){const x=Math.floor(r()*S),y=Math.floor(r()*S);c.set(x,y,V.deep);}
 tex.resonance_sand=c;}
// Verre de Résonance : un CADRE et presque rien dedans. Tout le travail est sur les
// bords — c'est ce qui fait lire « vitre » à travers la transparence, exactement comme
// le verre vanilla. Le centre reste vide, sinon on obtient un bloc teinté, pas une vitre.
{const c=new Canvas(S,S);
 const G={line:'#B79BD8',lite:'#D9C6EE',hot:'#F2E9FB'};
 for(let i=0;i<S;i++){c.set(i,0,G.line);c.set(i,S-1,G.line);c.set(0,i,G.line);c.set(S-1,i,G.line);}
 c.set(1,1,G.hot);c.set(S-2,1,G.lite);c.set(1,S-2,G.lite);
 // Deux éclats obliques : le reflet. Sans eux la vitre a l'air d'un trou.
 for(let i=2;i<7;i++)c.set(i,i,G.lite);
 for(let i=9;i<13;i++)c.set(i,i-6,G.lite);
 tex.resonance_glass=c;}

// Buisson de Floraison : QUATRE stades, et le passage du stade 2 au stade 3 doit se voir
// de loin — c'est le seul moment où le joueur a quelque chose à faire. La plante grandit
// aux stades 0-2 ; au stade 3 elle FLEURIT, et les fleurs sont la seule chose qui change
// de couleur. Un joueur qui traverse son champ doit repérer les plants mûrs sans
// s'arrêter devant chacun.
{const LEAF={deep:'#2E4A38',mid:'#3F6B4E',lite:'#54886A'};
 for(let age=0;age<4;age++){
  const c=new Canvas(S,S);const r=rng(0x770+age);
  const h=[5,9,13,14][age];
  // La tige, décentrée : une plante parfaitement symétrique lit comme un objet.
  for(let y=S-1;y>S-1-h;y--){c.set(7,y,LEAF.deep);c.set(8,y,LEAF.mid);}
  // Les feuilles, de plus en plus larges avec l'âge.
  const spread=[2,3,5,6][age];
  for(let n=0;n<8+age*6;n++){
   const y=S-2-Math.floor(r()*h);
   const x=7+Math.round((r()*2-1)*spread);
   if(x>0&&x<S)c.set(x,y,r()>.5?LEAF.mid:LEAF.lite);}
  // Les fleurs : seulement au dernier stade, et lumineuses.
  if(age===3){for(const[x,y]of[[4,4],[10,3],[6,7],[11,8],[3,9]]){
   c.rect(x,y,2,2,V.mid);c.set(x,y,V.hot);}}
  else if(age===2){for(const[x,y]of[[5,6],[10,7]])c.set(x,y,V.deep);}
  tex['resonance_bloom_bush_stage'+age]=c;}}

// Verre Lumineux : le MÊME cadre que le verre ordinaire, mais qui brûle. On garde la
// silhouette pour que les deux se rangent visuellement ensemble, et on ne change que
// l'intensité — parce que c'est exactement ce que la teinture change.
{const c=new Canvas(S,S);
 const G={line:'#E4CCF7',lite:'#F4ECFD',hot:'#FFFFFF'};
 for(let i=0;i<S;i++){c.set(i,0,G.line);c.set(i,S-1,G.line);c.set(0,i,G.line);c.set(S-1,i,G.line);}
 for(const[x,y]of[[1,1],[S-2,1],[1,S-2],[S-2,S-2]])c.set(x,y,G.hot);
 for(let i=2;i<7;i++)c.set(i,i,G.lite);
 for(let i=9;i<13;i++)c.set(i,i-6,G.lite);
 // Un halo interne, absent du verre ordinaire : la seule différence de motif.
 for(let i=4;i<12;i++){c.set(i,3,G.lite);c.set(i,12,G.lite);c.set(3,i,G.lite);c.set(12,i,G.lite);}
 tex.luminous_resonance_glass=c;}

// Pierre à Conduits Ancienne : de la pierre TRAVERSÉE. Des canaux gravés en creux, pas
// des veines — la veine dit « il y a du cristal dedans », le canal dit « quelqu'un a fait
// passer quelque chose ici ». C'est toute la différence entre le tell d'une poche et le
// tell d'une ruine, et les deux blocs doivent être distinguables au premier regard.
{const c=marble(ROCK,0x764);
 // Deux canaux orthogonaux, en creux, qui se croisent hors centre : un motif centré
 // lirait comme un ornement. Décentré, il lit comme de la plomberie.
 for(let x=0;x<S;x++){c.set(x,6,ROCK.line);c.set(x,7,ROCK.dark);c.set(x,8,ROCK.line);}
 for(let y=0;y<S;y++){c.set(11,y,ROCK.line);c.set(12,y,ROCK.dark);c.set(13,y,ROCK.line);}
 // Ce qui reste du flux : quelques points éteints, pas une ligne continue. Les conduits
 // sont MORTS — s'ils brillaient, le joueur croirait à un bloc actif.
 for(const[x,y]of[[2,7],[6,7],[12,3],[12,11],[12,7]])c.set(x,y,V.deep);
 c.set(12,7,V.mid);
 edges(c,ROCK);tex.ancient_conduit_stone=c;}

// Cratère météorique : un IMPACT. Un anneau sombre projeté, et un éclat lumineux au
// centre. Vu de dessus — c'est la seule façon dont on le verra jamais, puisqu'il est plat
// au sol — le contraste anneau/centre doit porter à vingt blocs, sinon on lui court à
// côté sans le voir pendant les dix minutes qu'on a.
{const c=new Canvas(S,S);const r=rng(0x765);
 const D={ring:'#2A2130',dust:'#4A3A5A'};
 for(let n=0;n<70;n++){const a=r()*Math.PI*2,d=3+r()*4.5;
  const x=Math.round(8+Math.cos(a)*d),y=Math.round(8+Math.sin(a)*d);
  if(x>=0&&x<S&&y>=0&&y<S)c.set(x,y,r()>.4?D.ring:D.dust);}
 c.rect(6,6,4,4,V.deep);c.rect(7,7,2,2,V.mid);c.set(7,7,V.hot);c.set(8,8,V.lite);
 tex.meteoric_crater=c;}

// VERRE CONNECTÉ : le cadre sort de la texture.
//
// Tant qu'il y était dessiné, chaque bloc portait ses quatre bordures et un mur de verre
// s'affichait en quadrillage. Le cadre est désormais posé en GÉOMÉTRIE, uniquement là où
// le verre s'arrête (voir ConnectedGlassBlock). Il faut donc deux textures par verre :
// une plaque sans bord, et une couleur de cadre pour les bagues.
for (const [name, G] of [
  ['resonance_glass', {line:'#B79BD8',lite:'#D9C6EE',hot:'#F2E9FB'}],
  ['luminous_resonance_glass', {line:'#E4CCF7',lite:'#F4ECFD',hot:'#FFFFFF'}]]) {
  // La plaque : les seuls reflets, rien au bord. Le centre reste vide, sinon on obtient
  // un bloc teinté et non une vitre.
  const pane = new Canvas(S, S);
  for (let i = 2; i < 7; i++) pane.set(i, i, G.lite);
  for (let i = 9; i < 13; i++) pane.set(i, i - 6, G.lite);
  pane.set(3, 2, G.hot); pane.set(11, 4, G.hot);
  tex[name + '_pane'] = pane;
  // Le cadre : plein, c'est une barre de quelques pixels vue de près.
  const frame = new Canvas(S, S);
  frame.rect(0, 0, S, S, G.line);
  for (let i = 0; i < S; i++) frame.set(i, 0, G.lite);
  tex[name + '_frame'] = frame;
}

const out=process.argv[2];fs.mkdirSync(out,{recursive:true});
const es=Object.entries(tex);
for(const[n,c]of es)fs.writeFileSync(path.join(out,n+'.png'),encodePNG(S,S,c.px));
const Z=9,CO=7,cell=S*Z+4,rows=Math.ceil(es.length/CO);
const sh=new Canvas(cell*CO,cell*rows);sh.fill('#101014');
es.forEach(([,c],i)=>{const ox=(i%CO)*cell+2,oy=Math.floor(i/CO)*cell+2;
 for(let y=0;y<S;y++)for(let x=0;x<S;x++){const s=(y*S+x)*4;
  for(let zy=0;zy<Z;zy++)for(let zx=0;zx<Z;zx++)sh.set(ox+x*Z+zx,oy+y*Z+zy,[c.px[s],c.px[s+1],c.px[s+2]]);}});
// LA PLANCHE DE CONTACT NE VA PAS DANS LE PACK DE RESSOURCES.
//
// Elle sert à relire toutes les textures d'un coup pendant le travail ; livrée dans
// assets/, Minecraft la COUD DANS L'ATLAS comme n'importe quelle texture. Une planche
// de 708x890 y plafonne le mipmapping de tout l'atlas au niveau 1 — les blocs vus de
// loin perdent leur lissage, sur l'ensemble du mod, à cause d'une image de debug.
// Le jeu le disait dans son log depuis le début : « limits mip level from 4 to 1 ».
const sheetDir = path.join('build', 'texture-sheets');
fs.mkdirSync(sheetDir, { recursive: true });
fs.writeFileSync(path.join(sheetDir, path.basename(out) + '.png'),
  encodePNG(sh.size, sh.height, sh.px));
let max=0;for(const[n,c]of es){const s=new Set();
 for(let i=0;i<S*S;i++)s.add((c.px[i*4]<<16)|(c.px[i*4+1]<<8)|c.px[i*4+2]);
 if(s.size>max)max=s.size;}
console.log(es.length+' textures — max '+max+' couleurs (vanilla : 4-19)');
