# 11 — Development Plan

Ce fichier ne réexplique rien : il transforme les fichiers 01-10 et 12 en tâches ordonnées.
Chaque phase a un objectif de test concret et ne s'ouvre pas tant que la précédente n'est pas
validée en jeu.

## Vue d'ensemble des phases

| Phase | Nom | Contenu principal | Sources | Sortie testable |
|---|---|---|---|---|
| 0 | Fondations | Squelette de projet, outillage | — (fait, `veskorius-mod/`) | Le mod se lance, un item existe |
| 1 | Boucle T1-T2 | Stabilizer, Assembler, Whetstone, Field Emitter, Purifier, Storage Cell, Locator, Crystal Roost, Crystal Crusher, Resonance Tuner, Catalyst Core, matériaux naturels | 03, 04, 05, 06, 09 | Boucle T1-T2 complète, y compris les voies alternatives (Roost, Crusher, augment) |
| 2 | Réseau régional T3 | Relay, Alloy Forge (branche conductive + Flux Slag), Synthesizer (+ résidu), Driller, Slag Vent, Flux Compressor, Sigma Laboratory, Poste de Garde | 03, 04, 05, 07, 08, 09 | Un joueur atteint et vide un Sigma Laboratory, gère le Slag |
| 3 | Synthèse profonde T4 | Amplifier, Deep Synthesis Chamber, Extraction Array, Network Hub, dérive de calibration, Archive Régionale, Orage de Résonance, agriculture | 03, 04, 05, 06, 07, 08, 09 | Bootstrap T4 validé, réseau relié via Amplifier, un orage traversé |
| 4 | Endgame Faille T5 | Convergence Core, Rift Anchor, Extractor, Ward Emitter, Cœur de Faille, Gardien de Faille, Rift-Ward Plate | 03, 04, 05, 07, 08, 09 | Convergence Core alimente un Rift Anchor, boss battu |
| 5 | Intégrations | Thaumcraft, Create, AE2, Mekanism, JEI/EMI | 10 | Chaque pont testé avec/sans le mod tiers |
| 6 | Assets & Polish | Textures, sons, modèles, datagen, équilibrage | 04-09, 12 | Rendu final, aucun placeholder |
| 7 | Publication | Packaging, changelog, CI | — | Version distribuable |

## Phase 0 — Fondations (réalisée, puis corrigée)

Voir dossier `veskorius-mod/`. Ne pas y revenir tant que la Phase 1 n'a pas révélé de manque.

**Correction du 2026-07-20.** La sortie testable « le mod se lance » était affirmée sans avoir
été vérifiée : le code de la Phase 0 ne compilait pas, et n'aurait pas démarré même corrigé.
Deux défauts distincts, tous deux trouvés au démarrage de la Phase 1 :

1. `DeferredRegister.create(BuiltInRegistries.ITEM, …)` renvoie un `DeferredRegister<T>`
   générique, qui n'expose pas `registerSimpleItem` / `registerSimpleBlockItem` /
   `registerSimpleBlock`. Ces helpers n'existent que sur `DeferredRegister.Items` et
   `DeferredRegister.Blocks` (`createItems` / `createBlocks`). 6 erreurs de compilation.
2. `NeoForge.EVENT_BUS.register(this)` sur la classe principale, qui ne porte aucune méthode
   `@SubscribeEvent` : depuis NeoForge 21.1 c'est une `IllegalArgumentException` au chargement.

Leçon de méthode, pas seulement de code : **une sortie testable n'est validée que si elle a
été exécutée.** Les phases suivantes ne se ferment pas sur une lecture du code.

**Architecture — recettes de fonctionnement data-driven (2026-07-21).** Les recettes de
fonctionnement des machines étaient codées en dur dans chaque `canRunCycle`/`runCycle` :
impossible de les modifier ou d'en ajouter sans recompiler. Remplacé par un vrai système de
recettes JSON (API `Recipe` 1.21), **un `RecipeType` par machine** (`veskorius:stabilizing`,
`assembling`, `purifying`, `sharpening`). Les valeurs de recette de `05-Machines.md` sont
inchangées ; c'est leur *stockage* qui devient data-driven. Voir `README.md` (section recettes)
pour l'architecture. Conséquence : les recettes de fonctionnement des futures machines sont du
datagen, plus du code ; et une variante (branche alternative de l'Assembler, recettes de
datapack tierces) est un simple JSON. Validé en jeu et par mutation des JSON générés.

## Phase 1 — Boucle T1-T2

**Objectif de test** : un joueur trouve un cristal brut, le stabilise, pose un premier champ, et
purifie un cristal — sans aide externe.

Tâches, dans l'ordre :
1. ✅ `ResonanceStabilizerBlockEntity` + cycle (30s, craft 4 Cobblestone + 2 Copper + 1 Raw
   Crystal) — `05-Machines.md` #1. Fait avec la tâche 15 (voir ci-dessous), sur un socle
   `AbstractMachineBlockEntity` / `AbstractMachineMenu` / `AbstractMachineScreen` réutilisable
   par les 22 machines suivantes. Cycle, consommation des entrées, remise à zéro et blocage
   sortie pleine couverts par `runGameTestServer`. **Reste à valider en jeu** : uniquement la
   partie visuelle (ouverture du GUI, barre de progression, orientation) — les GameTest tournent
   sans client.
2. ✅ `ComponentAssemblerBlockEntity` — #2. Codé **après** la tâche 5 (le système de champ) :
   il consomme 3 Osc/tick, donc le champ devait exister d'abord. 1 Stable Crystal + 2 Iron →
   2 Resonance Component, 5 s.
   - **Premier consommateur d'Osc.** Le socle `AbstractMachineBlockEntity` a reçu un hook
     `getOscPerTick()` : chaque tick d'avancement prélève le coût sur le champ via
     `ResonanceFieldManager.supply`. Les machines autonomes (Stabilizer, Whetstone) gardent le
     défaut 0 et sont inchangées.
   - **Décision de conception (le design ne la tranchait pas)** : une coupure d'énergie **met le
     cycle en pause** (progression conservée), alors qu'un retrait d'ingrédient **réinitialise**.
     Verrouillé par un GameTest qui coupe le courant à mi-cycle et vérifie que la progression
     reste figée.
   - **Branche alternative** (3 Resonance Dust + 2 Iron, `04-Materials.md`) : ✅ codée avec la
     tâche 13, une fois le `resonance_dust` créé. Comptes différents (1 cristal vs 3 poussières)
     donc pas un tag, mais **zéro code machine** : un second JSON `assembling/component_from_dust`
     a suffi, le slot d'entrée acceptant la poussière du seul fait de la recette.
   - 3 GameTest (production + prélèvement d'Osc réel sur l'émetteur, inertie hors champ, pause
     sur coupure).
3. ✅ `ResonanceWhetstoneBlockEntity` — #3, le plus simple, bon test de régression du pattern.
   **Codé avant la tâche 2** parce qu'il est autonome (aucun Osc) : il valide le socle sans
   dépendre du système de champ. Première machine qui *transforme* son entrée (réparation en
   place) au lieu de la consommer pour produire autre chose — bonne épreuve de généricité du
   socle. 4 GameTest.
4. ✅ Génération des poches de `raw_resonance_crystal` (`07-World-Generation.md`, strate 0/-20).
   Bloc `resonance_crystal_cluster` (ajouté au registre, voir `13-Registry-Index.md`) qui se mine
   → `raw_resonance_crystal`. Worldgen data-driven par datagen (ConfiguredFeature ore +
   PlacedFeature Y -20 à 0 + BiomeModifier `neoforge:add_features` sur tout l'Overworld). Densité
   de départ : poche taille 5, 6/chunk (à valider en playtest — première passe jugée correcte en
   jeu). La coquille de `resonance_veined_stone` (le « tell » visuel) reste la tâche 14. Non
   couvert par GameTest (le worldgen se valide en jeu) : vérifié via `/place feature` et
   génération naturelle en nouveau monde, aucune erreur de datapack. **La boucle T1 est jouable
   en survie** : miner → Stabilizer → Assembler/Purifier, sans créatif.
5. ✅ `FieldEmitterBlockEntity` + capability `IResonanceField` — #4. Pièce la plus structurante :
   toutes les machines suivantes en dépendent.
   - **Système de champ livré** : `IResonanceField` (capability de bloc), `ResonanceFieldManager`
     (index des émetteurs par dimension + routage machine→émetteur : portée, anti-stacking,
     « première posée première servie »), `FieldEmitterBlockEntity` (réserve 4000 Osc, portée 8,
     recharge en brûlant des Stable Crystals — voir `06-Energy.md`, section source primaire).
   - **Trou de conception comblé** : le design ne disait pas d'où vient l'Osc. Résolu et
     documenté dans `06-Energy.md` (les cristaux sont le carburant), décision validée avec le
     porteur du projet avant de coder.
   - **Insertion du carburant** : clic droit à main nue ouvre un **GUI dédié** (jauge verticale
     de réserve « X/4000 Osc », slot de carburant) ; clic droit avec un Stable Crystal insère
     directement ; hopper aussi (capability ItemHandler exposée). Le GUI est autonome
     (`FieldEmitterMenu`/`Screen`, pas le socle des machines : l'émetteur n'a ni cycle ni
     augment). Ajouté après coup, une fois les 5 machines codées ; valide en jeu.
   - 6 GameTest, dans une **arène isolée** de 21×21 (le manager est un index global ; sans
     isolation spatiale, les émetteurs de tests voisins se contaminent — problème qui reviendra
     amplifié au Relay T3, rayon 20 : agrandir l'arène à ce moment-là).
6. ✅ `FluxPurifierBlockEntity` + mode surchauffe — #5. Stable Crystal + Redstone → Refined
   Crystal, 45 s (22 s en surchauffe), 2 Osc/tick (4 en surchauffe). Première machine à
   surchauffe : temps ÷2 et conso ×2 gérés par le socle (`getEffectiveCycleTicks` /
   `getEffectiveOscPerTick`), seul le tirage à 20 % de perte d'input vit dans la machine.
   Toggle par le bouton `H` du GUI (le Resonance Tuner, tâche 9, agira sur le même état).
   - **Couche de contrôle** ajoutée en même temps (demande hors design, documentée dans
     `12-UX-and-Advancements.md`) : interrupteur manuel + mode redstone façon Thermal, sur
     toutes les machines. 3 boutons dans le GUI.
   - 5 GameTest : gating manuel, gating redstone REQUIRES_SIGNAL, production du Purifier,
     effets déterministes de la surchauffe (temps/conso), doublement de la conso d'Osc en
     surchauffe. **Non testé** : le tirage à 20 % de perte (RNG ; un test fiable exigerait
     ~50 cycles de 450 ticks, trop long ; un test court serait flaky). Vérifié par revue + jeu.
7. ✅ `ResonanceStorageCellItem` — #6. Batterie portable, capacité 8000 Osc, état de charge sur
   l'item (Data Component `storage_cell_charge`, persistant + synchronisé réseau). Se recharge
   dans un champ : tant qu'elle est dans l'inventaire d'un joueur couvert par un émetteur actif,
   elle prélève au plus 20 Osc/tick **sur la réserve de cet émetteur** — la même source que les
   machines, via `ResonanceFieldManager.supply` (cohérent avec « pas de câble », pilier 3).
   Craft : 2 Resonance Component + 1 Stable Crystal. Tooltip + barre de charge (réutilise la barre
   de durabilité). L'API `extractCharge` est prête pour son unique consommateur, le Locator.
   - **Codée seule (pas en paire avec la tâche 8)** : le Locator dépend des structures (tâche 10),
     encore à faire. La batterie, elle, est entièrement spécifiée (`06-Energy.md`, « Osc portable »)
     et testable sans lui — la recharge se valide contre un émetteur réel. La paire se ferme quand
     le Locator arrivera ; d'ici là la cellule se charge sans avoir encore où se vider.
   - 4 GameTest (recharge réelle prélevée sur l'émetteur, aucune charge hors champ, plafond de
     capacité sans gaspillage, `extractCharge` borné à la charge). Suite : **37 tests** verts.
8. ✅ `ResonanceLocatorItem` — #7. Détecteur de résonance à **courte portée** (~40 blocs, `07`).
   Clic droit → ping directionnel (message barre d'action : type + direction 8-vents + distance,
   + son) vers la **source de résonance la plus proche** : une poche de cristal brut (scan borné,
   les cristaux rayonnent) ou une **signature de champ** (Field Emitter actif, via l'index du
   `ResonanceFieldManager`, sans scan). Batterie interne 100 Osc, 5/ping (~20 pings), rechargée
   dans un champ ou en puisant sur une Storage Cell portée (valide enfin le consommateur de la
   tâche 7). Craft T2 gaté par le blueprint (recette proposée, le design n'en fixait pas). 6
   GameTest (conso, batterie vide, détection cristal, 8-vents, recharge champ, recharge cellule).
   - **Interprétation assumée** : le design disait « localiser les structures ». Comme mes
     structures (tâche 10) sont des features sans signature de champ, le Locator détecte pour
     l'instant les sources de résonance **indexables/scannables** (cristaux + émetteurs). Détecter
     spécifiquement les structures (Avant-poste, plus tard Sigma Laboratory) viendra quand elles
     porteront une signature de champ — cohérent avec « signature de champ détectable » (`07`).
9. ✅ `ResonanceTunerItem` — outil transversal, **outil à modes** (choix du porteur du projet,
   plus propre que la désambiguïsation par bloc — voir `12-UX-and-Advancements.md`). Modes :
   Pivoter, On/Off, Surchauffe, Redstone, adossés à la couche de contrôle ; mode stocké via Data
   Component. Gestes : clic droit = applique le mode (sans ouvrir le GUI, intercepté par
   `RightClickBlock`), clic droit dans le vide = mode suivant, **shift-clic droit = démontage**
   (rend le bloc + contenu au joueur, générique sur tout bloc-entité). Modes T4 (priorité Hub,
   recalibration) et retrait de Catalyst Core (tâche 15) à ajouter plus tard. 6 GameTest
   (routage des 4 modes + collecte et démontage). Validé en jeu (démontage propre, GUI ne
   s'ouvre plus, aucune exception dans les logs).
10. ✅ Structures « Habitation Modeste » et « Avant-poste » + **gatekeeping physique** (2026-07-22,
    voir `03`/`08`/`12`). Le déblocage d'un tier passe par un objet-clé, le `resonance_blueprint`
    (ingrédient **rendu** dans les recettes du tier), obtenu par un défi différent à chaque
    structure ; **aucune recette n'est masquée**. Sous-tâches :
    - ✅ **10a** — Items : `resonance_blueprint` (Data Component `blueprint_tier`, rendu au craft via
      `hasCraftingRemainingItem`/`getCraftingRemainingItem`), `codex_fragment` (lore, Data Component
      `codex_entry`, lisible au clic droit, non consommé), `fossilized_ration` (nourriture flavor).
    - ✅ **10b** — Recettes T2 gatées : `resonance_blueprint` ajouté comme ingrédient **rendu** aux 4
      recettes T2 existantes (Field Emitter, Flux Purifier, Storage Cell, Catalyst Core). Visible
      dans JEI, rien de masqué. GameTest : la recette échoue sans blueprint, réussit avec.
    - ✅ **10c** — Bloc `attunement_console` (généré en Avant-poste, clic droit sur place → blueprint
      T2 si le joueur n'en a pas ; sans objet, non récupérable), advancements de **feedback**
      (`tier1_awakening`, `tier2_field`) déclenchés par la possession de l'objet-repère.
    - ✅ **10d** — Structures **en tant que feature** (`RuinFeature`), pas le système Structure/jigsaw
      vanilla : réutilise le pipeline éprouvé des poches (ConfiguredFeature + PlacedFeature +
      BiomeModifier), donc peu de risque et validable au datagen. Compromis : pas de `/locate`
      vanilla → repérage par blocs (tell de surface, Locator). Deux ruines (avec/sans console),
      butin de coffre, souterraines, **tell de surface** (amorce de pierre veinée au-dessus de
      l'Avant-poste). **Densité/placement à valider en playtest**. Non couvert par GameTest
      (worldgen), mais le datapack se charge sans erreur au boot du serveur de test.
    - **10e** — Débloque la tâche 8 (le Locator a enfin une cible). ✅ fait (tâche 8).

    **Suites immédiates** : migration vers un `DataComponentIngredient`
    (tier ≥ N) quand le blueprint T3 arrivera, pour empêcher un blueprint de tier inférieur de
    crafter une machine de tier supérieur (aujourd'hui : match par item, sûr tant que seul le T2
    existe).
11. Mob « Custode » standard (+ drop Custode Alloy Fragment) et « Fileur de Cristal » (faune
    neutre, reproduction via Resonance Spore).
    - ✅ **Fileur de Cristal (`crystal_strider`) codé** (2026-07-22) : faune neutre (aucun goal de
      cible, fuit quand blessé), **traite** au clic droit à main nue (1 Raw Crystal, cooldown 5 min,
      persistant), **reproduction** au `resonance_spore` (élevage vanilla). Attributs (10 PV),
      œuf d'apparition, modèle/renderer placeholder, spawn souterrain Overworld (biome modifier
      `add_crystal_strider` + règle de placement Y 0/-40 ; **densité à valider en playtest**, le
      spawn souterrain d'une créature étant limité par l'algo vanilla). Mise en place du **registre
      d'entités** (`ModEntities`, `ModEntityEvents` pour attributs + placement, rendu client). 3
      GameTest (traite + cooldown, nourriture = spore uniquement, bébé = Fileur). Suite : **42**.
    - ✅ **Custode** codé (2026-07-22) : garde réactif (`Monster`), 30 PV / 6 dégâts, **réactif à
      6 blocs** (via `FOLLOW_RANGE`) + riposte si frappé — jamais agressif à distance (pilier 4).
      **Posé par la génération de l'Avant-poste** (`RuinFeature`, persistant), pas de spawn naturel
      errant (« garde un site, pas un territoire »). Drop 2-4 `custode_alloy_fragment`, **substitut
      1:1 du fer** dans toutes les recettes Veskorius via le nouveau tag `veskorius:iron_substitutes`
      (les recettes qui codaient le fer en dur ont été migrées vers ce tag). Œuf d'apparition,
      modèle/renderer placeholder. 3 GameTest (stats du garde, fragment ↔ fer, alerte sur casse de
      machine). Réaction à la casse d'une machine du site **faite** (`CustodeAlertHandler` sur
      `BlockEvent.BreakEvent`, rayon d'alerte configurable). **Patrouille** faite : il reste dans un
      rayon de 12 blocs autour de son point de garde (`restrictTo` + `MoveTowardsRestrictionGoal`,
      persisté). Il ne s'éloigne donc pas de son Avant-poste.
    - ⬜ **Bloc de récolte du `resonance_spore`** : l'item existe, mais sa source (pousse sur
      `resonance_veined_stone` en faible luminosité, façon glow lichen, repousse ~2 jours MC —
      `04-Materials.md`) est un bloc à part, non codé. Tant qu'il manque, la reproduction du Fileur
      n'est accessible qu'en créatif ; la traite, elle, est jouable en survie (Fileurs sauvages).
12. ✅ `CrystalRoostBlockEntity` (production passive) — machine « traitement » autonome
    (`veskorius:roosting` : 2 Quartz → 1 Raw Crystal, 600 s) réutilisant le socle process, avec
    **une condition en plus** codée dans la machine (`canRunCycle`) : au moins un Fileur de Cristal
    à moins de 6 blocs. Sans Fileur, inerte (et pas de Quartz consommé). Interprétation des chiffres
    du design : 2 Quartz/cycle × 2 cycles par jour MC = 4 Quartz/jour, 1 Raw Crystal/600 s — les
    deux valeurs de `05-Machines.md` #8 réconciliées. Craft T2 gaté par le blueprint (4 Planches +
    2 Stable Crystal + 1 Botte de Foin). GUI + slot d'augment gratuits (socle). Intégré à JEI.
    1 GameTest (le gate Fileur : progresse avec, pas sans). Utile maintenant que les poches sont
    rares. Débloqué par le Fileur (fait à la tâche 11).
13. ✅ `CrystalCrusherBlockEntity` (#22, alternative au Stabilizer, produit Resonance Dust).
    Cycle 10 s, autonome (aucun Osc) : 1 Raw Crystal → 3 Resonance Dust. Première machine
    « traitement » à **une seule entrée** — elle prouve que le socle
    `AbstractProcessingMachineBlockEntity` ne suppose pas deux slots d'entrée. Réutilise
    intégralement le socle : block + menu + écran de 3 lignes chacun, le reste est du datagen.
    Item `resonance_dust` ajouté (04-Materials.md : voie T1 plus rapide que le Stabilizer, mais
    sans Stable Crystal en sortie ; sert aussi d'engrais T3 et de la branche alt de l'Assembler).
    - **Branche alternative de l'Assembler débloquée** (voir tâche 2) : 3 Resonance Dust + 2 Iron
      → 2 Component est désormais un simple second JSON (`assembling/component_from_dust`), **zéro
      code machine** — le slot d'entrée 0 accepte la poussière du seul fait de la recette
      (`isItemValid` piloté par les recettes). Exactement ce que le socle data-driven promettait.
    - 3 GameTest (production 3 poussières + entrée unique, fonctionnement sans champ, branche alt
      de l'Assembler). Suite complète : **32 tests** verts. Reste la validation visuelle du GUI
      (barre + boutons), reportée à la passe `runClient` comme les autres machines.
14. ✅ Génération de `resonance_veined_stone` (coquille autour des poches) et de `raw_flux_deposit`
    (croûte brossable, réutilise le mécanisme de brosse vanilla).
    - Feature custom `veskorius:crystal_pocket` (remplace l'ore vanilla de la tâche 4) : amas de
      cristaux + coquille de pierre veinée, ~15 % de la coquille en croûte de flux brossable.
      Config data-driven (`crystal_tries`, `shell_thickness`, `flux_chance`).
    - `raw_flux_deposit` : bloc brossable (rattaché au type de BE vanilla BRUSHABLE_BLOCK) +
      item. **Brosser** donne le flux ; **miner** ne donne rien (croûte détruite) — comportement
      vanilla des blocs suspects, honore la « collecte silencieuse par observation » (pilier 2).
      L'item rejoint le tag `stabilizer_flux` → chemin T1 alternatif au Quartz, 1:1, **sans une
      ligne de code machine** (le tag le préparait depuis la tâche 1).
    - Validé en jeu : coquille présente, flux visible, brossage fonctionnel, alternative Quartz
      opérationnelle. Non couvert par GameTest (worldgen + interaction brosse) — vérifié en jeu.
15. Slot d'augment générique sur toutes les machines actives + `ResonanceCatalystCoreItem` —
    implémenter le slot dès cette phase, même si son usage réel ne devient intéressant qu'à
    partir de la Phase 2, pour éviter de le retrofit plus tard sur des machines déjà codées.
    - ✅ **Slot fait** (avec la tâche 1 : la tâche 1 code la première des 23 machines, donc
      attendre la tâche 15 aurait garanti exactement le retrofit que cette tâche veut éviter —
      l'ordre initial 1 → 15 était contradictoire sur ce point).
    - ✅ **Item `resonance_catalyst_core`** (craft 2 Resonance Component + 1 Refined Crystal +
      1 Redstone). Ajouté au tag `veskorius:machine_augments` : l'effet +15% était déjà porté par
      le socle (`AUGMENT_SPEED_MULTIPLIER` + `hasAugment()`), **aucun code machine touché** — la
      prédiction de la tâche 1 vérifiée. Récupérable via le démontage au Tuner (shift-clic) ou en
      cassant le bloc ; le **mode Tuner « retrait d'augment en place »** reste différé (groupé avec
      le contenu T4 dans `12-UX-and-Advancements.md`). 1 GameTest (acceptation dans le slot + cycle
      600 → 522 ticks). Couvre le critère de sortie « installation d'un Catalyst Core sur une T1 ».

**Critère de sortie** : les 15 tâches jouées en jeu sans crash, dans une seule partie continue,
du spawn jusqu'à la purification d'un cristal, avec au moins un test du mode surchauffe, du
Crystal Roost, et de l'installation d'un Catalyst Core sur une machine T1.

**Outillage — fait.** Le harnais `GameTest` est en place : `./gradlew runGameTestServer` lance
la suite sans interface et fait échouer le build au moindre test rouge. Coût réel : 24 secondes
pour toute la suite, boot de Minecraft compris — les cycles de 600 ticks s'exécutent en 2
secondes, le serveur de test ne tourne pas à 20 tps.

Deux points qui ont demandé une décision, à savoir pour la suite :

- NeoForge 21.1 ne fournit **aucun** template de structure vide (l'annotation `@EmptyTemplate`
  n'existe que dans des versions plus récentes). Un `ModStructureTemplateProvider` génère donc
  le `.nbt` vide en datagen, plutôt que de committer une ressource binaire écrite à la main.
- ModDevGradle n'a pas de raccourci `gameTestServer()` ; le type de run est déclaré par le
  userdev de NeoForge et posé à la main dans `build.gradle`.

**Ce que le harnais couvre, et ce qu'il ne couvre pas.** Il valide la logique serveur : cycle,
consommation des entrées, remise à zéro, blocage sortie pleine, filtrage du slot d'augment. Il ne
valide **rien de visuel** — GUI, barre de progression, orientation, textures se vérifient au
client. Le critère de sortie de la phase reste donc une partie jouée, mais réduite à ce qui se
voit, au lieu de tout revérifier à la main.

**Validation visuelle du 2026-07-21** : `runClient`, deux passes.
- Passe 1 — les 4 premiers blocs (Stabilizer, Component Assembler, Whetstone, Field Emitter) :
  GUI, barres de progression, orientation, textures OK.
- Passe 2 — Flux Purifier + la couche de contrôle : les 3 boutons (manuel, redstone, surchauffe)
  s'affichent et réagissent, le bouton surchauffe n'apparaît que sur le Purifier, tooltips OK,
  clic → effet serveur. Aucune erreur d'asset ni de widget dans les logs sur les deux passes.

Reste non couvert côté visuel : le GUI du Field Emitter (jauge de réserve, pas encore codé),
l'affichage du compteur d'Osc dans les GUI, et l'icône flamme d'état de surchauffe sur la barre
(distincte du bouton de contrôle) — tous prévus à la passe visuelle de la Phase 6 (12-UX).

Règle pour les 22 machines suivantes : **une machine n'est finie que quand ses GameTest passent.**
La valeur de référence (durée de cycle, quantités) est réécrite dans le test plutôt qu'importée
depuis la machine, pour qu'un changement de valeur non répercuté ici fasse échouer la suite.

**Travaux différés, à ne pas oublier :**

- **GUI du Field Emitter** (jauge de réserve `X/4000 Osc`, slot de carburant visible,
  `12-UX-and-Advancements.md`). Le bloc est jouable sans (clic droit + hopper), mais on ne voit
  pas sa réserve. À faire avant la fin de la Phase 1, avec les autres passes visuelles.
- **Perf du `ResonanceFieldManager`** : le routage scanne linéairement tous les émetteurs de la
  dimension. Correct pour un mod (rarement des centaines d'émetteurs), à indexer par chunk
  seulement si un playtest révèle un coût réel. Ne pas optimiser à l'aveugle.
- **GameTest dans `src/main/java`** : ils partiront dans le jar de release. Sans conséquence
  maintenant, à isoler dans un sourceSet dédié avant la Phase 7 (publication).

## Phase 2 — Réseau régional T3

**Objectif de test** : un joueur équipé du T2 atteint un Sigma Laboratory, résout le puzzle des
deux Relais, gère le Flux Slag de sa première Alloy Forge sans qu'elle ne se bloque.

Tâches, dans l'ordre :
1. `ResonanceRelayBlockEntity` + ray-cast de ligne de mire, craft utilisant le Veskorian
   Conductive Alloy Ingot (dépend de la tâche 2) — #9.
2. `VeskorianAlloyForgeBlockEntity` + tampon de Flux Slag (16 max, arrêt si plein) + branchement
   de recette selon le métal fourni (Iron → structurel, Gold → conductif) — #10.
3. `SlagVentBlockEntity` (dissipation passive, 1/10s/Forge à portée) — #13.
4. `StructuralSynthesizerBlockEntity` + sous-produit Synthesis Residue (utilisable directement,
   pas de maintenance requise contrairement au Flux Slag) — #11.
5. `DeepCrystalDrillerBlockEntity` + génération des veines profondes (Y < -40) — #12.
6. `FluxCompressorBlockEntity` (#23, produit Concentrated Flux, nécessaire pour la Phase 4).
7. Structure Sigma Laboratory complète (puzzle des deux Relais).
8. Structure Poste de Garde + mob Custode Lourd.
9. Outils/armure en Alliage Veskorien.

**Critère de sortie** : une Alloy Forge tourne jusqu'à saturation de son tampon de Slag sans
Slag Vent (doit s'arrêter proprement), puis reprend une fois un Slag Vent posé à portée.

## Phase 3 — Synthèse profonde T4

**Objectif de test** : un joueur construit sa première Deep Synthesis Chamber en respectant
exactement le bootstrap à 3 Hyper Refined Crystal (2 pour le Lattice, 1 catalyseur), relie deux
bases via un Harmonic Amplifier, et observe la dérive de calibration après plusieurs jours
Minecraft.

Tâches, dans l'ordre :
1. `HarmonicAmplifierBlockEntity` + dérive de calibration (-1%/jour, plafond -30%) — #14.
2. `DeepSynthesisChamberBlockEntity`, catalyseur installé au craft (pas par cycle), mode
   surchauffe — #15. **Vérifier explicitement en jeu que le bootstrap à 3 cristaux fonctionne
   sans blocage** (voir `05-Machines.md`, section Bootstrap du T4) — c'est le test le plus
   important de cette phase.
3. `AutomatedExtractionArrayBlockEntity` — #16, dépend du Driller (Phase 2).
4. `ResonanceNetworkHubBlockEntity` + logique de priorité + dérive — #17.
5. Extension du Resonance Tuner : configuration de priorité du Hub, recalibration.
6. Structure Archive Régionale (puzzle des 4 fragments, loot fixé à 3 Hyper Refined Crystal +
   chance de `ancient_seed`).
7. Mob Custode Archiviste (mini-boss optionnel).
8. Événement météo Orage de Résonance (actif seulement après cette phase, produit
   `meteoric_resonance_shard`, nécessaire pour la Rift-Ward Plate en Phase 4).
9. Culture `ancient_seed` → `resonance_bloom`, + `luminous_extract` et `resonance_glass`
   (teignable) — contenu optionnel, sans impact sur la progression principale.

**Critère de sortie** : le bootstrap T4 testé en partant strictement des 3 cristaux donnés par
l'Archive, sans triche créative, jusqu'à obtenir une Deep Synthesis Chamber fonctionnelle. Un
Orage de Résonance déclenché au moins une fois en test, avec récupération d'au moins un
Meteoric Resonance Shard avant sa disparition.

## Phase 4 — Endgame Faille T5

**Objectif de test** : un joueur construit un Convergence Core validé (multi-bloc), alimente un
Rift Anchor à distance grâce à lui, bat le Gardien de Faille, et récupère son loot (Rift Essence
+ Corrupted Alloy Ingot).

Tâches, dans l'ordre :
1. `ConvergenceCoreBlockEntity` + validation de structure (anneau de 8 Relay/Amplifier à 5
   blocs, ligne de mire) — #18, craft nécessitant 4 Concentrated Flux (Phase 2, tâche 6). Le
   plus complexe à coder de tout le mod : prévoir un item de debug affichant la validité de la
   structure en temps réel pendant le développement.
2. Génération des Failles (bulle sphérique, fissures de surface).
3. Dégâts de déphasage + `RiftAnchorBlockEntity` — #19.
4. Mob Gardien de Faille, 3 phases, drop garanti Corrupted Alloy Ingot ×3.
5. `RiftCoreExtractorBlockEntity`, compteur fini (6 extractions), 15% bonus Corrupted Alloy —
   #20.
6. `RiftWardEmitterBlockEntity` — #21.
7. Rift-Ward Plate — pièce unique, plastron uniquement (craft : Plastron en Alliage Veskorien +
   Corrupted Alloy Ingot + Meteoric Resonance Shard, obtenu en Phase 3, tâche 8).

**Critère de sortie** : une Faille jouée du repérage jusqu'à l'épuisement complet (6
extractions), avec le Rift Anchor alimenté exclusivement par un Convergence Core (pas par un
Relay manuel), sans bug de compteur ni de persistance.

## Phase 5 — Intégrations

Chaque pont du fichier 10 testé isolément, avec le mod tiers installé puis sans. Ordre suggéré :
JEI/EMI d'abord, puis Thaumcraft, puis Mekanism, puis Create et AE2 en dernier.

## Phase 6 — Assets & Polish

- Remplacer toutes les textures placeholder.
- Migrer les JSON écrits à la main vers un vrai datagen.
- Implémenter les conventions d'interface de `12-UX-and-Advancements.md` (barres de
  progression, icône surchauffe, indicateur de dérive) sur les 23 machines.
- Implémenter les 6 `Advancement` de `12-UX-and-Advancements.md`.
- Passe d'équilibrage complète sur toutes les valeurs marquées « à valider en playtest » (les
  constantes de `VeskoriusConfig` se règlent alors sans recompiler — voir `14-Configuration.md`).
- Finir la roadmap config de `14-Configuration.md` : registre de carburants data-driven, intensité
  de champ, granularité par machine si besoin, config CLIENT si une option visuelle apparaît.
- Sons et musique (hors périmètre, futur fichier dédié si besoin).

## Phase 7 — Publication

CI GitHub Actions, changelog, publication Modrinth/CurseForge — patterns déjà documentés dans
`references/common-patterns.md` du skill `minecraft-mod-dev`.

## Configuration — pilotable par modpack (transversal, voir `14-Configuration.md`)

Préoccupation transversale, pas une phase : le mod doit être rééquilibrable et adaptable par un
modpack maker sans recompiler. Deux leviers, répartis par `14-Configuration.md` : **datapack** pour
tout ce qui est contenu (recettes, tags, loot, worldgen — déjà en place depuis les tâches 1-14), et
**config TOML SERVER** (`VeskoriusConfig`) pour les constantes d'équilibrage codées en dur.

**Première passe faite (2026-07-22).** `VeskoriusConfig` (type SERVER, synchronisé, par monde,
livrable via `defaultconfigs/`) expose 9 constantes jusque-là en dur : portée / capacité / Osc par
cristal du champ, capacité et débit de la Storage Cell, bonus d'augment, et les trois facteurs de
surchauffe (vitesse, Osc, perte). Câblées dans `FieldEmitterBlockEntity`, `AbstractMachineBlockEntity`,
`FluxPurifierBlockEntity` et `ResonanceStorageCellItem`, lues à l'exécution (jamais en `static final`).
GameTest `configDefaultsMatchDesign` verrouille les défauts sur les valeurs de design.

**Registre de carburants data-driven fait (2026-07-22).** Le Field Emitter n'accepte plus un item
codé en dur : ses carburants sont un `RecipeType` `veskorius:fueling` (`ingredient → osc` par JSON),
surchargeable par datapack, affiché dans JEI. A remplacé le `stableCrystalOsc` de la config. Défaut
inchangé : Stable Crystal = 4000 Osc.

**Reste à faire** (détail dans `14-Configuration.md`, section roadmap) : intensité de champ quand
l'Amplifier l'utilisera (Phase 3) ; granularité par machine si un playtest la réclame ; config
CLIENT le jour où une option visuelle existe. **Règle** : toute nouvelle constante d'équilibrage des
Phases 2-4 passe par `VeskoriusConfig` (ou par un type de recette data-driven quand c'est du
contenu, comme les carburants) dès son écriture, pas en retrofit.

## Comment utiliser ce plan au quotidien

Une session de travail = une tâche numérotée d'une seule phase. Si une tâche révèle qu'un
fichier 01-12 doit changer, corriger ce fichier avant de continuer le code — ce dossier reste la
source de vérité, pas les commentaires dans le code.
