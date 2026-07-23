# 13 — Registry Index

Absent des versions précédentes. Sert à deux choses : une référence unique pour retrouver le
nom de registre exact de n'importe quel élément du mod sans rouvrir dix fichiers, et un outil de
validation — le compiler a permis de vérifier qu'aucun nom n'est utilisé deux fois pour deux
choses différentes. Tous les noms suivent la convention `snake_case` en anglais ; la prose reste
en français partout ailleurs (voir `01-Vision-Pillars.md`).

## Items — cristaux et chaîne de raffinage

| Registry name | Défini dans | Statut code |
|---|---|---|
| `raw_resonance_crystal` | 04 | ✅ codé (item) |
| `stable_resonance_crystal` | 04 | ✅ codé |
| `refined_resonance_crystal` | 04 | ✅ codé |
| `hyper_refined_crystal` | 04 | À coder (Phase 3) |
| `rift_essence` | 04 | À coder (Phase 4) |

## Items — matériaux naturels

| Registry name | Défini dans | Statut code |
|---|---|---|
| `resonance_veined_stone` | 04, 07 | À coder (Phase 1) |
| `raw_flux_deposit` | 04, 07 | ✅ codé (item + bloc brossable ; dans le tag stabilizer_flux) |
| `ancient_seed` | 04, 08 | À coder (Phase 3) |
| `meteoric_resonance_shard` | 04, 07 | À coder (Phase 3) |

## Items — action du joueur

| Registry name | Défini dans | Statut code |
|---|---|---|
| `custode_alloy_fragment` | 04, 09 | ✅ codé (drop du Custode ; substitut du fer via tag `iron_substitutes`) |
| `resonance_spore` | 04, 09 | ✅ codé — item (nourriture du Fileur) + récolte : la Resonance Veined Stone pousse un spore (état `spored`) en faible luminosité sur une face exposée, récolté au clic droit, repousse ensuite |
| `resonance_bloom` | 04 | À coder (Phase 3) |

## Items — progression (plans, fragments, loot de structure)

Ajoutés 2026-07-22 (tâche 10, voir `03`/`08`/`12`). Le gatekeeping est **physique** : le blueprint
est un objet-clé requis (et rendu) dans les recettes d'un tier ; les fragments de Codex sont du lore.

| Registry name | Défini dans | Statut code |
|---|---|---|
| `resonance_blueprint` | 03, 08 | ✅ codé — clé de craft T2, Data Component `blueprint_tier`, rendu au craft |
| `codex_fragment` | 02, 08 | ✅ codé — lore pur, Data Component `codex_entry`, lisible (clic droit) |
| `fossilized_ration` | 08 | ✅ codé — nourriture flavor (Habitation Modeste) |
| `resonance_codex` | 15 | ✅ codé — manuel en jeu qui s'écrit tout seul ; donné à la 1re connexion + recette de secours. État de déblocage sur le JOUEUR (attachment `codex_unlocks`, survit à la mort), synchronisé au client (`CodexSyncPayload`) ; l'objet n'est qu'une clé d'ouverture du `CodexScreen` |

## Items — procédé (sous-produits, alliages, augments)

| Registry name | Défini dans | Statut code |
|---|---|---|
| `resonance_component` | 04 | ✅ codé |
| `resonance_dust` | 04, 05 | ✅ codé (produit par le Crystal Crusher ; entrée de la branche alt de l'Assembler) |
| `veskorian_alloy_ingot` | 04, 05 | À coder (Phase 2) |
| `veskorian_conductive_alloy_ingot` | 04, 05 | À coder (Phase 2) |
| `veskorian_alloy_block` | 04 | À coder (Phase 2) |
| `harmonic_lattice` | 04, 05 | À coder (Phase 3) |
| `concentrated_flux` | 04, 05 | À coder (Phase 2) |
| `resonance_catalyst_core` | 04, 05 | ✅ codé (augment +15% ; craft 2 Component + 1 Refined Crystal + 1 Redstone) |
| `flux_slag` | 04, 05 | À coder (Phase 2) |
| `synthesis_residue` | 04, 05 | À coder (Phase 2) |
| `luminous_extract` | 04 | À coder (Phase 3) |
| `resonance_glass` | 04 | À coder (Phase 3) |
| `corrupted_veskorian_alloy_ingot` | 04, 08, 09 | À coder (Phase 4) |
| `ancient_conduit_stone` | 04 | À coder (Phase 2, trouvé tel quel) |

## Outils, armure, augment (items sans bloc associé)

| Registry name proposé | Défini dans | Statut code |
|---|---|---|
| `veskorian_alloy_sword` / `_pickaxe` | 04 | À coder (Phase 2) |
| `veskorian_alloy_helmet` / `_chestplate` / `_leggings` / `_boots` | 04 | À coder (Phase 2) |
| `rift_ward_plate` (remplace le `veskorian_alloy_chestplate`, pas cumulatif) | 04 | À coder (Phase 4) |
| `resonance_tuner` | 05, 12 | ✅ codé (outil à modes : Pivoter/On-Off/Surchauffe/Redstone) |
| `resonance_locator` | 05, 07 | ✅ codé (détecteur courte portée : poche de cristal ou signature de champ ; batterie interne) |
| `resonance_storage_cell` | 05 | ✅ codé (batterie portable 8000 Osc ; alimente le Locator) |

## Blocs — génération naturelle

| Registry name | Défini dans | Statut code |
|---|---|---|
| `resonance_crystal_cluster` | 07 (ajouté 2026-07-21) | ✅ codé (bloc de poche, se mine → `raw_resonance_crystal`) |
| `resonance_veined_stone` | 04, 07 | ✅ codé (coquille des poches, bloc décoratif) |
| `raw_flux_deposit` (bloc) | 07 | ✅ codé (croûte brossable → item `raw_flux_deposit`) |
| `attunement_console` | 08 | ✅ codé — bloc de l'Avant-poste, clic droit sur place → blueprint T2 ; miné = gravats, sans objet |

Note : le dossier nommait « poches de Raw Resonance Crystal » sans nommer le bloc généré (l'item
`raw_resonance_crystal` existait, pas un bloc). `resonance_crystal_cluster` est ce bloc — une
formation cristalline distinctive (pas un minerai façon fer, rejeté dans `04-Materials.md`), qui
se génère en petites poches (Y -20 à 0) et lâche l'item quand on la mine. La coquille de
`resonance_veined_stone` qui la rend reconnaissable de loin est séparée (tâche 14).

## Blocs — machines (23, voir `05-Machines.md` pour le détail complet)

| # | Registry name proposé | Tier | Statut code |
|---|---|---|---|
| 1 | `resonance_stabilizer` | T1 | ✅ codé (block entity + cycle 30s + GUI + slot d'augment) |
| 2 | `component_assembler` | T1 | ✅ codé (cycle 5s, 3 Osc/tick, premier consommateur du champ) |
| 3 | `resonance_whetstone` | T1 | ✅ codé (block entity + cycle 8s + GUI + slot d'augment) |
| 4 | `field_emitter` | T2 | ✅ codé (réserve + recharge + champ + GUI jauge, capability IResonanceField) |
| 5 | `flux_purifier` | T2 | ✅ codé (cycle 45s, 2 Osc/tick, mode surchauffe) |
| 8 | `crystal_roost` | T2 | ✅ codé (production passive : 2 Quartz → 1 Raw Crystal 600s, si un Fileur < 6 blocs) |
| 9 | `resonance_relay` | T3 | À coder (Phase 2) |
| 10 | `veskorian_alloy_forge` | T3 | À coder (Phase 2) |
| 11 | `structural_synthesizer` | T3 | À coder (Phase 2) |
| 12 | `deep_crystal_driller` | T3 | À coder (Phase 2) |
| 13 | `slag_vent` | T3 | À coder (Phase 2) |
| 14 | `harmonic_amplifier` | T4 | À coder (Phase 3) |
| 15 | `deep_synthesis_chamber` | T4 | À coder (Phase 3) |
| 16 | `automated_extraction_array` | T4 | À coder (Phase 3) |
| 17 | `resonance_network_hub` | T4 | À coder (Phase 3) |
| 18 | `convergence_core` | T4→T5 | À coder (Phase 4) |
| 19 | `rift_anchor` | T5 | À coder (Phase 4) |
| 20 | `rift_core_extractor` | T5 | À coder (Phase 4) |
| 21 | `rift_ward_emitter` | T5 | À coder (Phase 4) |
| 22 | `crystal_crusher` | T1 | ✅ codé (cycle 10s autonome, 1 Raw Crystal → 3 Resonance Dust) |
| 23 | `flux_compressor` | T3 | À coder (Phase 2) |

Note : les # 6 et 7 (Resonance Storage Cell, Resonance Locator) sont des items, pas des blocs —
déjà listés dans la section "Outils, armure, augment" ci-dessus pour éviter un doublon.

## Entités — noms de registre proposés

| Nom en jeu (prose) | Registry name proposé | Défini dans |
|---|---|---|
| Custode | `custode` | 09 (✅ codé : garde réactif 30 PV / 6 dég., spawn en Avant-poste, drop fragment) |
| Custode Lourd | `heavy_custode` | 09 |
| Fileur de Cristal | `crystal_strider` | 09 (✅ codé : faune neutre, traite, reproduction, spawn) |
| Custode Archiviste | `custode_archivist` | 09 |
| Gardien de Faille | `rift_guardian` | 09 |

## Structures — noms de registre proposés

| Nom en jeu (prose) | Registry name proposé | Défini dans |
|---|---|---|
| Habitation Modeste | `modest_dwelling` | 08 (✅ codé — **feature** `veskorius:ruin`, pas une Structure vanilla) |
| Avant-poste | `outpost` | 08 (✅ codé — feature `veskorius:ruin` avec console) |
| Poste de Garde | `guard_post` | 08 |
| Sigma Laboratory | `sigma_laboratory` | 08 |
| Archive Régionale | `regional_archive` | 08 |
| Cœur de Faille (poche générée, pas une structure `structure_set` classique) | `rift_pocket` | 07, 08 |

## Ajouts de la révision harmonique (2026-07-23, voir `06`/`16`)

| Registry name | Type | Défini dans | Statut |
|---|---|---|---|
| `tunable_field_emitter` | bloc (machine) | 05, 06 | ✅ codé — choix de bande au Tuner (mode Accorder) ; hérite du Field Emitter (carburant, réserve, GUI, coupole) |
| `damping_array` | bloc (machine) | 05, 06 | ✅ codé — absorbe la dissonance du champ le plus pollué, consomme un agent (`veskorius:damping`), cristallise le déchet. Autonome (0 Osc) à dessein |
| `reclaimer` | bloc (machine) | 05 | À coder (Phase 2) — re-stabilise les déchets |
| `advanced_assembler` | bloc (machine) | 05 | À coder (Phase 2) — produit le `resonance_matrix` |
| `resonance_sludge` | item | 04 | ✅ codé — dissonance cristallisée, produite par le Damping Array. Débouchés (Reclaimer, engrais) en Phase 2 |
| `resonance_matrix` | item | 04, 05 | À coder (Phase 2) — intermédiaire requis au T4 |
| `efficiency_core` / `yield_core` / `tuning_core` / `damping_core` | items (augments) | 04, 05 | À coder (Phase 2) |
| `attunement_lens` | item (optionnel) | 12 | À coder — porte le HUD de champ (sinon le Locator fait office) |
| `resonant_deeps` | biome | 07, 16 | À coder (Phase 2) — porte le gaz, abrite les structures profondes |
| `veskorius:locatable` | tag de structure | 16 | ✅ codé (vide tant que les Structures ne sont pas migrées) |
| `locator_mode` | data component | 16 | ✅ codé (outil à modes) |
| `codex_unlocks` | attachment joueur | 15 | ✅ codé |

Bandes harmoniques : identifiées par **couleur** côté joueur (violet / cyan / ambre), nombre réglable
en config — pas de registre dédié tant qu'elles restent un petit ensemble borné.

## Ce que cette liste a permis de vérifier

Aucune collision de nom trouvée entre les 4 groupes de matériaux, les 23 machines, les 5
entités et les 6 structures — chaque `snake_case` proposé est unique dans tout le projet.
`ancient_conduit_stone` et `resonance_veined_stone` restent bien deux blocs distincts malgré
leur rôle similaire de "tell" visuel (l'un pour les structures, l'autre pour les poches de
cristal) — vérifié qu'ils ne sont jamais utilisés l'un pour l'autre par erreur ailleurs dans le
dossier.

## Ouvert

- Les noms de registre des entités/structures sont proposés ici pour la première fois (les
  fichiers 07-09 ne parlaient qu'en prose française) — à valider une fois le code de ces
  éléments commencé, rien n'empêche de les ajuster avant la Phase 1/2/3 correspondante.
