// Textures d'entité 64x64 — mêmes règles que les blocs et les items : palette
// indexée, aucun fondu alpha, arêtes franches.
//
// Une texture d'entité n'est PAS une texture de bloc dépliée : elle est lue en
// mouvement, de loin, et souvent de dos. Ce qui doit porter, c'est la répartition
// des VALEURS (où est le sombre, où est le clair) — les détails fins disparaissent
// à trois blocs.
const fs = require('fs'), path = require('path');
const { encodePNG } = require('./png');
const { Canvas, rng } = require('./draw');

const S = 64;

/** Le Gardien : la matière de la Faille sur une carcasse veskorienne. */
function riftGuardian() {
  const c = new Canvas(S, S);
  const RIFT = { line: '#1E0F2E', deep: '#3A1D57', mid: '#5C2C86', lite: '#8A47B8', hot: '#B57CE0' };
  const IRON = { line: '#1A1E24', deep: '#2E323A', mid: '#454B56', lite: '#5E6672' };
  const r = rng(0x9A1);

  // Fond entièrement transparent : on ne peint que les faces utilisées, sinon les
  // marges du dépliage bavent sur les arêtes du modèle.
  // Torse (0,0 → 14x20 déplié en 4 faces sur 44 de large).
  c.rect(0, 0, 44, 28, IRON.deep);
  for (let y = 0; y < 28; y++) {
    for (let x = 0; x < 44; x++) {
      if (r() > 0.82) c.set(x, y, IRON.mid);
    }
  }
  // La fracture : une veine violette qui descend le torse de face. C'est le seul
  // élément clair de la silhouette, donc c'est lui qu'on voit en premier.
  for (let y = 4; y < 24; y++) {
    const x = 22 + Math.round(Math.sin(y * 0.5) * 2);
    c.set(x, y, RIFT.mid);
    c.set(x + 1, y, RIFT.deep);
    if (y % 5 === 0) c.set(x, y, RIFT.hot);
  }
  // Tête (0,28 → 8x7).
  c.rect(0, 28, 32, 14, IRON.mid);
  c.rect(8, 32, 8, 4, RIFT.deep);
  c.rect(9, 33, 2, 2, RIFT.hot);
  c.rect(13, 33, 2, 2, RIFT.hot);
  // Épaulières (32,28).
  c.rect(32, 28, 24, 12, IRON.lite);
  for (let y = 28; y < 40; y += 3) c.rect(32, y, 24, 1, IRON.line);
  // Bras (0,44) et jambes (24,44).
  c.rect(0, 44, 20, 20, IRON.deep);
  c.rect(24, 44, 24, 20, IRON.mid);
  for (let y = 44; y < 64; y++) {
    for (let x = 0; x < 48; x++) {
      if (r() > 0.88) c.set(x, y, IRON.line);
    }
  }
  return c;
}

const out = process.argv[2];
fs.mkdirSync(out, { recursive: true });
const set = { rift_guardian: riftGuardian() };
for (const [n, c] of Object.entries(set)) {
  fs.writeFileSync(path.join(out, n + '.png'), encodePNG(S, S, c.px));
}
console.log(Object.keys(set).length + ' texture(s) d\'entité');
