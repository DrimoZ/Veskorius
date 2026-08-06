# 03 — Progression complète (T1 → T5)

## Principe

5 tiers (pilier 5). Chaque transition de tier est gardée par une découverte physique (fragment
de Codex ou observation directe), jamais par un simple compteur de ressources.

## Vue d'ensemble

| Tier | Nom | Âge correspondant | Débloqué par | Change dans la lecture du monde |
|---|---|---|---|---|
| T1 | Stabilisation | Éveil | Rien (point de départ) | Reconnaît les poches de cristal brut |
| T2 | Réseau court | Essor | Blueprint restauré à l'Avant-poste (console sur place) | Le **Resonance Locator** révèle les sources de résonance **et les structures** (`/locate`) — le repérage post-T2, avant assuré par un tell de surface, passe désormais par l'outil (A7) |
| T3 | Réseau régional | Résonance (début) | Fragment du Sigma Laboratory | Reconnaît les grandes structures à distance |
| T4 | Synthèse profonde | Résonance (fin) | Fragment de l'Archive Régionale | Accède aux strates profondes en sécurité, relie plusieurs bases |
| T5 | Rupture de Faille | Effondrement | Découverte d'une Faille active | Voit et peut entrer dans les Failles ; contenu endgame |

## Le châssis, colonne vertébrale de l'arbre (ajouté 2026-08-06)

Avant de lire l'arbre : **chaque machine se fabrique sur le châssis de son palier**
(`05-Machines.md`). L'arbre ci-dessous montre donc les dépendances *en ressources* ; il faut y
superposer une chaîne parallèle, plus courte et strictement linéaire :

```
fractured_chassis (T1, pierre + cuivre)
        └──▶ attuned_chassis (T2, + fer + stable crystal)
                    └──▶ veskorian_chassis (T3, + fer + refined crystal)
```

Conséquence de progression : **franchir un palier, c'est d'abord fabriquer son châssis.** Le geste
est le même à chaque fois, ce qui rend le passage de palier reconnaissable, et le coût du palier
est payé une fois dans le châssis plutôt que redécouvert machine par machine.

Contrainte tenue : les châssis T1 et T2 n'exigent **aucun Resonance Component ni blueprint**, pour
ne pas reformer la dépendance circulaire Component ⇄ champ (voir `08-Structures.md`).

## Arbre complet de dépendances

```
raw_resonance_crystal (trouvé, Y -20 à 0)
        │
        ├──▶ [T1] Resonance Stabilizer ──▶ stable_resonance_crystal
        │         │
        │         ├──▶ [T1] Component Assembler ──▶ resonance_component
        │         └──▶ [T1] Resonance Whetstone (réparation d'outils)
        │
        └──▶ [T1] Crystal Crusher (alternative) ──▶ resonance_dust
                  (engrais agricole, ou branche alternative du Component Assembler)
        │
        ▼  (fragment Avant-poste)
[T2] Field Emitter (portée 8) ──▶ champ actif
        │
        ├──▶ [T2] Flux Purifier ──▶ refined_resonance_crystal
        ├──▶ [T2] Resonance Storage Cell (batterie portable)
        ├──▶ [T2] Resonance Locator (exploration)
        ├──▶ [T2] Crystal Roost (production passive alternative)
        ├──▶ [T2] Resonance Catalyst Core (augment, +15% vitesse, une fois posé sur n'importe
        │         quelle machine déjà construite, y compris T1)
        │
        ▼  (fragment Sigma Laboratory)
[T3] Resonance Relay (portée 20, chaîné, nécessite Veskorian Conductive Alloy Ingot) ──▶ réseau étendu
        │
        ├──▶ [T3] Veskorian Alloy Forge ──▶ veskorian_alloy_ingot (Iron) ou
        │         veskorian_conductive_alloy_ingot (Gold), + Flux Slag
        │         └──▶ [T3] Slag Vent (maintenance du Slag)
        ├──▶ [T3] Structural Synthesizer ──▶ veskorian_alloy_block + synthesis_residue
        ├──▶ [T3] Deep Crystal Driller (accès Y < -40)
        ├──▶ [T3] Flux Compressor ──▶ concentrated_flux
        │
        ▼  (fragment Archive Régionale — 3 Hyper Refined Crystal fournis, voir Bootstrap)
[T4] Harmonic Amplifier (portée ×2, jusqu'à 3 en chaîne, nécessite Harmonic Lattice)
        │
        ├──▶ [T4] Deep Synthesis Chamber ──▶ hyper_refined_crystal (renouvelable dès ce point)
        ├──▶ [T4] Automated Extraction Array (synchronise les Driller)
        ├──▶ [T4] Resonance Network Hub (priorité réseau)
        ├──▶ [T4→T5] Convergence Core (multi-bloc + concentrated_flux, portée 40, alimente le
        │         Rift Anchor sans base dédiée)
        │
        ▼  (découverte d'une Faille active en jeu, pas un craft)
[T5] Rift Anchor (stabilise une Faille)
        │
        ├──▶ [T5] Rift Core Extractor ──▶ rift_essence (fini, 6 max/Faille) + Corrupted Alloy Ingot (15%)
        └──▶ [T5] Rift Ward Emitter (protection post-extraction)
```

En parallèle de cet arbre, deux boucles annexes tournent sans dépendre d'un tier précis : la
récolte/élevage du Fileur de Cristal (Resonance Spore, dès que le joueur en croise un) et
l'agriculture de l'Ancient Seed trouvé à l'Archive Régionale (Resonance Bloom, voir
`04-Materials.md`).

## Bootstrap T4 — voir `05-Machines.md`

Le passage T3→T4 a un point de blocage potentiel corrigé explicitement dans `05-Machines.md` :
le Harmonic Lattice (nécessaire au premier Harmonic Amplifier) consomme du Hyper Refined
Crystal, qui n'est normalement produit que par la Deep Synthesis Chamber — elle-même un bloc T4.
L'Archive Régionale fournit exactement 3 Hyper Refined Crystal (2 pour le Lattice, 1 consommé
comme catalyseur permanent à la construction de la Chamber) pour amorcer les deux sans que le
joueur reste bloqué. Détail complet : `05-Machines.md`, section "Bootstrap du T4".

## Détail des paliers T4-T5

### T4 — Synthèse profonde
Débloqué par l'Archive Régionale (voir `08-Structures.md`). Introduit :
- L'accès sécurisé aux strates profondes (Y < -40), auparavant dangereuses (gaz de Résonance
  résiduel, voir `06-Energy.md`) sans le Deep Crystal Driller.
- Une portée de réseau régionale via le Harmonic Amplifier, ou à pleine puissance via le
  Convergence Core (multi-bloc, voir `05-Machines.md`) plutôt qu'en posant des dizaines de
  Relais bruts.
- Deux nouveaux styles de maintenance active : la dérive de calibration (Amplifier, Hub) et le
  sous-produit à évacuer (Alloy Forge → Flux Slag).

### T5 — Rupture de Faille (endgame)
Ne se débloque pas par craft mais par **découverte en jeu** d'une Faille active (voir
`07-World-Generation.md`). Le Rift Anchor est un bloc-ancre à poser au bord de la Faille ; sans
lui, s'approcher inflige des dégâts de déphasage progressifs. Une fois ancrée, la Faille devient
traversable et contient le combat de fin (`09-Entities.md`, Gardien de Faille) et la ressource
finale, non renouvelable une fois la Faille épuisée.

## Règle de déblocage transversale

Aucune machine de tier N+1 n'est craftable sans qu'un fragment ou une observation en jeu ne
l'ait précédée. À l'intérieur d'un tier déjà débloqué (ex : une fois Harmonic Amplifier obtenu),
toutes les autres machines du même tier (Deep Synthesis Chamber, Extraction Array, Network Hub,
Convergence Core) sont librement craftables sans fragment supplémentaire — cohérent avec le
pilier 1 (la restauration porte sur les catégories de machines par tier, pas sur chaque
variante).

## Courbe harmonique (ajouté 2026-07-23, voir `06`)

La complexité du réseau **n'arrive pas d'un coup** — c'est une progression à part entière, parallèle
aux tiers :

| Palier | Réseau | Complexité imposée |
|---|---|---|
| **T1** | machines autonomes ou **universelles** (l'Assembler accepte n'importe quelle bande) | **aucune** |
| **T2** | Field Emitter mono-bande : le joueur apprend juste que « le champ a une couleur » | aucune décision |
| **T2+ / T3** | Émetteur Accordable + machines à bande (Tuner, mode Accorder) : on **route** l'énergie par bande | le choix arrive quand le joueur est prêt |
| **T3+** | dissonance à gérer, infrastructure de damping | entretien actif |

Principe : **le début de partie ne doit jamais frustrer** (recettes T1 `stable`, aucune bande), et la
profondeur s'ouvre au rythme des paliers.

## Mécanique de déblocage — clé physique, pas recette masquée (révisé 2026-07-22)

> ⚠️ **Point contesté (2026-07-23), à revisiter.** Le porteur du projet a indiqué ne pas aimer le
> système de **blueprint** comme moyen de garder les tiers. Le modèle décrit ci-dessous reste en
> vigueur **pour l'instant** (il est codé et testé), mais il est **explicitement ouvert à
> remplacement** — aucune nouvelle mécanique ne doit s'appuyer dessus sans le signaler. À traiter
> dans une session dédiée.

La « découverte physique » qui garde chaque tier est **un objet-clé, le `resonance_blueprint` du
tier**, pas un drapeau invisible : **aucune recette n'est jamais masquée**. Les recettes des
machines d'un tier sont visibles dès le départ (JEI, recipe book) et **exigent le blueprint du
tier comme ingrédient — qui est *rendu* après le craft** (un seul blueprint sert pour un nombre
illimité de machines). Ce qui manque pour crafter est donc *visible* et pointe vers un lieu :
c'est le pilier 2 rendu littéral. Perdre son blueprint = retourner dans une structure du tier
(non bloquant, cohérent avec « retour à la ruine »).

La grammaire de craft est constante (toujours le blueprint), mais **l'acquisition du blueprint
varie par tier**, pour que le gatekeeping ne soit pas répétitif :

| Tier | Structure | Comment on obtient le blueprint |
|---|---|---|
| T2 | Avant-poste | **Restaurer** une console ancienne sur place (clic droit). Le coffre fournit en plus l'**amorçage garanti** (4 Resonance Component + 2 Gold) pour fabriquer le premier Field Emitter — sans quoi la progression serait verrouillée par une dépendance circulaire (Component ⇄ champ). Voir `08`. |
| T3 | Sigma Laboratory | **Réparer** 2 Relais → ouvre la salle centrale → le blueprint y est |
| T4 | Archive Régionale | **Collecter/ordonner** 4 fragments → ouvre la salle → blueprint + 3 Hyper Refined |
| T5 | Faille | Pas de blueprint : la **découverte + pose** du Rift Anchor *est* la porte |

Les **fragments de Codex** (`codex_fragment`) ne gatent plus rien : ce sont du **lore lisible** et,
parfois, des **indices** d'exploration (facultatifs). Détail complet : `08-Structures.md`.

## Ouvert

- Fréquence exacte des Failles : fixée dans `07-World-Generation.md` (1 / 15000 blocs) — valeur
  de départ à confirmer en playtest, pas une question ouverte de conception.
