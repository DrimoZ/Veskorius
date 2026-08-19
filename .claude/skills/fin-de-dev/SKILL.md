---
name: fin-de-dev
description: Clôture un développement sur Veskorius — régénère la datagen, lance la suite GameTest, audite la cohérence doc↔code, met à jour l'inventaire, puis committe. À lancer quand une fonctionnalité est écrite et compile.
disable-model-invocation: true
---

# Fin de développement

Le cycle de clôture du dépôt. Il ne s'arrête pas à « les tests passent » : la famille de panne
la plus coûteuse ici ne fait échouer aucun test.

Dérouler les étapes **dans l'ordre**, sans en sauter. Si une étape échoue, corriger puis
reprendre **à cette étape**, pas au début.

## 1. Régénérer la datagen

```
./gradlew runData --offline --console=plain
```

Obligatoire même si on n'a « touché qu'au Java » : blockstates, modèles, recettes, loot,
tags, avancements et traductions en dérivent tous. Sans ça, les étapes 2 et 3 mesurent l'état
précédent.

Vérifier ensuite `git status` : les JSON qui apparaissent sont attendus. Un JSON **disparu**
qu'on n'a pas voulu supprimer est un signal — un registre a changé de nom quelque part.

## 2. Valider

```
./gradlew runFastGameTests --offline --console=plain
```

Puis, **seulement si le diff touche `worldgen/`, `ModStructurePieceProvider` ou `Masonry`** :

```
./gradlew runWorldGameTests --offline --console=plain
```

Sinon on paie 17 s + 75 s de démarrage pour rien. Avant un commit qui touche à beaucoup de
choses, `runAllGameTests` lance les deux en deux JVM.

Ne jamais tenter de réunir les deux namespaces dans un seul run : tous les tests passent,
puis le serveur ne rend jamais la main.

## 3. Auditer

```
./gradlew audit
```

Il vérifie ce qu'aucun GameTest ne peut voir, parce qu'ils tournent dans le jar :

- bloc sans blockstate (cube violet) ou sans table de butin (disparaît au minage) ;
- objet sans modèle ;
- bloc qui exige le bon outil sans figurer dans un tag `mineable/*` ;
- clé sans traduction française, bloc ou objet sans nom anglais ;
- entrée absente de `13-Registry-Index.md` ;
- **chiffres de `18-Etat-des-lieux.md` qui ne correspondent plus au code**.

Le dernier point rend l'étape 4 non facultative : l'audit échoue tant que l'inventaire ment.

## 4. Mettre les docs à jour

Le code n'est pas la source de vérité du gameplay, `veskorius-design/` l'est.

- **`18-Etat-des-lieux.md`** — l'inventaire de ce qui est codé, chiffres compris (nombre de
  blocs, d'items, de fichiers Java, de GameTest).
- **`13-Registry-Index.md`** — passer les entrées concernées de « à coder » à « codé ».
- **`11-Development-Plan.md`** — cocher la tâche, et noter ce qui a bougé dans les dépendances.
- Si une valeur de gameplay a changé (temps de cycle, Osc/tick, portée), corriger le doc
  **source** (`05-Machines.md`, `04-Materials.md`, `06-Energy.md`) : le code ne définit
  jamais un chiffre de jeu tout seul.
- Si le comportement joueur change, `guide-joueur/` (fr) et `wiki/` (en) suivent.

## 5. Validation visuelle — quand, et seulement quand

Lancer `./gradlew runClient --offline` si le diff touche à un GUI, un modèle, une texture ou
un rendu. Un test ne voit rien de tout ça : les GameTest tournent sans client, et c'est
précisément le client qui casse dans ces cas-là.

Sinon, sauter cette étape — c'est plusieurs minutes.

## 6. Committer

Messages en **français**, forme `type(portée): ce que ça change`, comme l'historique :

```
feat(purifier): mode surchauffe, 45 s → 22 s
fix(cadre): les coins du pied d'un bloc, et le verre peint deux fois
refactor(cadre): une seule implementation, plus l'arete concave
docs: consigner la validation en jeu du Tuner et du demontage
```

Le corps du message dit **pourquoi**, et surtout ce qui a été *mesuré* — c'est la convention
du dépôt, visible dans `build.gradle` comme dans les commits.

Avant `git add` : supprimer les planches de contrôle laissées par les générateurs de texture
(fichiers préfixés `_`), ce ne sont pas des assets.

Committer sans demander confirmation — c'est le fonctionnement attendu ici.
