# Sources des textures d'items

Les 17 sprites 16×16 de `src/main/resources/assets/veskorius/textures/item/` sont décrits
ici sous forme de **cartes de pixels lisibles** (un caractère = une entrée de palette) plutôt
qu'édités à la main dans un éditeur d'images.

Pourquoi : un PNG 16×16 est illisible en diff et impossible à retoucher sans rouvrir un
éditeur. La carte, elle, se relit, se compare d'une version à l'autre, et rend une
modification de palette (« tous les violets un cran plus sombre ») triviale — alors qu'elle
serait un repixel complet autrement.

**Ce dossier ne fait pas partie du build Gradle.** Il n'est lancé qu'à la main, quand on veut
retoucher une texture :

```bash
node tools/item-textures/generate.js src/main/resources/assets/veskorius/textures/item
```

Le script écrit aussi une planche de contrôle `_planche.png` (zoom ×8) dans le dossier de
sortie — **à supprimer avant de committer**, ce n'est pas un asset du mod. Pour juger un
sprite, la regarder ne suffit pas : c'est à ×2/×3 (la taille réelle d'un slot d'inventaire)
qu'une silhouette se révèle illisible.

## Direction artistique

- lumière en haut à gauche ;
- contour 1px dans une teinte **sombre du matériau**, jamais du noir pur ;
- 4-5 valeurs par matériau, pas plus — à 16px, le bruit tue la silhouette ;
- **une famille de teintes par matière**, pour que la chaîne de raffinage se lise d'un coup
  d'œil : violet brut → violet stable saturé → cyan raffiné (la couleur de la bande Médiane,
  `HarmonicBand`) ;
- l'acier des objets fabriqués et le laiton des Custodes reprennent les teintes déjà posées
  par les entités (`ModItems`, œufs d'apparition), pour que rien ne jure entre item et mob ;
- **une silhouette par item** : c'est elle qui distingue deux objets dans une barre d'action,
  pas la couleur. D'où le rond du Locator, la diagonale du Tuner, l'octogone du Catalyst Core
  face à la plaque rectangulaire du Component.

Le script **valide strictement** les cartes (16 lignes de 16 colonnes, tout caractère présent
en palette) : une ligne mal alignée échoue au lieu de produire un sprite décalé en silence.

## Reste à faire

Les textures de **blocs** (`textures/block/`) sont encore des aplats de couleur — passe
visuelle de la Phase 6.
