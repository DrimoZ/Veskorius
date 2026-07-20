# 08 — Structures (6)

Chaque structure est rattachée à une strate sociale (`02-Lore.md`) et une strate Y
(`07-World-Generation.md`) avant tout contenu — règle non négociable.

---

## Habitation Modeste

**Strate sociale** : Peuple du réseau. **Y** : 0 à -20, aussi en surface. **Fréquence** : la
plus commune, ~1 / 800 blocs.

- **Fonction** : variété de loot quotidien, premier contact avec l'esthétique veskorienne sans
  rien de technique.
- **Ambiance** : petites pièces, mobilier simple, jamais de machine.
- **Loot** : nourriture fossilisée (item cosmétique), fragments de Codex "de vie quotidienne"
  (pur lore, aucun déblocage mécanique).
- **Machines/mobs** : aucun.

---

## Avant-poste

**Strate sociale** : mixte Architectes / peuple du réseau. **Y** : 0 à -40. **Fréquence** :
1 / 1500 blocs.

- **Fonction** : premier lieu où le joueur rencontre le principe de champ (T2).
- **Ambiance** : petit atelier, une machine morte visible mais non récupérable.
- **Puzzle** : aucun — structure d'accès rapide, pas de friction ici.
- **Lore** : fragment expliquant la propagation par champ.
- **Loot garanti** : fragment débloquant la recette du Field Emitter.
- **Machines débloquées** : Field Emitter.

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
