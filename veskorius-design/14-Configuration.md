# 14 — Configuration (pour modpack makers)

Absent des versions précédentes. Ce fichier définit **ce qui est modifiable dans Veskorius sans
recompiler**, par quel mécanisme, et pourquoi. Objectif : qu'un modpack maker puisse rééquilibrer
et adapter le mod de façon robuste, sans toucher au code.

Il y a **deux leviers**, et le choix entre les deux n'est pas arbitraire.

## Principe : data-driven d'abord, config TOML pour le reste

Veskorius est « data-driven d'abord » (voir `README.md`, section recettes ; `07` pour le worldgen).
La règle de répartition :

- **Datapack (JSON)** pour tout ce qui a une forme de **contenu** : recettes de fonctionnement et
  de craft, tags, tables de butin, génération de monde. Un modpack surcharge ces fichiers dans son
  propre datapack — rien à recompiler, rechargeable à chaud (`/reload`) pour les recettes/tags/loot.
- **Config TOML (`ModConfigSpec` NeoForge)** pour les **constantes d'équilibrage** globales qui
  n'ont pas de représentation JSON naturelle : portées, capacités, multiplicateurs, probabilités.

On ne duplique jamais une valeur des deux côtés. Une durée de cycle vit dans la recette JSON (pas
dans la config) ; une portée de champ vit dans la config (pas dans un JSON).

## Déjà pilotable par datapack (aucun code à écrire)

Un modpack place ces fichiers sous `data/veskorius/…` (ou un autre namespace pour en ajouter) :

| Contenu | Emplacement (dans un datapack) | Ce qu'on peut régler |
|---|---|---|
| Recettes de fonctionnement des machines | `data/veskorius/recipe/{stabilizing,assembling,purifying,crushing}/…json` | entrées (item/tag + count), sortie + count, `time`, `osc_per_tick` |
| Recette du Whetstone (réparation) | `data/veskorius/recipe/sharpening/…json` | catalyseur, `repair_percent`, `time` |
| Carburants du Field Emitter | `data/veskorius/recipe/fueling/…json` | quels items sont carburant (`ingredient`) et combien d'Osc ils rendent (`osc`) |
| Recettes de craft (blocs, outils, augment, cellule) | `data/veskorius/recipe/*.json` | forme et ingrédients de fabrication |
| Sources de flux du Stabilizer | tag `data/veskorius/tags/item/stabilizer_flux.json` | quels items valent « flux » (Quartz, Raw Flux Deposit, +) |
| Augments de machine | tag `data/veskorius/tags/item/machine_augments.json` | quels items s'insèrent dans le slot d'augment |
| Butin des blocs | `data/veskorius/loot_table/blocks/*.json` | ce que lâche chaque bloc |
| Poches de cristal (taille, coquille, flux) | `data/veskorius/worldgen/configured_feature/resonance_crystal_pocket.json` | `crystal_tries`, `shell_thickness`, `flux_chance` |
| Densité / tranche Y des poches | `data/veskorius/worldgen/placed_feature/resonance_crystal_pocket.json` | nombre par chunk, min/max Y |
| Biomes concernés | `data/veskorius/neoforge/biome_modifier/add_resonance_crystal.json` | où les poches génèrent |

**Le worldgen reste volontairement datapack-only** (pas de doublon en TOML) : les features sont lues
depuis le registre au moment de la génération, pas à l'exécution — une valeur TOML n'aurait aucun
effet dessus et casserait la surcharge par datapack.

## Config TOML — fichiers, sections et clés

Fichiers de type **SERVER** (`ModConfigSpec`), câblés le 2026-07-22, **découpés par thème** le
2026-07-23 (voir la section « Découpage » plus bas pour le pourquoi).

**Pourquoi SERVER (et pas COMMON/CLIENT)** : ces valeurs affectent la logique de jeu. En type
SERVER, NeoForge les **synchronise** vers les clients connectés (pas de désync sur les capacités
affichées), les stocke **par monde** (`saves/<monde>/serverconfig/<fichier>.toml`), et permet à un
modpack de **livrer ses défauts** via `defaultconfigs/<fichier>.toml` (copié dans chaque nouveau
monde). C'est le choix robuste : cohérent en multijoueur, résistant à la triche client, et propre à
chaque partie.

**Pour un modpack maker** : placer les fichiers ajustés dans `defaultconfigs/` à la racine de
l'instance. Ils deviennent le défaut de tout nouveau monde. Pour un monde existant, éditer
`saves/<monde>/serverconfig/`.

Valeurs exposées (défauts = valeurs de design d'origine). **Les noms de fichier et de section
ci-dessous sont ceux du TOML réel** — un tableau qui donnerait d'autres noms enverrait le modpack
maker éditer des clés qui n'existent pas.

| Fichier | Section | Clé | Défaut | Effet |
|---|---|---|---|---|
| `basics` | `field` | `fieldEmitterRange` | 8 | Portée (rayon, blocs) d'un Field Emitter |
| `basics` | `field` | `fieldEmitterCapacity` | 4000 | Réserve max d'Osc d'un émetteur (un multiple de la valeur d'un carburant permet d'en stocker plusieurs d'avance) |
| `basics` | `portable` | `storageCellCapacity` | 8000 | Capacité d'une Resonance Storage Cell |
| `basics` | `portable` | `storageCellChargeRate` | 20 | Osc/tick absorbés par une cellule dans un champ |
| `basics` | `portable` | `locatorCapacity` | 100 | Batterie interne du Resonance Locator (Osc) |
| `basics` | `portable` | `locatorCostPerUse` | 5 | Osc par ping du Locator |
| `basics` | `portable` | `locatorRechargeRate` | 5 | Osc/tick de recharge du Locator |
| `basics` | `portable` | `locatorRange` | 40 | Portée de détection du Locator (blocs) |
| `machines` | `augment` | `augmentSpeedBonusPercent` | 15 | Bonus de vitesse (%) d'un Catalyst Core |
| `machines` | `augment` | `augmentSlots` | 1 | Slots d'augment actifs (1-4) |
| `machines` | `augment` | `augmentStacking` | `FREE` | Cumul d'un même effet : `FORBID` / `CAPPED` / `FREE` |
| `machines` | `augment` | `augmentStackingCap` | 2 | Plafond de cumul quand `augmentStacking = CAPPED` |
| `machines` | `overheat` | `overheatSpeedMultiplier` | 2.0 | Surchauffe : diviseur de la durée de cycle |
| `machines` | `overheat` | `overheatOscMultiplier` | 2.0 | Surchauffe : multiplicateur de la conso d'Osc |
| `machines` | `overheat` | `overheatInputLossChance` | 0.2 | Surchauffe : proba de perdre l'entrée sans sortie |
| `machines` | `overheat` | `overheatIgnoresStable` | `true` | La surchauffe garde son risque même sur une recette `stable` |
| `mobs` | `custode` | `custodeHealth` | 30 | PV du Custode (individus nouvellement apparus) |
| `mobs` | `custode` | `custodeDamage` | 6 | Dégâts d'attaque du Custode |
| `mobs` | `custode` | `custodeDetectionRange` | 6 | Rayon de ciblage passif du Custode |
| `mobs` | `custode` | `custodeAlertRange` | 16 | Rayon d'alerte quand une machine du site est cassée |
| `mobs` | `fauna` | `striderMilkCooldown` | 6000 | Cooldown de traite du Fileur (ticks) |
| `mobs` | `fauna` | `roostStriderRange` | 6 | Rayon dans lequel un Fileur active un Crystal Roost |
| `generation` | `world` | `sporeGrowthChance` | 0.05 | Chance par random tick que la pierre veinée pousse un spore (face exposée, faible lumière) |
| `generation` | `world` | `bloomGrowthChance` | 5 | Un random tick sur N fait avancer le Buisson de Floraison d'un stade (≈ un buisson de baies) |
| `generation` | `storm` | `durationTicks` | 12000 | Durée d'un Orage de Résonance (10 min). Une durée plus longue est une FENÊTRE plus longue, jamais un stock plus gros : tout ce qui reste au sol à la fin disparaît |
| `generation` | `storm` | `rollIntervalTicks` | 24000 | Intervalle entre deux tirages (1 jour MC) |
| `generation` | `storm` | `rollChance` | 6 | Un tirage sur N déclenche l'orage — soit un tous les 5 à 7 jours. Très élevé = orages quasi absents |
| `generation` | `storm` | `seedRadius` | 48 | Rayon de dépôt des cratères autour de chaque joueur. Plus large étale la chasse, plus étroit la concentre |
| `harmonics` | `harmonics` | `enabled` | `true` | **Interrupteur maître** du système Harmoniques & Dissonance |
| `harmonics` | `harmonics` | `bandCount` | 3 | Nombre de bandes sélectionnables (1-3) |
| `harmonics` | `harmonics` | `detuneOscMultiplier` | 1.5 | Surcoût d'Osc d'une machine désaccordée |
| `harmonics` | `harmonics` | `dissonancePerDetunedTick` | 1 | Dissonance injectée par tick de fonctionnement désaccordé |
| `harmonics` | `harmonics` | `dissonanceCapacity` | 2000 | Plafond de dissonance d'un émetteur (= seuil de décharge) |
| `harmonics` | `harmonics` | `dissonanceUnstableThreshold` | 0.75 | Fraction du plafond au-delà de laquelle le champ devient intermittent |
| `harmonics` | `harmonics` | `dissonanceDecayPerSecond` | 1 | Décroissance naturelle de la dissonance (0 = Damping Array obligatoire) |
| `harmonics` | `discharge` | `enabled` | `true` | Décharge AoE au plafond de dissonance |
| `harmonics` | `discharge` | `radius` | 6 | Rayon de l'impulsion (blocs) |
| `harmonics` | `discharge` | `damage` | 6.0 | Dégâts par entité touchée (0 = visuel/sonore seul) |
| `harmonics` | `discharge` | `releaseFraction` | 0.5 | Fraction du plafond purgée par décharge (soupape) |
| `harmonics` | `discharge` | `cooldownTicks` | 100 | Ticks minimum entre deux décharges |
| `harmonics` | `damping` | `dampingRange` | 16 | Rayon de nettoyage d'un Damping Array |
| `harmonics` | `damping` | `dampingCycleTicks` | 100 | Durée d'un cycle de damping |
| `harmonics` | `hud` | `enabled` | `true` | HUD de champ (à `false` : aucun paquet émis) |
| `harmonics` | `hud` | `updateIntervalTicks` | 10 | Ticks entre deux lectures |

**Tous** les défauts du tableau ci-dessus sont re-testés : les changer sans mettre à jour ce
dossier fait échouer la suite. Trois GameTest se partagent le travail, un par famille de
curseurs :

| Test | Couvre |
|---|---|
| `configDefaultsMatchDesign` | `basics`, `machines.overheat`, le bonus d'augment, `mobs`, `generation` |
| `harmonicsConfigDefaultsMatchDesign` | tout `harmonics` : bandes, désaccord, dissonance, décharge, damping, HUD |
| `augmentConfigDefaultsMatchDesign` | `machines.augment` (A9) : `augmentSlots`, `augmentStacking`, `augmentStackingCap`, `MAX_AUGMENT_SLOTS` |

*(Les deux derniers ajoutés le 2026-08-06 : ce fichier affirmait depuis le découpage que la
couverture était complète — elle ne l'était pas, et le thème le plus riche en curseurs du mod
n'était tenu que par la relecture.)*

Deux de ces tests vérifient en plus un **invariant de conception**, pas seulement une valeur :

- `dampingRange > dischargeRadius` — le Damping Array doit porter plus loin que la décharge,
  sinon réparer un champ saturé obligerait à entrer dans la zone d'impulsion, ce que `06` interdit
  explicitement (« aucune décharge forcée sur le joueur qui répare ») ;
- `augmentSlots <= MAX_AUGMENT_SLOTS` — le nombre configuré ne peut pas dépasser ce que
  l'inventaire réserve réellement.

## Doctrine (révisée 2026-07-23) : tout doit pouvoir se moduler, jusqu'à se désactiver

Décision du porteur du projet : **la majorité des mécaniques doit être réglable, allégeable ou
désactivable en config.** Si un modpack maker ou un joueur veut « faciliter » sa partie, on le lui
permet — ça ne coûte presque rien à implémenter, et c'est toujours mieux qu'un fork. En contrepartie,
**chaque interrupteur est documenté avec ce qu'on perd** (« désactiver les harmoniques = tu perds la
couche de planification de réseau »).

Conséquence d'implémentation : chaque nouveau système arrive avec (a) ses constantes, (b) **un
interrupteur maître**, (c) une ligne de doc expliquant l'effet de le couper.

## Découpage des fichiers par thème (révisé 2026-07-23)

Le fichier unique `veskorius-server.toml` devient **plusieurs specs nommées, une par thème** (NeoForge
accepte plusieurs configs SERVER enregistrées sous des noms distincts). Objectif : un modpack maker
trouve immédiatement ce qu'il cherche, et surcharge un thème sans toucher aux autres.

| Fichier | Contenu | Statut |
|---|---|---|
| `veskorius-basics.toml` | le champ lui-même (portée, réserve) + objets portables (Storage Cell, Locator) | ✅ **fait** (`BasicsConfig`) |
| `veskorius-machines.toml` | augments, surchauffe ; accueillera **slots d'augment / règles de cumul / recettes increvables** | ✅ **fait** (`MachinesConfig`) |
| `veskorius-generation.toml` | croissance et aléas lus à l'exécution ; accueillera **gaz par strate** et rareté du biome | ✅ **fait** (`GenerationConfig`) |
| `veskorius-mobs.toml` | Custodes (PV/dégâts/portées), Fileur, Roost | ✅ **fait** (`MobsConfig`) |
| `veskorius-harmonics.toml` | **bandes, accord/désaccord, dissonance**, damping, **HUD de champ** — avec **interrupteur maître** (`enabled`) | ✅ **fait** (`HarmonicsConfig`) |
| ~~`veskorius-structures.toml`~~ | fréquence/espacement/biomes des structures | **Non créé (A7, 2026-07-23) — volontairement.** Ces valeurs vivent dans le JSON datapack `structure_set`/`structure` (`ModStructures`), **déjà surchargeable par datapack** : c'est exactement la doctrine « data-driven d'abord » ci-dessous. Un TOML qui les dupliquerait serait une « clé qui ne fait rien » — l'anti-pattern que ce dossier proscrit. *(✅ Confirmé par le porteur, 2026-07-24.)* |

**Règle tenue** : une config est livrée **avec le code qu'elle pilote**, jamais en avance — une clé
qui ne fait rien est un piège pour le modpack maker.

`VeskoriusConfig` reste la **façade** : elle ne déclare plus aucune valeur, seulement les accesseurs.
C'est ce qui a permis de découper les fichiers **sans toucher un seul appelant**.

**Migration** : les clés gardent leur sens, elles changent seulement de fichier. Un
`veskorius-server.toml` issu d'un monde antérieur devient **orphelin** (ses réglages personnalisés ne
sont pas repris automatiquement) — sans conséquence tant que le mod n'est pas publié, à mentionner au
changelog le jour venu. Le GameTest `configDefaultsMatchDesign` valide les défauts à travers la
façade, donc il couvre tous les thèmes d'un coup.

### Réglages notables introduits par la révision

| Thème | Clé | Effet |
|---|---|---|
| `harmonics` | `enabled` | **Interrupteur maître.** À `false`, le mod redevient « champ simple » (aucune bande, aucune dissonance) — comportement T1 partout |
| `harmonics` | `bandCount` | Nombre de bandes harmoniques (défaut 3) |
| `harmonics` | `detuneOscMultiplier` | Surcoût d'Osc d'une machine désaccordée |
| `harmonics` | `dissonancePerDetunedTick` / `dissonanceCapacity` | Vitesse d'accumulation et plafond (le plafond **est** le seuil de décharge) |
| `harmonics.discharge` | `enabled` / `radius` / `damage` / `releaseFraction` / `cooldownTicks` | **Décharge de résonance** au plafond : impulsion AoE (défauts : activée, rayon 6, 6 dégâts, purge 50 % du plafond, cooldown 100 ticks). `damage=0` garde l'effet visuel/sonore sans blesser ; `enabled=false` retire l'impulsion (la dissonance reste plafonnée) |
| `harmonics.hud` | `enabled` / `updateIntervalTicks` | HUD de champ : envoi serveur→client aux seuls porteurs du Locator. À `false`, **aucun paquet n'est émis** (défaut : activé, 10 ticks). Indépendant de l'interrupteur maître — harmoniques coupées, le HUD se réduit à la réserve, ce qui reste vrai |
| `machines.augment` | `augmentSlots` | ✅ **codé (A9)** — nombre de slots d'augment actifs, 1-4 (défaut **1** = comportement historique). Max réservé fixe (4) |
| `machines.augment` | `augmentStacking` / `augmentStackingCap` | ✅ **codé (A9)** — cumul d'un même effet entre slots : `FORBID` / `CAPPED` (+ cap) / `FREE` (défaut). Ne mord qu'avec >1 slot |
| `machines.overheat` | `overheatIgnoresStable` | ✅ **codé (2026-08-06)** — la surchauffe garde-t-elle son risque sur une recette `stable` ? Défaut **`true`** : oui. Une recette stable ne rate jamais par *désaccord* (c'est ce qui protège la boucle de départ), mais la surchauffe est un pari que le joueur **active** — sans risque, toute recette stable voudrait la surchauffe en permanence et le choix disparaîtrait. À `false`, `stable` veut dire « ne perd jamais rien, point » |
| `generation` | `gasIntensityByStrata` | ⚠️ **Pas codé** (arrive avec le gaz de Résonance, Phase 2). Intensité du gaz par strate (**garantit le scaling de difficulté**, voir `07`) |

## Reste à faire / différé (roadmap config)

À intégrer au fil des phases, pas en une fois :

1. ✅ **Registre de carburants data-driven** (fait 2026-07-22). Le Field Emitter n'accepte plus un
   item codé en dur : ses carburants sont un `RecipeType` `veskorius:fueling` (une entrée JSON
   `ingredient → osc` par carburant). Par défaut, un seul carburant, le Stable Crystal à 4000 Osc ;
   un modpack en ajoute (ex. Refined Crystal à 9000 Osc — penser à monter `fieldEmitterCapacity` en
   conséquence, sinon la réserve ne peut pas l'absorber), en retire, ou change les valeurs, sans
   recompiler. A remplacé l'ancien `stableCrystalOsc` de la config. Visible dans JEI (catégorie
   « Field Emitter » : carburant → Osc), interrogé par `EmitterFuelRecipe` via le RecipeManager.
   Choix du `RecipeType` plutôt qu'un tag + registre de données : cohérent avec l'architecture
   recettes déjà en place (datagen, JEI, `/reload`), et permet une valeur d'Osc **par** carburant
   (impossible avec un simple tag).
2. **Intensité de champ** (`FIELD_STRENGTH`, sans effet avant le T4) — à exposer quand le Harmonic
   Amplifier introduira des seuils d'intensité (Phase 3).
3. **Granularité par machine** — la config actuelle porte des multiplicateurs *globaux* (augment,
   surchauffe). Si un playtest montre le besoin de régler une machine en particulier, ajouter des
   surcharges par machine ; ne pas le faire à l'aveugle.
4. **Config CLIENT** — aucune option purement visuelle n'existe encore. En créer une (fichier
   `veskorius-client.toml`) le jour où une préférence d'affichage apparaît (icône de surchauffe,
   couleurs de barre…), pas avant.
5. **Nouvelles constantes des Phases 2-4** — chaque machine/mob/structure à venir apportera ses
   propres nombres. Les faire passer par la spec de leur thème dès leur écriture, plutôt que de les
   retrofit (même leçon que le slot d'augment).
6. ✅ **Couverture de test des défauts** (fait 2026-08-06) — `harmonics` et les clés A9 sont
   désormais testés, voir le tableau des trois GameTest plus haut. Le tableau de valeurs de ce
   fichier est de nouveau tenu par la suite, pas par la relecture.

## Règle d'implémentation (pour toute nouvelle constante d'équilibrage)

1. La déclarer dans la **spec du thème concerné** (`BasicsConfig`, `MachinesConfig`,
   `GenerationConfig`, `MobsConfig`, `HarmonicsConfig`), puis exposer un getter de commodité
   sur `VeskoriusConfig` — la façade ne déclare plus aucune valeur depuis le découpage.
2. La **lire à l'exécution**, jamais en `static final` ni au chargement de classe : la config
   SERVER n'est pas encore chargée à ce moment-là (elle l'est au chargement du monde).
3. Réécrire son défaut comme valeur attendue dans `configDefaultsMatchDesign`, et laisser les tests
   de comportement existants valider le câblage (ils tournent avec les défauts).
