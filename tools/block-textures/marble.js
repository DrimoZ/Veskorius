// Textures de bloc 16x16 — marbre veskorien.
//
// Deux corrections de fond, l'une mesurée, l'autre de conception.
//
// 1. LA PALETTE. J'ai mesuré le marbre d'Astral Sorcery plutôt que de m'en
//    souvenir : luminance 203-238, dominantes #dbdbdb à #e6e6e6, écart-type 8,8, et
//    R=G=B — un blanc cassé PARFAITEMENT NEUTRE, très peu contrasté. Toutes mes
//    versions précédentes posaient une pierre sombre et colorée (grise, tan,
//    violette) : le violet de la Résonance s'y noyait, faute de fond clair pour le
//    faire ressortir. Ici le marbre est presque blanc, et l'accent explose dessus.
//    Le contraste vient de l'ACCENT, jamais du matériau.
//
// 2. LA COMPOSITION. Je dessinais « un cadre + un petit logo au centre » : tous les
//    blocs étaient donc le même bloc avec un autocollant différent, et aucun ne se
//    reconnaissait de loin. Ici, CHAQUE MACHINE A UNE FAÇADE ENTIÈREMENT
//    DIFFÉRENTE — un cristal qui remplit le bloc, une cuve haute, deux mâchoires,
//    une roue géante, une cible concentrique, une grille. La silhouette et la
//    couleur dominante changent d'une machine à l'autre ; c'est ça qui les rend
//    identifiables d'un coup d'œil, pas un pictogramme de 6 pixels.
//
// Méthode inchangée (elle, elle était juste) : palette indexée, aucun fondu alpha,
// arêtes franches, grain dense à faible amplitude. Calibré sur les mesures vanilla.

const { Canvas, rng } = require('./draw');
const {
  DISC12, DISC8, DISC4, DIAMOND, DIAMOND_IN, DIAMOND_CORE, fill, outline, ring,
} = require('./shapes');

const S = 16;

// --- Marbre : trois états de restauration --------------------------------
const MARBLE = {
  t1: { // Ruine : marbre encrassé, gris, fissuré. Cuivre oxydé.
    tones: ['#9E9E9E', '#ACACAC', '#B8B8B8', '#C4C4C4'],
    w: [3, 4, 3, 2],
    line: '#6E6E6E', dark: '#5A5A5A',
    metal: '#A8632F', metalHi: '#C9834E',
    crack: '#7C7C7C',
  },
  t2: { // Restauré : le marbre d'Astral, presque blanc et neutre. Laiton.
    tones: ['#D3D3D3', '#DBDBDB', '#E0E0E0', '#E6E6E6'],
    w: [3, 4, 3, 2],
    line: '#A8A8A8', dark: '#8E8E8E',
    metal: '#C9A24A', metalHi: '#E8CE8A',
    crack: null,
  },
  t3: { // Haute époque : marbre sombre poli, ambre.
    tones: ['#3A3A44', '#44444F', '#4E4E5A', '#585866'],
    w: [3, 4, 3, 2],
    line: '#26262E', dark: '#1C1C22',
    metal: '#D8922A', metalHi: '#F0B863',
    crack: null,
  },
};

// Accents de la Résonance : les trois bandes harmoniques du mod.
const V = { deep: '#5C2C86', mid: '#8A47B8', lite: '#B57CE0', hot: '#E4CCF7' };
const C = { deep: '#166B72', mid: '#27A3AC', lite: '#5FD6DC', hot: '#CFF6F8' };
const WOOD = { deep: '#4A3520', mid: '#6E5436', lite: '#8F7048' };
const A = { deep: '#8E5A15', mid: '#D8922A', lite: '#F0B863', hot: '#FBE0B0' };
const IRON = { deep: '#2E323A', mid: '#454B56', lite: '#5E6672' };

/** Marbre : semis dense à faible amplitude, plus quelques fissures au T1. */
function marble(m, seed) {
  const c = new Canvas(S);
  const rand = rng(seed);
  const tot = m.w.reduce((a, b) => a + b, 0);
  for (let y = 0; y < S; y++) {
    for (let x = 0; x < S; x++) {
      let r = rand() * tot, i = 0;
      while (r > m.w[i] && i < m.w.length - 1) { r -= m.w[i]; i++; }
      c.set(x, y, m.tones[i]);
    }
  }
  if (m.crack) {
    // Le T1 est de la ruine : quelques cassures franches, pas un réseau.
    for (let n = 0; n < 3; n++) {
      let x = Math.floor(rand() * S), y = Math.floor(rand() * S);
      for (let i = 0; i < 3 + rand() * 4; i++) {
        c.set(x, y, m.crack);
        x += rand() > 0.5 ? 1 : 0;
        y += rand() > 0.4 ? 1 : -1;
      }
    }
  }
  return c;
}

/** Liseré du bloc : clair en haut/gauche, sombre en bas/droite. */
function edges(c, m) {
  for (let i = 0; i < S; i++) {
    c.set(i, 0, m.tones[3]);
    c.set(0, i, m.tones[3]);
    c.set(i, S - 1, m.line);
    c.set(S - 1, i, m.line);
  }
  return c;
}

/** Bloc plein cerné d'un pixel sombre : la brique de toutes les structures. */
function slab(c, x, y, w, h, body, hi, lo) {
  c.rect(x, y, w, h, body);
  for (let i = x; i < x + w; i++) { c.set(i, y, hi); c.set(i, y + h - 1, lo); }
  for (let j = y; j < y + h; j++) { c.set(x, j, hi); c.set(x + w - 1, j, lo); }
  return c;
}

// --- Façades : chacune occupe TOUT le bloc -------------------------------
// La consigne est qu'on doit nommer la machine sans lire de tooltip. Donc :
// silhouette dominante différente, couleur dominante différente.

const faces = {
  // UN CRISTAL GÉANT serré dans deux mors. Losange pris dans la table : centré au
  // pixel près, marge d'1 px. Tracé par formule, il était décalé d'un demi-pixel —
  // invisible à décrire, flagrant à l'œil.
  resonance_stabilizer: (c, m, a, on) => {
    outline(c, DIAMOND, m.line);
    fill(c, DIAMOND, on ? a.mid : a.deep);
    fill(c, DIAMOND_IN, on ? a.lite : a.mid);
    fill(c, DIAMOND_CORE, on ? a.hot : a.lite);
    slab(c, 1, 6, 2, 4, m.metal, m.metalHi, m.line);
    slab(c, 13, 6, 2, 4, m.metal, m.metalHi, m.line);
  },

  // DES PLAQUES BOULONNÉES en fer sombre, séparées par une fente centrale.
  component_assembler: (c, m, a, on) => {
    slab(c, 1, 1, 14, 6, IRON.mid, IRON.lite, IRON.deep);
    slab(c, 1, 9, 14, 6, IRON.mid, IRON.lite, IRON.deep);
    for (const [x, y] of [[3, 3], [11, 3], [3, 11], [11, 11]]) {
      c.rect(x, y, 2, 2, m.metal);
      c.set(x, y, m.metalHi);
    }
    c.rect(1, 7, 14, 2, on ? a.mid : IRON.deep);
    if (on) c.rect(1, 7, 14, 1, a.hot);
  },

  // UNE ROUE, cercle exact, sur son socle de laiton.
  resonance_whetstone: (c, m, a, on) => {
    outline(c, DISC12, m.line);
    fill(c, DISC12, m.tones[2]);
    ring(c, DISC12, DISC8, m.dark);
    fill(c, DISC8, m.tones[1]);
    fill(c, DISC4, on ? a.mid : m.dark);
    if (on) c.rect(7, 7, 2, 2, a.hot);
    slab(c, 1, 13, 14, 2, m.metal, m.metalHi, m.line);
  },

  // DEUX MÂCHOIRES dentées, et la poussière prise entre elles.
  crystal_crusher: (c, m, a, on) => {
    slab(c, 1, 1, 14, 5, IRON.mid, IRON.lite, IRON.deep);
    slab(c, 1, 10, 14, 5, IRON.mid, IRON.lite, IRON.deep);
    for (let x = 2; x <= 12; x += 3) {
      c.rect(x, 6, 2, 1, IRON.lite);
      c.rect(x + 1, 9, 2, 1, IRON.lite);
    }
    c.rect(1, 7, 14, 2, on ? a.mid : m.dark);
    if (on) { c.set(4, 8, a.hot); c.set(8, 7, a.hot); c.set(11, 8, a.hot); }
  },

  // UNE CUVE HAUTE avec son niveau : la seule silhouette verticale du lot.
  flux_purifier: (c, m, a, on) => {
    slab(c, 2, 1, 12, 14, m.tones[2], m.tones[3], m.line);
    c.rect(4, 3, 8, 10, m.dark);
    c.rect(4, 6, 8, 7, on ? a.mid : a.deep);
    c.rect(4, 6, 8, 1, on ? a.hot : a.mid);
    if (on) { c.set(6, 9, a.hot); c.set(9, 11, a.hot); }
    slab(c, 2, 1, 12, 2, m.metal, m.metalHi, m.line);
    slab(c, 2, 13, 12, 2, m.metal, m.metalHi, m.line);
  },

  // UNE CAISSE EN BOIS percée d'un trou rond. Seul bloc marron du mod.
  crystal_roost: (c, m, a, on) => {
    c.rect(0, 0, 16, 16, WOOD.mid);
    for (let y = 2; y < 16; y += 4) c.rect(0, y, 16, 1, WOOD.deep);
    outline(c, DISC12, WOOD.lite);
    fill(c, DISC12, m.dark);
    fill(c, DISC8, on ? a.deep : m.dark);
    if (on) { fill(c, DISC4, a.mid); c.rect(7, 7, 2, 2, a.hot); }
  },

  // UNE GRILLE À LAMES, pleine hauteur. Aucun centre : que du motif.
  damping_array: (c, m, a, on) => {
    c.rect(0, 0, 16, 16, m.dark);
    for (let y = 1; y < 15; y += 3) slab(c, 1, y, 14, 2, m.tones[1], m.tones[3], m.line);
    c.rect(7, 1, 2, 14, on ? a.mid : m.line);
    if (on) c.rect(7, 1, 1, 14, a.hot);
  },

  // UNE LENTILLE EN CIBLE : cercles concentriques exacts, marge respectée.
  field_emitter: (c, m, a, on) => {
    outline(c, DISC12, m.line);
    fill(c, DISC12, m.metal);
    ring(c, DISC12, DISC8, on ? a.deep : m.dark);
    fill(c, DISC8, on ? a.mid : m.tones[0]);
    fill(c, DISC4, on ? a.hot : m.dark);
  },

  // DEUX ARCS QUI SE RÉPONDENT, décalés à gauche et à droite d'un axe central.
  // L'émetteur est une cible : concentrique, donc un centre — « ça vient d'ici ».
  // Le relais doit dire l'inverse : ça entre d'un côté et ça ressort de l'autre. Rien
  // n'est centré, et c'est tout l'écart de lecture entre une source et un répéteur.
  resonance_relay: (c, m, a, on) => {
    c.rect(0, 0, 16, 16, m.dark);
    slab(c, 6, 0, 4, 16, m.metal, m.metalHi, m.line);
    // Deux arcs de chaque côté, de plus en plus larges : l'onde qui s'éloigne. Deux et
    // pas trois — un troisième tomberait sur la colonne 0, que `edges` repasse ensuite
    // en liseré : il ne resterait de lui que ses deux crochets, lus comme des pixels
    // égarés. Un motif doit tenir DANS la marge d'un pixel, sinon il n'existe qu'à
    // moitié une fois le bloc fini.
    for (let i = 0; i < 2; i++) {
      const x = 4 - i * 3, h = 6 + i * 6, y = 8 - Math.floor(h / 2);
      // Éteint, les arcs restent LISIBLES : un relais en panne doit se reconnaître
      // comme un relais, sinon on ne sait plus lequel des blocs sombres de la ligne
      // est celui qui ne reçoit plus. Ils perdent leur couleur, pas leur dessin.
      const col = on ? [a.hot, a.lite][i] : [m.tones[3], m.tones[2]][i];
      c.rect(x, y, 1, h, col);
      c.rect(15 - x, y, 1, h, col);
      c.set(x + 1, y, col);
      c.set(14 - x, y, col);
      c.set(x + 1, y + h - 1, col);
      c.set(14 - x, y + h - 1, col);
    }
    c.rect(7, 7, 2, 2, on ? a.hot : m.dark);
  },


  // UN BÉLIER SUSPENDU au-dessus d'une enclume, et l'ÉCART entre les deux. Toute la
  // lecture tient dans cet écart : c'est la seule façade du mod dont le sujet est un
  // vide. Comprimé, l'écart se remplit de lumière.
  flux_compressor: (c, m, a, on) => {
    c.rect(0, 0, 16, 16, m.dark);
    slab(c, 1, 1, 14, 5, IRON.mid, IRON.lite, IRON.deep);   // bélier
    slab(c, 1, 11, 14, 4, m.metal, m.metalHi, m.line);       // enclume
    for (let x = 3; x <= 12; x += 3) c.rect(x, 6, 2, 1, IRON.deep);
    c.rect(2, 7, 12, 4, on ? a.mid : m.tones[0]);
    if (on) { c.rect(2, 7, 12, 1, a.hot); c.rect(4, 9, 8, 1, a.lite); }
    for (const [x, y] of [[2, 2], [12, 2]]) { c.rect(x, y, 2, 2, m.metal); c.set(x, y, m.metalHi); }
  },

  // UN ENTONNOIR, et rien d'autre. Large en haut, étroit en bas, avec un bac dessous.
  // C'est la seule façade du mod qui se lit de haut en bas plutôt que par symétrie : le
  // Reclaimer ne transforme pas, il TRIE — ce qui entre est informe, ce qui sort est
  // compté. Allumé, le filet qui descend s'éclaire ; le haut reste terne, parce que le
  // déchet n'a jamais brillé.
  reclaimer: (c, m, a, on) => {
    c.rect(0, 0, 16, 16, m.dark);
    for (let y = 1; y <= 6; y++) {
      const inset = y - 1;
      c.rect(1 + inset, y, 14 - inset * 2, 1, y < 3 ? m.tones[0] : m.tones[2]);
    }
    c.rect(7, 7, 2, 3, on ? a.mid : m.tones[0]);
    if (on) { c.set(7, 7, a.hot); c.set(8, 9, a.lite); }
    slab(c, 2, 10, 12, 5, m.metal, m.metalHi, m.line);
    c.rect(4, 12, 8, 2, on ? a.deep : m.dark);
    for (const [x, y] of [[1, 1], [13, 1]]) { c.rect(x, y, 2, 2, m.metal); c.set(x, y, m.metalHi); }
  },

  // UN MOULE VU DE FACE : un cadre épais, et à l'intérieur QUATRE CASES — les quatre
  // blocs qu'un cycle produit. C'est le seul emblème du mod qui montre sa quantité.
  structural_synthesizer: (c, m, a, on) => {
    slab(c, 0, 0, 16, 16, m.tones[1], m.tones[3], m.line);
    c.rect(2, 2, 12, 12, m.dark);
    for (const [x, y] of [[3, 3], [9, 3], [3, 9], [9, 9]]) {
      slab(c, x, y, 4, 4, on ? a.deep : m.tones[0], on ? a.mid : m.tones[2], m.line);
      if (on) c.set(x + 1, y + 1, a.hot);
    }
    slab(c, 0, 7, 16, 2, m.metal, m.metalHi, m.line);
  },

  // UN TRÉPAN QUI DESCEND : une colonne centrale à dents, pointe en bas, et la roche
  // de part et d'autre. La seule façade orientée VERS LE BAS du lot — la machine dit
  // où elle travaille.
  deep_crystal_driller: (c, m, a, on) => {
    c.rect(0, 0, 16, 16, m.dark);
    slab(c, 0, 0, 16, 3, m.metal, m.metalHi, m.line);
    slab(c, 6, 3, 4, 9, IRON.mid, IRON.lite, IRON.deep);
    for (let y = 4; y < 12; y += 2) { c.set(5, y, IRON.lite); c.set(10, y, IRON.lite); }
    // Pointe : trois assises de plus en plus étroites.
    c.rect(6, 12, 4, 1, IRON.lite);
    c.rect(7, 13, 2, 1, IRON.mid);
    c.set(7, 14, on ? a.hot : IRON.deep);
    c.set(8, 14, on ? a.hot : IRON.deep);
    // La veine qu'elle vise, de part et d'autre.
    for (const x of [2, 13]) {
      c.rect(x, 6, 2, 6, on ? a.deep : m.tones[0]);
      if (on) c.set(x, 8, a.mid);
    }
  },

  // UNE BOUCHE D'ÉVACUATION : une grille horizontale barrée de lames, sans centre et
  // sans lentille. Rien n'y ressemble à un émetteur — l'évent ne produit rien, et sa
  // façade ne doit rien promettre.
  slag_vent: (c, m, a, on) => {
    c.rect(0, 0, 16, 16, m.dark);
    slab(c, 1, 1, 14, 14, m.tones[0], m.tones[3], m.line);
    for (let y = 3; y < 14; y += 3) {
      c.rect(3, y, 10, 2, m.dark);
      c.rect(3, y, 10, 1, on ? a.deep : m.line);
      if (on) { c.set(4, y, a.mid); c.set(11, y, a.mid); }
    }
    for (const x of [1, 14]) for (let y = 2; y < 14; y++) c.set(x, y, m.metal);
  },

  // UNE CUVE SCELLÉE, hublot rond au centre et rivets aux quatre coins. Le Purifier
  // montre son NIVEAU (une cuve ouverte), la Chambre montre qu'elle est FERMÉE : on ne
  // regarde pas dedans, on regarde par un hublot. Deux cuves, deux lectures.
  deep_synthesis_chamber: (c, m, a, on) => {
    slab(c, 0, 0, 16, 16, m.tones[1], m.tones[3], m.line);
    slab(c, 2, 2, 12, 12, IRON.mid, IRON.lite, IRON.deep);
    outline(c, DISC8, m.line);
    fill(c, DISC8, on ? a.mid : m.dark);
    if (on) { fill(c, DISC4, a.hot); }
    for (const [x, y] of [[1, 1], [13, 1], [1, 13], [13, 13]]) {
      c.rect(x, y, 2, 2, m.metal); c.set(x, y, m.metalHi);
    }
    // Deux brides horizontales : la cuve est boulonnée, pas soudée.
    for (const y of [4, 11]) c.rect(2, y, 12, 1, IRON.deep);
  },

  // TROIS ARCS CONCENTRIQUES OUVERTS, tous du même côté. Le relais renvoie de part et
  // d'autre (symétrique) ; l'amplificateur POUSSE — l'asymétrie est toute la lecture.
  harmonic_amplifier: (c, m, a, on) => {
    c.rect(0, 0, 16, 16, m.dark);
    slab(c, 1, 5, 4, 6, m.metal, m.metalHi, m.line);
    for (let i = 0; i < 3; i++) {
      const x = 6 + i * 3, h = 6 + i * 3, y = 8 - Math.floor(h / 2);
      const col = on ? [a.hot, a.lite, a.mid][i] : [m.tones[3], m.tones[2], m.tones[1]][i];
      c.rect(x, y, 1, h, col);
      c.set(x - 1, y, col);
      c.set(x - 1, y + h - 1, col);
    }
    c.rect(4, 7, 2, 2, on ? a.hot : m.tones[3]);
  },

  // UN PORTIQUE VU DE FACE : deux montants, une poutre, et un CHARIOT suspendu au
  // milieu. La foreuse montre son trépan (elle creuse) ; la matrice montre un chariot
  // sur un rail (elle DÉPLACE, elle ne creuse pas). Deux machines de mine, deux gestes.
  automated_extraction_array: (c, m, a, on) => {
    c.rect(0, 0, 16, 16, m.dark);
    slab(c, 0, 0, 16, 4, m.metal, m.metalHi, m.line);
    slab(c, 0, 4, 4, 12, IRON.mid, IRON.lite, IRON.deep);
    slab(c, 12, 4, 4, 12, IRON.mid, IRON.lite, IRON.deep);
    c.rect(4, 5, 8, 1, IRON.lite);
    // Le chariot, et ses deux câbles.
    c.rect(7, 6, 1, 3, IRON.deep);
    c.rect(8, 6, 1, 3, IRON.deep);
    slab(c, 5, 9, 6, 4, on ? a.deep : m.tones[0], on ? a.mid : m.tones[2], m.line);
    if (on) { c.rect(6, 10, 4, 2, a.hot); }
  },

  // TROIS BARRES DE HAUTEURS DÉCROISSANTES, comme un histogramme. Aucun cercle, aucune
  // lentille : le Hub n'émet rien, sa façade ne doit rien promettre d'un émetteur. Ce
  // qu'elle montre, c'est un CLASSEMENT — trois niveaux, du plus servi au premier délesté.
  resonance_network_hub: (c, m, a, on) => {
    c.rect(0, 0, 16, 16, m.dark);
    slab(c, 0, 13, 16, 3, m.metal, m.metalHi, m.line);
    const heights = [10, 7, 4];
    for (let i = 0; i < 3; i++) {
      const x = 2 + i * 4, h = heights[i];
      const col = on ? [a.hot, a.mid, a.deep][i] : [m.tones[3], m.tones[2], m.tones[1]][i];
      slab(c, x, 13 - h, 3, h, col, on ? a.lite : m.tones[3], m.line);
    }
    // La barre de rappel : le seuil au-dessus duquel personne n'est délesté.
    c.rect(1, 3, 14, 1, on ? a.lite : m.line);
  },

  // HUIT RAYONS partant d'un noyau central : la figure de l'anneau, dessinée sur la
  // façade. Le joueur voit ce qu'il doit bâtir avant qu'on le lui explique — et une fois
  // l'anneau posé, il reconnaît sur le bloc la forme qu'il vient de tracer au sol.
  convergence_core: (c, m, a, on) => {
    c.rect(0, 0, 16, 16, m.dark);
    slab(c, 0, 0, 16, 16, m.tones[0], m.tones[3], m.line);
    const spokes = [[8, 1], [8, 14], [1, 8], [14, 8], [3, 3], [12, 3], [3, 12], [12, 12]];
    for (const [x, y] of spokes) {
      c.rect(x - 1, y - 1, 2, 2, on ? a.mid : m.tones[2]);
      if (on) c.set(x - 1, y - 1, a.hot);
      c.line(x - 1, y - 1, 7, 7, on ? a.deep : m.line);
    }
    outline(c, DISC8, m.line);
    fill(c, DISC8, on ? a.deep : m.dark);
    fill(c, DISC4, on ? a.hot : m.tones[1]);
  },

  // UN PIEU ENFONCÉ : une tête large, un fût qui s'affine, et la roche qui se referme
  // dessus. Toutes les autres machines du mod sont POSÉES ; celle-ci se plante, et sa
  // façade doit le dire — c'est aussi la seule qu'on installe au bord d'un danger.
  rift_anchor: (c, m, a, on) => {
    c.rect(0, 0, 16, 16, m.dark);
    slab(c, 1, 1, 14, 3, m.metal, m.metalHi, m.line);
    for (let y = 4; y < 14; y++) {
      const w = Math.max(2, 8 - Math.floor((y - 4) / 2));
      slab(c, 8 - Math.floor(w / 2), y, w, 1, IRON.mid, IRON.lite, IRON.deep);
    }
    // La lueur du déphasage, prise dans le fût : violet quand l'Ancre tient.
    c.rect(7, 5, 2, 6, on ? a.mid : m.tones[0]);
    if (on) { c.set(7, 5, a.hot); c.set(8, 8, a.hot); }
    for (const [x, y] of [[2, 2], [13, 2]]) { c.rect(x, y, 2, 2, m.metalHi); c.set(x, y, m.metal); }
  },

  // UNE PINCE REFERMEE SUR UN VIDE : deux mors qui descendent vers un point noir.
  // La Foreuse mord la roche, celle-ci mord le RIEN — et le point noir au centre est
  // le meme que celui du noyau, pour qu'on relie les deux d'un coup d'oeil.
  rift_core_extractor: (c, m, a, on) => {
    c.rect(0, 0, 16, 16, m.dark);
    slab(c, 0, 0, 16, 4, m.metal, m.metalHi, m.line);
    for (const x of [3, 11]) {
      slab(c, x, 4, 2, 6, IRON.mid, IRON.lite, IRON.deep);
    }
    c.rect(5, 9, 2, 2, IRON.lite);
    c.rect(9, 9, 2, 2, IRON.lite);
    outline(c, DISC8, on ? a.mid : m.line);
    fill(c, DISC8, on ? a.deep : m.tones[0]);
    fill(c, DISC4, '#0A0410');
    if (on) { c.set(6, 6, a.hot); c.set(9, 10, a.hot); }
  },

  // UN DOME, et rien dessous. Le Ward ne fabrique pas, ne transporte pas, ne mord
  // pas : il COUVRE. Un arc plein sur toute la largeur, c'est la seule facade du mod
  // qui montre un abri.
  rift_ward_emitter: (c, m, a, on) => {
    c.rect(0, 0, 16, 16, m.dark);
    slab(c, 0, 12, 16, 4, m.metal, m.metalHi, m.line);
    const dome = [[3, 6, 9], [4, 4, 11], [5, 3, 12], [6, 2, 13], [7, 1, 14],
      [8, 1, 14], [9, 1, 14], [10, 1, 14], [11, 1, 14]];
    outline(c, dome, on ? a.lite : m.tones[3]);
    fill(c, dome, on ? a.deep : m.tones[0]);
    // Le vide protege, sous l'arc.
    fill(c, [[6, 4, 11], [7, 3, 12], [8, 3, 12], [9, 3, 12], [10, 3, 12], [11, 3, 12]], m.dark);
    if (on) { c.set(8, 3, a.hot); c.set(7, 4, a.hot); }
  },

  // La même lentille, plus la réglette des trois bandes.
  tunable_field_emitter: (c, m, a, on) => {
    faces.field_emitter(c, m, a, on);
    c.rect(2, 13, 12, 2, m.dark);
    const bands = on ? [V.mid, C.mid, '#D8922A'] : [V.deep, C.deep, '#6E4A15'];
    bands.forEach((col, i) => c.rect(3 + i * 4, 14, 3, 1, col));
  },

  // UN PUPITRE : bandeau de laiton et lignes de texte, alignées sur la grille.
  // UNE GUEULE DE FOUR : une arche noire barrée d'une grille, sous un manteau
  // massif. Aucune autre machine du mod n'a d'ouverture aussi large — on doit
  // reconnaître « ça fond du métal » sans lire une ligne.
  veskorian_alloy_forge: (c, m, a, on) => {
    slab(c, 0, 0, 16, 5, m.metal, m.metalHi, m.line);
    slab(c, 1, 5, 14, 10, m.tones[1], m.tones[3], m.line);
    const mouth = [[7, 4, 11], [8, 3, 12], [9, 3, 12], [10, 3, 12], [11, 3, 12], [12, 3, 12], [13, 3, 12]];
    outline(c, mouth, m.dark);
    fill(c, mouth, on ? A.deep : m.dark);
    if (on) {
      fill(c, [[9, 4, 11], [10, 4, 11], [11, 4, 11]], A.mid);
      fill(c, [[10, 6, 9], [11, 6, 9]], A.hot);
    }
    for (let x = 4; x <= 11; x += 2) c.rect(x, 7, 1, 6, on ? m.line : m.dark);
    for (const [x, y] of [[2, 2], [13, 2]]) { c.rect(x, y, 2, 2, m.metalHi); c.set(x, y, m.metal); }
  },

  attunement_console: (c, m, a, on) => {
    slab(c, 1, 1, 14, 3, m.metal, m.metalHi, m.line);
    c.rect(2, 5, 12, 9, m.dark);
    for (let i = 0; i < 3; i++) c.rect(4, 6 + i * 3, i === 1 ? 5 : 8, 1, a.lite);
    c.set(4, 6, a.hot);
  },
};

/** Une façade complète. */
function front(tier, seed, face, on, accent) {
  const m = MARBLE[tier];
  const c = marble(m, seed);
  face(c, m, accent || V, on);
  edges(c, m);
  return c;
}

/** Flanc : marbre nu et un bandeau de métal. Sobre exprès : la façade porte tout. */
function side(tier, seed) {
  const m = MARBLE[tier];
  const c = marble(m, seed);
  slab(c, 0, 6, 16, 3, m.metal, m.metalHi, m.line);
  edges(c, m);
  return c;
}

/** Dessus : marbre et un carré de métal. */
function top(tier, seed) {
  const m = MARBLE[tier];
  const c = marble(m, seed);
  c.frameRect(3, 3, 10, 10, m.metal);
  for (let i = 3; i < 13; i++) c.set(i, 3, m.metalHi);
  edges(c, m);
  return c;
}

module.exports = { MARBLE, V, C, A, WOOD, IRON, S, marble, edges, slab, faces, front, side, top };
