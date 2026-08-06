const fs=require('fs'),path=require('path');
const {encodePNG}=require('./png');
const {MARBLE,V,C,S,marble,edges,slab,faces,front,side,top}=require('./marble');
const {Canvas,rng}=require('./draw');
const TIER_OF={resonance_stabilizer:'t1',component_assembler:'t1',resonance_whetstone:'t1',
 crystal_crusher:'t1',flux_purifier:'t2',crystal_roost:'t2',field_emitter:'t2',
 tunable_field_emitter:'t2',damping_array:'t3'};
const ACCENT={damping_array:C};
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
