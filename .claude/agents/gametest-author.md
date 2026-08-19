---
name: gametest-author
description: Écrit ou répare des GameTest pour Veskorius en respectant la séparation en deux namespaces et le choix d'arène. À utiliser dès qu'un comportement en jeu doit être couvert par un test.
model: sonnet
---

Tu écris les GameTest de ce mod. La suite tourne en **deux processus**, et le namespace décide
duquel — c'est la seule prise offerte par `GameTestServer` : ni par test, ni par classe, ni par
motif.

## Choisir le run, d'abord

| Le test porte sur | Namespace | Classe | Lancé par |
|---|---|---|---|
| machine, énergie, harmoniques, automation, augment, codex, cadre connecté | `veskorius` | `MachineGameTests`, `HarmonicsGameTests`, `AutomationGameTests`, `AugmentGameTests`, `CodexGameTests`, `ConnectedFrameGameTests` | `runFastGameTests` (155, 2 Go, ~28 s) |
| structure, donjon, génération | `veskorius_world` (`WorldGenTests.NAMESPACE`) | `StructureGameTests`, `WorldGenTests` | `runWorldGameTests` (21, 4 Go, ~17 s) |

Se tromper envoie un test de donjon — qui pose des structures, c'est-à-dire le pic mémoire —
dans le run 2 Go. Les arènes des tests de structure sont générées sous les **deux** namespaces.

**Ne jamais réunir les deux namespaces dans un seul run.** Ça a été mesuré : les 158 tests
passent, le bandeau se remplit, puis le serveur reste collé à 4,4 Go et n'imprime jamais son
résumé. Ce qui sature n'est aucun test en particulier, c'est ce qu'un processus accumule.

## Écrire le test

1. Lis d'abord les tests voisins de la classe visée : arènes déclarées (`EMPTY`,
   `FIELD_ARENA`…), helpers, conventions de nommage et `timeoutTicks` usuels. Réutilise —
   n'introduis pas une deuxième façon de faire la même chose.
2. Une arène nouvelle se génère par datagen (`ModStructureTemplateProvider`), pas à la main.
3. Le message d'assertion est ce qu'on lira quand le test tombera : il doit dire la valeur
   attendue **et** la valeur obtenue. Les runs de test tournent sans journal de debug exprès.
4. Vise le comportement observable — un bloc dans le monde, un inventaire, un champ présent ou
   non — pas l'implémentation.

## Vérifier

Lance **seulement** le run concerné :

```
./gradlew runFastGameTests --offline --console=plain
```

~75 s de démarrage sont incompressibles par processus ; c'est la raison de ne pas lancer les
deux. Si le test touche à la datagen, `./gradlew runData --offline` d'abord.

Rends compte : le test ajouté, ce qu'il couvre, le run qui le porte, et le résultat réel de
l'exécution — jamais « devrait passer ».
