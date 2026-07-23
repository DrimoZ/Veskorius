# 06 — Energy (la Résonance)

## Principe

Pas de câbles. L'énergie (Oscillations, `Osc`) se propage par champ autour d'un émetteur ou
relais actif. Une machine fonctionne si elle est dans un champ, pas si elle est connectée
physiquement.

## Source primaire de l'énergie (précisé le 2026-07-21, au codage de la tâche 5)

Les versions précédentes de ce fichier décrivaient la propagation, les portées et les réserves,
mais **jamais d'où vient l'Osc au départ** : aucune machine de `05-Machines.md` ne *génère*
d'énergie, toutes en *consomment* (ou sont autonomes). Ce n'était pas un oubli anodin — c'est le
fondement de l'économie énergétique du mod. Résolu ainsi, cohérent avec les valeurs déjà posées :

**La source d'énergie, c'est la chaîne de cristaux.** Un Stable Resonance Crystal *est* une
batterie de 4000 Osc (voir le tableau ci-dessous). Le **Field Emitter consomme (brûle) des
Stable Crystals comme carburant** : 1 cristal → +4000 Osc dans sa réserve interne (plafond
4000 Osc = exactement un cristal, valeur déjà donnée pour le Field Emitter dans `05-Machines.md`
#4). Le cristal est détruit, comme du charbon dans un four.

Conséquences cohérentes avec les piliers :
- **Pas d'énergie infinie** (pilier 1) : produire de l'Osc coûte des Stable Crystals, donc du
  minage de Raw Crystals puis du temps de Stabilizer. L'énergie est bornée par ces ressources.
- Le **Stabilizer reste « autonome »** : il ne crée pas d'Osc, il rend simplement utilisable
  l'énergie latente d'un Raw Crystal (cohérent avec le lore du cristal brut instable/énergétique,
  `02-Lore.md`). L'Osc « apparaît » au moment où un Stable Crystal est brûlé, pas avant.
- La **Resonance Storage Cell** (`05-Machines.md` #6) reste la seule batterie *portable et
  rechargeable* dédiée. Le Stable Crystal, lui, est un carburant à usage unique — deux rôles
  distincts, pas de doublon.

Alternatives écartées : un Stable Crystal *déchargeable* (état de charge sur l'item — plus
complexe, demanderait une machine de recharge non prévue) ; un item carburant dédié (ajouterait
un item et une recette hors registre). Le choix « brûler des Stable Crystals » est le plus simple
et reste local au Field Emitter — réversible sans toucher au reste du système de champ.

## Osc portable (précisé le 2026-07-21, pas encore codé)

Second trou du même ordre : le stationnaire (champ → machine) était défini, mais pas comment un
*objet* porté stocke et dépense des Osc. Nécessaire pour la Resonance Storage Cell (`05` #6) et le
Resonance Locator (`05` #7). Résolu, à coder quand ces items seront implémentés (le Locator
attend en plus la génération des structures — `07`) :

- **Resonance Storage Cell** (batterie portable, capacité 8000 Osc, état de charge sur l'item) :
  se **charge dans un champ**. Tant qu'elle est dans l'inventaire d'un joueur situé dans un champ
  actif, elle absorbe des Osc prélevés sur ce champ (donc sur la réserve d'un émetteur — même
  source que les machines). Débit de charge à fixer en playtest (première estimation : ~20
  Osc/tick). Sert de réserve portable pour les outils.
- **Resonance Locator** (outil) : possède sa **propre petite batterie interne** (~20 utilisations
  = 100 Osc à 5 Osc/utilisation, `05` #7), qui se **recharge automatiquement** si le joueur est
  dans un champ **ou** s'il porte une Storage Cell chargée dans son inventaire. Chaque « ping »
  consomme 5 Osc de cette batterie ; sans charge, l'outil ne fait rien. Sa *fonction* de
  localisation dépend de la génération des structures (tâche 10), indépendante de ce modèle
  d'énergie.

Cohérent avec « pas de câble » (pilier 3) : la recharge portable passe elle aussi par le champ,
jamais par une prise. Aucune conversion cachée.

## Constantes de référence

| Constante | Valeur |
|---|---|
| Unité | Oscillation (`Osc`), mesurée en `Osc/tick` |
| Réserve d'un Stable Resonance Crystal (batterie de base) | 4000 Osc |
| Réserve d'un Refined Resonance Crystal | 9000 Osc (utilisé dans les machines T3+ portables) |
| Portée Field Emitter (T2) | 8 blocs |
| Portée Resonance Relay (T3) | 20 blocs, ligne de mire requise |
| Portée avec Harmonic Amplifier (T4) | ×2 par amplificateur, max 3 en chaîne (portée effective max ≈ 120 blocs) |
| Perte hors stabilisation (cristal brut porté) | 1 point de dégât / 20 ticks après 2400 ticks (2 min) sans traitement |
| Dégâts de déphasage (Faille non ancrée, < 8 blocs) | 2 cœurs / seconde après 3 secondes d'exposition |
| Gaz de Résonance résiduel (Y < -40 sans Deep Crystal Driller à proximité) | Effet de mining fatigue + 1 cœur / 10s, annulé par la présence d'un Driller actif dans un rayon de 16 blocs |
| Mode surchauffe (Flux Purifier, Deep Synthesis Chamber) | Temps ÷2, consommation Osc ×2, 20% de chance par cycle de perdre l'input sans output |
| Dérive de calibration (Harmonic Amplifier, Network Hub) | -1% d'efficacité / jour Minecraft d'utilisation continue, plafond -30%, reset via Resonance Tuner (coûte 1 Resonance Component) |
| Dissipation du Flux Slag (Slag Vent actif) | 1 Flux Slag dissipé / 10s par Veskorian Alloy Forge dans un rayon de 8 blocs |

## Comportement du réseau

- Champs superposés : pas d'addition, l'intensité retenue est celle de la source la plus forte
  (anti-stacking assumé, voir Piliers).
- Ligne de mire obligatoire pour toute retransmission par Relais/Amplificateur — impact
  esthétique voulu : bases ouvertes, pas de boîtes fermées.
- **Resonance Network Hub (T4)** introduit une nuance : quand un champ ne fournit pas assez
  d'Osc/tick pour alimenter toutes les machines qui s'y trouvent simultanément, le Hub applique
  un ordre de priorité configurable par le joueur (ex : Rift Anchor > Deep Synthesis Chamber >
  reste). Sans Hub, la répartition se fait par ordre d'ancienneté de pose (première posée,
  première servie) — volontairement basique pour forcer l'intérêt du Hub en T4.
- **Exception assumée à la non-addition** : le Convergence Core (`05-Machines.md`) émet un champ
  fixe à intensité maximale sans jamais être limité par les champs qu'il chevauche. C'est
  délibéré et unique à ce bloc — il simule à petite échelle un réseau de la puissance de l'âge
  d'or, ce qu'aucune combinaison de Relais/Amplificateurs normaux ne peut faire.

## Harmoniques & Dissonance (ajouté 2026-07-23, voir `16` §0)

> **État du code (2026-07-23) — système jouable.** ✅ Codé et testé (7 GameTest, suite à 85) :
> `HarmonicBand` (3 bandes-couleurs), `veskorius-harmonics.toml` avec **interrupteur maître**,
> bande sur le champ (`IResonanceField.getBand`) et sur les machines (universelle par défaut →
> **le T1 est inchangé**), **surcoût d'Osc en désaccord + injection de dissonance**, dissonance
> **plafonnée, persistée, à décroissance naturelle**, **instabilité** au-delà du seuil (le champ
> saute des ticks), **flag de recette `stable`** (les 5 recettes T1 le portent), **mode « Accorder »
> du Tuner**, **Émetteur Accordable** (`tunable_field_emitter` — choix de bande, accordable au
> Tuner), et **coloration de la coupole par bande** (elle grisaille avec la dissonance).
> **Reste à coder** : Damping Array, coloration du glow des machines par bande, HUD de champ +
> Curios, décharge de résonance (AoE). Le désaccord ne devient courant qu'avec les machines T3
> (Phase 2) — aujourd'hui il s'obtient en accordant volontairement une machine sur une autre bande.

Couche systémique posée **sur le système de champ existant**, pas à côté. Raison d'être : le pilier 3
donne un réseau sans câbles, mais les câbles rendent un service qu'on n'avait pas remplacé —
**choisir ce qu'on alimente**. Les bandes harmoniques sont cette réponse. Et le dérèglement du réseau
rejoue, à petite échelle et gérable, **la sur-résonance qui a causé l'Effondrement** (`02-Lore.md`) :
l'entretien devient la mécanique-signature du mod, pas un compteur de propreté générique.

### Le modèle mental, en une phrase

> **Un champ a une couleur. Une machine écoute sur une couleur. Même couleur = propre et rapide.
> Couleur différente = ça marche quand même, mais ça coûte plus cher et ça grince.**

### Bandes harmoniques

Volontairement **peu nombreuses** (mémorisables), identifiées par une **couleur** :

| Bande | Couleur | Disponibilité |
|---|---|---|
| Fondamentale | violet | bande par défaut (T2) |
| Médiane | cyan | avec l'Émetteur Accordable |
| Haute | ambre | avec l'Émetteur Accordable |

Nombre de bandes réglable en config (`16` §0, `14`).

### Lecture visuelle — aucun GUI à apprendre

Le système réutilise les deux visuels **déjà codés** :
- la **coupole de particules** de l'émetteur prend la couleur de la **bande du champ** ;
- le **glow d'une machine en marche** prend la couleur de la **bande de la machine**.

Couleurs identiques = accordé. Une machine ambre qui **clignote** dans un champ violet = désaccordée.
On diagnostique sa base **en la regardant**.

### Accord / Désaccord

| État | Effet |
|---|---|
| **Accordé** | rendement plein, aucune dissonance produite |
| **Désaccordé** | la machine **tourne quand même** : surcoût d'Osc + **génère de la dissonance** ; glow clignotant |

**Règle dure : le désaccord ne bloque JAMAIS une machine.** Elle ne refuse pas de fonctionner ; elle
coûte plus et salit. Le joueur est toujours informé (visuel), jamais bloqué sans comprendre.

### Les bandes comme outil de routage (ce n'est pas une taxe)

Deux émetteurs qui se chevauchent, l'un en violet, l'autre en cyan, alimentent **deux groupes de
machines distincts au même endroit, sans un seul fil**. Isoler un atelier, prioriser une chaîne,
couper une zone : la planification harmonique remplace le câblage sélectif. Le désaccord est le
**revers d'un pouvoir**, pas une corvée ajoutée.

### Dissonance — spatiale, visible, croissante

La dissonance **s'accumule dans l'émetteur / le champ** (pas dans une barre cachée) :
1. la coupole se **désature et grésille** ;
2. le champ devient **intermittent** (les machines hoquettent) ;
3. au maximum : **décharge de résonance** — impulsion AoE brève (l'écho local de l'Effondrement).

Elle se gère par **infrastructure**, pas par un slot : le **Damping Array** (`05`) l'absorbe et se
**sature** (container à purger), en consommant du **Concentrated Flux**. La dissonance évacuée se
matérialise en **déchet** (`resonance_sludge` / `flux_slag`, voir `04`) — de la dissonance
cristallisée, la substance même de l'Effondrement.

### Courbe d'introduction (T1 reste simple)

| Palier | Ce que le joueur vit | Complexité |
|---|---|---|
| **T1** | Stabilizer / Crusher / Whetstone sont **autonomes** (aucun champ). Le Component Assembler est **universel** : il accepte n'importe quelle bande. | **Zéro** — le champ « marche », point |
| **T2** | Le Field Emitter émet en Fondamentale, **sans choix**. Le joueur apprend juste que « le champ a une couleur ». | **Aucune décision** |
| **T2+ / T3** | L'**Émetteur Accordable** permet de choisir la bande ; les machines T3 portent une bande, réglée au **Resonance Tuner** (mode « Accorder »). | Le choix arrive **quand le joueur est prêt** |

### Recettes increvables

Une recette peut être marquée **`stable: true`** (`05`, data-driven) : elle **réussit toujours**, quel
que soit le désaccord ou la dissonance. Toutes les recettes **T1** le sont par défaut — la boucle de
départ ne peut jamais frustrer. La **surchauffe** garde son risque (c'est un pari volontaire du
joueur), sauf config contraire.

### Lecture du champ par le joueur

Un **overlay HUD** affiche le champ où l'on se tient (bande, réserve, dissonance) dès que le joueur
porte l'objet dédié — inventaire, ou **slot Curios si le mod est détecté** (voir `12` et `10`).

### Articulation avec la « dérive de calibration » T4 (Amplifier / Hub)

Ce dossier définissait déjà une dérive d'efficacité sur le Harmonic Amplifier et le Network Hub
(-1 %/jour, plafond -30 %, reset au Resonance Tuner). **Ce n'est pas un second système** : c'est le
**même geste** (ça dérive, on ré-accorde au Tuner) appliqué au T4. On l'unifie donc explicitement —
le joueur n'apprend qu'un concept, « la Résonance se désaccorde à l'usage » :

| Niveau | Ce qui dérive | Symptôme | Remède |
|---|---|---|---|
| T3 (harmonique) | la **bande** d'une machine | glow clignotant, surcoût d'Osc, dissonance | Tuner, mode **Accorder** |
| T4 (calibration) | l'**efficacité** d'un Amplifier / Hub | portée / répartition dégradée (plafond -30 %) | Tuner, **recalibration** (coûte 1 Resonance Component) |

Même outil, même vocabulaire, deux échelles. Le *Tuning Core* (augment, `05`) ralentit les deux.

### Configuration

Tout est modulable, jusqu'à l'**interrupteur maître** (`veskorius-harmonics.toml`) : harmoniques
**OFF** = le mod redevient « champ simple », comme au T1 partout. Voir `14`.

## Équilibrage face à une alternative connue (FE/RF câblé)

| | Câble RF/FE classique | Résonance |
|---|---|---|
| Coût de transport | Un bloc de câble par mètre | Nul (seul le Relais/Amplificateur coûte) |
| Contrainte | Aucune, portée illimitée si assez de câbles | Ligne de mire, portée finie par palier |
| Compatibilité multi-mods | Directe (FE universel) | Passe uniquement par des machines de conversion dédiées (voir `10-Mod-Integrations.md`) — jamais d'équivalence cachée |

## Problèmes / Alternatives rejetées

- **Rejeté : tuyaux de Résonance visibles mais fins.** Un câble déguisé reste un câble — casse le
  pilier 3 au premier bloc posé.
- **Rejeté : conversion directe 1 FE = 1 Osc.** Banaliserait la Résonance en reskin ; toute
  compatibilité passe par une machine de conversion dédiée avec son propre coût.
- **Rejeté : champs cumulatifs en cas de chevauchement.** Recrée le problème du stacking de
  générateurs qu'on cherchait justement à éviter.

## Ouvert

- Le rayon de 8 blocs du Field Emitter T2 reste une première estimation à valider en jeu.
- ~~Indicateur visuel de bord de champ ?~~ **Résolu (2026-07-23)** : le Field Emitter actif émet une
  coupole de particules sur sa sphère de portée (`FieldEmitterBlockEntity.pulseFieldDome`). Le glow des
  machines actives complète la lecture (une machine qui s'allume est couverte).
