# 01 — Vision & Piliers

## Vision en une phrase

Le joueur ne construit pas une usine, il **remet en marche une technologie qui a déjà existé, en
mieux, il y a des siècles** — et ne sait jamais avec certitude s'il l'utilise correctement,
seulement qu'elle fonctionne à nouveau.

## Ce que ça change concrètement par rapport à un mod de tech classique

| | Mod de tech classique (Mekanism, IE) | Veskorius |
|---|---|---|
| Origine de la technologie | Inventée par le joueur | Restaurée, préexistante |
| Découverte des recettes | Craft en aveugle depuis l'établi | Fragment trouvé en exploration, jamais deviné |
| Énergie | Câbles, stock transporté (FE/RF) | Champ de propagation, pas de câble |
| Fin de partie | Automatisation infinie, N tiers | 5 tiers max, endgame = compréhension, pas production |
| Danger du monde | Souvent gratuit (juste un mob) | Toujours motivé par une cause écrite dans le lore |

## Les cinq piliers (règles de tranchage)

1. **La technologie est une ruine, pas une invention.** Aucune recette de nouvelle catégorie de
   machine n'est devinable depuis un établi vide — elle vient d'un lieu précis.
2. **La connaissance est spatiale, pas administrative.** Pas de grille de recherche abstraite.
   Chaque mécanique apprise est rattachable à un lieu du monde où elle a été rencontrée.
3. **Le réseau est vivant, jamais un tuyau.** Pas de câble comme brique de base. L'énergie se
   propage par champ (voir `06-Energy.md`). *Corollaire (2026-07-23)* : le service que rendent les
   câbles — **choisir ce qu'on alimente** — est rendu par les **bandes harmoniques**, pas par des
   fils ; et « vivant » est littéral : un réseau désaccordé **dérive et produit de la dissonance**,
   l'écho local de l'Effondrement (`06`, `02`).
4. **Chaque danger a une cause écrite avant d'être un obstacle.** Un mob, un piège, une porte
   verrouillée : la raison doit exister dans `02-Lore.md` avant que le contenu soit codé.
5. **Peu de tiers (5 max), mais chaque tier change la lecture du monde.** Pas de course à
   l'automatisation infinie — chaque palier change ce que le joueur *voit*, pas seulement ce
   qu'il *peut fabriquer*.

## Anti-piliers (refusés explicitement, pour ne pas les revalider plus tard)

- Énergie infinie générable dès le tier 1 (casse pilier 1).
- GUI de recherche façon points de compétence débloqués par grind (casse pilier 2).
- Câbles comme bloc de base du réseau (casse pilier 3).
- Boss "loot piñata" sans cause de lore (casse pilier 4).
- Plus de 5 tiers de machines de traitement (casse pilier 5).

## Comment lire tout le reste du dossier

Chaque fichier suivant applique ces piliers sans les répéter. Si une idée dans `03` à `10`
semble contredire un pilier, c'est un bug de conception à corriger, pas une exception à
justifier.
