// Textures d'items 16x16 — mêmes règles que les blocs.
//
// Les items dataient d'avant la calibration : ils étaient dessinés en cartes de
// pixels, à la main, avec des palettes inventées au fil de l'eau. Ils ne juraient
// pas seuls, mais à côté du marbre ils appartenaient à un autre mod.
//
// Règles reprises telles quelles :
//   - palette INDEXÉE, aucun fondu alpha, arêtes franches ;
//   - les teintes viennent des MÊMES familles que les blocs (marbre, laiton,
//     cuivre, fer, violet/cyan/ambre de la Résonance) ;
//   - formes prises dans les tables de spans quand elles sont symétriques, donc
//     centrées au pixel près ;
//   - un item = une silhouette. Deux objets ne doivent jamais se distinguer par la
//     seule couleur : dans une barre d'action on lit la forme d'abord.

const { Canvas } = require('./draw');
const { fill, outline } = require('./shapes');

const S = 16;

// --- Familles de teintes, communes avec les blocs -------------------------
const V = { line: '#3A1D57', deep: '#5C2C86', mid: '#8A47B8', lite: '#B57CE0', hot: '#E4CCF7' };
const C = { line: '#0E4449', deep: '#166B72', mid: '#27A3AC', lite: '#5FD6DC', hot: '#CFF6F8' };
const A = { line: '#6E4A15', deep: '#8E5A15', mid: '#D8922A', lite: '#F0B863', hot: '#FBE0B0' };
const M = { line: '#8E8E8E', deep: '#B8B8B8', mid: '#D3D3D3', lite: '#E0E0E0', hot: '#F2F2F2' };
const RIFT = { line: '#1E0F2E', deep: '#3A1D57', mid: '#5C2C86', lite: '#8A47B8', hot: '#B57CE0' };
const IRON = { line: '#1E2228', deep: '#2E323A', mid: '#454B56', lite: '#5E6672', hot: '#828A96' };
// Le T4 : presque blanc, a peine teinte. La chaine va du violet sourd au blanc
// — plus on raffine, moins la matiere a de couleur propre.
const HYPER = { line: '#5E7A8E', deep: '#8FB3C6', mid: '#BBD9E6', lite: '#DDEEF5', hot: '#FFFFFF' };
const BRASS = { line: '#6E5420', deep: '#8A6A2A', mid: '#C9A24A', lite: '#E8CE8A', hot: '#F7EBC4' };
const COPPER = { line: '#5E3517', deep: '#8A4E24', mid: '#A8632F', lite: '#C9834E', hot: '#E0A87A' };
const WOOD = { line: '#33240F', deep: '#4A3520', mid: '#6E5436', lite: '#8F7048', hot: '#B08E5E' };
const PAPER = { line: '#6E5E3A', deep: '#9E8A5E', mid: '#C6B183', lite: '#DCCBA4', hot: '#EFE3C6' };
// Alliage veskorien : un acier PÂLE veiné de violet, et surtout pas le fer vanilla
// — dessiné dans la palette IRON, le lingot virait au galet sombre, illisible à
// côté du minerai brut. Un alliage se reconnaît à sa clarté.
const ALLOY = { line: '#2A2833', deep: '#4A4658', mid: '#7C7890', lite: '#A8A4BA', hot: '#D2CEE0' };
const SLUDGE = { line: '#241E2C', deep: '#3A3048', mid: '#4E4260', lite: '#665A7A', hot: '#847A96' };

/** Cristal : losange taillé, trois facettes. La forme signature du mod. */
function crystal(c, p, x, y, w, h) {
  const cx = x + w / 2, cy = y + h / 2;
  const spans = [];
  for (let j = 0; j < h; j++) {
    const t = 1 - Math.abs((j + 0.5) - h / 2) / (h / 2);
    const half = Math.max(1, Math.round((w / 2) * t));
    spans.push([y + j, Math.round(cx - half), Math.round(cx + half) - 1]);
  }
  outline(c, spans, p.line);
  fill(c, spans, p.mid);
  // Facette éclairée : la moitié gauche, d'un cran plus clair. Deux valeurs
  // suffisent à dire « taillé » ; trois font du dégradé, donc de la bouillie.
  for (const [yy, x0, x1] of spans) {
    const midX = Math.round((x0 + x1) / 2);
    for (let xx = x0; xx <= midX; xx++) c.set(xx, yy, p.lite);
  }
  c.set(Math.round(cx) - 1, Math.round(cy) - 2, p.hot);
  return spans;
}

/** Pastille métallique 2x2 avec son point de lumière. */
function stud(c, p, x, y) {
  c.rect(x, y, 2, 2, p.mid);
  c.set(x, y, p.lite);
  c.set(x + 1, y + 1, p.deep);
  return c;
}

/** Barre pleine cernée : la brique des outils et des plaques. */
function bar(c, p, x, y, w, h) {
  c.rect(x - 1, y - 1, w + 2, h + 2, p.line);
  c.rect(x, y, w, h, p.mid);
  c.rect(x, y, w, 1, p.lite);
  c.rect(x, y + h - 1, w, 1, p.deep);
  return c;
}

/**
 * Lingot hexagonal : face supérieure éclairée, flancs biseautés, arête basse
 * sombre. C'est LA silhouette d'un lingot, et elle n'appartient qu'à eux dans ce
 * mod — tout le reste est rond (minerais) ou taillé (cristaux).
 */
function ingot(c, p, vein) {
  const body = [[5, 5, 10], [6, 4, 11], [7, 3, 12], [8, 3, 12], [9, 4, 11], [10, 5, 10]];
  outline(c, body, p.line);
  fill(c, body, p.mid);
  fill(c, [[5, 6, 9]], p.hot);
  fill(c, [[6, 5, 10]], p.lite);
  fill(c, [[10, 6, 9]], p.deep);
  if (vein) {
    for (let x = 4; x <= 11; x++) c.set(x, 8, vein.mid);
    c.set(5, 8, vein.hot);
    c.set(10, 8, vein.hot);
  }
}

const items = {
  // --- La chaîne de raffinage : même forme, matière de plus en plus noble ----
  // Elles DOIVENT se ressembler (c'est la même pierre à trois états) mais la
  // taille et l'éclat progressent, donc on les distingue côte à côte.
  raw_resonance_crystal: (c) => {
    // Brut : un éclat ébréché, asymétrique, plus une esquille.
    outline(c, [[3, 6, 9], [4, 5, 10], [5, 4, 10], [6, 4, 11], [7, 4, 11],
      [8, 5, 10], [9, 5, 10], [10, 6, 9]], V.line);
    fill(c, [[3, 6, 9], [4, 5, 10], [5, 4, 10], [6, 4, 11], [7, 4, 11],
      [8, 5, 10], [9, 5, 10], [10, 6, 9]], V.deep);
    fill(c, [[4, 5, 7], [5, 4, 7], [6, 4, 7], [7, 5, 7], [8, 5, 7]], V.mid);
    c.set(6, 5, V.lite);
    c.rect(10, 11, 3, 3, V.line);
    c.rect(11, 12, 2, 2, V.deep);
  },
  stable_resonance_crystal: (c) => crystal(c, V, 3, 2, 10, 12),
  refined_resonance_crystal: (c) => {
    crystal(c, C, 3, 1, 10, 14);
    c.set(6, 5, C.hot);
    c.set(9, 9, C.hot);
  },

  // --- Matière ouvrée -------------------------------------------------------
  resonance_component: (c) => {
    // Une plaque usinée, quatre pastilles, un cristal serti : un objet FABRIQUÉ.
    bar(c, IRON, 3, 4, 10, 8);
    for (const [x, y] of [[4, 5], [10, 5], [4, 9], [10, 9]]) stud(c, BRASS, x, y);
    c.rect(7, 6, 2, 4, V.mid);
    c.set(7, 6, V.hot);
  },
  resonance_dust: (c) => {
    // Un tas conique : la seule silhouette « en vrac » du lot.
    outline(c, [[9, 6, 9], [10, 4, 11], [11, 3, 12], [12, 2, 13], [13, 1, 14]], V.line);
    fill(c, [[9, 6, 9], [10, 4, 11], [11, 3, 12], [12, 2, 13], [13, 1, 14]], V.deep);
    fill(c, [[10, 5, 8], [11, 4, 9], [12, 3, 10], [13, 2, 11]], V.mid);
    for (const [x, y] of [[5, 4], [9, 2], [11, 6], [3, 7]]) c.set(x, y, V.lite);
  },
  raw_flux_deposit: (c) => {
    // Un caillou irrégulier, moucheté : ça se brosse, ça ne se taille pas.
    const rock = [[4, 5, 10], [5, 3, 12], [6, 2, 13], [7, 2, 13],
      [8, 2, 13], [9, 3, 12], [10, 4, 11], [11, 6, 9]];
    outline(c, rock, IRON.line);
    fill(c, rock, IRON.mid);
    fill(c, [[5, 4, 7], [6, 3, 6], [7, 3, 5]], IRON.lite);
    for (const [x, y] of [[5, 6], [9, 5], [7, 8], [11, 8], [4, 9]]) c.set(x, y, V.mid);
  },
  resonance_sludge: (c) => {
    // Une masse qui coule, deux gouttes de longueurs différentes.
    const blob = [[4, 4, 11], [5, 3, 12], [6, 2, 13], [7, 2, 13], [8, 3, 12], [9, 4, 11]];
    outline(c, blob, SLUDGE.line);
    fill(c, blob, SLUDGE.mid);
    fill(c, [[5, 4, 7], [6, 3, 6]], SLUDGE.lite);
    c.rect(4, 10, 2, 3, SLUDGE.mid);
    c.rect(9, 10, 2, 5, SLUDGE.mid);
    c.rect(4, 10, 1, 3, SLUDGE.line);
    c.rect(9, 10, 1, 5, SLUDGE.line);
    c.set(6, 6, SLUDGE.hot);
  },
  resonance_spore: (c) => {
    // Une capsule organique : la seule forme molle, et la seule qui flotte.
    const pod = [[5, 6, 9], [6, 5, 10], [7, 4, 11], [8, 4, 11], [9, 5, 10], [10, 6, 9]];
    outline(c, pod, V.line);
    fill(c, pod, V.mid);
    fill(c, [[6, 5, 7], [7, 4, 6], [8, 4, 6]], V.lite);
    c.set(6, 7, V.hot);
    c.set(7, 2, V.lite);
    c.set(10, 3, V.lite);
  },
  custode_alloy_fragment: (c) => {
    // Une plaque BRISÉE : bord droit en escalier, liseré laiton en haut.
    const shard = [[3, 3, 11], [4, 3, 12], [5, 3, 11], [6, 3, 10],
      [7, 3, 10], [8, 3, 9], [9, 3, 8], [10, 3, 7], [11, 3, 6], [12, 3, 5]];
    outline(c, shard, IRON.line);
    fill(c, shard, IRON.mid);
    c.rect(3, 3, 9, 1, BRASS.mid);
    c.set(3, 3, BRASS.lite);
    fill(c, [[5, 4, 5], [6, 4, 5], [7, 4, 5]], IRON.lite);
    c.set(6, 6, IRON.line);
    c.set(9, 8, IRON.line);
  },

  // --- Outils : silhouettes franchement différentes -------------------------
  resonance_tuner: (c) => {
    // Diagonale : c'est un outil qu'on tient. Manche bois, tête laiton, molette.
    for (let i = 0; i < 7; i++) bar(c, WOOD, 3 + i, 12 - i, 2, 2);
    bar(c, BRASS, 9, 3, 5, 5);
    c.rect(10, 4, 3, 3, A.mid);
    c.set(11, 5, A.hot);
  },
  resonance_locator: (c) => {
    // Rond : une boussole. Aucun autre item n'est circulaire.
    const disc = [[3, 6, 9], [4, 4, 11], [5, 3, 12], [6, 2, 13], [7, 2, 13],
      [8, 2, 13], [9, 2, 13], [10, 3, 12], [11, 4, 11], [12, 6, 9]];
    outline(c, disc, IRON.line);
    fill(c, disc, IRON.mid);
    fill(c, [[4, 5, 7], [5, 4, 6]], IRON.lite);
    fill(c, [[5, 6, 9], [6, 5, 10], [7, 5, 10], [8, 5, 10], [9, 6, 9]], IRON.line);
    c.rect(7, 5, 2, 5, C.mid);
    c.rect(7, 6, 2, 2, C.hot);
  },
  resonance_storage_cell: (c) => {
    // Une pile debout, avec sa borne et sa fenêtre de charge.
    bar(c, IRON, 4, 3, 8, 11);
    c.rect(6, 1, 4, 2, BRASS.mid);
    c.set(6, 1, BRASS.lite);
    c.rect(6, 5, 4, 7, IRON.line);
    c.rect(6, 5, 4, 4, C.mid);
    c.rect(6, 5, 4, 1, C.hot);
    c.rect(6, 9, 4, 3, C.deep);
  },
  resonance_catalyst_core: (c) => {
    // Un octogone serti : compact, scellé, symétrique. Ça s'installe et s'oublie.
    const oct = [[3, 6, 9], [4, 4, 11], [5, 3, 12], [6, 3, 12], [7, 3, 12],
      [8, 3, 12], [9, 3, 12], [10, 4, 11], [11, 6, 9]];
    outline(c, oct, BRASS.line);
    fill(c, oct, BRASS.mid);
    fill(c, [[4, 5, 7], [5, 4, 6]], BRASS.lite);
    fill(c, [[5, 6, 9], [6, 5, 10], [7, 5, 10], [8, 5, 10], [9, 6, 9]], V.deep);
    c.rect(7, 6, 2, 3, V.mid);
    c.set(7, 6, V.hot);
  },

  // --- Papier et lore -------------------------------------------------------
  resonance_blueprint: (c) => {
    // Une feuille bien découpée, tracés violets. Bord NET : c'est un plan.
    bar(c, PAPER, 2, 2, 12, 12);
    for (const [y, w] of [[4, 8], [6, 5], [8, 9], [10, 6]]) c.rect(3, y, w, 1, V.mid);
    c.rect(3, 3, 10, 1, PAPER.hot);
  },
  codex_fragment: (c) => {
    // Une page DÉCHIRÉE : bord droit irrégulier. C'est ce qui la distingue du plan.
    const page = [[2, 3, 10], [3, 3, 11], [4, 3, 10], [5, 3, 12], [6, 3, 11],
      [7, 3, 12], [8, 3, 10], [9, 3, 11], [10, 3, 9], [11, 3, 10], [12, 3, 8]];
    outline(c, page, PAPER.line);
    fill(c, page, PAPER.mid);
    for (const [y, w] of [[4, 5], [6, 6], [8, 4], [10, 5]]) c.rect(4, y, w, 1, PAPER.line);
    c.rect(4, 3, 6, 1, PAPER.hot);
  },
  resonance_codex: (c) => {
    // Un tome relié : tranche de pages à droite, emblème sur la couverture.
    bar(c, V, 2, 2, 11, 12);
    c.rect(12, 3, 2, 10, PAPER.mid);
    c.rect(12, 3, 2, 1, PAPER.hot);
    c.rect(2, 2, 2, 12, V.deep);
    fill(c, [[6, 6, 8], [7, 5, 9], [8, 6, 8]], V.hot);
  },
  fossilized_ration: (c) => {
    // Un paquet ficelé : la corde en croix est ce qui dit « nourriture emballée ».
    bar(c, WOOD, 3, 4, 10, 9);
    c.rect(7, 4, 2, 9, WOOD.line);
    c.rect(3, 7, 10, 2, WOOD.line);
    c.rect(4, 5, 3, 2, WOOD.lite);
    c.rect(6, 2, 4, 2, WOOD.deep);
  },

  // --- Matériaux T3 (04-Materials, 05-Machines) ---------------------------
  //
  // Contrainte du fichier, et elle a failli être ratée : « un item = une
  // silhouette ; deux objets ne doivent jamais se distinguer par la seule
  // couleur ». Les deux lingots dessinés en galets ronds étaient impossibles à
  // séparer du raw_flux_deposit et de la scorie — quatre cailloux gris dans une
  // barre d'action. D'où un LINGOT hexagonal, à face supérieure éclairée : la
  // silhouette la plus universellement lisible qui soit, et la seule du mod.
  veskorian_alloy_ingot: (c) => {
    ingot(c, ALLOY, null);
  },
  // La conductrice se lit à sa VEINE et à son laiton, pas à une nuance de gris :
  // même silhouette, un filet de résonance qui la traverse de part en part.
  veskorian_conductive_alloy_ingot: (c) => {
    ingot(c, BRASS, V);
  },
  // Scorie : ANGULEUSE et mate, là où les minerais sont ronds. C'est un déchet,
  // il doit se refuser à l'oeil avant même qu'on lise son nom.
  flux_slag: (c) => {
    const shard = [[4, 7, 9], [5, 5, 11], [6, 4, 12], [7, 3, 12], [8, 3, 11], [9, 4, 9], [10, 6, 8]];
    outline(c, shard, SLUDGE.line);
    fill(c, shard, SLUDGE.deep);
    fill(c, [[5, 6, 8], [6, 5, 7]], SLUDGE.mid);
    c.rect(2, 11, 3, 2, SLUDGE.deep); c.rect(2, 11, 1, 2, SLUDGE.line);
    c.rect(11, 10, 3, 3, SLUDGE.deep); c.rect(11, 10, 1, 3, SLUDGE.line);
    c.set(7, 5, V.deep); c.set(6, 8, V.line);
  },
  // Résidu de synthèse : une poudre TASSÉE en galette, pas un tas. Elle sort
  // d'une presse, elle ne s'est pas déposée.
  synthesis_residue: (c) => {
    bar(c, M, 3, 7, 10, 5);
    c.rect(4, 8, 8, 3, SLUDGE.mid);
    for (const [x, y] of [[5, 9], [8, 8], [10, 10]]) c.set(x, y, V.deep);
    c.rect(3, 6, 10, 1, M.hot);
  },
  // --- Matiere du T4 -------------------------------------------------------
  //
  // Le quatrieme etat du cristal doit se lire comme le PLUS NOBLE de la chaine
  // sans casser la famille : meme losange taille, mais DOUBLE — un coeur pris
  // dans une gangue. Aucun autre item du mod n'a de forme imbriquee, donc la
  // silhouette reste unique meme reduite a sa decoupe.
  hyper_refined_crystal: (c) => {
    crystal(c, HYPER, 2, 0, 12, 16);
    // Le coeur : un second losange, plus petit, d'une autre matiere. C'est lui
    // qui dit « synthetise » plutot que « taille ».
    const core = [[6, 7, 8], [7, 6, 9], [8, 6, 9], [9, 7, 8]];
    outline(c, core, V.line);
    fill(c, core, V.mid);
    c.set(7, 7, V.hot);
    for (const [x, y] of [[4, 3], [11, 5], [5, 12], [10, 11]]) c.set(x, y, HYPER.hot);
  },

  // Treillis harmonique : une GRILLE OUVERTE. Ni un cristal (taille), ni un
  // lingot (hexagone), ni une poudre (tas) — la seule silhouette ajouree du mod,
  // et c'est ce qui la rend reconnaissable a la decoupe seule. Elle dit aussi ce
  // que fait la piece : elle ne stocke rien, elle laisse passer et repartit.
  harmonic_lattice: (c) => {
    // Cadre : DEUX anneaux de 1 px, jamais un carre plein. Le vide central n'est
    // pas efface apres coup — il n'est simplement jamais peint. La toile est
    // transparente au depart, et c'est la seule facon d'obtenir un objet ajoure :
    // `set` ecrit toujours en opaque, donc rien ne se « creuse » a posteriori.
    c.frameRect(2, 2, 12, 12, BRASS.line);
    c.frameRect(3, 3, 10, 10, BRASS.mid);
    c.rect(3, 3, 10, 1, BRASS.lite);
    c.rect(3, 12, 10, 1, BRASS.deep);
    // Deux montants et deux traverses : le tissage.
    c.rect(7, 3, 2, 10, BRASS.mid);
    c.rect(3, 7, 10, 2, BRASS.mid);
    c.rect(7, 3, 1, 10, BRASS.lite);
    c.rect(3, 7, 10, 1, BRASS.lite);
    // Noeuds de resonance aux croisements : c'est la ou l'energie se repartit.
    for (const [x, y] of [[7, 7], [7, 3], [7, 11], [3, 7], [11, 7]]) {
      c.rect(x, y, 2, 2, V.mid);
      c.set(x, y, V.hot);
    }
  },

  // Matrice de Resonance : NEUF CASES PLEINES, cadrees. C'est l'inverse exact du
  // Treillis Harmonique, qui est ajoure — et cette opposition est voulue, parce que
  // les deux sont des pieces intermediaires du meme reseau et qu'on doit les separer
  // d'un coup d'oeil dans un inventaire. Le Treillis LAISSE PASSER et repartit ; la
  // Matrice CONTIENT et ordonne. Ajoure contre plein, c'est tout ce qu'il faut.
  resonance_matrix: (c) => {
    c.frameRect(2, 2, 12, 12, BRASS.line);
    c.rect(3, 3, 10, 10, BRASS.deep);
    for (const gx of [0, 1, 2]) for (const gy of [0, 1, 2]) {
      const x = 4 + gx * 3, y = 4 + gy * 3;
      const mid = gx === 1 && gy === 1;
      c.rect(x, y, 2, 2, mid ? V.mid : BRASS.mid);
      c.set(x, y, mid ? V.hot : BRASS.lite);
    }
  },

  // Graine Ancienne : une graine, PAS un cristal. Le joueur doit penser à la planter
  // sans lire son infobulle — donc la silhouette est celle d'une amande, et la seule
  // marque veskorienne est une veine qui la traverse.
  ancient_seed: (c) => {
    const SEED = { line: '#4A3A22', deep: '#6E5836', mid: '#8F7648', lite: '#B39A66' };
    const grain = [[5,6,10],[6,5,11],[7,4,12],[8,4,12],[9,5,11],[10,6,10]];
    outline(c, grain, SEED.line);
    fill(c, grain, SEED.mid);
    fill(c, [[6,6,9],[7,5,10],[8,5,10]], SEED.lite);
    for (let y = 5; y < 11; y++) c.set(8, y, V.deep);
    c.set(8, 7, V.mid);
  },

  // Floraison : quatre pétales autour d'un coeur clair. Elle se mange et elle éclaire —
  // le coeur est donc la partie la plus lumineuse de tout le lot d'items.
  resonance_bloom: (c) => {
    const LEAF = { deep: '#2E4A38', mid: '#3F6B4E' };
    for (let y = 10; y < 15; y++) c.set(8, y, LEAF.mid);
    c.set(7, 12, LEAF.deep); c.set(9, 13, LEAF.deep);
    for (const [x, y] of [[7,3],[7,7],[4,5],[10,5]]) {
      c.rect(x, y, 3, 3, V.mid);
      c.set(x + 1, y + 1, V.lite);
    }
    c.rect(7, 5, 3, 3, V.hot);
    c.set(8, 6, '#FFFFFF');
  },

  // Extrait Lumineux : la MEME fiole que le Flux Concentre, remplie d'autre chose. Deux
  // liquides du mod, une seule silhouette de contenant — c'est ce qui fait lire « ceci
  // se verse » avant de lire lequel. Le contenu, lui, est presque blanc : c'est de la
  // lumiere en bouteille, pas un cristal fondu.
  luminous_extract: (c) => {
    const vial = [[3, 6, 9], [4, 5, 10], [5, 4, 11], [6, 4, 11], [7, 4, 11],
      [8, 4, 11], [9, 4, 11], [10, 5, 10], [11, 6, 9], [12, 7, 8]];
    outline(c, vial, IRON.line);
    fill(c, vial, V.mid);
    fill(c, [[5, 5, 10], [6, 5, 10], [7, 5, 10], [8, 5, 10]], V.lite);
    fill(c, [[6, 6, 9], [7, 6, 9]], V.hot);
    c.set(7, 7, '#FFFFFF'); c.set(8, 7, '#FFFFFF');
    bar(c, IRON, 5, 2, 6, 2);
    c.set(7, 3, V.hot);
  },

  // --- Matiere du T5 -------------------------------------------------------
  //
  // L'essence est le SEUL item du mod dont le centre soit noir. Toute la chaine du
  // cristal va vers la lumiere ; celle-ci va vers le trou. C'est ce qui la separe
  // d'un cristal de plus, et ce qui dit « ceci vient d'ailleurs ».
  rift_essence: (c) => {
    const orb = [[4, 5, 10], [5, 4, 11], [6, 3, 12], [7, 3, 12], [8, 3, 12],
      [9, 4, 11], [10, 5, 10]];
    outline(c, orb, RIFT.line);
    fill(c, orb, RIFT.mid);
    fill(c, [[5, 5, 10], [6, 4, 11], [7, 4, 11], [8, 4, 11], [9, 5, 10]], RIFT.lite);
    fill(c, [[6, 6, 9], [7, 6, 9], [8, 6, 9]], RIFT.deep);
    fill(c, [[7, 7, 8]], '#0A0410');
    // Deux echardes qui s'echappent : rien dans ce mod ne fuit, sauf ca.
    c.set(2, 3, RIFT.lite); c.set(3, 4, RIFT.mid);
    c.set(13, 12, RIFT.lite); c.set(12, 11, RIFT.mid);
  },

  // Meme lingot hexagonal que les autres alliages — c'est le meme metal — mais RONGE :
  // l'arete basse est mangee et la veine est noire au lieu d'etre lumineuse.
  corrupted_veskorian_alloy_ingot: (c) => {
    ingot(c, RIFT, null);
    for (let x = 4; x <= 11; x++) c.set(x, 8, '#0A0410');
    c.set(5, 8, RIFT.lite);
    c.set(10, 8, RIFT.lite);
    // Morsures : trois pixels retires de la silhouette, en bas.
    for (const [x, y] of [[5, 10], [8, 10], [11, 9]]) c.set(x, y, RIFT.line);
  },

  // --- Outils et armure ----------------------------------------------------
  //
  // Silhouettes vanilla, delibérément : une épée doit se lire comme une épée dans une
  // barre d'action, à la découpe, avant toute couleur. Ce qui les rattache au mod est
  // la MATIERE — l'acier pale de l'alliage et la veine de résonance, jamais la forme.

  veskorian_alloy_sword: (c) => {
    // Lame en diagonale, du coin bas-gauche au coin haut-droit.
    for (let i = 0; i < 10; i++) {
      c.set(4 + i, 11 - i, ALLOY.lite);
      c.set(5 + i, 11 - i, ALLOY.mid);
      c.set(4 + i, 12 - i, ALLOY.line);
    }
    c.set(13, 2, ALLOY.hot);
    // Garde et poignée.
    for (let i = 0; i < 4; i++) c.set(2 + i, 14 - i, WOOD.mid);
    c.set(2, 14, WOOD.deep);
    c.line(2, 11, 6, 15, BRASS.mid);
    c.set(4, 13, V.mid);
  },

  veskorian_alloy_pickaxe: (c) => {
    // Tête en arc, manche en diagonale.
    for (let i = 0; i < 5; i++) { c.set(3 + i, 4 - Math.min(i, 2), ALLOY.mid); }
    for (let i = 0; i < 5; i++) { c.set(8 + i, 2 + Math.min(i, 2), ALLOY.mid); }
    c.rect(3, 5, 10, 1, ALLOY.line);
    c.rect(6, 3, 4, 1, ALLOY.lite);
    c.set(7, 3, V.mid);
    for (let i = 0; i < 8; i++) { c.set(7 + i - i, 6 + i, WOOD.mid); c.set(6 - 0, 6 + i, WOOD.mid); }
    for (let i = 0; i < 8; i++) c.set(6, 6 + i, WOOD.deep);
    for (let i = 0; i < 8; i++) c.set(7, 6 + i, WOOD.lite);
  },

  veskorian_alloy_helmet: (c) => {
    const dome = [[3, 4, 11], [4, 3, 12], [5, 2, 13], [6, 2, 13], [7, 2, 13]];
    outline(c, dome, ALLOY.line);
    fill(c, dome, ALLOY.mid);
    fill(c, [[4, 4, 8], [5, 3, 7]], ALLOY.lite);
    // Visière ouverte : la bande sombre qui fait lire « casque » et non « bol ».
    c.rect(3, 8, 10, 3, ALLOY.deep);
    c.rect(4, 9, 8, 1, IRON.line);
    c.set(7, 6, V.mid);
    c.set(8, 6, V.hot);
    c.rect(2, 11, 12, 2, ALLOY.mid);
    c.rect(2, 11, 12, 1, ALLOY.lite);
  },

  veskorian_alloy_chestplate: (c) => {
    bar(c, ALLOY, 4, 4, 8, 9);
    // Epaulieres, qui debordent : c'est ce qui separe un plastron d'une plaque.
    c.rect(2, 3, 3, 4, ALLOY.mid); c.rect(2, 3, 3, 1, ALLOY.lite);
    c.rect(11, 3, 3, 4, ALLOY.mid); c.rect(11, 3, 3, 1, ALLOY.lite);
    c.rect(7, 5, 2, 6, ALLOY.line);
    c.rect(7, 6, 2, 2, V.mid);
    c.set(7, 6, V.hot);
  },

  veskorian_alloy_leggings: (c) => {
    bar(c, ALLOY, 4, 2, 8, 5);
    // Deux jambes separees par un vide : la silhouette la plus reconnaissable du lot.
    c.rect(4, 7, 3, 7, ALLOY.mid); c.rect(4, 7, 1, 7, ALLOY.lite);
    c.rect(9, 7, 3, 7, ALLOY.mid); c.rect(9, 7, 1, 7, ALLOY.lite);
    c.rect(4, 13, 3, 1, ALLOY.deep);
    c.rect(9, 13, 3, 1, ALLOY.deep);
    c.set(7, 4, V.mid);
  },

  veskorian_alloy_boots: (c) => {
    for (const x of [3, 9]) {
      c.rect(x, 6, 4, 5, ALLOY.mid);
      c.rect(x, 6, 4, 1, ALLOY.lite);
      c.rect(x, 11, 4, 2, ALLOY.deep);
      c.rect(x, 12, 4, 1, ALLOY.line);
    }
    c.set(4, 8, V.mid);
    c.set(10, 8, V.mid);
  },

  // Le plastron anti-Faille : MEME silhouette que celui d'alliage, matiere de la Faille.
  // On doit voir que c'est le meme objet, transforme — pas un objet de plus.
  rift_ward_plate: (c) => {
    bar(c, RIFT, 4, 4, 8, 9);
    c.rect(2, 3, 3, 4, RIFT.mid); c.rect(2, 3, 3, 1, RIFT.lite);
    c.rect(11, 3, 3, 4, RIFT.mid); c.rect(11, 3, 3, 1, RIFT.lite);
    // Le coeur noir de la Faille, au centre de la poitrine.
    c.rect(6, 6, 4, 4, RIFT.deep);
    c.rect(7, 7, 2, 2, '#0A0410');
    for (const [x, y] of [[5, 5], [10, 5], [5, 10], [10, 10]]) c.set(x, y, RIFT.hot);
  },

  concentrated_flux: (c) => {
    const vial = [[3, 6, 9], [4, 5, 10], [5, 4, 11], [6, 4, 11], [7, 4, 11],
      [8, 4, 11], [9, 4, 11], [10, 5, 10], [11, 6, 9], [12, 7, 8]];
    outline(c, vial, IRON.line);
    fill(c, vial, C.deep);
    fill(c, [[5, 5, 10], [6, 5, 10], [7, 5, 10], [8, 5, 10]], C.mid);
    fill(c, [[6, 6, 9], [7, 6, 9]], C.hot);
    bar(c, IRON, 5, 2, 6, 2);
    c.set(7, 3, C.hot); c.set(8, 3, C.lite);
  },
};

module.exports = { items, S };
