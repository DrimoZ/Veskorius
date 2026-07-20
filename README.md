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
- **2 machines fonctionnelles** : Resonance Stabilizer (#1, Raw Crystal + Quartz → Stable
  Crystal, 30 s) et Resonance Whetstone (#3, répare un outil de 25 %, 8 s). Block entity, cycle,
  GUI avec barre de progression, orientation, slot d'augment, inventaire persistant et vidé au
  sol quand le bloc est cassé.
- Un socle réutilisable pour les 21 machines restantes : `AbstractMachineBlock`,
  `AbstractMachineBlockEntity`, `AbstractMachineMenu`, `AbstractMachineScreen`. Ajouter une
  machine « standard » = une block entity (cycle), un bloc/menu/écran de 3 méthodes chacun, et
  quelques lignes de datagen.
- Datagen complet : plus aucun blockstate / modèle / recette / loot table / tag / traduction
  n'est écrit à la main.
- Harnais `GameTest` : 9 tests couvrant le cycle des machines, `./gradlew runGameTestServer`.
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

1. **`FieldEmitterBlockEntity` + capability `IResonanceField`** (tâche 5) — remontée avant la
   tâche 2 : le Component Assembler consomme des Osc, donc le système de champ doit exister
   d'abord (voir la note sur la tâche 2 dans le plan).
2. **`ComponentAssemblerBlockEntity`** (tâche 2) — une fois le champ disponible.
3. Puis le reste de la Phase 1 dans l'ordre du plan.

Reste aussi à valider en jeu la partie visuelle du Stabilizer (ouverture du GUI, barre de
progression, orientation) : les GameTest tournent sans client et ne couvrent rien de graphique.

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
├── datagen/                    ← les 8 providers + GatherDataEvent
├── gametest/                   ← tests joués par runGameTestServer
└── tag/ModTags.java

src/main/resources/assets/veskorius/textures/  ← seules ressources écrites à la main
src/generated/resources/                       ← tout le reste, produit par runData
src/main/templates/META-INF/neoforge.mods.toml ← généré vers le jar au build
```
