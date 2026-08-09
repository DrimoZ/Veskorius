const fs=require('fs'),path=require('path');
const {encodePNG}=require('./png');const {Canvas}=require('./draw');
const {items,S}=require('./items');
const out=process.argv[2];fs.mkdirSync(out,{recursive:true});
const made=[];
for(const [n,draw] of Object.entries(items)){
  const c=new Canvas(S);draw(c);
  fs.writeFileSync(path.join(out,n+'.png'),encodePNG(S,S,c.px));made.push([n,c]);}
// Planche : zoom x7 sur damier, plus une bande à taille réelle (x2/x3).
const Z=7,C=6,cell=S*Z+6,rows=Math.ceil(made.length/C);
const sh=new Canvas(cell*C,cell*rows+3*S+16);
for(let y=0;y<sh.height;y++)for(let x=0;x<sh.size;x++){
  const t=(Math.floor(x/8)+Math.floor(y/8))%2===0?86:66;sh.set(x,y,[t,t,t]);}
made.forEach(([,c],i)=>{const ox=(i%C)*cell+3,oy=Math.floor(i/C)*cell+3;
 for(let y=0;y<S;y++)for(let x=0;x<S;x++){const s=(y*S+x)*4;if(c.px[s+3]===0)continue;
  for(let zy=0;zy<Z;zy++)for(let zx=0;zx<Z;zx++)sh.set(ox+x*Z+zx,oy+y*Z+zy,[c.px[s],c.px[s+1],c.px[s+2]]);}});
// Bande à taille réelle : c'est là qu'un sprite se révèle illisible.
let bx=4;const by=cell*rows+6;
for(const [,c] of made){for(let z of [2,3]){
  for(let y=0;y<S;y++)for(let x=0;x<S;x++){const s=(y*S+x)*4;if(c.px[s+3]===0)continue;
   for(let zy=0;zy<z;zy++)for(let zx=0;zx<z;zx++)sh.set(bx+x*z+zx,by+(z===2?0:S*2+4)+y*z+zy,[c.px[s],c.px[s+1],c.px[s+2]]);}}
  bx+=S*3+3;}
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
let max=0;for(const[,c]of made){const s=new Set();
 for(let i=0;i<S*S;i++)if(c.px[i*4+3]>0)s.add((c.px[i*4]<<16)|(c.px[i*4+1]<<8)|c.px[i*4+2]);
 if(s.size>max)max=s.size;}
console.log(made.length+' items — max '+max+' couleurs');
