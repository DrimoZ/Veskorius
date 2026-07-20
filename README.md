# Veskorius (NeoForge 1.21.1)

Squelette de mod fonctionnel — pas une démo, un vrai point de départ. Ce qui est codé ici
compile et se lance ; ce qui manque est listé en bas, dans l'ordre où le coder.

**Source de vérité pour tout ce qui est design/gameplay : `../veskorius-design/`.** Ce dossier
`veskorius-mod/` ne contient que du code et ne redéfinit jamais une valeur de jeu — un ancien
`TECH-SPEC.md` faisait doublon avec `veskorius-design/` (et avait fini par diverger : 6 machines
listées ici contre 23 dans la conception à jour) ; il a été supprimé pour ne garder qu'une seule
référence. Si une valeur manque ici, elle est dans `veskorius-design/05-Machines.md` ou
`04-Materials.md`, pas à redéfinir dans ce dossier.

## Contenu actuel

- Projet Gradle complet (ModDevGradle, NeoForge 1.21.1, Java 21).
- 4 items enregistrés : `raw_resonance_crystal`, `stable_resonance_crystal`,
  `refined_resonance_crystal`, `resonance_component`.
- 1 bloc enregistré : `resonance_stabilizer` — pour l'instant un bloc simple (se pose, se
  casse, drop lui-même). Aucune logique de craft/stabilisation encore.
- Un onglet créatif dédié ("Veskorius") contenant les 5 objets ci-dessus.
- Textures placeholder (16×16, couleur unie) pour ne pas avoir de texture manquante violette/
  noire au premier lancement — à remplacer par du vrai pixel art dans
  `src/main/resources/assets/veskorius/textures/`.

Ces 5 éléments correspondent exactement aux entrées #1 (Resonance Stabilizer) et à 4 des
matériaux du groupe 1 dans `veskorius-design/13-Registry-Index.md`, qui liste l'état "codé /
à coder" de tout le contenu prévu — c'est le point de départ à consulter avant de reprendre le
développement, plutôt que de deviner où on en est.

## Mise en route

1. Ce dépôt n'inclut pas le Gradle Wrapper binaire (pas de réseau vers `services.gradle.org`
   dans l'environnement qui l'a généré). Deux options :
   - Ouvrir le dossier dans **IntelliJ IDEA** avec le plugin Gradle : il proposera de générer
     le wrapper automatiquement au premier import.
   - Ou, si Gradle est installé en local : `gradle wrapper --gradle-version 8.10` à la racine.
2. `./gradlew runClient` (ou via IntelliJ : Gradle > Tasks > neoforge > runClient) pour lancer
   le jeu avec le mod chargé.
3. Vérifier `gradle.properties` : la version `neo_version=21.1.172` est celle documentée au
   moment de la rédaction — si le build échoue avec une erreur de résolution de dépendance,
   vérifier la dernière version patch sur https://projects.neoforged.net/neoforged/neoforge.

## Ce qui n'est PAS encore fait (dans l'ordre à coder)

Suivre `veskorius-design/11-Development-Plan.md`, Phase 1, tâches 1 à 15 — c'est la liste
ordonnée complète et à jour (recettes exactes, chiffres d'équilibrage, dépendances entre
tâches). Résumé des toutes premières étapes :

1. **Logique du Resonance Stabilizer.** Actuellement un bloc décoratif. Il faut : une classe
   `ResonanceStabilizerBlockEntity` (cycle de 30s, input Raw Resonance Crystal + Quartz →
   output Stable Resonance Crystal, voir `veskorius-design/05-Machines.md`, machine #1) + un
   Menu/Screen pour le GUI. Pattern de référence : `references/neoforge-api.md` du skill
   `minecraft-mod-dev` (sections Block Entity et Menu/GUI).
2. **Recette de craft du Resonance Stabilizer lui-même**, débloquée par un `Advancement` avec
   `RecipeUnlockedTrigger` plutôt qu'un flag custom — le mécanisme exact est spécifié dans
   `veskorius-design/12-UX-and-Advancements.md`, pas à réinventer.
3. **Les 22 autres machines**, une par une, dans l'ordre de
   `veskorius-design/11-Development-Plan.md`.
4. **Datagen** (`ModBlockStateProvider`, `ModItemModelProvider`, `ModRecipeProvider`,
   `ModLootTableProvider`) une fois qu'il y a plus de 5-6 objets à maintenir à la main.
5. **Vraies textures.** Les PNG actuels sont des carrés de couleur unie, juste pour éviter
   l'écran violet/noir de texture manquante.

## Structure

```
src/main/java/com/veskorius/
├── Veskorius.java          ← point d'entrée, enregistrement, onglet créatif
├── item/ModItems.java      ← les 4 items matériaux + BlockItem du Stabilizer
└── block/ModBlocks.java    ← le bloc Resonance Stabilizer (squelette)

src/main/resources/
├── assets/veskorius/       ← blockstates, models, textures, lang
└── data/veskorius/         ← loot tables

src/main/templates/META-INF/neoforge.mods.toml  ← généré vers le jar au build
```
