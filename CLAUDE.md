# Veskorius — instructions de travail

Mod Minecraft **NeoForge 1.21.1 / Java 21**, énergie sans câble : un émetteur emplit un volume,
une machine tourne si elle s'y tient. 243 fichiers Java, 58 blocs, 88 items, 176 GameTest.

---

## 1. Les docs de design font foi

`veskorius-design/` est la **source de vérité** pour tout ce qui est gameplay. Le code ne
redéfinit jamais une valeur de jeu : si un chiffre manque, il est dans `05-Machines.md`,
`04-Materials.md` ou `06-Energy.md`. Avant d'inventer une constante, la chercher là.

| Fichier | Sert à |
|---|---|
| `18-Etat-des-lieux.md` | **Ce qui est réellement codé.** À lire avant de croire qu'une fonctionnalité manque |
| `11-Development-Plan.md` | Ce qui reste, ordonné, avec les dépendances |
| `13-Registry-Index.md` | Tous les noms de registre, statut codé / à coder. Évite les collisions |
| `16-Revision-and-Expansion.md` | **Prime sur 01-15 là où il les contredit** |
| `guide-joueur/` | Le guide joueur (fr) · `wiki/` : le wiki GitHub (en) |

Une fonctionnalité livrée met à jour `18-Etat-des-lieux.md` et `13-Registry-Index.md`. Sinon
l'inventaire dérive, et il a déjà dérivé.

---

## 2. Commandes

```
./gradlew compileJava        # boucle courte
./gradlew runData            # REGÉNÈRE src/generated/resources/ — à lancer AVANT les tests
./gradlew audit              # node tools/audit.js : registre ↔ ressources ↔ design
./gradlew runFastGameTests   # 155 tests, 2 Go, ~28 s — le run de travail
./gradlew runWorldGameTests  # 21 donjons, 4 Go, ~17 s — dès qu'on touche aux structures
./gradlew runAllGameTests    # les deux, en deux JVM — avant un commit
./gradlew runClient          # validation visuelle (GUI, modèles, textures)
```

Ajouter `--offline` quand le réseau n'est pas nécessaire : c'est plus rapide et ça a déjà
sauvé des sessions.

**`runGameTestServer` n'existe plus, et ce n'est pas un oubli.** Les 158 tests réunis dans une
seule JVM passent tous, puis le serveur reste collé à 4,4 Go sans imprimer son résumé,
indéfiniment. Ce qui sature n'est aucun test en particulier, c'est ce qu'un processus
*accumule*. La seule borne est de ne pas tout mettre dans le même. Le filtrage passe par le
**namespace** — seule prise offerte par GameTestServer — d'où `veskorius` d'un côté et
`veskorius_world` de l'autre.

Incompressible : ~75 s de démarrage par processus. Aucune optimisation de test n'y touchera ;
la seule réponse est de ne lancer que le sous-ensemble concerné.

---

## 3. Ce qui ne s'édite pas à la main

**`src/generated/resources/**`** — 708 JSON produits par `runData`, qui vide même son `.cache`
à chaque lancement. Toute édition manuelle est écrasée au run suivant. Pour changer une
recette, un modèle, une loot table ou une traduction : **modifier le DataProvider Java** dans
`com.veskorius.datagen`, puis `./gradlew runData`.

Blockstates, modèles d'item, recettes, loot tables, tags, avancements, traductions (en + fr),
worldgen et structures sont **tous** dérivés du code — donc impossibles à désynchroniser des
registres. C'est délibéré, ne pas contourner.

Les textures sont **dessinées par code** (`tools/*-textures/*.js`, hors build Gradle). Les
fichiers préfixés `_` que ces scripts écrivent sont des planches de contrôle : à supprimer
avant de committer.

---

## 4. L'audit attrape ce que les tests ne peuvent pas

Les GameTest tournent dans le jar : ils ne voient ni `src/generated`, ni `veskorius-design`.
Or la famille de panne la plus coûteuse de ce dépôt vit exactement là — un objet enregistré
sans modèle s'affiche en cube violet, un bloc sans traduction montre sa clé, une entrée absente
de l'index de registres n'existe pour personne. **Rien ne plante.** Le mod se charge, les tests
passent, et le défaut se découvre en jeu — ou jamais.

D'où `./gradlew audit`, qui *échoue* au lieu de prévenir. Un hook `Stop` le lance
automatiquement en fin de tour (`.claude/hooks/audit-on-stop.js`).

---

## 5. Conventions de code

- **Les commentaires disent POURQUOI, pas quoi.** Le dépôt en est plein et c'est sa principale
  documentation : chaque garde inhabituelle explique la panne qu'elle a coûtée. Écrire dans ce
  registre — un commentaire qui paraphrase la ligne suivante est du bruit à supprimer.
- **Français** pour les commentaires, la Javadoc et les messages de commit. **Anglais** pour
  `en_us.json`, le wiki et la page CurseForge.
- **Une seule implémentation d'un mécanisme.** Quand deux endroits font presque la même chose,
  factoriser le mécanisme et paramétrer ce qui diffère — jamais dupliquer.
- Toute machine à cycle hérite d'`AbstractMachineBlockEntity` (progression, slot d'augment,
  On/Off, mode redstone, surchauffe, faces configurables, bande harmonique). Une machine
  input→output n'écrit **aucun** code de recette : `AbstractProcessingMachineBlockEntity` +
  un `RecipeType` + des JSON.
- **14 types de recette data-driven** : les valeurs se changent par datapack, sans recompiler.
  Une recette de plus de 9 ingrédients est écartée silencieusement au chargement du monde.
- `gradle.properties` est lu en **ISO-8859-1** : pas d'accent dedans (`é` sinon).

## 6. Le cycle de développement

Tout développement se termine par : **valider** (`runData` → `runAllGameTests`, et `runClient`
si le visuel change) → **auditer** la cohérence doc ↔ code → **committer**, sans demander.
Le skill `/fin-de-dev` déroule ce cycle.

---

## 7. L'outillage Claude Code de ce dépôt

| | |
|---|---|
| `.claude/hooks/audit-on-stop.js` | Lance `tools/audit.js` en fin de tour et bloque si la cohérence est cassée |
| `.claude/hooks/no-generated-edit.js` | Refuse les écritures dans `src/generated/resources/`, Edit comme `sed`/`perl`. Testé par `node .claude/hooks/no-generated-edit.test.js` |
| `/fin-de-dev` | Le cycle de clôture : datagen → tests → audit → docs → commit |
| `/nouveau-bloc` | Les sept endroits obligatoires quand un bloc apparaît |
| Sous-agent `coherence-design` | Confronte les valeurs du diff aux docs de conception (ce que l'audit ne voit pas) |
| Sous-agent `gametest-author` | Écrit un GameTest dans le bon namespace, donc le bon run |

`.claude/settings.json` est partagé ; `.claude/settings.local.json` est personnel et ignoré
par git.
