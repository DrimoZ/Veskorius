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
- Faut-il un indicateur visuel de bord de champ (particules) ou seulement une lecture via le
  Resonance Locator ? Question mixte gameplay/assets, à trancher avant de coder le Field
  Emitter.
