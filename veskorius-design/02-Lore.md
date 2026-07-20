# 02 — Lore

Vérité d'équipe, jamais livrée telle quelle au joueur (le joueur reçoit des fragments : Codex,
inscriptions, machines observées — jamais ce fichier entier).

## Chronologie — 4 âges

| Âge | Nom interne | Ce qu'il laisse au joueur | Tier correspondant |
|---|---|---|---|
| 1 | L'Éveil | Cristaux bruts, poches peu profondes | T1 |
| 2 | L'Essor | Avant-postes, réseaux courts | T2 |
| 3 | La Résonance (âge d'or) | Grandes structures, réseau régional | T3-T4 |
| 4 | L'Effondrement | Ruines profondes, gardiens actifs, la Faille | T5 |

### Âge 1 — L'Éveil
Découverte du Cristal de Résonance brut à l'état naturel (poches Y -20 à 0). Instable sans
traitement — justifie mécaniquement le Resonance Stabilizer et le risque de dégâts du cristal
brut porté (voir `06-Energy.md`).

### Âge 2 — L'Essor
Premières machines de stabilisation, réseaux courts (portée physique, pas de propagation par
champ). Naissance des Avant-postes. Fin d'âge marquée par la découverte de la propagation par
champ — le virage technique qui justifie le pilier 3.

### Âge 3 — La Résonance (âge d'or)
Expansion régionale. Réseaux à grande échelle, grandes structures (Sigma Laboratory), premiers
gardiens automatisés — construits pour **protéger**, pas pour attaquer (justifie des IA
défensives, voir `09-Entities.md`). C'est le sommet technique que le joueur peut reconstituer
en T3-T4.

### Âge 4 — L'Effondrement
Cause fixée (contrairement à la version précédente laissée ouverte) : le réseau de Résonance,
à pleine échelle régionale, a atteint un point de **sur-résonance** — un phénomène de
rétroaction où trop de champs superposés à trop grande échelle ont fini par se déphaser plutôt
que de s'additionner (cohérent avec la règle de non-stacking du pilier 3 : la civilisation a
ignoré cette limite à l'échelle régionale, pas le joueur à l'échelle d'une base). La
sur-résonance a créé des poches d'espace instables — les **Failles** — qui subsistent
aujourd'hui comme la source du contenu de tier 5 / endgame.

Ce que ça donne concrètement :
- Les Custodes (gardiens), conçus pour fonctionner sans supervision, ont largement survécu —
  justifie leur présence aujourd'hui sans leurs créateurs.
- Les Failles sont le point d'entrée du contenu endgame (voir `07-World-Generation.md`, section
  Faille, et `08-Structures.md`, structure "Cœur de Faille").
- Le boss de fin (voir `09-Entities.md`) est directement lié à une Faille, pas un mob ajouté
  sans justification.
- Le Flux Slag produit par le Veskorian Alloy Forge du joueur (`05-Machines.md`) est
  chimiquement la même substance résiduelle qui, accumulée à l'échelle régionale par les
  Architectes, a fini par déclencher la sur-résonance. Le joueur reproduit le même phénomène en
  miniature, de façon gérable — un rappel mécanique, pas seulement narratif, de la cause de
  l'Effondrement.

## Structure sociale (justifie le contenu de `08-Structures.md` et `09-Entities.md`)

| Strate | Rôle | Type de structure associée | Contenu typique |
|---|---|---|---|
| Architectes | Concepteurs des machines/réseau | Laboratoires, ateliers | Machines T2-T4, recettes |
| Custodes | Gardiens dédiés, réactifs pas agressifs | Postes de garde, Failles | Mobs gardiens, loot de combat |
| Peuple du réseau | Population sans accès à la pointe | Habitations, avant-postes | Loot quotidien, matériaux T1 |

Règle de cohérence : une structure du peuple du réseau ne contient jamais de machine T3+. Une
structure Architectes profonde (proche d'une Faille) peut contenir du T4-T5.

## Ce qu'on refuse d'écrire

Pas de personnages historiques nommés individuellement pour l'instant — sans usage de gameplay
concret (dialogue, quête), ce serait de la décoration. Si un système de PNJ ou de journal
narratif est ajouté plus tard, ce fichier sera étendu à ce moment-là.

## Ouvert

- Écart de temps entre l'Effondrement et l'arrivée du joueur : non chiffré, utile seulement si
  un système de datation en jeu est ajouté.
