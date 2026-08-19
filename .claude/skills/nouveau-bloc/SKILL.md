---
name: nouveau-bloc
description: Ajoute un bloc (ou une machine) à Veskorius en touchant les sept endroits obligatoires — registre, item, blockstate, modèle, butin, tag d'outil, traductions, index de registres, test. Utiliser dès qu'un nouveau bloc doit exister.
disable-model-invocation: true
---

# Ajouter un bloc

Un bloc n'existe pas parce qu'il est enregistré : il existe quand `./gradlew audit` se tait.
Sept endroits au minimum, et en oublier un ne casse **rien** — le mod se charge, les tests
passent, et le bloc s'affiche en cube violet ou disparaît au minage.

## 0. Avant d'écrire

Le nom et les valeurs viennent des docs, pas de l'inspiration :

- **Le nom de registre** est déjà réservé dans `veskorius-design/13-Registry-Index.md`.
  L'y chercher ; s'il n'y est pas, l'y ajouter avant de coder — c'est ce qui évite les
  collisions.
- **Les chiffres** (temps de cycle, Osc/tick, portée, dureté) sont dans `05-Machines.md`,
  `04-Materials.md` ou `06-Energy.md`. Le code ne définit jamais une valeur de gameplay.
- Vérifier dans `18-Etat-des-lieux.md` que le bloc n'est pas déjà codé sous un autre nom.

## 1. Le registre

`src/main/java/com/veskorius/block/ModBlocks.java`. Trois formes coexistent, toutes reconnues
par l'audit :

- `BLOCKS.registerBlock("nom", Ctor::new, properties)` — le cas courant ;
- `BLOCKS.register("nom", …)` — pour les blocs connectés (châssis) ;
- `BLOCKS.registerSimpleBlock("nom", properties)` — bloc nu, sans classe.

## 2. L'item du bloc

`src/main/java/com/veskorius/item/ModItems.java` :
`ITEMS.registerSimpleBlockItem(ModBlocks.LE_BLOC)`. Sans lui, le bloc n'est ni ramassable ni
craftable. Puis l'ajouter à l'onglet créatif dans `Veskorius.java`.

## 3. Une machine à cycle ? Ne rien réécrire

Le socle existe et couvre déjà progression, slot d'augment (Catalyst Core +15 %), On/Off,
mode redstone, surchauffe, faces configurables et bande harmonique :

- `AbstractMachineBlock` / `AbstractMachineBlockEntity` / `AbstractMachineMenu` /
  `AbstractMachineScreen` — trois méthodes chacun à surcharger ;
- une machine **input → output** n'écrit aucun code de recette : elle hérite
  d'`AbstractProcessingMachineBlockEntity`, déclare un `RecipeType`, et ses recettes sont des
  **JSON** modifiables par datapack. Le Crystal Crusher a réutilisé ce socle sans le modifier.

Ne pas dupliquer un mécanisme existant : le factoriser et paramétrer ce qui diffère.

## 4. La datagen — jamais de JSON à la main

`src/generated/resources/` est écrasé à chaque `runData`. Tout passe par
`src/main/java/com/veskorius/datagen/` :

| Provider | Ce qu'il faut y ajouter |
|---|---|
| `ModBlockStateProvider` | blockstate + modèle de bloc — sinon **cube violet** |
| `ModItemModelProvider` | modèle de l'item |
| `ModBlockLootProvider` | table de butin — sinon le bloc **disparaît au minage** |
| `ModBlockTagsProvider` | tag `mineable/*` + niveau d'outil, obligatoire si le bloc exige le bon outil |
| `ModItemTagsProvider` | si l'item entre dans une recette par tag |
| `ModLanguageProvider` + `ModFrenchLangProvider` | nom **en** et **fr** — les deux, l'audit vérifie les deux |
| `ModRecipeProvider` | recette de **construction** (celle de fonctionnement est un JSON de datapack) |

Puis `./gradlew runData --offline`.

## 5. La texture

Dessinée par code, pas éditée en binaire :
`node tools/block-textures/genmarble.js src/main/resources/assets/veskorius/textures/block`.
Supprimer les planches de contrôle préfixées `_` avant de committer.

## 6. Le test

Ajouter un GameTest dans la classe pertinente de `com.veskorius.gametest` :

- comportement de machine, énergie, harmoniques, automation → namespace `veskorius`,
  vérifié par `runFastGameTests` ;
- structure ou génération → `WorldGenTests.NAMESPACE` (`veskorius_world`),
  vérifié par `runWorldGameTests`.

Se tromper de classe envoie un test de donjon dans le run 2 Go, ou l'inverse.

## 7. Les docs

`13-Registry-Index.md` (passer en « codé ») et `18-Etat-des-lieux.md` (**chiffres compris** :
l'audit compare le nombre de blocs et d'items annoncé au nombre réel et échoue s'il ment).

## 8. Clôturer

Enchaîner avec `/fin-de-dev`.
