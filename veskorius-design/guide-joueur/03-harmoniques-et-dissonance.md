# 3 — Harmoniques & Dissonance : le système-signature

C'est **le** système qui distingue Veskorius. Le pilier « pas de câbles » vous prive d'un service que
les câbles rendaient : **choisir ce qu'on alimente**. Les **bandes harmoniques** sont cette réponse —
et le **dérèglement** du réseau rejoue, à petite échelle, la sur-résonance qui a causé l'Effondrement.

> **Tout ce système est optionnel et réglable**, jusqu'à un **interrupteur maître** : un modpack peut
> le désactiver entièrement (le mod redevient « champ simple »). Et le **T1 n'y touche jamais**.

## Le modèle mental, en une phrase

> **Un champ a une couleur. Une machine écoute sur une couleur. Même couleur = propre et rapide.
> Couleur différente = ça marche quand même, mais ça coûte plus cher et ça grince.**

## Les bandes = des couleurs

Trois bandes, identifiées par une **couleur** (pas un chiffre à mémoriser) :

| Bande | Couleur | Disponibilité |
|---|---|---|
| Fondamentale | violet | bande par défaut du Field Emitter T2 |
| Médiane | cyan | avec l'Émetteur Accordable |
| Haute | ambre | avec l'Émetteur Accordable |

## Ça se lit à l'œil, sans aucun menu

- La **coupole** de l'émetteur prend la couleur de **la bande du champ**.
- Le **glow** d'une machine en marche prend la couleur de **sa** bande.
- **Mêmes couleurs = accordé.** Une machine dont le glow **clignote** entre deux couleurs est
  **désaccordée** (elle tourne sur une bande différente de son champ).

Vous **diagnostiquez votre base en la regardant**.

## Accordé vs désaccordé

| État | Effet |
|---|---|
| **Accordé** (même bande) | rendement plein, aucune dissonance |
| **Désaccordé** (bandes différentes) | la machine **tourne quand même**, mais **coûte plus d'Osc** (×1,5 par défaut) et **génère de la dissonance** ; son glow clignote |

> **Règle d'or : le désaccord ne bloque JAMAIS une machine.** Elle coûte plus et salit, mais ne
> refuse jamais de fonctionner. Vous êtes toujours informé (visuel), jamais bloqué sans comprendre.

Et les **recettes T1** sont marquées **`stable`** : elles réussissent **toujours**, quel que soit le
désaccord. La boucle de départ ne peut pas frustrer.

## Les bandes comme outil de routage (ce n'est pas une taxe)

Deux émetteurs qui se chevauchent, l'un **violet**, l'autre **cyan**, alimentent **deux groupes de
machines distincts au même endroit, sans un seul fil**. Isoler un atelier, prioriser une chaîne,
couper une zone : la planification harmonique **remplace le câblage sélectif**. Le désaccord est le
**revers d'un pouvoir**, pas une corvée.

## Comment on choisit les couleurs

- **Émetteur Accordable** (`tunable_field_emitter`) : un *upgrade* du Field Emitter (Field Emitter +
  2 Refined Crystal). C'est **lui** qui porte le choix de bande du champ. On l'accorde au **Resonance
  Tuner**, mode **« Accorder »** (l'outil à modes que vous avez déjà).
- **Une machine** : au **Resonance Tuner**, mode Accorder, clic droit dessus fait défiler sa bande.
  Le cycle **repasse par « universelle »**, donc accorder n'est jamais irréversible.

> Avant le T3, la **seule machine accordable est le Flux Purifier** (les autres T2 sont universelles).
> C'est volontaire : ça vous laisse *essayer* le système sans rien imposer. Une machine
> **universelle** accepte n'importe quelle bande et ne se désaccorde jamais.

## La dissonance : entretenir le réseau

Une machine désaccordée **injecte de la dissonance** dans l'émetteur qui la sert. La dissonance
**s'accumule dans le champ** (pas dans une barre cachée), en trois étapes visibles :

1. la coupole **grisaille** (se désature) ;
2. au-delà d'un seuil (~75 % de la capacité), le champ devient **instable** : il **saute des ticks**,
   donc les machines **hoquettent** (leur glow clignote) au lieu de se dégrader en silence ;
3. **au maximum** : **décharge de résonance** — une **impulsion AoE** brève (rayon ~6) qui **blesse**
   ce qui est à portée (message de mort dédié : « déchiqueté par une décharge de résonance »).
   L'écho local de l'Effondrement.

La décharge **purge une partie** de la saturation : si vous corrigez la cause, le champ se rétablit ;
sinon il remonte et re-décharge. La dissonance **décroît aussi lentement toute seule** — une base qui
cesse de mal se comporter guérit sans rien faire.

## Le Damping Array : l'infrastructure d'entretien

Pour gérer une dissonance importante, on **pose une infrastructure**, pas un slot :

- **Damping Array** (`4 Fer + 2 Refined Crystal + 1 Bloc de Redstone`) : il **absorbe la dissonance**
  du champ le plus pollué à portée (16 blocs), en consommant un **agent de damping** (data-driven ;
  par défaut le **Refined Crystal**), et **cristallise le déchet** en **`resonance_sludge`** — de la
  dissonance solidifiée, la substance même de l'Effondrement.
- Il est **autonome** (0 Osc) **à dessein** : s'il dépendait du champ qu'il répare, un champ saturé
  l'empêcherait de le réparer. Quand sa sortie est pleine, il s'arrête : **videz-le**.
- **Efficace** : un Array (~5 dissonance/tick) compense largement plusieurs machines désaccordées
  (1/tick chacune). Un champ saturé est **toujours récupérable** en posant l'infrastructure —
  d'ailleurs le Damping Array (16 blocs) porte plus loin que la décharge (6 blocs), donc on peut
  toujours nettoyer **à distance sûre**.

## Lire le champ où l'on se tient : le HUD

Portez le **Resonance Locator** (inventaire, ou slot **Curios** si présent) et un **overlay discret**
apparaît en coin d'écran : la **bande** (pastille de couleur), la **réserve** de l'émetteur qui vous
couvre, et une **jauge de dissonance** (qui vire au **rouge « champ instable »** au seuil). Il
n'apparaît que si vous portez l'objet — c'est un **instrument** qu'on emporte.

## En résumé

| Vous voulez… | …vous faites |
|---|---|
| Alimenter deux ateliers séparément au même endroit | deux émetteurs de bandes différentes |
| Réparer une machine sur une autre bande | Tuner, mode Accorder |
| Nettoyer un champ qui grésille | poser un Damping Array + agent, le vider |
| Voir l'état d'un champ | porter le Locator (HUD) / regarder la coupole |

➡️ Suite : **[Référence : machines, objets, outils](04-reference.md)**.
