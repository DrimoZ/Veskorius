# Sources des textures de blocs (32×32)

Les 29 textures de `src/main/resources/assets/veskorius/textures/block/` sont **dessinées par
code** — pas de cartes de pixels comme pour les items.

Pourquoi : à 16×16 une carte de caractères se lit et s'édite ; à 32×32 c'est 1024 caractères par
texture, illisible et intenable. Surtout, la cohérence d'une **série** de machines vient de ce
que le biseau, l'occlusion, la patine et les gravures sont **la même routine** partout — pas de
vingt dessins qui se ressemblent à peu près.

```bash
node tools/block-textures/gen32.js src/main/resources/assets/veskorius/textures/block
```

Le script écrit aussi des planches de contrôle préfixées `_` (vue d'ensemble ×4, et un
**carrelage 3×3** pour les roches) — **à supprimer avant de committer**, ce ne sont pas des
assets. Le carrelage n'est pas optionnel pour juger une roche : c'est lui qui a révélé que les
veines s'arrêtaient net aux bordures et qu'un mur entier se lisait comme du papier peint
tamponné.

## Direction artistique

**Magie ET technologie, pas l'un décoré par l'autre.** La technologie apporte la *géométrie* :
plaques, biseaux, rivets, aérations, lunettes. La magie apporte le *tracé* : des gravures
creusées dans le métal qui ne sont pas un décor mais un circuit — elles partent des bords,
convergent vers le cœur, et s'allument quand la Résonance passe. Éteinte, une machine montre un
sillon mort ; en marche, le même sillon conduit. **C'est la seule chose qui change entre les deux
états**, et c'est ce qui rend l'allumage lisible plutôt que de faire clignoter tout le bloc.

**Le palier se lit sur le châssis**, jamais sur un chiffre :

| Palier | Matière | Gravures |
|---|---|---|
| T1 « Fracturé » | pierre récupérée, cuivre patiné | **interrompues** — de la ruine qui ne conduit qu'à moitié |
| T2 « Accordé » | acier et laiton | **continues** — c'est restauré, ça circule |
| T3 « Veskorien » | alliage sombre, accents ambre | continues, bande cyan |

**Un emblème = le geste de la machine**, pas son nom : on doit pouvoir dire ce que fait le bloc
sans avoir lu une ligne de doc. Le Damping Array a des anneaux qui se *referment* vers le centre,
l'émetteur des arcs qui s'*ouvrent* vers l'extérieur — les deux blocs font des choses opposées et
doivent se distinguer d'un seul regard.

## Les couches (`draw.js`)

Aucune de ces passes n'est visible seule ; c'est l'**empilement à faible opacité** qui fait une
surface plutôt qu'un aplat bruité. Ordre : matière → modelé → usure → lumière.

`grain` · `gradient` (lumière haut-gauche) · `dither` (transition sans couleur en plus) · `ao`
(occlusion : ce qui rend un creux *creux*) · `scratches` · `bevel` · `bloom` (la lumière déborde
de ce qui l'émet) · `vignette` (deux blocs côte à côte gardent une frontière) · `speculars`
· `patina`.

La roche empile **trois octaves** de taches (grandes masses, cailloux, grain fin) : une seule
passe de bruit donne un gravier sans échelle, c'est la superposition de tailles qui fait lire
« pierre ».

## Ce qui n'est PAS ici

La **géométrie 3D** (lunettes en relief, tours d'émetteur, pupitre de la console) vit dans
`ModBlockStateProvider`, pas dans ces textures — voir sa javadoc pour les trois pièges de
non-cube (`noOcclusion`, `VoxelShape`, `parent block/block`).

Les **GUI** (`textures/gui/container/`) sont encore des placeholders.
