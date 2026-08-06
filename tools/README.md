# Générateurs de textures

Les textures du mod sont **dessinées par code**, pas éditées en binaire : un PNG 16×16 est
illisible en diff et intouchable sans éditeur, alors qu'un générateur se relit, se compare
d'une version à l'autre, et rend un réglage de palette trivial.

| Dossier | Contenu | Lancement |
|---|---|---|
| `block-textures/` | 29 textures de bloc (châssis, façades, roche) | `node tools/block-textures/genmarble.js src/main/resources/assets/veskorius/textures/block` |
| `item-textures/` | 17 items | `node tools/item-textures/genitems.js src/main/resources/assets/veskorius/textures/item` |
| `gui-textures/` | 8 panneaux, un par machine | `node tools/gui-textures/gui3.js src/main/resources/assets/veskorius/textures/gui/container` |

**Hors build Gradle** : à lancer à la main quand on retouche une texture. Les fichiers préfixés
`_` que les scripts écrivent sont des planches de contrôle — **à supprimer avant de committer**,
ce ne sont pas des assets.

## La règle qui gouverne tout : mesurer, pas se souvenir

Quatre versions ont été jetées avant de trouver la bonne méthode, et le déclic est venu d'avoir
**mesuré les assets existants** (`decode.js` décode les PNG, y compris les palettes 4 bits) :

| Référence | Couleurs | % pixels ≠ voisin |
|---|---|---|
| `stone` | 4 | 46 % |
| `deepslate` | 5 | 62 % |
| `amethyst_block` | 7 | 85 % |
| `blast_furnace_front` | 16 | 63 % |
| GUI du four | 6 | — (panneau en aplat) |
| Marbre d'Astral Sorcery | 16 | 59 % (luminance 203-238, R=G=B) |
| **Mes textures d'alors** | **58 à 178** | — |

D'où les quatre règles, dans cet ordre d'importance :

1. **Palette indexée, aucun fondu alpha.** Composer en fondu (halos, occlusion, dégradés,
   tramage) fabrique des dizaines de teintes presque identiques : de la bouillie, quel que soit
   le style choisi. C'était la cause racine, pas le style.
2. **Grain dense, faible amplitude.** Une pierre, c'est un semis serré de 4 gris quasi
   identiques. Lisser les aplats est l'erreur inverse et donne du plastique.
3. **Le contraste vient de l'accent, pas du matériau.** Marbre presque blanc → le violet de la
   Résonance explose dessus.
4. **Un GUI est un aplat.** Le grain des blocs, transposé sur 176×166, devient du bruit.

## Cadrage

Sur une grille 16×16 le centre tombe **entre** les pixels 7 et 8. Centrer un cercle sur 8 le
décale d'un demi-pixel — invisible à décrire, flagrant à l'œil (« pas cadré »). Les formes
symétriques passent donc par `shapes.js`, des tables de spans dont un contrôle vérifie que
`x0 + x1 = 15` sur chaque ligne et qu'une marge d'1 px est respectée.

## Ce qui reste à faire

Le rendu est reconnaissable et cohérent, **pas abouti** : la passe de finition esthétique est
encore devant. La méthode ci-dessus, elle, est acquise et ne devrait pas être re-perdue.
