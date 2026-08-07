const fs=require('fs'),path=require('path');
const {encodePNG}=require('./png');
const {MARBLE,V,C,S,marble,edges,slab,faces,front,side,top}=require('./marble');
const {Canvas,rng}=require('./draw');
const TIER_OF={resonance_stabilizer:'t1',component_assembler:'t1',resonance_whetstone:'t1',
 crystal_crusher:'t1',flux_purifier:'t2',crystal_roost:'t2',field_emitter:'t2',
 tunable_field_emitter:'t2',damping_array:'t3',veskorian_alloy_forge:'t3'};
const ACCENT={damping_array:C,veskorian_alloy_forge:{deep:'#8E5A15',mid:'#D8922A',lite:'#F0B863',hot:'#FBE0B0'}};
const tex={};
for(const k of ['t1','t2','t3']){
  const nm={t1:'fractured',t2:'attuned',t3:'veskorian'}[k];
  tex[nm+'_chassis_side']=side(k,0x100+k.charCodeAt(1));
  tex[nm+'_chassis_top']=top(k,0x200+k.charCodeAt(1));}
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
tex.damaged_relay_front=front('t3',0x74D,faces.field_emitter,false);
tex.damaged_relay_front_on=front('t3',0x74D,faces.field_emitter,true);
tex.ancient_emitter_front_on=front('t1',0x748,faces.field_emitter,true);

const out=process.argv[2];fs.mkdirSync(out,{recursive:true});
const es=Object.entries(tex);
for(const[n,c]of es)fs.writeFileSync(path.join(out,n+'.png'),encodePNG(S,S,c.px));
const Z=9,CO=7,cell=S*Z+4,rows=Math.ceil(es.length/CO);
const sh=new Canvas(cell*CO,cell*rows);sh.fill('#101014');
es.forEach(([,c],i)=>{const ox=(i%CO)*cell+2,oy=Math.floor(i/CO)*cell+2;
 for(let y=0;y<S;y++)for(let x=0;x<S;x++){const s=(y*S+x)*4;
  for(let zy=0;zy<Z;zy++)for(let zx=0;zx<Z;zx++)sh.set(ox+x*Z+zx,oy+y*Z+zy,[c.px[s],c.px[s+1],c.px[s+2]]);}});
fs.writeFileSync(path.join(out,'_planche.png'),encodePNG(sh.size,sh.height,sh.px));
let max=0;for(const[n,c]of es){const s=new Set();
 for(let i=0;i<S*S;i++)s.add((c.px[i*4]<<16)|(c.px[i*4+1]<<8)|c.px[i*4+2]);
 if(s.size>max)max=s.size;}
console.log(es.length+' textures — max '+max+' couleurs (vanilla : 4-19)');
