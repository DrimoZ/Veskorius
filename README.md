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
- **4 machines à cycle fonctionnelles** : Resonance Stabilizer (#1, autonome), Component
  Assembler (#2, 3 Osc/tick), Resonance Whetstone (#3, autonome) et Flux Purifier (#5, 2 Osc/tick,
  **mode surchauffe**). Block entity, cycle, GUI avec barre de progression, orientation, slot
  d'augment, inventaire persistant et vidé au sol quand le bloc est cassé.
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
    ne code aucune recette.
  - `veskorius:sharpening` : le Whetstone (réparation), forme à part (catalyseur, **% réparé**,
    temps ; l'outil réparé est calculé, pas un résultat fixe).
  - Conséquence directe : la « branche alternative » de l'Assembler (3 poussière + 2 fer) sera un
    simple second JSON, zéro code.
- **Système d'énergie de Résonance (le champ)** : capability `IResonanceField`,
  `ResonanceFieldManager` (routage machine→émetteur par champ, pas de câble), le **Field
  Emitter** (#4) — réserve de 4000 Osc rechargée en brûlant des Stable Crystals, portée 8, avec
  un **GUI dédié** (jauge de réserve `X/4000 Osc`) — et la consommation d'Osc branchée dans le
  socle des machines (`getOscPerTick`). Le Component Assembler en est le premier client.
- Datagen complet : plus aucun blockstate / modèle / recette / loot table / tag / traduction
  n'est écrit à la main.
- Harnais `GameTest` : 29 tests (cycles, champ, énergie, contrôles, surchauffe, Tuner, démontage), `./gradlew runGameTestServer`.
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
   lui qui produit le template de structure vide dont les tests ont besoin.
3. `./gradlew runGameTestServer` : joue les tests des machines sans interface (~24 s). Le build
   échoue si un test échoue. Une machine n'est considérée finie que quand ses tests passent.
4. `gradle.properties` : la version `neo_version=21.1.172` est celle utilisée pour valider le
   build. Si la résolution de dépendance échoue, vérifier la dernière version patch sur
   https://projects.neoforged.net/neoforged/neoforge.

## Conventions de code à respecter pour les machines suivantes

Elles ne sont pas décoratives — s'en écarter casse le socle générique :

- **Le dernier slot de l'inventaire d'une machine est toujours le slot d'augment.** C'est ce qui
  permet à `AbstractMachineBlockEntity` de gérer l'augment sans code par machine.
- **Les slots de la machine sont ajoutés au menu avant l'inventaire du joueur**, donc l'indice
  d'un slot dans le menu est exactement son indice dans l'inventaire de la block entity.
- **Une entrée qui aura d'autres membres plus tard passe par un tag, pas par un item en dur**
  (voir `ModTags`) — ça transforme une évolution prévue en un ajout de datagen d'une ligne.

## Ce qui n'est PAS encore fait (dans l'ordre à coder)

Suivre `veskorius-design/11-Development-Plan.md`, Phase 1 — c'est la liste ordonnée complète et à
jour (recettes exactes, chiffres d'équilibrage, dépendances entre tâches). Les tâches 1 et 15
(slot d'augment) y sont marquées faites. Les toutes prochaines étapes :

1. **`ResonanceStorageCellItem`** (tâche 7) — une **batterie portable** (item, pas une machine à
   cycle), stocke 8000 Osc. Registre différent de tout ce qui précède.
2. **`ResonanceLocatorItem`** (tâche 8) et **`ResonanceTunerItem`** (tâche 9) — outils. Le Tuner
   devra brancher son toggle surchauffe sur le même `toggleOverheat()` que le bouton `H`.
3. Puis le reste de la Phase 1.

Prochaine étape logique : **la génération de monde (tâche 4)** — les poches de `raw_resonance_crystal`.
C'est le vrai déblocage : aujourd'hui les cristaux ne s'obtiennent qu'en créatif, et la tâche 4
ouvre le gameplay d'exploration + débloque le Locator (tâche 8) et les Storage Cell (tâche 7),
dont le design d'énergie portable est déjà résolu dans `06-Energy.md` (section « Osc portable »).

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
├── energy/                     ← IResonanceField, ResonanceFieldManager, capabilities
├── recipe/                     ← MachineRecipe(Input/Serializer) + RecipeTypes/Serializers
├── datagen/                    ← providers + GatherDataEvent
├── gametest/                   ← tests joués par runGameTestServer
└── tag/ModTags.java

src/main/resources/assets/veskorius/textures/  ← seules ressources écrites à la main
src/generated/resources/                       ← tout le reste, produit par runData
src/main/templates/META-INF/neoforge.mods.toml ← généré vers le jar au build
```
