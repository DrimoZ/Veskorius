# 08 — Structures (6)

Chaque structure est rattachée à une strate sociale (`02-Lore.md`) et une strate Y
(`07-World-Generation.md`) avant tout contenu — règle non négociable.

## Gatekeeping — clé physique, acquisition variée (2026-07-22)

Le déblocage d'un tier passe par un **objet-clé physique**, le `resonance_blueprint` du tier
(voir `03-Progression.md`), **jamais par une recette masquée**. Chaque structure « porte de tier »
donne accès à ce blueprint par un **défi différent** (pour ne pas être répétitif) : Avant-poste =
restaurer une console sur place ; Sigma Laboratory = réparer 2 Relais ; Archive = ordonner 4
fragments ; Faille = découvrir + poser un Rift Anchor. Les **fragments de Codex** ne débloquent
rien : ce sont du lore lisible (et parfois des indices d'exploration).

---

## Habitation Modeste

**Strate sociale** : Peuple du réseau. **Y** : 0 à -20, aussi en surface. **Fréquence** : la
plus commune, ~1 / 800 blocs.

- **Fonction** : variété de loot quotidien, premier contact avec l'esthétique veskorienne sans
  rien de technique. Sert aussi à apprendre le geste « lire un fragment » sur un enjeu faible.
- **Ambiance** : petites pièces, mobilier simple, jamais de machine.
- **Loot** (coffre) : `fossilized_ration` (nourriture flavor) ×2-4, matériaux T1 communs
  (cobblestone, copper, parfois 1 raw crystal), et 1 `codex_fragment` "de vie quotidienne"
  (pur lore, non consommé à la lecture, aucun déblocage).
- **Machines/mobs** : aucun.

---

## Avant-poste

**Strate sociale** : mixte Architectes / peuple du réseau. **Y** : 0 à -40. **Fréquence** :
1 / 1500 blocs.

- **Fonction** : premier lieu où le joueur rencontre le principe de champ (T2). Porte du T2.
- **Ambiance** : petit atelier, une **console d'attunement** (`attunement_console`) encore intacte
  parmi des gravats — une « machine morte » qu'on peut en fait *réveiller sur place*.
- **Puzzle** : aucun au sens énigme — mais un **geste sur place** : clic droit sur la console →
  elle se remet en marche le temps d'un cycle et remet au joueur le **`resonance_blueprint` T2**
  (s'il n'en a pas déjà un). La console ne se récupère pas (minée = gravats). C'est la porte du T2 :
  *faire fonctionner l'ancienne machine là où elle se tient*.
- **Loot** (coffre-atelier) : matériaux d'amorçage T2 (fer, redstone, or) pour fabriquer le premier
  Field Emitter juste après. Le blueprint **ne vient pas du coffre** mais de la console.
- **Débloque** : toute la catégorie T2 (le blueprint T2 est l'ingrédient rendu de chaque recette
  T2 — Field Emitter, Flux Purifier, Storage Cell, Crystal Roost, Catalyst Core, Locator).
- **Tell de surface** : une amorce de pilier en `resonance_veined_stone` (3-4 blocs) au-dessus de
  l'Avant-poste souterrain, pour le repérer une fois le T2 acquis (pilier 5) et via le Locator.

---

## Poste de Garde

**Strate sociale** : Custodes. **Y** : 0 à -40, souvent à moins de 300 blocs d'un Avant-poste.
**Fréquence** : 1 / 3000 blocs.

- **Fonction** : premier contact avec un Custode actif (voir `09-Entities.md`).
- **Ambiance** : couloir unique, un seul Custode en veille, réactif seulement près du point de
  garde central.
- **Puzzle** : aucun — le combat est optionnel, évitable en restant hors détection.
- **Loot** : composants T2, jamais de fragment de recette.
- **Machines débloquées** : aucune.

---

## Sigma Laboratory

**Strate sociale** : Architectes. **Y** : -40 à -55. **Fréquence** : 1 / 6000 blocs.

- **Fonction** : centre de recherche sur la Résonance, débloque le T3.
- **Ambiance** : silencieux, quelques machines encore alimentées seules dans le noir, deux
  portes verrouillées, salle centrale coupée du reste.
- **Puzzle** : réparer deux Relais endommagés, la salle centrale s'ouvre seulement si les deux
  sont actifs simultanément.
- **Lore** : fragments sur la propagation par champ, mention indirecte de l'Effondrement.
- **Loot garanti (salle centrale)** : fragment débloquant le Resonance Relay.
- **Machines débloquées** : Resonance Relay.

---

## Archive Régionale

**Strate sociale** : Architectes (fin d'âge d'or). **Y** : -55 à -64. **Fréquence** :
1 / 12000 blocs.

- **Fonction** : débloque le T4. Site de stockage de connaissances, plus de fragments, moins de
  machines actives que le Sigma Laboratory.
- **Ambiance** : rangées de socles de stockage, la plupart vides ou effondrés ; une salle de
  lecture centrale intacte.
- **Puzzle** : reconstituer l'ordre de 4 fragments de Codex dispersés pour déverrouiller la
  salle de lecture (lecture, pas de combat).
- **Lore** : premier fragment explicite sur la sur-résonance et les Failles.
- **Loot garanti** : fragment débloquant le Harmonic Amplifier + **exactement 3** Hyper Refined
  Crystal — quantité calibrée précisément (voir `05-Machines.md`, section Bootstrap du T4) pour
  amorcer à la fois le premier Harmonic Amplifier et la construction de la Deep Synthesis
  Chamber, sans que le joueur n'ait à choisir entre les deux.
- **Machines débloquées** : Harmonic Amplifier (et par extension, tout le reste du T4, voir
  règle de déblocage transversale dans `03-Progression.md`).

---

## Cœur de Faille

**Strate sociale** : aucune (accident naturel de l'Effondrement). **Y** : sous -60, génération
indépendante des strates normales. **Fréquence** : 1 / 15000 blocs.

- **Fonction** : contenu endgame, débloque le T5.
- **Apparition** : bulle sphérique 5-9 blocs de rayon, vide semi-translucide, un Rift Core
  flottant au centre. Fissures de pierre déformée en surface, visibles avant d'entrer.
- **Ambiance** : silence total, légère distorsion visuelle en s'approchant sans Rift Anchor posé.
- **Danger** : dégâts de déphasage sans Rift Anchor (voir `06-Energy.md`).
- **Combat** : Gardien de Faille (voir `09-Entities.md`), rencontré une fois le Rift Anchor posé.
- **Loot** : Rift Essence (finie, 6 extractions max) + drop garanti de 3 Corrupted Veskorian
  Alloy Ingot à la mort du Gardien de Faille (voir `04-Materials.md`, Rift-Ward Plate).
- **Machines débloquées** : aucune nouvelle recette ici — T5 débloqué en amont par l'Archive
  Régionale.

## Problèmes / Alternatives rejetées

- **Rejeté : loot aléatoire pour les fragments de recette dans toutes les structures.** La
  progression ne doit jamais dépendre d'un tirage (pilier 2).
- **Rejeté : combat obligatoire dans le Poste de Garde.** Les Custodes gardent, ils n'imposent
  pas un mur de contenu.
- **Rejeté (résolu) : quantité floue "2-3" Hyper Refined Crystal à l'Archive.** Fixé à
  exactement 3 — voir justification ci-dessus.

## Ouvert

- Faut-il une variante de surface de l'Avant-poste pour varier le rythme d'exploration en tout
  début de partie ?
