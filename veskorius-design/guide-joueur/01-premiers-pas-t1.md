# 1 — Premiers pas (T1) : la boucle sans énergie

Au tier 1, **aucune énergie à gérer**. Les machines de départ sont **autonomes** : elles tournent
toutes seules, posées n'importe où. Le seul but du T1 : transformer le cristal brut en matériaux, et
trouver une ruine pour ouvrir le T2.

## Trouver le cristal

Le **cristal brut** (`raw_resonance_crystal`) se génère en **poches** souterraines, entre **Y 0 et
−20**. Elles sont **rares** (un peu plus rares que le diamant) — mais une poche est un gros nœud
(plusieurs cristaux), donc rare ne veut pas dire chiche.

Le **tell visuel** : la poche est enrobée d'une coquille de **pierre veinée**
(`resonance_veined_stone`), reconnaissable. Quand vous la voyez en creusant, vous y êtes.

- Minez les **amas de cristal** (`resonance_crystal_cluster`) : ils lâchent du cristal brut.
- Sur la coquille, ~15 % des blocs sont des **croûtes de flux brossables** : passez la **brosse
  vanilla** dessus pour récupérer du **Raw Flux Deposit** (une alternative au Quartz, voir plus bas).

> ⚠️ **Le cristal brut est instable si on le porte trop longtemps sans le traiter.** Après ~2 min
> dans l'inventaire, il commence à infliger de légers dégâts. Stabilisez-le sans traîner.

## La chaîne de fabrication T1

Toutes ces machines se fabriquent **sans blueprint** (le T1 n'est pas gaté) et tournent **sans
champ**.

### Resonance Stabilizer — le cœur du T1
- **Recette de fonctionnement** : `Raw Crystal + Quartz` → **1 Stable Crystal**, ~30 s.
- Le **Quartz** est remplaçable **1:1** par du **Raw Flux Deposit** (brossé sur les poches) — c'est
  le même tag d'entrée.
- **Autonome** : aucune énergie requise. Il ne *crée* pas d'énergie, il rend juste utilisable
  l'énergie latente du cristal.
- Le **Stable Crystal** est la brique de tout le reste — et, plus tard, le **carburant** de votre
  premier champ (chaque Stable Crystal = 4000 Osc quand on le brûle dans un Field Emitter).

### Crystal Crusher
- `1 Stable Crystal` → **3 Resonance Dust**, ~10 s. **Autonome.**
- La poussière ouvre une **branche alternative** pour fabriquer des Resonance Component
  (`3 poussière + 2 fer`), utile si vous êtes court en cristaux.

### Resonance Whetstone (meule)
- Répare un **outil endommagé** de **25 %** par cycle (catalyseur : Stable Crystal), ~8 s.
  **Autonome.** Un usage pratique du cristal dès le début.

### Component Assembler — votre première machine « branchée »
- **Recette** : `Stable Crystal + 2 Iron` → **2 Resonance Component**, ~5 s.
- ⚠️ **Différence clé : l'Assembler a besoin d'un champ** (3 Osc/tick). Vous pouvez le **fabriquer**
  tôt, mais il ne **tournera** qu'une fois votre premier champ posé (T2). C'est votre « premier
  client » du réseau.
- Vos **tout premiers** Component ne viennent donc pas de l'Assembler mais du **coffre de
  l'Avant-poste** (amorçage garanti, voir [chapitre 2](02-le-champ-t2.md)). Ensuite, l'Assembler en
  produit autant que vous voulez, dans votre champ.

## Le fil rouge vers le T2

En explorant/creusant, vous tomberez sur des **ruines** :
- une **Habitation Modeste** : du loot quotidien et un **fragment de Codex** — parfois un **indice**
  qui pointe vers « un atelier à réveiller » ;
- un **Avant-poste** : c'est **la porte du T2** (chapitre suivant).

Aucun outil de repérage n'est disponible en T1 (le Locator est un objet T2) : le premier Avant-poste
se trouve **à l'exploration**, comme dans un donjon vanilla.

## Ce que le T1 vous apprend

- La boucle **miner → stabiliser → fabriquer**.
- Le réflexe visuel : **pierre veinée = ressource**.
- L'idée que le cristal est de l'**énergie en réserve** — ce qui prépare le chapitre du champ.

➡️ Suite : **[Le champ (T2)](02-le-champ-t2.md)**.
