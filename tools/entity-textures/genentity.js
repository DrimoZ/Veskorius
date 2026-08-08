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

/**
 * Couches d'armure portée. Ce ne sont pas des textures d'objet : elles s'appliquent sur
 * le MODELE du joueur, donc ce qui compte est la répartition des valeurs sur des bandes
 * larges — un détail fin y disparaît complètement.
 *
 * Deux fichiers obligatoires : layer_1 porte casque, plastron et bottes, layer_2 les
 * jambières. Il en manque un et le joueur s'affiche en damier violet — un défaut qu'on ne
 * voit qu'en équipant, jamais en regardant l'objet.
 */
function armorLayer(second) {
  const c = new Canvas(64, 32);
  const A = { line: '#2A2833', deep: '#4A4658', mid: '#7C7890', lite: '#A8A4BA', hot: '#D2CEE0' };
  const V = { deep: '#5C2C86', mid: '#8A47B8', hot: '#E4CCF7' };
  const r = rng(second ? 0xB02 : 0xB01);
  // Toute la planche en métal, grain léger : ce qui n'est pas utilisé par le modèle ne
  // se voit pas, et remplir évite les bavures aux coutures.
  c.rect(0, 0, 64, 32, A.mid);
  for (let y = 0; y < 32; y++) {
    for (let x = 0; x < 64; x++) {
      if (r() > 0.86) c.set(x, y, A.deep);
      else if (r() > 0.94) c.set(x, y, A.lite);
    }
  }
  // Bandes horizontales : elles lisent comme des plaques rivetées une fois sur le corps.
  for (let y = second ? 4 : 6; y < 32; y += 6) c.rect(0, y, 64, 1, A.line);
  if (!second) {
    // La veine de résonance sur le torse (zone du plastron du modèle vanilla).
    for (let y = 21; y < 31; y++) c.set(22, y, V.mid);
    c.set(22, 24, V.hot);
    // Épaulières : deux carrés plus clairs aux zones de bras.
    c.rect(44, 18, 8, 4, A.lite);
    c.rect(52, 18, 8, 4, A.lite);
  }
  return c;
}

const out = process.argv[2];
fs.mkdirSync(out, { recursive: true });
const set = { rift_guardian: riftGuardian() };
for (const [n, c] of Object.entries(set)) {
  fs.writeFileSync(path.join(out, n + '.png'), encodePNG(S, S, c.px));
}
// Les couches d'armure vont ailleurs : models/armor, pas entity.
const armorOut = process.argv[3];
if (armorOut) {
  fs.mkdirSync(armorOut, { recursive: true });
  for (const [suffix, second] of [['1', false], ['2', true]]) {
    const c = armorLayer(second);
    fs.writeFileSync(path.join(armorOut, 'veskorian_alloy_layer_' + suffix + '.png'),
      encodePNG(64, 32, c.px));
  }
  console.log('2 couches d armure');
}
console.log(Object.keys(set).length + ' texture(s) d\'entité');
