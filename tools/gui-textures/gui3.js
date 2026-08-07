// GUI — un panneau PAR MACHINE, en aplats.
//
// Deux corrections, la première mesurée.
//
// 1. AUCUN GRAIN. Mesuré sur le GUI du four vanilla : 6 couleurs en tout, et le
//    panneau est un aplat #c6c6c6 (21 % des pixels), les alvéoles #8b8b8b (16 %).
//    J'y avais transposé le semis de marbre des blocs : sur 176x166 ça donne du
//    bruit sur toute la surface. Un bloc doit avoir de la matière dans un décor ;
//    un panneau doit se taire derrière son contenu. Donc : aplats francs.
//
// 2. UN GUI PAR MACHINE. Un fond partagé oblige toutes les machines à se ressembler
//    à l'ouverture, alors qu'elles ne font pas le même métier. Chacune a désormais
//    son panneau : sa bande de titre à sa couleur, et un BANDEAU D'ATELIER qui
//    reprend le motif de sa façade (mâchoires, cuve, roue, grille…). On reconnaît
//    la machine ouverte sans lire son nom.
//
// Le fond ne contient toujours aucune alvéole de slot : l'écran les dessine une par
// slot réel, ce qui laisse un seul fond valable quelle que soit la disposition.

const { Canvas } = require('./draw');
const { encodePNG } = require('./png');
const fs = require('fs'), path = require('path');

const W = 176, H = 166;

// Palette commune, calquée sur les valeurs vanilla mais en marbre.
const P = {
  hi: '#FFFFFF',
  panel: '#D6D6D6',   // le corps (l'équivalent du #c6c6c6 vanilla)
  panelLo: '#B4B4B4',
  slot: '#8E8E8E',
  slotLo: '#6E6E6E',
  line: '#4A4A4A',
  brass: '#C9A24A',
  brassLo: '#8A6A2A',
};

// Accent par machine : c'est lui qui donne son identité au panneau.
const ACCENT = {
  resonance_stabilizer: { deep: '#5C2C86', mid: '#8A47B8', hi: '#B57CE0' },
  component_assembler: { deep: '#2E323A', mid: '#454B56', hi: '#5E6672' },
  resonance_whetstone: { deep: '#6E6E6E', mid: '#9A9A9A', hi: '#C4C4C4' },
  crystal_crusher: { deep: '#5E3517', mid: '#A8632F', hi: '#C9834E' },
  flux_purifier: { deep: '#5C2C86', mid: '#8A47B8', hi: '#B57CE0' },
  crystal_roost: { deep: '#4A3520', mid: '#6E5436', hi: '#8F7048' },
  damping_array: { deep: '#166B72', mid: '#27A3AC', hi: '#5FD6DC' },
  field_emitter: { deep: '#5C2C86', mid: '#8A47B8', hi: '#B57CE0' },
  veskorian_alloy_forge: { deep: '#8E5A15', mid: '#D8922A', hi: '#F0B863' },
};

/** Rectangle en relief : liseré clair en haut/gauche, sombre en bas/droite. */
function raised(c, x, y, w, h, body, hi, lo) {
  c.rect(x, y, w, h, body);
  for (let i = x; i < x + w; i++) { c.set(i, y, hi); c.set(i, y + h - 1, lo); }
  for (let j = y; j < y + h; j++) { c.set(x, j, hi); c.set(x + w - 1, j, lo); }
  return c;
}

/** Rectangle en creux : l'inverse. */
function sunken(c, x, y, w, h, body, hi, lo) {
  c.rect(x, y, w, h, body);
  for (let i = x; i < x + w; i++) { c.set(i, y, lo); c.set(i, y + h - 1, hi); }
  for (let j = y; j < y + h; j++) { c.set(x, j, lo); c.set(x + w - 1, j, hi); }
  return c;
}

/** Flèche de progression : creusée (vide) ou pleine. */
function arrow(c, x, y, a, filled) {
  const body = filled ? a.mid : P.slot;
  c.rect(x, y + 5, 16, 7, body);
  for (let i = 0; i < 8; i++) c.rect(x + 16 + i, y + i, 1, 17 - i * 2, body);
  c.rect(x, y + 5, 16, 1, filled ? a.hi : P.slotLo);
  if (filled) c.rect(x, y + 6, 15, 1, a.hi);
  return c;
}

// Bandeaux d'atelier : le motif de la façade, transposé dans le GUI. C'est le
// signe le plus rapide pour savoir quelle machine on a ouverte.
const BANNER = {
  resonance_stabilizer: (c, a) => { // un cristal entre deux mors
    for (let i = 0; i < 3; i++) {
      const x = 20 + i * 46;
      c.poly([[x + 6, 60], [x + 12, 66], [x + 6, 72], [x, 66]], a.mid);
      c.poly([[x + 6, 63], [x + 9, 66], [x + 6, 69], [x + 3, 66]], a.hi);
    }
  },
  component_assembler: (c, a) => { // une file de plaques boulonnées
    for (let i = 0; i < 8; i++) raised(c, 14 + i * 19, 62, 15, 8, a.mid, a.hi, a.deep);
    for (let i = 0; i < 8; i++) c.rect(17 + i * 19, 65, 2, 2, P.brass);
  },
  resonance_whetstone: (c, a) => { // une file de meules
    for (let i = 0; i < 5; i++) {
      const x = 24 + i * 28;
      c.disc(x, 66, 5, a.mid);
      c.disc(x, 66, 2, a.deep);
    }
  },
  crystal_crusher: (c, a) => { // des dents qui se croisent
    for (let i = 0; i < 14; i++) {
      c.poly([[12 + i * 11, 60], [18 + i * 11, 60], [15 + i * 11, 66]], a.mid);
      c.poly([[17 + i * 11, 72], [23 + i * 11, 72], [20 + i * 11, 66]], a.deep);
    }
  },
  flux_purifier: (c, a) => { // un niveau de liquide sur toute la largeur
    sunken(c, 12, 60, 152, 12, P.slot, P.hi, P.slotLo);
    c.rect(13, 65, 150, 6, a.mid);
    c.rect(13, 65, 150, 1, a.hi);
  },
  crystal_roost: (c, a) => { // des planches et de la paille
    for (let i = 0; i < 4; i++) raised(c, 12, 58 + i * 4, 152, 4, a.mid, a.hi, a.deep);
  },
  damping_array: (c, a) => { // des lames espacées
    for (let i = 0; i < 5; i++) raised(c, 12, 58 + i * 3, 152, 2, a.mid, a.hi, a.deep);
  },
  field_emitter: (c, a) => { // des ondes concentriques
    for (let r = 4; r <= 28; r += 6) {
      for (let ang = 200; ang <= 340; ang += 3) {
        const rad = (ang * Math.PI) / 180;
        c.set(Math.round(88 + Math.cos(rad) * r), Math.round(74 + Math.sin(rad) * r), a.mid);
      }
    }
  },
};

/** Panneau de base : aplats francs, zéro grain. */
function panel(a) {
  const c = new Canvas(W, H);
  c.fill(P.panel);
  // Cadre : biseau clair dehors, ligne sombre au bord.
  for (let i = 0; i < W; i++) { c.set(i, 0, P.hi); c.set(i, H - 1, P.line); }
  for (let j = 0; j < H; j++) { c.set(0, j, P.hi); c.set(W - 1, j, P.line); }
  for (let i = 1; i < W - 1; i++) { c.set(i, H - 2, P.panelLo); }
  for (let j = 1; j < H - 1; j++) { c.set(W - 2, j, P.panelLo); }
  // Bandeau de titre à la couleur de la machine : on sait ce qu'on a ouvert avant
  // même d'avoir lu le nom.
  raised(c, 4, 4, W - 8, 12, a.deep, a.mid, P.line);
  // Zone d'atelier, en creux.
  sunken(c, 4, 18, W - 8, 58, P.panelLo, P.hi, P.slotLo);
  // Filet laiton de séparation.
  c.rect(4, 78, W - 8, 1, P.brass);
  c.rect(4, 79, W - 8, 1, P.brassLo);
  return c;
}

function build(name) {
  const a = ACCENT[name];
  const c = panel(a);
  if (BANNER[name]) BANNER[name](c, a);
  arrow(c, 79, 34, a, false);
  const atlas = new Canvas(256);
  atlas.draw(c, 0, 0);
  arrow(atlas, 176, 0, a, true);
  sunken(atlas, 200, 0, 18, 18, P.slot, P.hi, P.slotLo);
  sunken(atlas, 220, 0, 18, 18, P.slot, P.hi, P.slotLo);
  atlas.frameRect(220, 0, 18, 18, P.brass);
  return atlas;
}

/** L'émetteur a sa jauge au lieu d'une flèche. */
function emitter() {
  const a = ACCENT.field_emitter;
  const c = panel(a);
  BANNER.field_emitter(c, a);
  sunken(c, 151, 17, 12, 54, P.slot, P.hi, P.slotLo);
  c.rect(150, 16, 14, 1, P.brass);
  const atlas = new Canvas(256);
  atlas.draw(c, 0, 0);
  for (let y = 0; y < 52; y++) {
    const t = 1 - y / 52;
    atlas.rect(176, y, 10, 1, t > 0.66 ? a.hi : t > 0.33 ? a.mid : a.deep);
  }
  for (let y = 12; y < 52; y += 13) atlas.rect(176, y, 10, 1, a.deep);
  sunken(atlas, 200, 0, 18, 18, P.slot, P.hi, P.slotLo);
  sunken(atlas, 220, 0, 18, 18, P.slot, P.hi, P.slotLo);
  atlas.frameRect(220, 0, 18, 18, P.brass);
  return atlas;
}

const out = process.argv[2];
fs.mkdirSync(out, { recursive: true });
const set = {};
for (const n of Object.keys(ACCENT)) if (n !== 'field_emitter') set[n] = build(n);
set.field_emitter = emitter();
let max = 0;
for (const [n, c] of Object.entries(set)) {
  fs.writeFileSync(path.join(out, n + '.png'), encodePNG(256, 256, c.px));
  const s = new Set();
  for (let i = 0; i < 256 * 256; i++) if (c.px[i * 4 + 3] > 0) {
    s.add((c.px[i * 4] << 16) | (c.px[i * 4 + 1] << 8) | c.px[i * 4 + 2]);
  }
  if (s.size > max) max = s.size;
}
console.log(Object.keys(set).length + ' GUI — max ' + max + ' couleurs (four vanilla : 6)');
