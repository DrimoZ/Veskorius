# 08 — Structures (6)

Chaque structure est rattachée à une strate sociale (`02-Lore.md`) et une strate Y
(`07-World-Generation.md`) avant tout contenu — règle non négociable.

## Implémentation : vraies Structures vanilla, en **jigsaw** (décidé 2026-07-23, voir `16` §2)

> **✅ CODÉ le 2026-07-23 (A7).** L'Habitation Modeste et l'Avant-poste sont de vraies `Structure`
> jigsaw (`ModStructures`), leurs pièces des NBT générés par datagen (`ModStructurePieceProvider`),
> taguées `#veskorius:locatable` → **`/locate` marche, le mode Structures du Locator s'allume**. La
> `RuinFeature` est supprimée. Pièces placeholder (une salle meublée) ; les vrais layouts
> multi-pièces sont Phase 6. Les 4 autres structures (Poste de Garde, Sigma, Archive, Faille) restent
> à créer en Phase 2+ sur ce même socle.

Les ruines étaient des `RuinFeature` (boîtes creuses posées comme une *feature*) : pas de
`/locate`, pas de vraies pièces. **C'est remplacé** par le système `Structure` vanilla, en **jigsaw**.

Pourquoi jigsaw plutôt qu'un template NBT unique :

| Approche | Ce que c'est | Limite |
|---|---|---|
| Template NBT unique | un blob figé, posé tel quel | aucune variété, **ne scale pas** au massif |
| **Jigsaw (pools de pièces)** ✅ | assemble des **pièces modulaires** connectées récursivement — ce que Mojang utilise pour villages, **cités antiques**, trial chambers | plus de setup initial, mais **variété procédurale et structures massives** à partir de petites pièces réutilisables |
| Code full `StructureType` | génération algorithmique sur-mesure (Manoirs, Strongholds) | beaucoup de code, réservé au vraiment bespoke |

Les structures étant appelées à devenir **massives**, jigsaw est le seul choix qui tient : une Sigma
Lab de 3 salles aujourd'hui devient tentaculaire demain **en ajoutant des pièces au pool**, sans
réécrire.

**Configuration de spawn data-driven** : `StructureSet` + `RandomSpreadStructurePlacement` (spacing,
separation, salt) + biomes autorisés + strate Y, le tout en JSON de datapack — fréquence et
placement réglables sans recompiler (voir `14`, `veskorius-structures.toml` pour les curseurs
associés). Bénéfice immédiat : **`/locate` fonctionne**, et le **mode Structures du Locator**
s'allume automatiquement (tag `#veskorius:locatable`, déjà en place).

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
plus commune. *(Valeur codée : `RandomSpreadStructurePlacement` spacing 20 / separation 7, soit
~1 / 320 blocs — plus dense que le « ~1 / 800 » que ce fichier annonçait. Chiffre de départ à
valider en playtest ; la source de vérité est le `structure_set`, surchargeable par datapack.)*

- **Fonction** : variété de loot quotidien, premier contact avec l'esthétique veskorienne sans
  rien de technique. Sert aussi à apprendre le geste « lire un fragment » sur un enjeu faible.
- **Ambiance** : petites pièces, mobilier simple, jamais de machine.
- **Loot** (coffre) : 2-4 tirages parmi `fossilized_ration` (nourriture flavor) ×1-3, cobblestone
  ×4-8 et copper ingot ×1-3 ; plus 1 `codex_fragment` "de vie quotidienne" garanti (pur lore, non
  consommé à la lecture, aucun déblocage). *(Corrigé 2026-08-06 : ce fichier listait « parfois
  1 raw crystal », qui n'a jamais été dans la table — le cristal brut se mine, il ne se trouve pas
  dans une maison.)*
- **Machines/mobs** : aucun.

---

## Avant-poste

**Strate sociale** : mixte Architectes / peuple du réseau. **Y** : 0 à -40. **Fréquence** :
*(Valeur codée : spacing 32 / separation 9, soit ~1 / 512 blocs — plus dense que le « 1 / 1500 »
que ce fichier annonçait. Même remarque que ci-dessus : à valider en playtest, la vérité est dans
le `structure_set`.)*

- **Fonction** : premier lieu où le joueur rencontre le principe de champ (T2). Porte du T2.
- **Ambiance** : petit atelier, une **console d'attunement** (`attunement_console`) encore intacte
  parmi des gravats — une « machine morte » qu'on peut en fait *réveiller sur place*.
- **Puzzle** : aucun au sens énigme — mais un **geste sur place** : clic droit sur la console →
  elle se remet en marche le temps d'un cycle et remet au joueur le **`resonance_blueprint` T2**
  (s'il n'en a pas déjà un). La console ne se récupère pas (minée = gravats). C'est la porte du T2 :
  *faire fonctionner l'ancienne machine là où elle se tient*.
- **Loot** (coffre-atelier) : **amorçage T2 garanti** = **4 Resonance Component + 2 Gold Ingot**,
  plus des matériaux d'appoint aléatoires (fer, redstone, or). Le blueprint **ne vient pas du
  coffre** mais de la console.
  > *Marge (2026-08-06)* : depuis le passage aux châssis, le Field Emitter consomme **2** Component
  > et 2 Gold — le lot garanti couvre donc la recette avec une Component d'avance. C'est
  > délibérément gardé à 4 : le joueur peut se tromper une fois, ou dépenser une Component
  > ailleurs, sans avoir à retrouver un second Avant-poste.
  > **⚠️ Pourquoi les Component sont garantis (corrigé le 2026-07-24).** La recette du Field Emitter
  > exige des Component ; or les Component ne s'obtiennent qu'au **Component Assembler**, qui **a besoin
  > d'un champ** pour tourner — champ que **seul le Field Emitter** fournit. Sans amorçage, **dépendance
  > circulaire** : un joueur neuf ne pourrait jamais atteindre le T2. L'Avant-poste fournit donc le
  > premier lot de Component ; ce premier champ alimente ensuite l'Assembler et la boucle
  > s'auto-entretient. (Le loot listait « fer/redstone/or », qui ne correspondent pas à la recette :
  > l'omission est réparée, un GameTest garantit désormais ce lot.)
  >
  > **Le verrou a failli se reformer par un autre chemin (2026-08-06).** En introduisant les
  > châssis, il aurait été naturel d'exiger un Resonance Component dans le châssis T2 — ce qui
  > aurait recréé exactement la même impasse, une case plus loin et bien plus difficile à voir.
  > Les châssis T1 et T2 sont donc **contraints à ne demander que des matériaux accessibles sans
  > champ** (pierre, cuivre, fer, cristaux stables — le Stabilizer est autonome). C'est une règle
  > à tenir pour tout futur châssis de bas palier, pas une coïncidence.
  >
  > **Deux GameTest gardent la porte** : `outpostLootGuaranteesBootstrapComponents` roule la table
  > 30 fois et exige Component **et** Gold à chaque tirage (une vérification à un seul tirage ne
  > distingue pas un pool certain d'un pool à 50 %, et laissait précisément passer ce bug), et
  > `fieldEmitterRecipeRequiresBlueprint` verrouille la recette elle-même.
- **Débloque** : toute la catégorie T2 (le blueprint T2 est l'ingrédient rendu de chaque recette
  T2 — Field Emitter, Flux Purifier, Storage Cell, Crystal Roost, Catalyst Core, Locator).
- ~~**Tell de surface** : une amorce de pilier en `resonance_veined_stone`.~~ **Abandonné à la
  migration (A7, 2026-07-23).** Sa raison d'être — « repérer une fois le T2 acquis » — est désormais
  couverte, mieux, par `/locate` et le mode Structures du Locator (maintenant fonctionnels). Le
  **premier** Avant-poste se trouve toujours en explorant/creusant (inchangé), aiguillé par le
  fragment `HINT_WORKSHOP` de l'Habitation Modeste. *(✅ Confirmé par le porteur, 2026-07-24.)*

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
