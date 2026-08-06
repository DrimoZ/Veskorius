// Rend les sprites en PNG 16x16 + une planche de contrôle agrandie.
const fs = require('fs');
const path = require('path');
const { encodePNG } = require('./png');
const { sprites } = require('./sprites');

const SIZE = 16;

function hex(c) {
  const m = /^#([0-9a-f]{6})$/i.exec(c);
  if (!m) throw new Error('couleur invalide : ' + c);
  const v = parseInt(m[1], 16);
  return [(v >> 16) & 255, (v >> 8) & 255, v & 255];
}

/** Valide strictement : une carte mal alignée produirait un sprite décalé en silence. */
function rasterize(name, def) {
  const { palette, rows } = def;
  if (rows.length !== SIZE) {
    throw new Error(`${name} : ${rows.length} lignes au lieu de ${SIZE}`);
  }
  const px = new Uint8Array(SIZE * SIZE * 4);
  rows.forEach((row, y) => {
    if (row.length !== SIZE) {
      throw new Error(`${name} ligne ${y} : ${row.length} colonnes au lieu de ${SIZE} — "${row}"`);
    }
    for (let x = 0; x < SIZE; x++) {
      const ch = row[x];
      if (ch === '.') continue;
      const col = palette[ch];
      if (!col) throw new Error(`${name} ligne ${y} col ${x} : '${ch}' absent de la palette`);
      const [r, g, b] = hex(col);
      const i = (y * SIZE + x) * 4;
      px[i] = r; px[i + 1] = g; px[i + 2] = b; px[i + 3] = 255;
    }
  });
  return px;
}

/** Planche de contrôle : tous les sprites agrandis, pour juger à l'œil. */
function contactSheet(entries, zoom, cols) {
  const cell = SIZE * zoom + zoom * 2;
  const rowsN = Math.ceil(entries.length / cols);
  const W = cell * cols;
  const H = cell * rowsN;
  const px = new Uint8Array(W * H * 4);
  // Fond damier clair/sombre pour voir la transparence ET les contours sombres.
  for (let y = 0; y < H; y++) {
    for (let x = 0; x < W; x++) {
      const i = (y * W + x) * 4;
      const t = (Math.floor(x / 8) + Math.floor(y / 8)) % 2 === 0 ? 90 : 70;
      px[i] = t; px[i + 1] = t; px[i + 2] = t; px[i + 3] = 255;
    }
  }
  entries.forEach(([, sprite], idx) => {
    const ox = (idx % cols) * cell + zoom;
    const oy = Math.floor(idx / cols) * cell + zoom;
    for (let y = 0; y < SIZE; y++) {
      for (let x = 0; x < SIZE; x++) {
        const s = (y * SIZE + x) * 4;
        if (sprite[s + 3] === 0) continue;
        for (let dy = 0; dy < zoom; dy++) {
          for (let dx = 0; dx < zoom; dx++) {
            const d = ((oy + y * zoom + dy) * W + ox + x * zoom + dx) * 4;
            px[d] = sprite[s]; px[d + 1] = sprite[s + 1];
            px[d + 2] = sprite[s + 2]; px[d + 3] = 255;
          }
        }
      }
    }
  });
  return { W, H, px };
}

const outDir = process.argv[2];
if (!outDir) throw new Error('usage: node generate.js <dossier de sortie>');
fs.mkdirSync(outDir, { recursive: true });

const rendered = [];
for (const [name, def] of Object.entries(sprites)) {
  const px = rasterize(name, def);
  rendered.push([name, px]);
  fs.writeFileSync(path.join(outDir, name + '.png'), encodePNG(SIZE, SIZE, px));
}

const sheet = contactSheet(rendered, 8, 6);
fs.writeFileSync(path.join(outDir, '_planche.png'), encodePNG(sheet.W, sheet.H, sheet.px));
console.log(`${rendered.length} sprites écrits dans ${outDir}`);
console.log(rendered.map(([n]) => n).join('\n'));
