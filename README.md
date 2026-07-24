# Veskorius (NeoForge 1.21.1)

Squelette de mod fonctionnel — pas une démo, un vrai point de départ. Ce qui est codé ici
compile, se charge et se lance ; ce qui manque est listé en bas, dans l'ordre où le coder.

**Source de vérité pour tout ce qui est design/gameplay : `veskorius-design/`.** Ce dépôt ne
contient que du code et ne redéfinit jamais une valeur de jeu. Si une valeur manque ici, elle est
dans `veskorius-design/05-Machines.md` ou `04-Materials.md`, pas à redéfinir dans le code ni dans
ce fichier.

## Contenu actuel

- Projet Gradle complet (ModDevGradle, NeoForge 1.21.1, Java 21). `./gradlew build` et
  `./gradlew runData` passent.
- 4 items enregistrés : `raw_resonance_crystal`, `stable_resonance_crystal`,
  `refined_resonance_crystal`, `resonance_component`.
- **5 machines à cycle fonctionnelles** : Resonance Stabilizer (#1, autonome), Component
  Assembler (#2, 3 Osc/tick), Resonance Whetstone (#3, autonome), Flux Purifier (#5, 2 Osc/tick,
  **mode surchauffe**) et Crystal Crusher (#22, autonome, 1 Raw Crystal → 3 Resonance Dust en
  10 s, alternative rapide au Stabilizer). Block entity, cycle, GUI avec barre de progression,
  orientation, slot d'augment, inventaire persistant et vidé au sol quand le bloc est cassé.
- **Contrôles sur toutes les machines** (3 boutons dans le GUI) : interrupteur manuel on/off,
  contrôle redstone façon Thermal (ignoré / requiert un signal / requiert l'absence), et
  surchauffe pour les machines qui la supportent. Aucun packet custom (canal vanilla
  `clickMenuButton`).
- **Resonance Tuner** : outil à modes (Pivoter / On-Off / Surchauffe / Redstone, stocké sur
  l'item via Data Component). Clic droit applique le mode, clic droit dans le vide change de
  mode, **shift-clic droit démonte** n'importe quel bloc-entité (bloc + contenu → inventaire).
  L'interaction passe par un événement (`RightClickBlock`) pour intercepter avant l'ouverture du
  GUI du bloc.
- Un socle réutilisable pour les machines à cycle restantes : `AbstractMachineBlock`,
  `AbstractMachineBlockEntity`, `AbstractMachineMenu`, `AbstractMachineScreen`. Ajouter une
  machine « standard » = une block entity (cycle), un bloc/menu/écran de 3 méthodes chacun, et
  quelques lignes de datagen.
- **Recettes de fonctionnement data-driven** (`com.veskorius.recipe`) : **un `RecipeType` par
  machine**, recettes en **JSON** — modifiables/ajoutables par datapack, sans recompiler.
  - `veskorius:stabilizing` / `assembling` / `purifying` : recettes input→output partageant la
    classe `MachineRecipe` (ingrédients item/tag **+ count**, résultat, **temps**, **Osc/tick**).
    Cycle générique via `AbstractProcessingMachineBlockEntity` — une nouvelle machine input→output
    ne code aucune recette. Le Crystal Crusher (`veskorius:crushing`) en est le dernier exemple :
    première machine à **une seule entrée**, elle a réutilisé le socle sans le modifier. Sa
    poussière a aussi débloqué la **branche alternative de l'Assembler** (3 Resonance Dust + 2 Iron)
    en un simple second JSON, sans une ligne de code — la promesse annoncée ci-dessous, réalisée.
  - `veskorius:sharpening` : le Whetstone (réparation), forme à part (catalyseur, **% réparé**,
    temps ; l'outil réparé est calculé, pas un résultat fixe).
  - Conséquence directe : la « branche alternative » de l'Assembler (3 poussière + 2 fer) est un
    simple second JSON, zéro code — désormais faite (avec le Crystal Crusher).
- **Système d'énergie de Résonance (le champ)** : capability `IResonanceField`,
  `ResonanceFieldManager` (routage machine→émetteur par champ, pas de câble), le **Field
  Emitter** (#4) — réserve de 4000 Osc rechargée en brûlant des Stable Crystals, portée 8, avec
  un **GUI dédié** (jauge de réserve `X/4000 Osc`) — et la consommation d'Osc branchée dans le
  socle des machines (`getOscPerTick`). Le Component Assembler en est le premier client.
- **Harmoniques & Dissonance** (`06`, le système-signature) : un champ a une **bande** (une
  **couleur** : violet / cyan / ambre), une machine « écoute » sur une bande. Même couleur = propre ;
  couleur différente = **ça marche quand même**, mais ça coûte plus d'Osc et ça injecte de la
  **dissonance** dans l'émetteur (coupole qui grisaille → champ intermittent). C'est le remplaçant
  du câble : deux émetteurs de bandes différentes alimentent deux groupes de machines au même
  endroit, sans un fil. Codé : bandes + désaccord + dissonance (plafonnée, persistée, décroissante),
  **Émetteur Accordable**, mode « Accorder » du Tuner (réversible : le cycle revient à
  « universelle »), **Damping Array** (agents data-driven → `resonance_sludge`), flag de recette
  `stable` (toutes les recettes T1 le portent), **glow des machines coloré par bande** (clignotant
  si désaccordée) et **HUD de champ** (bande / réserve / dissonance) visible en portant le
  **Resonance Locator** — inventaire ou slot **Curios** (dépendance douce, par réflexion : aucune
  dépendance de build). **La T1 ne gagne aucune complexité** : une machine sans bande est
  universelle et n'affiche rien. Interrupteur maître dans `veskorius-harmonics.toml`. Au **plafond**
  de dissonance, l'émetteur libère une **décharge de résonance** : impulsion AoE qui blesse à portée
  (type de dégâts dédié, message de mort de lore) et purge une partie de la saturation — le champ
  se rétablit si la cause disparaît, re-décharge sinon. **Le système est complet** (reste la passe
  visuelle Phase 6).
- Datagen complet : plus aucun blockstate / modèle / recette / loot table / tag / traduction
  n'est écrit à la main.
- **Resonance Storage Cell** (#6) : batterie portable (item, 8000 Osc, charge sur l'item via Data
  Component). Se recharge dans un champ — tant qu'elle est dans l'inventaire d'un joueur couvert
  par un émetteur, elle prélève ≤20 Osc/tick sur la réserve de celui-ci (même source que les
  machines, aucune conversion cachée). Tooltip + barre de charge ; `extractCharge` prêt pour le
  Locator (tâche 8). Son consommateur viendra avec les structures.
- **Configuration modpack** (`14-Configuration.md`) : philosophie « data-driven d'abord ». Tout le
  contenu (recettes de fonctionnement/craft, tags, loot, worldgen) est déjà surchargeable par
  **datapack**. Les constantes d'équilibrage codées en dur (portée/capacité du champ, capacité
  et débit de la Storage Cell, bonus d'augment, facteurs de surchauffe) passent par un
  `ModConfigSpec` **SERVER** (`VeskoriusConfig`, `veskorius-server.toml`) — synchronisé, par monde,
  livrable via `defaultconfigs/`. Lues à l'exécution ; défauts verrouillés sur le design par un test.
- **Carburants du Field Emitter data-driven** : type de recette `veskorius:fueling`
  (`ingredient → osc`). Par défaut un seul carburant (Stable Crystal, 4000 Osc) ; un datapack en
  ajoute/retire ou change les valeurs sans recompiler. Affiché dans JEI (carburant → Osc). A
  remplacé le filtre d'item et la valeur d'Osc jusque-là en dur.
- **Première entité — Fileur de Cristal** (`crystal_strider`, 09-Entities.md) : faune neutre des
  poches (ne combat jamais, fuit quand blessé). **Traite** au clic droit à main nue (1 Raw Crystal,
  cooldown 5 min) et **reproduction** au `resonance_spore` (élevage vanilla). Œuf d'apparition,
  spawn souterrain (densité à valider en playtest), modèle/renderer placeholder. Met en place le
  socle entités (`ModEntities`, événements d'attributs/placement, rendu client) réutilisable pour
  les mobs suivants. Le Custode et le bloc de récolte du spore restent à coder.
- **Structures T1-T2 + gatekeeping physique** (`03`/`08`) : deux ruines (Habitation Modeste,
  Avant-poste) sont de **vraies `Structure` vanilla en jigsaw** (migrées de features, A7). Leurs
  pièces sont des **NBT générés par datagen** (salle de pierre veinée, coffre à loot, console, et un
  **Custode gardien intégré** à la pièce de l'Avant-poste). Elles sont taguées `#veskorius:locatable`
  → **`/locate` et le mode Structures du Locator fonctionnent**. Le déblocage d'un tier passe par un
  objet-clé, le `resonance_blueprint` — **ingrédient rendu** dans les recettes du tier (aucune
  recette masquée, tout est visible dans JEI ; ce qui bloque, c'est de ne pas avoir le plan). Le T2
  s'obtient en **réveillant la console** (`attunement_console`) de l'Avant-poste sur place. Les
  `codex_fragment` sont du lore lisible, pas un gate. Advancements de feedback. *(Vrais layouts
  multi-pièces = Phase 6 ; fréquence de génération = JSON `structure_set`, à valider en playtest.)*
- **Resonance Locator** (#7) : détecteur de résonance à courte portée (~40 blocs). Clic droit →
  ping directionnel vers la source la plus proche — poche de cristal (utile maintenant qu'elles
  sont rares) ou signature de champ (Field Emitter). Batterie interne 100 Osc (5/ping), rechargée
  dans un champ ou en puisant sur une Storage Cell portée.
- **Crystal Roost** (#8) : production passive de cristal brut (2 Quartz → 1 Raw Crystal, 600 s) à
  condition qu'un **Fileur de Cristal** soit à moins de 6 blocs — alternative lente au minage,
  pertinente maintenant que les poches sont rares. Réutilise le socle process + un gate de proximité.
- **Custode** (garde réactif) : 30 PV / 6 dégâts, ne cible un joueur qu'à **6 blocs** (ou s'il est
  frappé) — jamais agressif à distance. **Posé par la génération de l'Avant-poste** (garde le site),
  pas de spawn errant. Drop 2-4 `custode_alloy_fragment`, **substitut 1:1 du fer** dans les recettes
  Veskorius (tag `iron_substitutes`) — récompense le combat plutôt que le minage.
- **Passe de polish** (configs/actions/mécaniques) : config étendue (`tools`/`entities` — Locator,
  Custode, Fileur, Roost tous tunables) ; **défense de site** (casser une machine Veskorius alerte
  les Custodes proches) ; retours de la traite du Fileur (son, particules, message de cooldown) ;
  sons des mobs.
- **Récolte de spore** : la Resonance Veined Stone pousse un `resonance_spore` (état `spored`) sur
  une face exposée en faible luminosité, récolté au clic droit (sans casser la pierre), puis
  repousse — la reproduction du Fileur devient jouable en survie. Taux de pousse configurable.
- Harnais `GameTest` : **101 tests** (machines, augments, automatisation d'objets, Codex, harmoniques, structures… +
  Custode, défense de site, récolte de spore, amorçage T2 garanti, défauts de config),
  `./gradlew runGameTestServer`. Le serveur de test charge tout le datapack sans erreur. Ce qui est
  **visuel ou réseau** (rendu des GUI et du HUD, particules, coupole, génération réelle des
  structures) n'est pas couvert ici : ça se valide en `runClient` — **passe faite le 2026-07-25, tout
  est bon** (seul le pont **Curios** reste non vérifié, le mod n'étant pas une dépendance de dev).
- **Intégration JEI** (dev) : les recettes des 4 machines s'affichent dans JEI, une catégorie par
  machine, avec temps et Osc/tick. JEI est en `compileOnly` (API) + `localRuntime` (mod complet
  dans `runClient`), pas exporté dans le jar. Sert à vérifier les recettes en jeu.
- **Génération de monde** : feature custom `crystal_pocket` (data-driven) — poches de
  `resonance_crystal_cluster` (Y -20 à 0), enrobées d'une coquille de `resonance_veined_stone`
  (le « tell » visuel), avec ~15 % de croûtes de `raw_flux_deposit` **brossables** (brosse
  vanilla → flux, alternative T1 au Quartz). La boucle T1 est jouable en survie, minage comme
  brossage.
- Textures placeholder (couleur unie) — à remplacer par du vrai pixel art en Phase 6.

Consulter `veskorius-design/13-Registry-Index.md` pour l'état « codé / à coder » de tout le
contenu prévu : c'est le point de départ avant de reprendre le développement, plutôt que de
deviner où on en est.

## Mise en route

1. `./gradlew runClient` (ou via IntelliJ : Gradle > Tasks > neoforge > runClient) pour lancer
   le jeu avec le mod chargé.
2. `./gradlew runData` après tout ajout de bloc/item/recette : régénère `src/generated/resources/`.
   Ce dossier est volontairement ignoré par git — il se reconstruit à partir du code, et le
   versionner créerait des conflits sans valeur. **À lancer avant `runGameTestServer`** : c'est
   lui qui produit le template de structure vide dont les tests ont besoin. Le cache de datagen
   est vidé automatiquement avant chaque run, donc la sortie correspond toujours au code (une
   édition manuelle d'un JSON généré, pour tester une recette + `/reload`, est transitoire : le
   prochain `runData` la remplace). Dépendances déjà en cache → ajouter `--offline` évite un accès
   réseau.
3. `./gradlew runGameTestServer` : joue les tests des machines sans interface (~24 s). Le build
   échoue si un test échoue. Une machine n'est considérée finie que quand ses tests passent.
4. `gradle.properties` : la version `neo_version=21.1.172` est celle utilisée pour valider le
   build. Si la résolution de dépendance échoue, vérifier la dernière version patch sur
   https://projects.neoforged.net/neoforged/neoforge.

## Conventions de code à respecter pour les machines suivantes

Elles ne sont pas décoratives — s'en écarter casse le socle générique :

- **Le dernier slot déclaré d'une machine est son (premier) slot d'augment.** Le socle en réserve
  `MAX_AUGMENT_SLOTS` (4) à la suite ; le nombre **actif** est réglable en config (`augmentSlots`,
  défaut 1 = comportement historique), avec une **règle de cumul** (FORBID/CAPPED/FREE) qui borne le
  cumul d'un même effet. C'est ce qui permet à `AbstractMachineBlockEntity` de gérer les augments
  sans code par machine.
- **Les slots de la machine sont ajoutés au menu avant l'inventaire du joueur**, donc l'indice
  d'un slot dans le menu est exactement son indice dans l'inventaire de la block entity.
- **Une entrée qui aura d'autres membres plus tard passe par un tag, pas par un item en dur**
  (voir `ModTags`) — ça transforme une évolution prévue en un ajout de datagen d'une ligne.

## Ce qui n'est PAS encore fait (dans l'ordre à coder)

Suivre `veskorius-design/11-Development-Plan.md`, Phase 1 — c'est la liste ordonnée complète et à
jour (recettes exactes, chiffres d'équilibrage, dépendances entre tâches). Faites : tâches 1, 2,
3, 4, 5, 6, 7, 9, 10, 13, 14, 15. Restent, avec leurs dépendances :

1. **`ResonanceLocatorItem`** (tâche 8) — outil de localisation ; désormais **débloqué** (les
   structures existent, il a une cible). Modèle d'énergie déjà résolu (batterie interne + recharge
   par champ/Storage Cell). Prochain candidat naturel.
2. **Mobs** (tâche 11) — le *Fileur de Cristal* est fait ; reste le *Custode* (garde réactif,
   dépend des structures pour son spawn). Puis le **Crystal Roost** (tâche 12, débloqué par le
   Fileur).
3. **Finitions tâche 10** : tell de surface de l'Avant-poste ; migration du match blueprint vers
   `DataComponentIngredient` quand le T3 arrivera. Bloc de récolte du `resonance_spore`.

La boucle T1-T2 est jouable en survie de bout en bout : miner → stabiliser → **trouver un
Avant-poste, réveiller la console → blueprint T2** → poser un champ → purifier. L'augment +15%, la
Storage Cell et les carburants data-driven sont en place. Reste surtout du contenu (Locator, Custode,
Roost) ; le mode Tuner « retrait d'augment en place » reste différé.

## Structure

```
src/main/java/com/veskorius/
├── Veskorius.java              ← point d'entrée, registres, onglet créatif
├── block/
│   ├── ModBlocks.java
│   ├── ResonanceStabilizerBlock.java
│   └── entity/                 ← AbstractMachineBlockEntity + machines
├── item/ModItems.java
├── menu/                       ← AbstractMachineMenu + menus de machines
├── client/                     ← écrans (Dist.CLIENT uniquement)
├── config/                     ← VeskoriusConfig (ModConfigSpec SERVER, réglages modpack)
├── entity/                     ← ModEntities + mobs (CrystalStrider) + événements (attributs/spawn)
├── energy/                     ← IResonanceField, ResonanceFieldManager, capabilities
├── recipe/                     ← MachineRecipe(Input/Serializer) + RecipeTypes/Serializers
├── worldgen/                   ← ModWorldGen (features + biome modifier, data-driven)
├── compat/jei/                 ← intégration JEI (chargée seulement si JEI présent)
├── datagen/                    ← providers + GatherDataEvent
├── gametest/                   ← tests joués par runGameTestServer
└── tag/ModTags.java

src/main/resources/assets/veskorius/textures/  ← seules ressources écrites à la main
src/generated/resources/                       ← tout le reste, produit par runData
src/main/templates/META-INF/neoforge.mods.toml ← généré vers le jar au build
```
