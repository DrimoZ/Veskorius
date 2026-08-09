// Logo du mod — icône de la liste des mods (logo.png, 256) et image de projet CurseForge (512).
//
// Mêmes règles que les textures (voir README, section « textures ») : palette indexée, AUCUN
// fondu alpha, arêtes franches. Un logo composé en dégradé fabrique des dizaines de teintes
// presque identiques et devient de la bouillie dès qu'il est affiché à 32 px — or c'est à cette
// taille qu'on le verra le plus souvent, dans la liste des mods.
//
// Le sujet est le mod en une image : un cristal, et le CHAMP autour de lui. Pas de câble, pas
// d'engrenage. La coupole est ce que le joueur voit vraiment en jeu.
//
//   node tools/logo/genlogo.js
//
// Écrit src/main/resources/logo.png (versionné, embarqué dans le jar) et
// build/texture-sheets/veskorius-512.png (pour CurseForge, hors dépôt).

const fs = require('fs');
const path = require('path');
const { encodePNG } = require('../block-textures/png');

// Palette fixe. Dix teintes, pas une de plus — chacune a une raison d'être.
const P = {
  stone: [0x1a, 0x1d, 0x24, 255],   // la roche, fond
  stoneL: [0x21, 0x25, 0x2e, 255],  // grain clair
  stoneD: [0x15, 0x18, 0x1e, 255],  // grain sombre
  vein: [0x2b, 0x3a, 0x4a, 255],    // la veine dans la pierre
  ringFar: [0x1f, 0x4d, 0x5c, 255], // coupole, anneau lointain
  ringMid: [0x2f, 0x7f, 0x92, 255],
  ringNear: [0x4f, 0xc3, 0xd9, 255],
  facetD: [0x3e, 0x8f, 0xa8, 255],  // cristal, facette à l'ombre
  facetL: [0xbf, 0xf2, 0xfa, 255],  // cristal, facette éclairée
  core: [0x8b, 0x5c, 0xf6, 255],    // le cœur : la bande violette
  amber: [0xe0, 0xa3, 0x3c, 255],   // la troisième bande, en pointillé
};

// PRNG déterministe : le logo doit être identique à chaque exécution, sinon le fichier
// versionné bouge à chaque régénération pour rien.
function mulberry32(a) {
  return function () {
    a |= 0; a = (a + 0x6d2b79f5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

function render(S) {
  const px = new Uint8Array(S * S * 4);
  const rnd = mulberry32(0x5e5c0de);
  const set = (x, y, c) => {
    if (x < 0 || y < 0 || x >= S || y >= S) return;
    const i = (y * S + x) * 4;
    px[i] = c[0]; px[i + 1] = c[1]; px[i + 2] = c[2]; px[i + 3] = c[3];
  };

  const cx = S / 2;
  const cy = S * 0.56;
  const u = S / 256; // tout est dessiné en unités de 256, puis mis à l'échelle

  // 1. La roche, avec son grain. Beaucoup de pixels diffèrent de leur voisin, mais de très peu :
  //    c'est la FRÉQUENCE du grain qui fait la matière, pas son amplitude.
  for (let y = 0; y < S; y++) {
    for (let x = 0; x < S; x++) {
      const r = rnd();
      set(x, y, r < 0.28 ? P.stoneD : r < 0.56 ? P.stoneL : P.stone);
    }
  }

  // 2. Des veines : quelques marches aléatoires, comme dans la pierre veinée du mod.
  for (let v = 0; v < 7; v++) {
    let x = rnd() * S;
    let y = rnd() * S;
    let dx = (rnd() - 0.5) * 2;
    let dy = (rnd() - 0.5) * 2;
    for (let step = 0; step < S * 0.9; step++) {
      set(Math.round(x), Math.round(y), P.vein);
      if (rnd() < 0.35) set(Math.round(x) + 1, Math.round(y), P.vein);
      dx += (rnd() - 0.5) * 0.4;
      dy += (rnd() - 0.5) * 0.4;
      const n = Math.hypot(dx, dy) || 1;
      x += dx / n; y += dy / n;
      if (x < 0 || y < 0 || x >= S || y >= S) break;
    }
  }

  // 3. La coupole : trois anneaux nets. C'est la signature visuelle du champ en jeu — la portée
  //    se lit comme une sphère de particules, et c'est ce qui doit se reconnaître à 32 px.
  const rings = [
    { r: 116 * u, t: 2 * u, c: P.ringFar, gap: 9 },
    { r: 90 * u, t: 2 * u, c: P.ringMid, gap: 6 },
    { r: 64 * u, t: 3 * u, c: P.ringNear, gap: 0 },
  ];
  for (const ring of rings) {
    const steps = Math.ceil(2 * Math.PI * ring.r * 2);
    for (let i = 0; i < steps; i++) {
      const a = (i / steps) * Math.PI * 2;
      // Un anneau pointillé se lit comme des particules ; un anneau plein se lit comme un tuyau.
      // C'est précisément ce qu'on ne veut PAS suggérer.
      if (ring.gap && i % (ring.gap * 2) < ring.gap) continue;
      for (let t = 0; t < ring.t; t++) {
        const rr = ring.r + t;
        set(Math.round(cx + Math.cos(a) * rr), Math.round(cy + Math.sin(a) * rr), ring.c);
      }
    }
  }

  // 4. Trois éclats ambre sur l'anneau lointain : la troisième bande harmonique existe, et le
  //    logo le dit sans l'expliquer.
  for (const a of [-Math.PI / 2, Math.PI / 6, (Math.PI * 5) / 6]) {
    for (let t = -2 * u; t < 3 * u; t++) {
      for (let w = -1; w <= 1; w++) {
        const rr = 116 * u + t;
        const aa = a + (w * 1.6) / (116 * u);
        set(Math.round(cx + Math.cos(aa) * rr), Math.round(cy + Math.sin(aa) * rr), P.amber);
      }
    }
  }

  // 5. Le cristal : un hexagone allongé, deux facettes franches, un cœur violet. Aucune
  //    transition douce — l'arête entre les deux facettes EST le volume.
  const hw = 26 * u;   // demi-largeur
  const hh = 62 * u;   // demi-hauteur
  const sh = 22 * u;   // hauteur des pointes
  for (let y = -hh; y <= hh; y++) {
    const ay = Math.abs(y);
    let w = hw;
    if (ay > hh - sh) w = hw * ((hh - ay) / sh); // les deux pointes
    for (let x = -w; x <= w; x++) {
      set(Math.round(cx + x), Math.round(cy + y), x < -w * 0.15 ? P.facetD : P.facetL);
    }
  }
  // L'arête centrale, tracée par-dessus : c'est elle qui sépare les deux facettes.
  for (let y = -hh; y <= hh; y++) {
    const ay = Math.abs(y);
    let w = hw;
    if (ay > hh - sh) w = hw * ((hh - ay) / sh);
    if (w < 1) continue;
    set(Math.round(cx - w * 0.15), Math.round(cy + y), P.ringMid);
  }
  // Le cœur : un losange violet, la bande du champ vue à travers le cristal.
  const ch = 26 * u;
  for (let y = -ch; y <= ch; y++) {
    const w = (hw * 0.5) * (1 - Math.abs(y) / ch);
    for (let x = -w; x <= w; x++) set(Math.round(cx + x), Math.round(cy + y), P.core);
  }

  return px;
}

const out = [
  [256, path.join('src', 'main', 'resources', 'logo.png')],
  [512, path.join('build', 'texture-sheets', 'veskorius-512.png')],
];
for (const [size, file] of out) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, encodePNG(size, size, render(size)));
  console.log(size + '  ->  ' + file);
}
