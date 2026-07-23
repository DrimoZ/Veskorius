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

## Config TOML — `veskorius-server.toml`

Fichier de type **SERVER** (`VeskoriusConfig`, `ModConfigSpec`), câblé le 2026-07-22.

**Pourquoi SERVER (et pas COMMON/CLIENT)** : ces valeurs affectent la logique de jeu. En type
SERVER, NeoForge les **synchronise** vers les clients connectés (pas de désync sur les capacités
affichées), les stocke **par monde** (`saves/<monde>/serverconfig/veskorius-server.toml`), et
permet à un modpack de **livrer ses défauts** via `defaultconfigs/veskorius-server.toml` (copié dans
chaque nouveau monde). C'est le choix robuste : cohérent en multijoueur, résistant à la triche
client, et propre à chaque partie.

**Pour un modpack maker** : placer le fichier ajusté dans `defaultconfigs/veskorius-server.toml` à
la racine de l'instance. Il devient le défaut de tout nouveau monde. Pour un monde existant, éditer
`saves/<monde>/serverconfig/veskorius-server.toml`.

Valeurs exposées (défauts = valeurs de design d'origine) :

| Section | Clé | Défaut | Effet |
|---|---|---|---|
| `energy` | `fieldEmitterRange` | 8 | Portée (rayon, blocs) d'un Field Emitter |
| `energy` | `fieldEmitterCapacity` | 4000 | Réserve max d'Osc d'un émetteur (un multiple de la valeur d'un carburant permet d'en stocker plusieurs d'avance) |
| `energy` | `storageCellCapacity` | 8000 | Capacité d'une Resonance Storage Cell |
| `energy` | `storageCellChargeRate` | 20 | Osc/tick absorbés par une cellule dans un champ |
| `machines` | `augmentSpeedBonusPercent` | 15 | Bonus de vitesse (%) d'un Catalyst Core |
| `machines` | `overheatSpeedMultiplier` | 2.0 | Surchauffe : diviseur de la durée de cycle |
| `machines` | `overheatOscMultiplier` | 2.0 | Surchauffe : multiplicateur de la conso d'Osc |
| `machines` | `overheatInputLossChance` | 0.2 | Surchauffe : proba de perdre l'entrée sans sortie |
| `tools` | `locatorCapacity` | 100 | Batterie interne du Resonance Locator (Osc) |
| `tools` | `locatorCostPerUse` | 5 | Osc par ping du Locator |
| `tools` | `locatorRechargeRate` | 5 | Osc/tick de recharge du Locator |
| `tools` | `locatorRange` | 40 | Portée de détection du Locator (blocs) |
| `entities` | `custodeHealth` | 30 | PV du Custode (individus nouvellement apparus) |
| `entities` | `custodeDamage` | 6 | Dégâts d'attaque du Custode |
| `entities` | `custodeDetectionRange` | 6 | Rayon de ciblage passif du Custode |
| `entities` | `custodeAlertRange` | 16 | Rayon d'alerte quand une machine du site est cassée |
| `entities` | `striderMilkCooldown` | 6000 | Cooldown de traite du Fileur (ticks) |
| `entities` | `roostStriderRange` | 6 | Rayon dans lequel un Fileur active un Crystal Roost |
| `world` | `sporeGrowthChance` | 0.05 | Chance par random tick que la pierre veinée pousse un spore (face exposée, faible lumière) |

Chaque défaut est re-testé par le GameTest `configDefaultsMatchDesign` : le changer sans mettre à
jour ce dossier fait échouer la suite (même discipline que la réécriture des valeurs de référence
dans les autres tests).

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
| `veskorius-harmonics.toml` | **bandes, accord/désaccord, dissonance** — avec **interrupteur maître** (`enabled`) | ✅ **fait** (`HarmonicsConfig`) ; clés de damping à l'arrivée du Damping Array |
| `veskorius-structures.toml` | fréquence/espacement/biomes des structures, densité de mobs | à livrer **avec** la migration Structures (`08`) |

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
| `harmonics` | `dissonancePerCycle` / `dischargeThreshold` | Vitesse d'accumulation et seuil de décharge |
| `machines` | `augmentSlots` | Nombre de slots d'augment (par machine ou par tier) |
| `machines` | `augmentStackingMode` | Cumul d'un même effet : interdit / plafonné / libre, dans un slot et entre slots |
| `machines` | `overheatIgnoresStable` | La surchauffe garde-t-elle son risque sur une recette `stable` ? |
| `generation` | `gasIntensityByStrata` | Intensité du gaz de Résonance par strate (**garantit le scaling de difficulté**, voir `07`) |

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
   propres nombres. Les faire passer par `VeskoriusConfig` dès leur écriture, plutôt que de les
   retrofit (même leçon que le slot d'augment).

## Règle d'implémentation (pour toute nouvelle constante d'équilibrage)

1. La déclarer dans `VeskoriusConfig` (SPEC + un getter de commodité).
2. La **lire à l'exécution**, jamais en `static final` ni au chargement de classe : la config
   SERVER n'est pas encore chargée à ce moment-là (elle l'est au chargement du monde).
3. Réécrire son défaut comme valeur attendue dans `configDefaultsMatchDesign`, et laisser les tests
   de comportement existants valider le câblage (ils tournent avec les défauts).
