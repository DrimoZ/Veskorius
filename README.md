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
- **Resonance Stabilizer fonctionnel** (machine #1) : block entity, cycle de 30 s
  (Raw Crystal + Quartz → Stable Crystal), GUI avec barre de progression, orientation, slot
  d'augment, inventaire persistant et vidé au sol quand le bloc est cassé.
- Un socle réutilisable pour les 22 machines restantes : `AbstractMachineBlockEntity`,
  `AbstractMachineMenu`, `AbstractMachineScreen`.
- Datagen complet : plus aucun blockstate / modèle / recette / loot table / tag / traduction
  n'est écrit à la main.
- Textures placeholder (couleur unie) — à remplacer par du vrai pixel art en Phase 6.

Consulter `veskorius-design/13-Registry-Index.md` pour l'état « codé / à coder » de tout le
contenu prévu : c'est le point de départ avant de reprendre le développement, plutôt que de
deviner où on en est.

## Mise en route

1. `./gradlew runClient` (ou via IntelliJ : Gradle > Tasks > neoforge > runClient) pour lancer
   le jeu avec le mod chargé.
2. `./gradlew runData` après tout ajout de bloc/item/recette : régénère `src/generated/resources/`.
   Ce dossier est volontairement ignoré par git — il se reconstruit à partir du code, et le
   versionner créerait des conflits sans valeur.
3. `gradle.properties` : la version `neo_version=21.1.172` est celle utilisée pour valider le
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

1. **Harnais `GameTest`** (noté en bas de la Phase 1). Rien ne valide aujourd'hui le cycle du
   Stabilizer automatiquement. À faire avant la 2e ou 3e machine, pas après.
2. **`ComponentAssemblerBlockEntity`** (tâche 2) — premier vrai test du socle générique, et
   première machine consommant de l'énergie.
3. **`ResonanceWhetstoneBlockEntity`** (tâche 3).
4. Puis les tâches 4 à 15 dans l'ordre du plan.

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
├── datagen/                    ← les 7 providers + GatherDataEvent
└── tag/ModTags.java

src/main/resources/assets/veskorius/textures/  ← seules ressources écrites à la main
src/generated/resources/                       ← tout le reste, produit par runData
src/main/templates/META-INF/neoforge.mods.toml ← généré vers le jar au build
```
