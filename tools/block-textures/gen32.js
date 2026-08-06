const fs = require('fs');
const path = require('path');
const { encodePNG } = require('./png');
const { textures } = require('./blocks32');

const S = 32;
const outDir = process.argv[2];
if (!outDir) throw new Error('usage: node gen32.js <dossier>');
fs.mkdirSync(outDir, { recursive: true });

const entries = Object.entries(textures);
for (const [name, c] of entries) {
  fs.writeFileSync(path.join(outDir, name + '.png'), encodePNG(S, S, c.px));
}

/** Planche : chaque texture ×4, plus une bande de rendu « en paroi » (3x3 carrelé). */
function sheet(list, zoom, cols) {
  const cell = S * zoom + 4;
  const rowsN = Math.ceil(list.length / cols);
  const W = cell * cols;
  const H = cell * rowsN;
  const px = new Uint8Array(W * H * 4);
  for (let i = 0; i < W * H; i++) {
    px[i * 4] = 18; px[i * 4 + 1] = 18; px[i * 4 + 2] = 22; px[i * 4 + 3] = 255;
  }
  list.forEach(([, c], idx) => {
    const ox = (idx % cols) * cell + 2;
    const oy = Math.floor(idx / cols) * cell + 2;
    for (let y = 0; y < S; y++) {
      for (let x = 0; x < S; x++) {
        const s = (y * S + x) * 4;
        for (let dy = 0; dy < zoom; dy++) {
          for (let dx = 0; dx < zoom; dx++) {
            const d = ((oy + y * zoom + dy) * W + ox + x * zoom + dx) * 4;
            px[d] = c.px[s]; px[d + 1] = c.px[s + 1]; px[d + 2] = c.px[s + 2]; px[d + 3] = 255;
          }
        }
      }
    }
  });
  return { W, H, px };
}

/** Carrelage 3x3 d'une texture : le seul test honnête pour une roche. */
function tiled(c, zoom) {
  const W = S * 3 * zoom, H = S * 3 * zoom;
  const px = new Uint8Array(W * H * 4);
  for (let y = 0; y < H; y++) {
    for (let x = 0; x < W; x++) {
      const sx = Math.floor(x / zoom) % S, sy = Math.floor(y / zoom) % S;
      const s = (sy * S + sx) * 4;
      const d = (y * W + x) * 4;
      px[d] = c.px[s]; px[d + 1] = c.px[s + 1]; px[d + 2] = c.px[s + 2]; px[d + 3] = 255;
    }
  }
  return { W, H, px };
}

const s = sheet(entries, 4, 7);
fs.writeFileSync(path.join(outDir, '_planche32.png'), encodePNG(s.W, s.H, s.px));

for (const n of ['resonance_veined_stone', 'resonance_crystal_cluster', 'raw_flux_deposit']) {
  const t = tiled(textures[n], 3);
  fs.writeFileSync(path.join(outDir, '_tile_' + n + '.png'), encodePNG(t.W, t.H, t.px));
}

console.log(`${entries.length} textures 32x32`);
console.log(entries.map(([n], i) => `${i}: ${n}`).join('\n'));
