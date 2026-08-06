// Sprites 16x16 des items de Veskorius.
//
// Direction artistique commune :
//  - lumière en haut à gauche (highlight en haut/gauche, ombre en bas/droite) ;
//  - contour 1px dans une teinte SOMBRE DU MATÉRIAU, jamais du noir pur ;
//  - 4-5 valeurs par matériau, pas plus : à 16px, le bruit tue la silhouette ;
//  - une famille de teintes par matière, pour que la chaîne de raffinage se lise
//    d'un coup d'œil (violet brut → violet stable → cyan raffiné).
//
// Chaque caractère = une entrée de palette ; '.' = transparent.

// --- Familles de teintes partagées ---------------------------------------
const RAW = { o: '#2A1338', d: '#4A2168', m: '#7A3AA8', l: '#A85FD6', h: '#D9A6F5' };
const STABLE = { o: '#2E1740', d: '#6B3D94', m: '#9B59D0', l: '#C9A0E8', h: '#EEDCFA' };
const REFINED = {
  o: '#0E3338', d: '#1E6E77', m: '#35C0C8', l: '#5FD6DC', h: '#A8EEF2', H: '#EDFEFF',
};
const STEEL = { o: '#181C22', d: '#3A3F4A', m: '#5A6272', l: '#8A93A3', h: '#C2C9D6' };
const BRASS = '#C9A24A';
const REDSTONE = '#E23A3A';

const sprites = {
  // Éclat brut : arête cassée, asymétrique, avec un éclat détaché. L'irrégularité
  // EST l'information — c'est ce qui le distingue du cristal stabilisé.
  raw_resonance_crystal: {
    palette: RAW,
    rows: [
      '................',
      '..........oo....',
      '.........ohlo...',
      '........ohhldo..',
      '.......ohhmldo..',
      '......ohhmmldo..',
      '.....ohlmmmldo..',
      '....ohlmmmmldo..',
      '...ohlmmmmmldo..',
      '..ohlmmmmmldo...',
      '..olmmmmmldo....',
      '..odmmmmldo.....',
      '..oodmmldo.ooo..',
      '...oodmldo.ohlo.',
      '....oodddo.olmo.',
      '.....ooooo.oooo.',
    ],
  },

  // Cristal stabilisé : gemme taillée, symétrique, facettes nettes. Le contraste
  // avec l'éclat brut raconte tout le travail du Stabilizer.
  stable_resonance_crystal: {
    palette: STABLE,
    rows: [
      '................',
      '......oooo......',
      '.....ohhllo.....',
      '....ohhdmllo....',
      '...ohhmdmmllo...',
      '..ohhmmdmmmllo..',
      '..ohmmmdmmmmlo..',
      '..odmmmdmmmmdo..',
      '..odmmmdmmmmdo..',
      '..oodmmdmmmdoo..',
      '...oodmdmmdoo...',
      '....oodmddoo....',
      '.....oodmdoo....',
      '......oodoo.....',
      '.......ooo......',
      '................',
    ],
  },

  // Cristal raffiné : bascule en cyan (la bande Médiane) et gagne un cœur
  // lumineux — le raffinage se voit à la couleur, pas à un chiffre.
  refined_resonance_crystal: {
    palette: REFINED,
    rows: [
      '.......oo.......',
      '......ohho......',
      '.....ohhmlo.....',
      '....ohhmmmlo....',
      '...ohhmmmmmlo...',
      '..ohhmmHHmmmlo..',
      '..ohmmmHHmmmlo..',
      '..odmmmHHmmmdo..',
      '..odmmmHHmmmdo..',
      '..oodmmHHmmdoo..',
      '...oodmmmmdoo...',
      '....oodmmdoo....',
      '.....oodmdo.....',
      '......oodo......',
      '.......oo.......',
      '................',
    ],
  },

  // Composant : plaque usinée, rivets laiton, cristal serti au centre. Première
  // pièce *fabriquée* du mod — d'où la géométrie stricte face au minéral.
  resonance_component: {
    palette: { ...STEEL, r: BRASS, c: '#9B59D0', C: '#F0E4FF' },
    rows: [
      '................',
      '................',
      '..oooooooooooo..',
      '..ohhlllllllho..',
      '..olrmmmmmmrlo..',
      '..olmmoccommlo..',
      '..olmmocCCcomo..',
      '..olmmocCCcomo..',
      '..olmmoccommlo..',
      '..olrmmmmmmrlo..',
      '..odmmmmmmmmdo..',
      '..oddddddddddo..',
      '..oooooooooooo..',
      '................',
      '................',
      '................',
    ],
  },

  // Poussière : tas conique + quelques grains en suspension. Le tas dit « vrac »,
  // les grains disent « ça vole » — deux infos, zéro texte.
  resonance_dust: {
    palette: { o: '#2A1338', d: '#5B2C80', m: '#8A47B8', h: '#C08FE0', s: '#A85FD6' },
    rows: [
      '................',
      '................',
      '.........s......',
      '...s............',
      '..............s.',
      '.......s........',
      '....s...........',
      '..........s.....',
      '.......oo.......',
      '......ohho......',
      '....oohmmhoo....',
      '...ohmmmmmmho...',
      '..ohmmmdmdmmho..',
      '.ohmmdmmmmdmmho.',
      '.odmmmmmmmmmmdo.',
      '..oooooooooooo..',
    ],
  },

  // Dépôt de flux : croûte de pierre irrégulière, mouchetée de violet. La roche
  // domine, le violet n'est qu'un indice — c'est un minerai, pas un cristal.
  raw_flux_deposit: {
    palette: {
      o: '#241E26', d: '#4A4050', m: '#6E6478', l: '#948CA0', h: '#B8B2C2', p: '#B06FE0',
    },
    rows: [
      '................',
      '................',
      '....oooo........',
      '...ohhllo.......',
      '..ohhllmmoo.....',
      '..ohlpmmmmlo....',
      '.oohlmmmpmmlo...',
      '.ohlmmmmmmmmlo..',
      '.ohmmpmmmmpmdo..',
      '.odmmmmmmmmmdo..',
      '..odmmmpmmmmdo..',
      '..oddmmmmmmddo..',
      '...oddmmmmddo...',
      '....odddddo.....',
      '.....oooo.......',
      '................',
    ],
  },

  // Fragment d'alliage : plaque BRISÉE (bord droit déchiqueté), acier sombre à
  // liseré laiton — les deux teintes du Custode dont il tombe.
  custode_alloy_fragment: {
    palette: {
      o: '#14181E', d: '#2A2F38', m: '#3A3F4A', l: '#5F6878', h: '#8A93A3',
      b: BRASS, r: '#1E2228',
    },
    rows: [
      '................',
      '................',
      '...oooooooo.....',
      '..obhhhhhhbo....',
      '..ohlmmmmmlbo...',
      '..ohmmrmmmmbo...',
      '..ohmmmmmmmlo...',
      '..ohmmmmrmmoo...',
      '..ohmmmmmmoo....',
      '..ohmmmmmoo.....',
      '..ohmrmmoo......',
      '..ohmmmoo.......',
      '..odmmoo........',
      '..oddoo.........',
      '..ooo...........',
      '................',
    ],
  },

  // Catalyst Core : boîtier fermé, cœur violet visible par une fenêtre. Un objet
  // qu'on installe et qu'on oublie — donc compact et scellé, pas un outil.
  resonance_catalyst_core: {
    palette: { ...STEEL, G: '#4A2A6E', H: '#D9B6F5' },
    rows: [
      '................',
      '................',
      '.....oooooo.....',
      '...oohhllhhoo...',
      '..ohhlmmmmlhho..',
      '..ohlmGGGGmmlo..',
      '.ohlmGHHHHGmmlo.',
      '.ohlmGHHHHGmmlo.',
      '.ohmmGHHHHGmmdo.',
      '.odmmGGGGGGmmdo.',
      '..odmmmmmmmmdo..',
      '..oddmmmmmmddo..',
      '...oodddddddoo..',
      '.....oooooo.....',
      '................',
      '................',
    ],
  },

  // Tuner : outil tenu en main, donc en diagonale (silhouette d'outil, pas de
  // bloc). Poignée en bas à gauche, tête et molette redstone en haut à droite.
  resonance_tuner: {
    palette: { ...STEEL, g: '#4A3526', R: REDSTONE },
    rows: [
      '................',
      '.........oooo...',
      '........ohhllo..',
      '........ohRRlo..',
      '........ohRRlo..',
      '........olmmlo..',
      '.......oolmmo...',
      '......ogmmlo....',
      '.....ogmmlo.....',
      '....ogmmlo......',
      '...ogmmlo.......',
      '..ogmmlo........',
      '..ogmlo.........',
      '.oggmo..........',
      '.oggo...........',
      '..oo............',
    ],
  },

  // Locator : boîtier rond façon boussole, cadran sombre, aiguille cyan. Rond =
  // « ça pointe », le joueur n'a pas besoin qu'on le lui explique.
  resonance_locator: {
    palette: {
      o: '#14181E', d: '#3A3F4A', h: '#5F6878', l: '#8A93A3', m: '#2A3038', C: '#35C0C8',
    },
    rows: [
      '................',
      '.....oooooo.....',
      '...ooddddddoo...',
      '..odhhllllhhdo..',
      '.odhlmmmmmmlhdo.',
      '.ohlmmmCmmmmlho.',
      'odhlmmmCCmmmlhdo',
      'odhlmmCCCCmmlhdo',
      'odhlmmmCCmmmlhdo',
      'odhlmmmCmmmmlhdo',
      '.ohlmmmmmmmmlho.',
      '.odhlmmmmmmlhdo.',
      '..odhhllllhhdo..',
      '...ooddddddoo...',
      '.....oooooo.....',
      '................',
    ],
  },

  // Storage Cell : pile debout, borne en haut, fenêtre de charge en dégradé
  // (clair en haut, sombre en bas) — on lit « c'est une batterie » sans tooltip.
  resonance_storage_cell: {
    palette: {
      o: '#14181E', d: '#2A2F38', m: '#5A6272', h: '#8A93A3', l: '#C2C9D6',
      C: '#7FE3E8', c: '#1E6E77',
    },
    rows: [
      '................',
      '......oooo......',
      '......ollo......',
      '...oooooooooo...',
      '...ohhllllhho...',
      '...ohmCCCCmho...',
      '...ohmCCCCmho...',
      '...ohmCCCCmho...',
      '...ohmccccmho...',
      '...ohmccccmho...',
      '...ohmmmmmmho...',
      '...odmmmmmmdo...',
      '...oddddddddo...',
      '...oooooooooo...',
      '................',
      '................',
    ],
  },

  // Blueprint : feuille de parchemin ancien couverte de tracés violets. Papier
  // vieilli plutôt que bleu d'architecte — c'est un plan RESTAURÉ, pas neuf.
  resonance_blueprint: {
    palette: { o: '#3A2E18', h: '#D8C9A0', p: '#A8946A', v: '#6B3D94' },
    rows: [
      '................',
      '..oooooooooooo..',
      '..ohhhhhhhhhpo..',
      '..ohvvvvvvhhpo..',
      '..ohhhhhhhhhpo..',
      '..ohvvvvhhhhpo..',
      '..ohhhhhhhhhpo..',
      '..ohvvvvvvvhpo..',
      '..ohhhhhhhhhpo..',
      '..opvvvvhhhhpo..',
      '..oppppppppppo..',
      '..opvvvvvvpppo..',
      '..oppppppppppo..',
      '..oooooooooooo..',
      '................',
      '................',
    ],
  },

  // Fragment de Codex : page DÉCHIRÉE (bords irréguliers), encre passée. Le
  // contour cassé le distingue au premier regard du blueprint, bien découpé.
  codex_fragment: {
    palette: { o: '#3A2E18', h: '#D6CCB0', t: '#3E3220' },
    rows: [
      '................',
      '..oooooooo......',
      '..ohhhhhhoo.....',
      '..ohttttthho....',
      '..ohhhhhhhho....',
      '..ohtttttthoo...',
      '..ohhhhhhhhho...',
      '..ohtttttttho...',
      '..ohhhhhhhhho...',
      '..ohttttttho....',
      '..ohhhhhhhoo....',
      '..ohtttttho.....',
      '..ohhhhhho......',
      '..oooooooo......',
      '................',
      '................',
    ],
  },

  // Ration fossilisée : barre sèche ficelée. Teintes terreuses, aucune saturation
  // — c'est vieux de mille ans, ça ne doit pas avoir l'air appétissant.
  fossilized_ration: {
    palette: {
      o: '#241C14', d: '#4A3A2A', m: '#7A6048', l: '#A08868', h: '#C9B492', w: '#3A2E20',
    },
    rows: [
      '................',
      '................',
      '.......oo.......',
      '......owwo......',
      '...oooooooooo...',
      '..ohhhlwlhhhlo..',
      '..ohllllwllllo..',
      '..ohllmmwmmllo..',
      '..ohlmmmwmmmlo..',
      '..odmmmmwmmmdo..',
      '..odmmmmwmmmdo..',
      '..oddmmmwmmddo..',
      '...oddddwdddo...',
      '....oooooooo....',
      '................',
      '................',
    ],
  },

  // Sludge : masse visqueuse qui coule, avec deux gouttes qui pendent. Violet
  // DÉSATURÉ, presque sale : c'est de la dissonance cristallisée, pas une gemme.
  resonance_sludge: {
    palette: { o: '#16121C', m: '#4A3E58', h: '#6A5A7A', g: '#8A66AE' },
    rows: [
      '................',
      '................',
      '...oooooooo.....',
      '..ohhmmmmmoo....',
      '.ohmmgmmmmmmo...',
      '.ommmmmmgmmmo...',
      '.ommgmmmmmmmo...',
      '.ommmmmmmgmmo...',
      '..ommmmmmmmmo...',
      '..oommmmmmmoo...',
      '..ommoommmommo..',
      '..ommo.ommo.oo..',
      '..ooo..ommo.....',
      '........omo.....',
      '........ooo.....',
      '................',
    ],
  },

  // Spore : capsule organique lumineuse + deux spores qui s'échappent. Formes
  // arrondies partout : c'est le seul item VIVANT du lot, il ne doit pas être
  // taillé comme un minéral.
  resonance_spore: {
    palette: {
      o: '#221A30', d: '#4A3A62', m: '#6E5A8E', l: '#9E8ABE', h: '#CFC3E8',
      H: '#F2EBFF', g: '#E8DCFF',
    },
    rows: [
      '................',
      '.......g........',
      '......g.g.......',
      '.....oooo.......',
      '....ohhllo......',
      '...ohHHllmo.....',
      '...ohHHlmmo.....',
      '..oohHllmmoo....',
      '..ohHllmmmdo....',
      '..ohllmmmmdo....',
      '..odlmmmmmdo....',
      '...odmmmmdo.....',
      '...oodmmdoo.....',
      '....oooooo......',
      '................',
      '................',
    ],
  },

  // Codex : tome relié, emblème en losange sur la couverture, tranche de pages à
  // droite. Remplace le modèle de livre vanilla — le manuel signature du mod ne
  // devrait pas se confondre avec un livre d'enchantement.
  resonance_codex: {
    palette: {
      o: '#1A0F24', d: '#2E1A3C', c: '#4A2A5E', C: '#6B3D94', p: '#D8C9A0', V: '#C9A0E8',
    },
    rows: [
      '................',
      '................',
      '..oooooooooooo..',
      '..oCCCCCCCCCpo..',
      '..occcccccccpo..',
      '..occccVccccpo..',
      '..occcVVVcccpo..',
      '..occVVVVVccpo..',
      '..occcVVVcccpo..',
      '..occccVccccpo..',
      '..occcccccccpo..',
      '..occcccccccpo..',
      '..odddddddddpo..',
      '..oooooooooooo..',
      '................',
      '................',
    ],
  },
};

module.exports = { sprites };
