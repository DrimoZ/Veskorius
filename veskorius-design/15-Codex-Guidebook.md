# 15 — Codex de Résonance (manuel en jeu)

Ajouté le 2026-07-22. Un manuel en jeu qui **documente les machines, la progression, le monde et le
lore**, et qui **s'écrit tout seul** à mesure que le joueur découvre le mod. C'est l'outil
d'onboarding et de progression narrative que ni JEI (recettes seules) ni les advancements (toasts
fugaces) ne fournissent. Décision de conception validée avec le porteur du projet le 2026-07-22 :
**Codex custom** (pas de dépendance Patchouli) et **auto-généré** (donné tôt, pages débloquées au fil
de la progression).

## Concept

Le mod raconte la **restauration d'un savoir perdu** (`02-Lore.md`). Le Codex est la connaissance du
joueur qui **se reconstitue** : ramasser son premier cristal, réveiller une console, poser une
machine, croiser la faune ou lire un fragment ajoute des pages. Il **absorbe** le système de lore
existant (`codex_fragment` / `CodexEntries`, `08-Structures.md`) plutôt que de le dupliquer : les
fragments trouvés en ruine deviennent les pages de la catégorie *Lore*.

## Architecture

- **Objet** `resonance_codex` (item, non empilable). Clic droit → ouvre un **écran custom**
  (`CodexScreen`), pas un menu à conteneur. Aucun asset de texture requis : le modèle reprend le livre
  vanilla (`item/book`), et le GUI se dessine au `GuiGraphics` (panneaux + police + icônes d'objets).
  L'objet n'est qu'une **clé d'ouverture** : il ne porte aucun état.
- **Catalogue d'entrées** défini en **code** (`codex/CodexRegistry`), chaque entrée portant : un id
  (`ResourceLocation`), une catégorie, une icône (`ItemLike`), un ordre, et une **condition de
  déblocage**. Le **texte** vit dans les fichiers de langue (en_us canonique + fr_fr), clés
  `codex.<ns>.<path>.title/.text` — **exactement la convention des fragments** (`CodexFragmentItem`),
  donc les entrées *Lore* réutilisent le texte des fragments sans le réécrire.
  - *Choix v1 assumé* : le catalogue est en code (les deux côtés client/serveur le partagent). La
    surchargeabilité *datapack* du catalogue (registre datapack synchronisé) est différée. Le **texte**
    reste, lui, de la donnée (langue), donc traduisible/surchargeable dès maintenant.
- **État de déblocage stocké SUR LE JOUEUR** (`ModAttachments.CODEX_UNLOCKS`, un
  `Set<ResourceLocation>`, `copyOnDeath`), **pas sur l'objet**. Conséquences voulues (corrigé le
  2026-07-23 après une première version qui stockait sur l'objet) :
  - la connaissance **s'accumule même quand le Codex n'est pas porté** (rangé dans un coffre, jamais
    ramassé) — c'est la propriété que demandait le porteur du projet ;
  - elle **survit à la mort** et à la perte de l'objet ; un Codex neuf (recraft, second exemplaire)
    affiche immédiatement tout le savoir du joueur ;
  - **un seul point de vérité** par joueur, quel que soit le nombre d'exemplaires.
- **Synchronisation** serveur → client par paquet (`CodexSyncPayload`, `network/`) : poussé à la
  connexion, au respawn, au changement de dimension, et à chaque nouveau déblocage. Le client garde un
  cache (`ClientCodexData`) que lit le `CodexScreen`. Les entrées `ALWAYS` ne transitent jamais (elles
  comptent débloquées d'office des deux côtés).

## Déclencheurs de déblocage (`CodexUnlock`)

| Type | Débloque quand… | Détection |
|---|---|---|
| `ALWAYS` | dès le départ (intro) | — |
| `ITEM(x)` | le joueur possède l'objet `x` | scan d'inventaire throttlé (1×/s) **+ au craft** (`ItemCraftedEvent`, comble le cas « crafter puis poser dans la seconde ») |
| `ADVANCEMENT(a)` | le joueur gagne l'advancement `a` | `AdvancementEvent.AdvancementEarnEvent` (immédiat) **+ reconstruction** depuis les advancements réellement possédés (au login et au scan) — auto-réparant |
| `FRAGMENT` | le joueur **lit** le `codex_fragment` d'id égal | hook dans `CodexFragmentItem.use` |

À chaque nouvelle page : retour discret (message barre d'action « Nouvelle entrée : X » + son de page).
Le Codex est **donné à la première connexion** (`PlayerLoggedInEvent`, drapeau persistant une fois) ;
une **recette de secours** (Livre + Cristal Brut) permet d'en refaire un s'il est perdu — comme l'état
vit sur le joueur, l'exemplaire neuf est immédiatement à jour.

## Catégories et contenu v1

Introduction · Cristaux & Raffinage · Champs & Énergie · Machines · Monde & Structures · Faune ·
Lore · Progression. Le contenu v1 couvre toute la **boucle T1-T2** (les 15 tâches de la Phase 1) :
chaque machine active, chaque cristal, les champs, la Storage Cell, le Locator, la pierre veinée, le
dépôt de flux, l'Avant-poste, le Fileur, le Custode, les deux paliers, et 3 pages de lore réutilisant
les fragments existants. Les tiers T3-T5 ajouteront leurs pages au fur et à mesure qu'ils seront codés
(une entrée par machine/structure/entité, même motif).

## Convivialité (`CodexScreen`)

- Catégories à gauche, entrées au centre, page à droite. **Défilement à la molette** si une catégorie
  déborde. Compteur global « x/total connues » et par catégorie « x/total découvertes ».
- **Les entrées verrouillées restent visibles** (« ??? ») et sont **cliquables** : elles ouvrent une
  page qui dit *comment* les débloquer (« Verrouillé — obtenir : ‹objet› », « progressez », « lisez le
  fragment correspondant »). Guide la progression sans tout dévoiler.

## Ce qui est testé, ce qui ne l'est pas

Testé par GameTest (logique de déblocage, statique donc testable, 7 tests) : ALWAYS débloquée d'office ;
idempotence ; **accumulation sans porter le Codex** ; déblocage par ITEM / ADVANCEMENT / FRAGMENT (et
le fait que le chemin FRAGMENT ne débloque que du lore) ; compteur de catégorie ; **intégrité du
catalogue** (ids uniques, cible cohérente avec le type, aucune catégorie vide). **Non testé** (visuel,
comme les autres écrans) : le rendu du `CodexScreen` et le trajet réseau, vérifiés en `runClient`.

## Suites (différées, à ne pas oublier)

- **Marqueur « NEW »** sur les entrées non encore ouvertes (badge dans le GUI, effacé à la lecture).
  Demande un second ensemble « vues » (attachment) + un paquet client→serveur « marquer vue ». Proposé,
  non fait — le toast + le son + le compteur suffisent en v1.
- **Catalogue surchargeable par datapack** : migrer le catalogue code → registre datapack synchronisé
  quand un besoin d'addon/modpack apparaît. Le texte est déjà data.
- **Recettes intégrées à la page** : afficher la recette de craft dans l'entrée d'une machine (JEI la
  montre déjà ; intégration au Codex = confort).
- **Pages T3-T5** : ajoutées avec chaque phase (une par machine/structure/entité, même motif).
