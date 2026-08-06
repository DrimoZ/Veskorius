// Décodeur PNG minimal (assez pour lire les textures vanilla : 8 bits, RGB/RGBA/palette).
const zlib=require('zlib');
function decodePNG(buf){
  let p=8,w=0,h=0,depth=0,type=0,idat=[],plte=null,trns=null;
  while(p<buf.length){
    const len=buf.readUInt32BE(p), tag=buf.toString('ascii',p+4,p+8);
    const data=buf.subarray(p+8,p+8+len);
    if(tag==='IHDR'){w=data.readUInt32BE(0);h=data.readUInt32BE(4);depth=data[8];type=data[9];}
    else if(tag==='IDAT')idat.push(data);
    else if(tag==='PLTE')plte=data;
    else if(tag==='tRNS')trns=data;
    else if(tag==='IEND')break;
    p+=12+len;
  }

  const raw=zlib.inflateSync(Buffer.concat(idat));
  const ch={0:1,2:3,3:1,4:2,6:4}[type];
  const bits=ch*depth;
  const bpp=Math.max(1,Math.ceil(bits/8));
  const stride=Math.ceil(w*bits/8);
  const out=Buffer.alloc(h*stride);
  let pos=0;
  for(let y=0;y<h;y++){
    const f=raw[pos++]; const line=raw.subarray(pos,pos+stride); pos+=stride;
    const prev=y>0?out.subarray((y-1)*stride,y*stride):Buffer.alloc(stride);
    const cur=out.subarray(y*stride,(y+1)*stride);
    for(let i=0;i<stride;i++){
      const a=i>=bpp?cur[i-bpp]:0, b=prev[i], c=i>=bpp?prev[i-bpp]:0, x=line[i];
      let v;
      switch(f){case 0:v=x;break;case 1:v=x+a;break;case 2:v=x+b;break;
        case 3:v=x+((a+b)>>1);break;
        case 4:{const pa=Math.abs(b-c),pb=Math.abs(a-c),pc=Math.abs(a+b-2*c);
          v=x+(pa<=pb&&pa<=pc?a:pb<=pc?b:c);break;}
        default:throw new Error('filtre '+f);}
      cur[i]=v&255;
    }
  }
  // -> RGBA
  const px=new Uint8Array(w*h*4);
  for(let i=0;i<w*h;i++){
    let r,g,b,a=255;
    if(depth<8){
      // Palette compactée : on relit l'index bit à bit avant de le résoudre.
      const y=Math.floor(i/w), x=i%w;
      const perByte=8/depth;
      const byte=out[y*stride+Math.floor(x/perByte)];
      const shift=8-depth*((x%perByte)+1);
      const idx=(byte>>shift)&((1<<depth)-1);
      r=plte[idx*3];g=plte[idx*3+1];b=plte[idx*3+2];
      if(trns&&idx<trns.length)a=trns[idx];
    }
    else if(type===2){r=out[i*3];g=out[i*3+1];b=out[i*3+2];}
    else if(type===6){r=out[i*4];g=out[i*4+1];b=out[i*4+2];a=out[i*4+3];}
    else if(type===0){r=g=b=out[i];}
    else if(type===4){r=g=b=out[i*2];a=out[i*2+1];}
    else if(type===3){const idx=out[i];r=plte[idx*3];g=plte[idx*3+1];b=plte[idx*3+2];
      if(trns&&idx<trns.length)a=trns[idx];}
    px[i*4]=r;px[i*4+1]=g;px[i*4+2]=b;px[i*4+3]=a;
  }
  return {w,h,px};
}
module.exports={decodePNG};
