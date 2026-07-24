# 5 — Le monde, les créatures, les structures

## Génération : les poches de cristal

- **Poches de cristal brut** : sous terre, **Y 0 à −20**, **rares** (un peu plus que le diamant).
- Chaque poche = un amas de **`resonance_crystal_cluster`** enrobé d'une **coquille de pierre
  veinée** (`resonance_veined_stone`) — le **tell visuel**.
- ~15 % de la coquille = des **croûtes de flux brossables** (`raw_flux_deposit` à la **brosse**).

C'est toute la matière première du T1. Minez et brossez.

## La pierre veinée et le spore

La **pierre veinée exposée**, en faible luminosité, fait **pousser un `resonance_spore`** sur ses
faces (façon glow lichen). **Clic droit** pour le récolter **sans casser la pierre** — il **repousse**
ensuite. Le spore sert à **reproduire le Fileur de Cristal** : la reproduction devient donc jouable
en survie (pas seulement en créatif).

## Les créatures

### Fileur de Cristal (`crystal_strider`)
- **Faune neutre** des profondeurs : il **ne combat jamais** et **fuit** quand on le blesse.
- **10 PV.** Spawn souterrain (Overworld, bande Y 0/−40).
- **Traite** : **clic droit à main nue** → **1 Raw Crystal** (cooldown 5 min). Une source de cristal
  **renouvelable et sans minage** — mais lente.
- **Reproduction** : au **`resonance_spore`** (élevage vanilla, 2 adultes → 1 bébé).
- Sert aussi de **condition** au **Crystal Roost** (il doit être à moins de 6 blocs pour qu'il
  produise).

### Custode (`custode`)
- **Garde réactif** : **30 PV / 6 dégâts**. Il ne cible un joueur **qu'à 6 blocs** (ou s'il se fait
  frapper) — **jamais agressif à distance**. Restez à l'écart, il vous ignore.
- **Intégré à la génération de l'Avant-poste** (persistant, il ne despawn pas) — il **garde un
  site**, il ne patrouille pas un territoire. Il **réagit** aussi si vous cassez une machine du site.
- **Drop** : 2-4 **`custode_alloy_fragment`**, un **substitut 1:1 du fer** dans toutes les recettes
  Veskorius (via un tag). Le combat est donc **récompensé** — mais **optionnel** (le fer reste
  minable normalement).

## Les structures

Ce sont de **vraies structures vanilla** (système jigsaw) : `/locate` fonctionne, et le **mode
Structures du Locator** les trouve.

### Habitation Modeste
- La plus commune. Souterraine. **Aucune machine, aucun garde.**
- **Loot** : rations, matériaux T1 communs, et **un fragment de Codex** (lore) — parfois un
  **indice** qui pointe vers « un atelier à réveiller » (l'Avant-poste).

### Avant-poste — la porte du T2
- Souterrain, **gardé par un Custode**.
- Contient une **console d'attunement** : clic droit → **blueprint T2**.
- **Coffre** : **amorçage garanti** (**4 Resonance Component + 2 Gold** = un Field Emitter) +
  matériaux d'appoint. Voir [chapitre 2](02-le-champ-t2.md).

> **Comment trouver le premier Avant-poste ?** Le Locator est un objet **T2** (il faut le blueprint
> pour le fabriquer), donc le **tout premier** Avant-poste se trouve **en explorant/creusant**, comme
> un donjon — aiguillé par l'indice des Habitations. Une fois le T2 acquis, le Locator (mode
> Structures) et `/locate` trouvent les suivants.

*(Les structures profondes — Sigma Laboratory, Archive, Faille — et le biome profond arrivent en
Phase 2 et au-delà. Elles ne sont pas encore en jeu.)*

## Ce que chaque tier change dans la lecture du monde

| Tier | Ce que vous « voyez » de neuf |
|---|---|
| **T1** | Vous reconnaissez les **poches de cristal** (pierre veinée). |
| **T2** | Le **Locator** révèle sources de résonance **et structures** ; les **couleurs** harmoniques deviennent lisibles. |

*(T3+ : à venir.)*

⬅️ Retour au **[sommaire](README.md)**.
