# 04 — Matériaux

Format : nom interne → pourquoi il existe → génération/obtention → interactions → remplaçable
par du vanilla ? Organisé en 4 groupes : chaîne de cristaux (déjà connue), matériaux naturels
(trouvés, pas fabriqués), matériaux d'action du joueur (combat, élevage, agriculture), matériaux
de procédé (sous-produits et branches alternatives de fabrication).

## 1. Cristaux (chaîne de raffinage principale, inchangée)

| Nom interne | Tier | Obtention | Renouvelable ? |
|---|---|---|---|
| `raw_resonance_crystal` | T1 | Poches Y -20 à 0, ou Crystal Roost (600s/unité) | Oui, lentement |
| `stable_resonance_crystal` | T1 | Resonance Stabilizer | Oui |
| `refined_resonance_crystal` | T2 | Flux Purifier | Oui |
| `hyper_refined_crystal` | T4 | Deep Synthesis Chamber (une fois construite) | Oui, à partir du T4 |
| `rift_essence` | T5 | Rift Core Extractor, 6 extractions max par Faille | **Non — seule ressource volontairement finie** |

## 2. Matériaux naturels (trouvés en exploration, jamais fabriqués)

| Nom interne | Où | Obtention | Usage |
|---|---|---|---|
| `resonance_veined_stone` | Coquille de 3-5 blocs autour de chaque poche de `raw_resonance_crystal` | Minage classique, drop lui-même | Bloc de construction décoratif ; sert aussi de "tell" — un joueur qui voit ce bloc sait qu'une poche de cristal est proche sans avoir encore rien miné |
| `raw_flux_deposit` | Croûte sur les parois à moins de 2 blocs d'une poche de cristal, ~15% des blocs de paroi éligibles | **Brosse vanilla** (mécanique déjà existante, aucun nouvel outil à coder) | Remplace le Quartz dans la recette du Resonance Stabilizer (1:1) — permet un chemin T1 alternatif basé sur l'observation fine plutôt que le minage de Quartz |
| `ancient_seed` | Loot bonus (non garanti, ~20%) de l'Archive Régionale | Fouille de structure | Plantable, pousse en `resonance_bloom` (voir groupe 3) |
| `meteoric_resonance_shard` | Événement météo "Orage de Résonance" (voir `07-World-Generation.md`) | Se pose brièvement en cratères à la surface pendant l'orage, disparaît si non ramassé avant la fin | Ingrédient de la Rift-Ward Plate (voir groupe 4) |

## 3. Matériaux d'action du joueur (combat, élevage, agriculture)

| Nom interne | Source | Obtention | Usage |
|---|---|---|---|
| `custode_alloy_fragment` | Drop de combat (Custode, Custode Lourd) | 2-4 par kill | Substitut 1:1 de l'Iron Ingot dans **toute** recette Veskorius — récompense un style de jeu combat plutôt que minage pur |
| `resonance_spore` | Poussée sur `resonance_veined_stone` en faible luminosité, repousse après ~2 jours MC (mécanique proche du glow lichen vanilla) | Récolte à la main | Reproduction du Fileur de Cristal (2 adultes + 1 spore = bébé, voir `09-Entities.md`) |
| `resonance_bloom` | Cultivé depuis `ancient_seed`, récolte répétée façon buisson de baies (pas à usage unique) | Agriculture | Consommable (effet Lueur ~ Vision Nocturne faible, 60s) ou transformé en `luminous_extract` |

## 4. Matériaux de procédé (sous-produits et branches alternatives de fabrication)

| Nom interne | Tier | Obtention | Usage |
|---|---|---|---|
| `resonance_component` | T1 | Component Assembler (Stable Crystal + Iron Ingot), **ou branche alternative** : 3 Resonance Dust + 2 Iron Ingot, rendement identique mais sans consommer de cristal stable | Composant de craft interne |
| `resonance_dust` | T1 | Crystal Crusher (nouvelle machine, voir `05-Machines.md`) : 1 Raw Crystal → 3 Resonance Dust, **alternative** au Stabilizer (10s contre 30s, mais pas de cristal stable en sortie) | Engrais pour `ancient_seed`/`resonance_bloom` (accélère la pousse comme un os à moelle), ou branche alternative du Component Assembler ci-dessus |
| `veskorian_alloy_ingot` | T3 | 2 Refined Crystal + 2 Iron Ingot, fondu au Veskorian Alloy Forge | Outils/armure T3, blocs structurels, machines "lourdes" (Driller, Extraction Array, Synthesizer) |
| `veskorian_conductive_alloy_ingot` | T3 | **Même machine, branche alternative** : 2 Refined Crystal + 2 Gold Ingot au lieu de 2 Iron Ingot | Machines "énergétiques" (Resonance Relay, Harmonic Lattice) — sépare les deux familles de machines en deux chaînes de matériaux distinctes, un vrai choix de planification plutôt qu'un seul alliage universel |
| `veskorian_alloy_block` | T3 | 4 Veskorian Alloy Ingot | Décoratif + structurel |
| `harmonic_lattice` | T4 | 4 Veskorian Conductive Alloy Ingot + 2 Hyper Refined Crystal | Harmonic Amplifier, Convergence Core |
| `concentrated_flux` | T3 | Flux Compressor (nouvelle machine) : 4 Refined Crystal → 1 Concentrated Flux | Convergence Core (compression obligatoire pour un craft de cette ampleur, plutôt que de simplement demander plus de cristaux bruts) |
| `resonance_catalyst_core` | T2 | 2 Resonance Component + 1 Refined Crystal + 1 Redstone | **Non consommé** — s'insère dans le nouveau slot d'augment de n'importe quelle machine active, +15% de vitesse permanent, un seul par machine, retirable au Resonance Tuner |
| `flux_slag` | T3 (sous-produit) | Byproduct du Veskorian Alloy Forge, 1/cycle | Nuisance à évacuer (Slag Vent) — lien de lore avec l'Effondrement (`02-Lore.md`) |
| `synthesis_residue` | T3 (sous-produit) | Byproduct du Structural Synthesizer, 1/cycle | Contrairement au Flux Slag, directement utile : compressible en bloc de construction gris terne, aucune maintenance requise — tous les sous-produits ne sont pas des nuisances |
| `luminous_extract` | T4 | 2 Resonance Bloom broyés | Teinture pour `resonance_glass` |
| `resonance_glass` | T3 | 4 Sand + 1 Stable Resonance Crystal, fondu au four vanilla | Bloc décoratif semi-transparent, luminosité 8, teignable avec `luminous_extract` |
| `corrupted_veskorian_alloy_ingot` | T5 | Drop garanti (×3) du Gardien de Faille, ou 15% de chance par extraction du Rift Core Extractor | Rift-Ward Plate |

## Blocs de construction dérivés

- `veskorian_alloy_block`, `resonance_veined_stone`, `synthesis_residue` (compressé) — variantes
  standard (dalle, escalier, mur) générées par datagen une fois codées.
- `ancient_conduit_stone` — bloc trouvé tel quel dans les structures Architectes, non craftable
  avant T4. Sert de "tell" visuel au même titre que `resonance_veined_stone`, mais pour les
  structures plutôt que les poches de cristal.
- `resonance_glass` — seul bloc du mod pensé uniquement pour la construction/décoration, pas de
  fonction de craft en aval.

## Outils et armure

| Item | Tier | Stats de référence |
|---|---|---|
| Épée en Alliage Veskorien | T3 | Dégâts équivalents diamant, durabilité +20% |
| Pioche en Alliage Veskorien | T3 | Tier netherite, requise pour miner `ancient_conduit_stone` sans le casser |
| Armure en Alliage Veskorien | T3 | Protection diamant ; réduit de moitié les dégâts de déphasage près d'une Faille non ancrée |
| **Rift-Ward Plate** (upgrade, pièce unique) | T5 | Craft : Plastron en Alliage Veskorien + 3 Corrupted Alloy Ingot + 2 Meteoric Resonance Shard. Protection plastron diamant + immunité totale au déphasage sur tout le corps ; contrepartie : -10% vitesse de minage tant qu'elle est portée |

## Problèmes / Alternatives rejetées

- **Rejeté : un minerai `veskorium_ore` généré nativement comme le fer.** Casserait le pilier 1.
- **Rejeté : enchantements custom sur les outils en alliage.** Non prévu par les piliers.
- **Rejeté : Rift-Ward Plate comme un set complet à 4 pièces (12 Corrupted Alloy Ingot au
  total).** Rejeté après vérification des chiffres bout en bout : une seule Faille ne rapporte
  qu'environ 4 Corrupted Alloy Ingot au total (3 garantis du boss + bonus aléatoire de
  l'Extractor), ce qui aurait forcé à vaincre 3-4 Gardiens de Faille différents pour un seul
  équipement complet. Réduit à une pièce unique (le plastron), calibrée pour être atteignable
  avec le seul drop garanti d'un Faille.
- **Rejeté : un troisième alliage pour une troisième "famille" de machines.** Deux familles
  (structurelle/énergétique) suffisent pour créer un choix de planification sans multiplier les
  chaînes de craft à retenir — un troisième alliage ajouterait de la charge mentale sans
  bénéfice de gameplay clair.
- **Rejeté : rendre `resonance_dust` strictement meilleur que la stabilisation classique.**
  Le Crystal Crusher est plus rapide (10s vs 30s) mais ne produit jamais de Stable Crystal —
  reste un choix, pas un remplacement.

## Ouvert

- Faut-il une variante teintée de `resonance_glass` obtenue directement (sans `luminous_extract`)
  à partir d'un cristal déjà raffiné, pour éviter de dépendre entièrement de l'agriculture ? Idée
  mineure, non bloquante.
