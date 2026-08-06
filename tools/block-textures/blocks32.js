// Textures de bloc 32x32 — châssis par palier + emblèmes de machine + roche.
//
// PRINCIPE DIRECTEUR : magie ET technologie, pas l'un décoré par l'autre.
// La technologie apporte la GÉOMÉTRIE (plaques, biseaux, rivets, aérations, bezels).
// La magie apporte le TRACÉ : des gravures creusées dans le métal, qui ne sont pas
// un décor mais un circuit — elles partent des bords, convergent vers le cœur de la
// machine, et s'allument quand la Résonance passe. Éteinte, une machine montre un
// sillon mort ; en marche, le même sillon conduit. C'est la seule chose qui change,
// et c'est ce qui doit se lire de loin.
//
// LE PALIER SE LIT SUR LE CHÂSSIS, pas sur un chiffre :
//   T1 « Fracturé »  — pierre récupérée, cuivre patiné, gravures INTERROMPUES : de la
//                      technologie ramassée dans une ruine, qui ne conduit qu'à moitié.
//   T2 « Accordé »   — acier et laiton, gravures CONTINUES : c'est restauré, ça circule.
//   T3 « Veskorien » — alliage sombre, double réseau de gravures, accents ambre.

const {
  Canvas, rng, grain, bevel, rivet, engrave, patina,
  gradient, ao, dither, scratches, bloom, vignette, speculars,
} = require('./draw');

const S = 32;

// --- Paliers --------------------------------------------------------------

const TIERS = {
  t1: {
    name: 'fractured',
    out: '#211E1A', dark: '#38352E', base: '#4A4740', light: '#5E5A50', edge: '#767063',
    trim: '#B4633A', trimHi: '#D68B5C', trimLo: '#7A3F22',
    patina: '#4E9B7A',
    groove: '#241F1A',
    glowOff: '#3A2C48', glowOn: '#9B59D0', coreOn: '#E7CDF7',
    lampOff: '#2C2A26', lampOn: '#8CE8A0',
    broken: true, // gravures interrompues
  },
  t2: {
    name: 'attuned',
    out: '#14181E', dark: '#282E3A', base: '#39404E', light: '#4E5768', edge: '#6E7889',
    trim: '#C9A24A', trimHi: '#EAC97E', trimLo: '#8A6A2A',
    patina: null,
    groove: '#171B22',
    glowOff: '#33294A', glowOn: '#9B59D0', coreOn: '#EEDCFA',
    lampOff: '#22262E', lampOn: '#8CE8A0',
    broken: false,
  },
  t3: {
    name: 'veskorian',
    out: '#15131C', dark: '#26232F', base: '#35323F', light: '#474351', edge: '#615C6E',
    trim: '#D8922A', trimHi: '#F2BC63', trimLo: '#8E5A15',
    patina: null,
    groove: '#181521',
    glowOff: '#20303A', glowOn: '#35C0C8', coreOn: '#D6FAFC',
    lampOff: '#20222A', lampOn: '#8CE8A0',
    broken: false,
  },
};

// --- Plaque de base -------------------------------------------------------

/**
 * Tôle : la brique de tout le reste. Sept couches, dans l'ordre matière → modelé →
 * usure → lumière. Une seule passe de bruit sur un aplat reste un aplat bruité ;
 * c'est l'EMPILEMENT à faible opacité qui fait une surface.
 */
function plate(t, seed) {
  const c = new Canvas(S);
  c.fill(t.base);                                   // 1. matière
  grain(c, seed, t.light, t.dark, 0.20);            // 2. grain de laminage
  dither(c, 0, 0, S, S, t.dark, 0.2, 40);           // 3. trame : casse l'uniformité
  gradient(c, t.light, t.dark, 30);                 // 4. lumière haut-gauche
  scratches(c, seed + 41, t.edge, t.out, 6);        // 5. usure
  c.frameRect(0, 0, S, S, t.out);                   // 6. contour
  bevel(c, 1, 1, S - 2, S - 2, t.edge, t.dark);
  vignette(c, t.out, 30);                           // 7. séparation entre blocs
  speculars(c, [[2, 2], [3, 1], [1, 3]], t.edge);
  return c;
}

/** Les quatre rivets d'angle, à l'intérieur du biseau. */
function corners(c, t) {
  for (const [x, y] of [[3, 3], [S - 5, 3], [3, S - 5], [S - 5, S - 5]]) {
    rivet(c, x, y, t.trim, t.trimHi, t.trimLo);
  }
  return c;
}

/**
 * Le réseau de gravures qui monte des quatre bords vers le centre. Au palier 1 il est
 * INTERROMPU (des segments manquent) : la machine est bricolée à partir de restes, et
 * ça se voit sans qu'on ait à l'écrire nulle part.
 */
function channels(c, t, glow, toward = 12) {
  const m = S / 2;
  const runs = [
    [[m - 1, 1], [m - 1, toward]],
    [[m, S - 2], [m, S - 1 - toward]],
    [[1, m - 1], [toward, m - 1]],
    [[S - 2, m], [S - 1 - toward, m]],
  ];
  runs.forEach((pts, i) => {
    if (t.broken && i % 2 === 1) {
      // segment mangé : on ne trace que la moitié depuis le bord
      const [a, b] = pts;
      const mid = [Math.round((a[0] + b[0]) / 2), Math.round((a[1] + b[1]) / 2)];
      engrave(c, [a, mid], t.groove, glow);
    } else {
      engrave(c, pts, t.groove, glow);
    }
  });
  return c;
}

// --- Châssis (le bloc lui-même, et les flancs de toutes ses machines) -----

function chassisSide(t, seed) {
  const c = plate(t, seed);
  // Panneau central en retrait. L'occlusion sur son bord intérieur est ce qui le
  // rend CREUX plutôt que peint : sans elle, le biseau seul se lit comme un trait.
  c.rect(6, 6, S - 12, S - 12, t.dark);
  grain(c, seed + 7, t.base, t.out, 0.16, (x, y) => x > 6 && x < S - 7 && y > 6 && y < S - 7);
  dither(c, 7, 7, S - 14, S - 14, t.base, 0.2, 55);
  ao(c, 6, 6, S - 12, S - 12, t.out, 3);
  bevel(c, 6, 6, S - 12, S - 12, t.dark, t.edge); // biseau inversé = creux

  // Deux fers plats horizontaux, façon cerclage, avec leur propre ombre portée.
  for (const y of [12, S - 14]) {
    c.rect(1, y, S - 2, 2, t.base);
    grain(c, seed + y, t.light, t.dark, 0.3, (px, py) => py >= y && py < y + 2);
    bevel(c, 1, y, S - 2, 2, t.edge, t.dark);
    c.rect(1, y + 2, S - 2, 1, t.out, 60); // ombre portée sous le fer plat
  }

  channels(c, t, t.glowOff, 13);
  corners(c, t);
  if (t.patina) patina(c, seed + 3, t.patina, 6);
  return c;
}

function chassisTop(t, seed) {
  const c = plate(t, seed);
  // Aérations : deux grilles de fentes, la technologie qui respire. Chaque fente est
  // un vrai trou — fond noir, occlusion, arête éclairée sur sa lèvre basse.
  for (const ox of [7, 18]) {
    for (let i = 0; i < 3; i++) {
      const oy = 9 + i * 5;
      c.rect(ox, oy, 7, 3, t.out);
      c.rect(ox + 1, oy + 1, 5, 1, '#000000', 90);
      ao(c, ox, oy, 7, 3, t.out, 1);
      c.rect(ox, oy + 3, 7, 1, t.edge, 120); // lèvre éclairée sous la fente
    }
  }
  // Un anneau gravé autour des grilles : le circuit fait le tour avant de descendre.
  engrave(c, [[4, 5], [S - 5, 5], [S - 5, S - 6], [4, S - 6], [4, 5]], t.groove, t.glowOff);
  corners(c, t);
  if (t.patina) patina(c, seed + 5, t.patina, 5);
  return c;
}

// --- Face avant d'une machine --------------------------------------------

/**
 * Façade : le châssis du palier, un bezel encastré, l'emblème de la machine dedans,
 * un témoin d'état. Allumée, SEUL change ce qui conduit — gravures, cœur de
 * l'emblème, témoin. Le boîtier, lui, ne bouge pas : c'est ce qui rend l'allumage
 * lisible au lieu de faire clignoter tout le bloc.
 */
function machineFront(t, seed, emblem, on) {
  const c = plate(t, seed);
  channels(c, t, on ? t.glowOn : t.glowOff, 9);
  corners(c, t);
  if (t.patina) patina(c, seed + 3, t.patina, 5);

  // Bezel : une vraie lunette métallique — cadre biseauté, ombre portée sur le
  // boîtier, puis un puits sombre et occlus. C'est l'empilement qui donne la
  // profondeur ; un simple rectangle noir se serait lu comme un autocollant.
  c.rect(6, 6, 20, 20, t.dark);
  bevel(c, 6, 6, 20, 20, t.edge, t.out);
  c.rect(6, 26, 20, 1, t.out, 70);          // ombre portée de la lunette
  // Lunette : teinte SOURDE du trim, éclairée seulement en haut à gauche. Montée en
  // trim plein, elle formait un anneau saturé qui volait la vedette à l'emblème —
  // or c'est l'emblème qui porte l'information, le cadre ne fait que l'encadrer.
  c.frameRect(7, 7, 18, 18, t.trimLo);
  for (let i = 7; i < 25; i++) {
    c.set(i, 7, t.trim, 150);
    c.set(7, i, t.trim, 110);
  }
  c.rect(8, 8, 16, 16, t.groove);
  grain(c, seed + 91, t.dark, '#000000', 0.2,
    (x, y) => x >= 8 && x < 24 && y >= 8 && y < 24);
  ao(c, 8, 8, 16, 16, '#000000', 3);
  speculars(c, [[8, 8], [9, 7]], t.trimHi);

  // Halo : la vitre s'éclaire AVANT l'emblème, et la lumière déborde du cœur.
  if (on) {
    c.rect(9, 9, 14, 14, t.glowOn, 30);
    bloom(c, 16, 16, 9, t.glowOn, 46);
  }

  emblem(c, t, on);
  if (on) bloom(c, 16, 16, 5, t.coreOn, 34);

  // Témoin d'état : une réglette sous le bezel, encastrée dans le boîtier. Placé sur
  // les BORDS du bloc (première version), il se lisait comme deux languettes vertes
  // collées à l'extérieur de la machine — un accessoire, pas un voyant.
  const lamp = on ? t.lampOn : t.lampOff;
  c.rect(13, 26, 6, 2, t.out);
  c.rect(14, 26, 4, 1, lamp);
  if (on) c.rect(14, 27, 4, 1, lamp, 90);
  return c;
}

// --- Emblèmes (dessinés dans la fenêtre 8..23) ---------------------------
// Un emblème = le GESTE de la machine, pas son nom. On doit pouvoir dire ce que
// fait le bloc sans jamais avoir lu une ligne de doc.

const dim = (t, on, bright, dull) => (on ? bright : dull);

const emblems = {
  // Un cristal serré entre deux mors : stabiliser, c'est tenir.
  resonance_stabilizer: (c, t, on) => {
    const core = dim(t, on, t.coreOn, '#4A3660');
    const body = dim(t, on, t.glowOn, '#3E2C52');
    c.poly([[16, 9], [21, 16], [16, 23], [11, 16]], body);
    c.poly([[16, 11], [19, 16], [16, 21], [13, 16]], core, on ? 255 : 160);
    c.rect(8, 14, 3, 5, t.edge);
    c.rect(21, 14, 3, 5, t.edge);
    bevel(c, 8, 14, 3, 5, t.trimHi, t.dark);
    bevel(c, 21, 14, 3, 5, t.trimHi, t.dark);
  },

  // Une pièce en cours de sertissage, ses quatre boulons : assembler, c'est fixer.
  component_assembler: (c, t, on) => {
    c.rect(10, 10, 12, 12, t.dark);
    bevel(c, 10, 10, 12, 12, t.edge, t.out);
    c.rect(13, 13, 6, 6, dim(t, on, t.glowOn, '#3E2C52'));
    c.rect(14, 14, 4, 4, dim(t, on, t.coreOn, '#4A3660'));
    for (const [x, y] of [[10, 10], [20, 10], [10, 20], [20, 20]]) {
      rivet(c, x, y, t.trim, t.trimHi, t.trimLo);
    }
  },

  // Une meule et sa gerbe d'étincelles : affûter, c'est frotter.
  resonance_whetstone: (c, t, on) => {
    c.disc(16, 16, 7, t.edge);
    c.disc(16, 16, 6, t.dark);
    c.disc(16, 16, 6, t.edge, false);
    c.disc(16, 16, 3, dim(t, on, t.glowOn, '#3E2C52'));
    c.disc(16, 16, 1.5, dim(t, on, t.coreOn, '#4A3660'));
    if (on) {
      c.set(23, 11, t.coreOn); c.set(24, 13, t.glowOn);
      c.set(22, 9, t.glowOn); c.set(25, 10, t.coreOn);
    }
  },

  // Une cornue et son bain : purifier, c'est décanter. Le seul emblème qui se
  // remplit par le bas — on doit voir un NIVEAU.
  flux_purifier: (c, t, on) => {
    c.poly([[13, 9], [19, 9], [19, 13], [22, 20], [22, 23], [10, 23], [10, 20], [13, 13]], t.dark);
    c.poly([[13, 9], [19, 9], [19, 13], [22, 20], [22, 23], [10, 23], [10, 20], [13, 13]], t.edge, 60);
    c.poly([[11, 18], [21, 18], [21, 22], [11, 22]], dim(t, on, t.glowOn, '#3E2C52'));
    c.poly([[12, 19], [20, 19], [20, 21], [12, 21]], dim(t, on, t.coreOn, '#4A3660'), on ? 200 : 140);
    c.frameRect(13, 9, 7, 2, t.trimLo);
    if (on) { c.set(14, 16, t.coreOn); c.set(18, 14, t.coreOn); c.set(16, 12, t.glowOn); }
  },

  // Un pilon au-dessus d'un mortier : broyer, c'est frapper. Vertical, donc.
  crystal_crusher: (c, t, on) => {
    c.rect(11, 8, 10, 4, t.edge);
    bevel(c, 11, 8, 10, 4, t.trimHi, t.dark);
    c.rect(14, 12, 4, 4, t.dark);
    c.poly([[9, 19], [23, 19], [21, 24], [11, 24]], t.dark);
    c.poly([[9, 19], [23, 19], [21, 24], [11, 24]], t.edge, 70);
    c.rect(12, 20, 8, 3, dim(t, on, t.glowOn, '#3E2C52'));
    if (on) { c.set(13, 17, t.coreOn); c.set(19, 17, t.coreOn); c.set(16, 18, t.glowOn); }
  },

  // Un nid et son cristal en couvaison : la seule machine du lot qui n'usine rien.
  crystal_roost: (c, t, on) => {
    for (let i = 0; i < 4; i++) {
      c.line(9, 21 + (i % 2), 23, 20 + (i % 2), t.trimLo);
      c.line(9 + i * 2, 23, 14 + i * 2, 19, '#6E5436');
    }
    c.poly([[16, 11], [20, 17], [16, 22], [12, 17]], dim(t, on, t.glowOn, '#3E2C52'));
    c.poly([[16, 13], [18, 17], [16, 20], [14, 17]], dim(t, on, t.coreOn, '#4A3660'), on ? 255 : 150);
    c.rect(9, 22, 15, 2, '#6E5436');
  },

  // Des anneaux qui se referment vers le centre : absorber. C'est l'inverse exact de
  // l'émetteur, dont les arcs s'ouvrent vers l'extérieur — les deux blocs doivent se
  // distinguer d'un seul regard, ils font des choses opposées.
  damping_array: (c, t, on) => {
    for (let i = 0; i < 3; i++) {
      c.disc(16, 16, 7 - i * 2, i % 2 ? t.dark : t.edge, false);
    }
    c.disc(16, 16, 2, dim(t, on, t.glowOn, '#3E2C52'));
    c.disc(16, 16, 1, dim(t, on, t.coreOn, '#4A3660'));
    for (const [x, y] of [[15, 8], [15, 23], [8, 15], [23, 15]]) c.rect(x, y, 2, 1, t.trimLo);
  },

  // Une parabole et ses ondes : émettre. Arcs ouverts vers le haut.
  field_emitter: (c, t, on) => {
    c.poly([[9, 22], [23, 22], [20, 15], [12, 15]], t.dark);
    c.poly([[9, 22], [23, 22], [20, 15], [12, 15]], t.edge, 70);
    c.rect(15, 18, 2, 5, t.trimLo);
    c.disc(16, 15, 3, dim(t, on, t.glowOn, '#3E2C52'));
    c.disc(16, 15, 1.5, dim(t, on, t.coreOn, '#4A3660'));
    const wave = on ? t.glowOn : t.groove;
    c.disc(16, 15, 6, wave, false, on ? 200 : 255);
    c.disc(16, 15, 9, wave, false, on ? 120 : 160);
  },

  // La même parabole, plus trois pastilles de bande. C'est le SEUL ajout : le joueur
  // doit lire « l'émetteur, mais avec le choix de fréquence », pas une machine de plus.
  tunable_field_emitter: (c, t, on) => {
    emblems.field_emitter(c, t, on);
    const bands = on ? ['#9B59D0', '#35C0C8', '#D8922A'] : ['#3E2C52', '#22414A', '#4A3A22'];
    bands.forEach((col, i) => c.rect(11 + i * 5, 24, 3, 2, col));
  },

  // Un écran de glyphes encore vivant : la console est la seule « machine » qu'on ne
  // fabrique pas — on la réveille. Elle n'a donc pas d'état éteint.
  attunement_console: (c, t, on) => {
    c.rect(9, 9, 14, 14, '#101820');
    bevel(c, 9, 9, 14, 14, t.out, t.dark);
    const rand = rng(0xc0de);
    for (let row = 0; row < 5; row++) {
      let x = 11;
      while (x < 21) {
        const w = 1 + Math.floor(rand() * 3);
        if (rand() > 0.35) c.rect(x, 11 + row * 3, Math.min(w, 21 - x), 1, on ? t.glowOn : '#2A2438');
        x += w + 1;
      }
    }
    if (on) { c.rect(11, 11, 3, 1, t.coreOn); c.rect(15, 17, 4, 1, t.coreOn); }
  },
};

// --- Roche et minerais ----------------------------------------------------

/**
 * Roche : trois octaves de taches (grosses masses → moyennes → grain fin), puis des
 * fissures, puis une occlusion douce. Une seule passe de bruit donne un gravier
 * uniforme, sans échelle ; c'est la superposition de tailles qui fait lire « pierre ».
 */
function stone(seed, shades) {
  const c = new Canvas(S);
  c.fill(shades[1]);
  const rand = rng(seed);
  // Octave 1 : grandes masses, elles donnent la structure générale.
  for (let n = 0; n < 14; n++) {
    c.disc(rand() * S, rand() * S, 4 + rand() * 4,
      shades[Math.floor(rand() * shades.length)], true, 70);
  }
  // Octave 2 : cailloux moyens.
  for (let n = 0; n < 90; n++) {
    c.disc(rand() * S, rand() * S, 1.2 + rand() * 2.2,
      shades[Math.floor(rand() * shades.length)], true, 95);
  }
  // Octave 3 : grain fin.
  grain(c, seed + 11, shades[3], shades[0], 0.34);
  dither(c, 0, 0, S, S, shades[0], 0.2, 34);

  // Fissures : de courtes cassures sombres, avec une arête claire d'un côté — c'est
  // ce liseré qui donne du relief plutôt qu'un simple trait sale.
  for (let n = 0; n < 5; n++) {
    let x = rand() * S, y = rand() * S;
    const dx = rand() - 0.5, dy = rand() - 0.5;
    for (let i = 0; i < 4 + rand() * 6; i++) {
      c.set(Math.round(x), Math.round(y), shades[0], 150);
      c.set(Math.round(x) + 1, Math.round(y), shades[3], 60);
      x += dx * 2; y += dy * 2;
    }
  }
  vignette(c, shades[0], 40);
  return c;
}

const STONE = ['#262B33', '#333944', '#3E4550', '#4C5460'];

/**
 * La veine : un tracé sinueux, un seul, avec une branche. Surtout PAS un motif
 * symétrique — dès que le bloc se répète dans une paroi, une symétrie se lit comme
 * une grille et la roche redevient du papier peint.
 */
function veinPath(c, dark, mid, glow) {
  // Les extrémités sont RACCORDÉES aux bords : la veine sort en x=12 en haut comme en
  // bas, et en y=17 à gauche comme à droite. Sans ce raccord, chaque bloc affiche une
  // veine qui s'arrête net à sa bordure — un mur entier se lit alors comme du papier
  // peint tamponné, l'inverse exact du « tell » minéral qu'on cherche (pilier 2).
  const spine = [[12, 0], [14, 6], [11, 12], [15, 18], [12, 25], [12, 31]];
  const branchR = [[15, 18], [22, 16], [31, 17]];
  const branchL = [[0, 17], [6, 15], [11, 12]];
  // Trois passes par veine : le sillon creusé (sombre) sous la matière, la matière
  // elle-même, puis une arête claire d'un seul côté. Un trait unique lit comme un
  // coup de feutre ; c'est le décalage d'un pixel qui donne le volume.
  for (const path of [spine, branchR, branchL]) {
    for (let i = 0; i + 1 < path.length; i++) {
      const [x1, y1] = path[i], [x2, y2] = path[i + 1];
      c.line(x1 - 1, y1 + 1, x2 - 1, y2 + 1, dark, 150); // ombre du sillon
      c.line(x1, y1, x2, y2, dark);
      c.line(x1 + 1, y1, x2 + 1, y2, mid);
      c.line(x1 + 1, y1 - 1, x2 + 1, y2 - 1, mid, 90);   // arête éclairée
    }
  }
  // Nœuds lumineux aux embranchements, avec leur halo : la veine « pulse » là où
  // elle se divise, ce qui attire l'œil sur la structure plutôt que sur le trait.
  for (const [x, y] of [[14, 6], [11, 12], [15, 18], [22, 16]]) {
    bloom(c, x, y, 3, mid, 60);
    c.set(x, y, glow);
  }
  return c;
}

const textures = {};

for (const key of ['t1', 't2', 't3']) {
  const t = TIERS[key];
  textures[`${t.name}_chassis_side`] = chassisSide(t, 0x1000 + key.charCodeAt(1));
  textures[`${t.name}_chassis_top`] = chassisTop(t, 0x2000 + key.charCodeAt(1));
}

// Quelle machine sur quel palier — c'est cette table qui fait que le palier se lit
// sur le bloc (03-Progression.md).
const MACHINE_TIER = {
  resonance_stabilizer: 't1',
  component_assembler: 't1',
  resonance_whetstone: 't1',
  crystal_crusher: 't1',
  flux_purifier: 't2',
  crystal_roost: 't2',
  field_emitter: 't2',
  tunable_field_emitter: 't2',
  damping_array: 't3',
};

let seed = 0x3000;
for (const [name, tierKey] of Object.entries(MACHINE_TIER)) {
  const t = TIERS[tierKey];
  textures[`${name}_front`] = machineFront(t, seed, emblems[name], false);
  textures[`${name}_front_on`] = machineFront(t, seed, emblems[name], true);
  seed += 17;
}

// Console : toujours vivante, sur le châssis T1 (c'est une ruine).
textures.attunement_console_front = machineFront(TIERS.t1, 0x4242, emblems.attunement_console, true);

// --- Blocs naturels -------------------------------------------------------

const veined = stone(0x7a11, STONE);
veinPath(veined, '#3B1E5A', '#7A3AA8', '#C79BEB');
textures.resonance_veined_stone = veined;

const spored = stone(0x7a11, STONE);
veinPath(spored, '#3B1E5A', '#7A3AA8', '#C79BEB');
{
  const rand = rng(0x9911);
  for (let n = 0; n < 7; n++) {
    const cx = 3 + rand() * 26, cy = 3 + rand() * 26;
    spored.disc(cx, cy, 2.2, '#4A4466');
    spored.disc(cx, cy, 1.5, '#9E8ABE');
    spored.disc(cx - 0.4, cy - 0.4, 0.9, '#E4DBF7');
  }
}
textures.resonance_veined_stone_spored = spored;

// Amas : des pointes qui PERCENT la roche et cassent la silhouette du cube. C'est ce
// qui les rend repérables dans une paroi — un minerai à pois se rate, pas ça.
const cluster = stone(0x7a22, STONE);
{
  const spikes = [
    [[16, 2], [21, 12], [16, 18], [11, 12]],
    [[7, 12], [11, 19], [7, 26], [3, 19]],
    [[24, 14], [28, 21], [24, 28], [20, 21]],
    [[15, 20], [19, 25], [15, 30], [11, 25]],
  ];
  // Chaque pointe : ombre portée sur la roche, corps, facette éclairée d'un côté,
  // cœur, éclat spéculaire, halo. Six couches — c'est ce qui sépare un losange
  // violet d'un cristal qui accroche la lumière.
  for (const s of spikes) {
    const cx = s.reduce((a, p) => a + p[0], 0) / 4;
    const cy = s.reduce((a, p) => a + p[1], 0) / 4;
    cluster.poly(s.map(([x, y]) => [x + 1, y + 1]), '#14101C', 120); // ombre portée
    cluster.poly(s, '#3B1E5A');
    cluster.poly(s.map(([x, y]) => [x - 0.8, y]), '#5C2C86');        // facette sombre
    const inner = s.map(([x, y]) => [x + (x < cx ? 1.2 : -0.6), y]);
    cluster.poly(inner, '#7A3AA8');
    cluster.disc(cx, cy, 2, '#A85FD6');
    bloom(cluster, cx, cy, 4, '#A85FD6', 55);
    cluster.disc(cx - 0.5, cy - 1, 1, '#E3C4F7');
    cluster.set(Math.round(cx - 1), Math.round(cy - 2), '#F6EAFE');  // éclat
  }
}
textures.resonance_crystal_cluster = cluster;

// Croûte de flux : la roche affleure, striée horizontalement (ça se BROSSE, ça ne se
// mine pas) et mouchetée de violet.
const flux = stone(0x7a33, ['#332C2E', '#40383A', '#4C4244', '#5A4E50']);
{
  const rand = rng(0x1234);
  for (let y = 2; y < S; y += 5) {
    for (let x = 0; x < S; x++) if (rand() > 0.25) flux.set(x, y + Math.floor(rand() * 2), '#2A2426', 120);
  }
  for (let n = 0; n < 34; n++) {
    const x = Math.floor(rand() * S), y = Math.floor(rand() * S);
    flux.set(x, y, '#9B59D0');
    if (rand() > 0.5) flux.set(x + 1, y, '#7A3AA8', 170);
    if (rand() > 0.7) flux.set(x, y + 1, '#C79BEB', 150);
  }
}
textures.raw_flux_deposit = flux;

module.exports = { textures, MACHINE_TIER, TIERS };
