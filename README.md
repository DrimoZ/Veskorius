# Veskorius (NeoForge 1.21.1)

De l'énergie qui ne passe par aucun câble : elle emplit un volume, et une machine tourne si elle
s'y tient. Mod de progression technique en cinq paliers, **jouables de bout en bout**.

**Alpha 0.1.0.** Java 21, NeoForge 21.1.172+.

## Où est quoi

| | |
|---|---|
| **`veskorius-design/`** | **Source de vérité** pour tout ce qui est design et gameplay. Le code ne redéfinit jamais une valeur de jeu ; si un chiffre manque ici, il est dans `05-Machines.md` ou `04-Materials.md` |
| `veskorius-design/18-Etat-des-lieux.md` | **L'inventaire de ce qui est codé**, écrit à partir du code et vérifié par `./gradlew audit`. C'est là qu'on regarde avant de croire qu'une fonctionnalité manque |
| `veskorius-design/13-Registry-Index.md` | L'état « codé / à coder » de tout le contenu prévu |
| `veskorius-design/11-Development-Plan.md` | La liste ordonnée de ce qui reste, avec les dépendances |
| `veskorius-design/guide-joueur/` | Le guide joueur, en français |
| `wiki/` | Les pages du wiki GitHub, en anglais |
| `curseforge.md` · `PUBLISHING.md` | La page projet, et la procédure de publication |

Ce README décrit le **dépôt** : comment le construire, et les conventions de code qu'on ne
contourne pas. Il ne tient pas la liste du contenu — c'est le travail de `18-Etat-des-lieux.md`,
qui est audité, alors qu'une liste tenue ici dériverait en silence. Elle a dérivé.

## Historique de développement

Ce qui suit est un **journal**, pas un inventaire : il raconte comment chaque système est arrivé
et pourquoi il est fait comme ça. Pour savoir ce qui existe aujourd'hui, voir
`18-Etat-des-lieux.md`.

- Projet Gradle complet (ModDevGradle, NeoForge 1.21.1, Java 21). `./gradlew build` et
  `./gradlew runData` passent.
- 4 items enregistrés : `raw_resonance_crystal`, `stable_resonance_crystal`,
  `refined_resonance_crystal`, `resonance_component`.
- **5 machines à cycle fonctionnelles** : Resonance Stabilizer (#1, autonome), Component
  Assembler (#2, 3 Osc/tick), Resonance Whetstone (#3, autonome), Flux Purifier (#5, 2 Osc/tick,
  **mode surchauffe**) et Crystal Crusher (#22, autonome, 1 Raw Crystal → 3 Resonance Dust en
  10 s, alternative rapide au Stabilizer). Block entity, cycle, GUI avec barre de progression,
  orientation, slot d'augment, inventaire persistant et vidé au sol quand le bloc est cassé.
- **Contrôles sur toutes les machines** (3 boutons dans le GUI) : interrupteur manuel on/off,
  contrôle redstone façon Thermal (ignoré / requiert un signal / requiert l'absence), et
  surchauffe pour les machines qui la supportent. Aucun packet custom (canal vanilla
  `clickMenuButton`).
- **Resonance Tuner** : outil à modes (Pivoter / On-Off / Surchauffe / Redstone, stocké sur
  l'item via Data Component). Clic droit applique le mode, clic droit dans le vide change de
  mode, **shift-clic droit démonte** n'importe quel bloc-entité (bloc + contenu → inventaire).
  L'interaction passe par un événement (`RightClickBlock`) pour intercepter avant l'ouverture du
  GUI du bloc.
- Un socle réutilisable pour les machines à cycle restantes : `AbstractMachineBlock`,
  `AbstractMachineBlockEntity`, `AbstractMachineMenu`, `AbstractMachineScreen`. Ajouter une
  machine « standard » = une block entity (cycle), un bloc/menu/écran de 3 méthodes chacun, et
  quelques lignes de datagen.
- **Recettes de fonctionnement data-driven** (`com.veskorius.recipe`) : **un `RecipeType` par
  machine**, recettes en **JSON** — modifiables/ajoutables par datapack, sans recompiler.
  - `veskorius:stabilizing` / `assembling` / `purifying` : recettes input→output partageant la
    classe `MachineRecipe` (ingrédients item/tag **+ count**, résultat, **temps**, **Osc/tick**).
    Cycle générique via `AbstractProcessingMachineBlockEntity` — une nouvelle machine input→output
    ne code aucune recette. Le Crystal Crusher (`veskorius:crushing`) en est le dernier exemple :
    première machine à **une seule entrée**, elle a réutilisé le socle sans le modifier. Sa
    poussière a aussi débloqué la **branche alternative de l'Assembler** (3 Resonance Dust + 2 Iron)
    en un simple second JSON, sans une ligne de code — la promesse annoncée ci-dessous, réalisée.
  - `veskorius:sharpening` : le Whetstone (réparation), forme à part (catalyseur, **% réparé**,
    temps ; l'outil réparé est calculé, pas un résultat fixe).
  - Conséquence directe : la « branche alternative » de l'Assembler (3 poussière + 2 fer) est un
    simple second JSON, zéro code — désormais faite (avec le Crystal Crusher).
- **Système d'énergie de Résonance (le champ)** : capability `IResonanceField`,
  `ResonanceFieldManager` (routage machine→émetteur par champ, pas de câble), le **Field
  Emitter** (#4) — réserve de 4000 Osc rechargée en brûlant des Stable Crystals, portée 8, avec
  un **GUI dédié** (jauge de réserve `X/4000 Osc`) — et la consommation d'Osc branchée dans le
  socle des machines (`getOscPerTick`). Le Component Assembler en est le premier client.
- **Harmoniques & Dissonance** (`06`, le système-signature) : un champ a une **bande** (une
  **couleur** : violet / cyan / ambre), une machine « écoute » sur une bande. Même couleur = propre ;
  couleur différente = **ça marche quand même**, mais ça coûte plus d'Osc et ça injecte de la
  **dissonance** dans l'émetteur (coupole qui grisaille → champ intermittent). C'est le remplaçant
  du câble : deux émetteurs de bandes différentes alimentent deux groupes de machines au même
  endroit, sans un fil. Codé : bandes + désaccord + dissonance (plafonnée, persistée, décroissante),
  **Émetteur Accordable**, mode « Accorder » du Tuner (réversible : le cycle revient à
  « universelle »), **Damping Array** (agents data-driven → `resonance_sludge`), flag de recette
  `stable` (toutes les recettes T1 le portent), **glow des machines coloré par bande** (clignotant
  si désaccordée) et **HUD de champ** (bande / réserve / dissonance) visible en portant le
  **Resonance Locator** — inventaire ou slot **Curios** (dépendance douce, par réflexion : aucune
  dépendance de build). **La T1 ne gagne aucune complexité** : une machine sans bande est
  universelle et n'affiche rien. Interrupteur maître dans `veskorius-harmonics.toml`. Au **plafond**
  de dissonance, l'émetteur libère une **décharge de résonance** : impulsion AoE qui blesse à portée
  (type de dégâts dédié, message de mort de lore) et purge une partie de la saturation — le champ
  se rétablit si la cause disparaît, re-décharge sinon. **Le système est complet** (reste la passe
  visuelle Phase 6).
- Datagen complet : plus aucun blockstate / modèle / recette / loot table / tag / traduction
  n'est écrit à la main.
- **Resonance Storage Cell** (#6) : batterie portable (item, 8000 Osc, charge sur l'item via Data
  Component). Se recharge dans un champ — tant qu'elle est dans l'inventaire d'un joueur couvert
  par un émetteur, elle prélève ≤20 Osc/tick sur la réserve de celui-ci (même source que les
  machines, aucune conversion cachée). Tooltip + barre de charge ; `extractCharge` prêt pour le
  Locator (tâche 8). Son consommateur viendra avec les structures.
- **Configuration modpack** (`14-Configuration.md`) : philosophie « data-driven d'abord ». Tout le
  contenu (recettes de fonctionnement/craft, tags, loot, worldgen) est déjà surchargeable par
  **datapack**. Les constantes d'équilibrage codées en dur (portée/capacité du champ, capacité
  et débit de la Storage Cell, bonus d'augment, facteurs de surchauffe) passent par un
  `ModConfigSpec` **SERVER** (`VeskoriusConfig`, `veskorius-server.toml`) — synchronisé, par monde,
  livrable via `defaultconfigs/`. Lues à l'exécution ; défauts verrouillés sur le design par un test.
- **Carburants du Field Emitter data-driven** : type de recette `veskorius:fueling`
  (`ingredient → osc`). Par défaut un seul carburant (Stable Crystal, 4000 Osc) ; un datapack en
  ajoute/retire ou change les valeurs sans recompiler. Affiché dans JEI (carburant → Osc). A
  remplacé le filtre d'item et la valeur d'Osc jusque-là en dur.
- **Première entité — Fileur de Cristal** (`crystal_strider`, 09-Entities.md) : faune neutre des
  poches (ne combat jamais, fuit quand blessé). **Traite** au clic droit à main nue (1 Raw Crystal,
  cooldown 5 min) et **reproduction** au `resonance_spore` (élevage vanilla). Œuf d'apparition,
  spawn souterrain (densité à valider en playtest), modèle/renderer placeholder. Met en place le
  socle entités (`ModEntities`, événements d'attributs/placement, rendu client) réutilisable pour
  les mobs suivants. Le Custode et le bloc de récolte du spore restent à coder.
- **Structures T1-T2 + gatekeeping physique** (`03`/`08`) : deux ruines (Habitation Modeste,
  Avant-poste) sont de **vraies `Structure` vanilla en jigsaw** (migrées de features, A7). Leurs
  pièces sont des **NBT générés par datagen** (salle de pierre veinée, coffre à loot, console, et un
  **Custode gardien intégré** à la pièce de l'Avant-poste). Elles sont taguées `#veskorius:locatable`
  → **`/locate` et le mode Structures du Locator fonctionnent**. Le déblocage d'un tier passe par un
  objet-clé, le `resonance_blueprint` — **ingrédient rendu** dans les recettes du tier (aucune
  recette masquée, tout est visible dans JEI ; ce qui bloque, c'est de ne pas avoir le plan). Le T2
  s'obtient en **réveillant la console** (`attunement_console`) de l'Avant-poste sur place. Les
  `codex_fragment` sont du lore lisible, pas un gate. Advancements de feedback. *(Fréquence de
  génération = JSON `structure_set`, à valider en playtest.)*
- **De vrais donjons** (`17-Dungeons.md`, 2026-08-07). Les pièces de structure étaient des salles
  plates, identiques d'une ruine à l'autre, bâties en deepslate vanilla — et **sans un seul bloc
  jigsaw** : la profondeur d'assemblage valait 1, donc ajouter une pièce à un pool *remplaçait* le
  bâtiment au lieu de l'agrandir. Rien ne le signalait, la structure se générait très bien,
  simplement toujours seule. Ce qui existe maintenant :
  - **Une doctrine** : *le donjon est une machine morte, l'explorer c'est la rallumer*. La clé est
    toujours **un champ**, jamais un objet (pilier 3 rendu spatial) ; la lumière raconte l'état du
    réseau ; le danger principal est la **dissonance**, pas le mob.
  - **Trois mécaniques** : le **sas de Résonance** (indestructible, ouvert seulement dans un champ),
    les **conduits et lampes** qui s'allument avec le champ, et l'**émetteur ancien** — qui est
    *littéralement* un `field_emitter`, même bloc et même block entity, donc il ne peut pas diverger
    de la machine que le joueur fabriquera ensuite.
  - **Onze blocs d'architecture** (maçonnerie de pierre veinée, gravée, dalle/escalier/muret, lampe,
    conduit, efflorescence de dissonance), textures générées par `tools/block-textures`.
  - **Une architecture, pas des cubes** (seconde passe). Chaque salle est **creusée dans la masse**
    et chemisée séparément — il reste de la **roche** entre elles, et on circule par des galeries.
    Trois gestes non négociables : **voûte en berceau**, **angles coupés**, et surtout
    **effondrement causal** (le trou dans la voûte, et la matière manquante **en cône exactement
    dessous** — c'est cette correspondance, et elle seule, qui se lit comme « ça s'est écroulé »).
    Plus, pour ce qui doit être remarquable : **rotonde octogonale à coupole** et **escalier en
    vis**.
  - **L'Avant-poste** : deux niveaux voûtés reliés par la vis, plus la rotonde de la console
    derrière le sas. On l'ouvre en réveillant l'émetteur ancien avec un Stable Crystal — **le
    coffre-réserve en garantit un sur place**, contenu fixe, pour qu'aucun tirage ne garde la porte
    du T2. Allumer réveille aussi les Custodes en alcôve : **allumer, c'est armer le donjon**.
  - **L'Habitation Modeste** devient un **hameau** (logis, atelier, citerne, logis effondré).
  - **Une échelle de ruines** : bornes de conduit (~1 / 130 blocs), bouts de galerie ensevelis,
    chambres englouties. Le monde n'avait que deux structures, toutes deux grandes ; une
    civilisation effondrée laisse surtout des miettes, et c'est le contraste avec elles qui donne
    son poids à un vrai donjon.
  - **Un parcours automatisé de bout en bout** (`outpostIsWalkableFromEntranceToConsole`). Un
    donjon écrit par code peut être **parfaitement valide et infranchissable** : une galerie qui ne
    perce aucun des deux murs qu'elle relie, un escalier en vis dont la première marche tombe à
    l'opposé de la galerie qui y mène (on en sort dans le vide, dix blocs de chute). **Les deux
    étaient présents** et aucune relecture ne les avait vus.
  - **Le jigsaw est réel** : connecteurs, profondeur 5, pools de bouchons en `fallback` (plus de
    branche ouverte sur la roche), processors d'usure (deux ruines ne s'abîment plus pareil),
    spawn overrides (fini les zombies vanilla dans nos ruines), `IGNORE_WATERLOGGING`.
  - **Phase E** : le **Poste de Garde** devient une *tour inversée* — une garnison qui descend,
    escalier en vis ouvert sur un puits de 23 blocs (le combat devient positionnel), alcôves vides
    en haut et occupées en bas, arsenal voûté au fond où l'on débouche **par le plafond**, à
    découvert. Et le **Puits de Forage abandonné**, la structure qui manquait au T1 : un fût de
    vingt blocs, six plateformes décalées, le foreur brisé au fond sur la poche de cristal qu'il
    cherchait. Elle ne débloque rien — elle enseigne « descendre = cristaux » dans la première heure.
  - **Phase F — le Sigma Laboratory**, et le seul vrai puzzle du mod. Une **roue** :
    sanctuaire octogonal scellé, déambulatoire annulaire, quatre ailes. Deux **relais
    endommagés** à réparer avant que le premier ne retombe (90 s). La simultanéité n'a demandé
    aucune serrure à deux clés : un relais **rediffuse**, il ne produit pas, donc il ne se répare
    que s'il est déjà dans un champ. Les deux sont posés **en chaîne** — A dans la portée de
    l'émetteur encore vivant, B dans la portée de A seulement, le sas dans la portée de B
    seulement — et la simultanéité devient une **contrainte de trajet**. A→sas = 21 blocs pour une
    portée de 20 : c'est ce chiffre qui fait exister le puzzle.
  - **Cinq tests d'invariants**, dont `optionalWingsNeverCarryTheCriticalPath` : le chemin critique
    du T2 vit dans la pièce de départ, jamais dans un pool. C'est la troisième fois que le mod se
    protège de cette classe de bug ; cette fois dans les deux sens.
- **Resonance Locator** (#7) : détecteur de résonance à courte portée (~40 blocs). Clic droit →
  ping directionnel vers la source la plus proche — poche de cristal (utile maintenant qu'elles
  sont rares) ou signature de champ (Field Emitter). Batterie interne 100 Osc (5/ping), rechargée
  dans un champ ou en puisant sur une Storage Cell portée.
- **Crystal Roost** (#8) : production passive de cristal brut (2 Quartz → 1 Raw Crystal, 600 s) à
  condition qu'un **Fileur de Cristal** soit à moins de 6 blocs — alternative lente au minage,
  pertinente maintenant que les poches sont rares. Réutilise le socle process + un gate de proximité.
- **Custode** (garde réactif) : 30 PV / 6 dégâts, ne cible un joueur qu'à **6 blocs** (ou s'il est
  frappé) — jamais agressif à distance. **Posé par la génération de l'Avant-poste** (garde le site),
  pas de spawn errant. Drop 2-4 `custode_alloy_fragment`, **substitut 1:1 du fer** dans les recettes
  Veskorius (tag `iron_substitutes`) — récompense le combat plutôt que le minage.
- **Passe de polish** (configs/actions/mécaniques) : config étendue (`basics.portable` et
  `mobs.custode`/`mobs.fauna` — Locator, Custode, Fileur, Roost tous tunables) ; **défense de site** (casser une machine Veskorius alerte
  les Custodes proches) ; retours de la traite du Fileur (son, particules, message de cooldown) ;
  sons des mobs.
- **Récolte de spore** : la Resonance Veined Stone pousse un `resonance_spore` (état `spored`) sur
  une face exposée en faible luminosité, récolté au clic droit (sans casser la pierre), puis
  repousse — la reproduction du Fileur devient jouable en survie. Taux de pousse configurable.
- Harnais `GameTest` : **165 tests** (machines, augments, automatisation d'objets, Codex, harmoniques, structures… +
  Custode, défense de site, récolte de spore, amorçage T2 garanti, défauts de config), en **deux
  processus** (`runFastGameTests` / `runWorldGameTests`, voir « Mise en route »). Le serveur de
  test charge tout le datapack sans erreur. Ce qui est
  **visuel ou réseau** (rendu des GUI et du HUD, particules, coupole, génération réelle des
  structures) n'est pas couvert ici : ça se valide en `runClient` — **passe faite le 2026-07-25, tout
  est bon** (seul le pont **Curios** reste non vérifié, le mod n'étant pas une dépendance de dev).
- **Intégration JEI** (dev) : les recettes des 4 machines s'affichent dans JEI, une catégorie par
  machine, avec temps et Osc/tick. JEI est en `compileOnly` (API) + `localRuntime` (mod complet
  dans `runClient`), pas exporté dans le jar. Sert à vérifier les recettes en jeu.
- **Génération de monde** : feature custom `crystal_pocket` (data-driven) — poches de
  `resonance_crystal_cluster` (Y -20 à 0), enrobées d'une coquille de `resonance_veined_stone`
  (le « tell » visuel), avec ~15 % de croûtes de `raw_flux_deposit` **brossables** (brosse
  vanilla → flux, alternative T1 au Quartz). La boucle T1 est jouable en survie, minage comme
  brossage.
- **Châssis par palier** (`fractured` T1 / `attuned` T2 / `veskorian` T3) : base **de craft** —
  une machine = le châssis de son palier + ce qui la distingue — **et de texture**, puisque le
  châssis porte les flancs et le dessus de toutes les machines de son palier. Le palier se lit
  donc sur le bloc, à distance. Chaque châssis contient le précédent. Détail : `05-Machines.md`.
- **Textures 16×16, calibrées sur des mesures** (blocs, items, GUI ; sources dans
  `tools/{block,item,gui}-textures/`, hors build Gradle). La règle qui les gouverne toutes vient
  d'avoir mesuré les assets plutôt que de les décrire de mémoire :
  - le vanilla tient en **4 à 19 couleurs** par texture (`stone` 4, `deepslate` 5,
    `blast_furnace_front` 16) — d'où **palette indexée et aucun fondu alpha**. Composer en fondu
    fabrique des dizaines de teintes presque identiques, ce qui donne de la bouillie quel que
    soit le style ;
  - il est en revanche **très bruité** (46-85 % des pixels diffèrent de leur voisin) mais à
    faible amplitude : ce qui fait la matière est la *fréquence* du grain, pas son amplitude ;
  - le marbre d'Astral Sorcery est **quasi blanc et neutre** (luminance 203-238, R=G=B) — le
    contraste vient de l'accent, jamais du matériau. D'où le marbre veskorien, encrassé au T1,
    blanc restauré au T2, sombre poli au T3 ;
  - un GUI vanilla est un **aplat** (four : 6 couleurs, panneau `#c6c6c6`) — donc aucun grain
    sur les panneaux, contrairement aux blocs.
  - **et les caissons non plus n'ont aucun grain.** La règle du bruit vaut pour de la pierre et
    du marbre, matières qui *sont* granuleuses. Un châssis est une tôle : un semis de pixels
    dessus ne se lit pas comme du métal mais comme de la saleté. Pire, il fabrique un motif qui
    se répète à chaque bloc — sur un mur de châssis connectés, dont tout l'intérêt est de ne
    plus montrer où un bloc finit, c'est l'inverse exact de l'effet cherché. La plaque est un
    **aplat d'un seul ton** ; le relief vient du cadre, de l'ombre qu'il porte, et du demi-pixel
    dont il déborde.
  Les formes symétriques passent par des tables de spans vérifiées (`x0 + x1 = 15` par ligne,
  marge d'1 px) : sur une grille 16×16 le centre tombe entre les pixels 7 et 8, et centrer sur 8
  décale tout d'un demi-pixel.
- **Une façade par machine, pas un logo dans un cadre** : cristal serré dans des mors, plaques
  boulonnées, roue sur socle, mâchoires, cuve à niveau, caisse percée, lentille, grille. Idem en
  GUI, **un fichier par machine** (bande de titre à sa couleur + bandeau d'atelier).
- **États actif/inactif visibles** : les variantes `lit` pointaient le même modèle et le modèle
  était un `cube_all` — une machine en marche, à l'arrêt ou hors champ étaient identiques, et
  l'orientation invisible. Les machines sont maintenant orientées, avec façade éteinte et façade
  allumée ; les émetteurs ont gagné une propriété `LIT`.
- **Une silhouette 3D par machine** : socle à étage, presse, roue sur bâti, deux mâchoires
  séparées par un vide traversant, cuve élancée, nichoir évidé, grille ajourée, tour d'émission,
  pupitre incliné pour la console. `noOcclusion()` partout où le bloc n'est plus un cube plein.

> **État visuel (2026-08-06)** : reconnaissable et cohérent, **pas abouti**. Le porteur du projet
> juge le rendu encore insatisfaisant esthétiquement ; la passe de finition reste à faire. Ce qui
> est acquis et à ne pas re-perdre, c'est la *méthode* ci-dessus — les quatre versions précédentes
> ont échoué en composant en fondu alpha, pas en choisissant le mauvais style.

Consulter `veskorius-design/13-Registry-Index.md` pour l'état « codé / à coder » de tout le
contenu prévu : c'est le point de départ avant de reprendre le développement, plutôt que de
deviner où on en est.

## Mise en route

1. `./gradlew runClient` (ou via IntelliJ : Gradle > Tasks > neoforge > runClient) pour lancer
   le jeu avec le mod chargé.
2. `./gradlew runData` après tout ajout de bloc/item/recette : régénère `src/generated/resources/`.
   **Ce dossier EST versionné**, et il l'est depuis qu'on a mesuré le coût de l'inverse : ignoré,
   il donnait à un clone frais un `./gradlew build` qui réussit et un jar sans un seul modèle,
   sans une recette, sans une traduction. Vert au build, mort en jeu. Le commit de la datagen
   fait aussi que ses diffs se relisent — c'est là qu'on voit qu'une recette a changé de coût.
   **À lancer avant les GameTest** : c'est lui qui produit le template de structure vide dont
   les tests ont besoin. Le cache de datagen est vidé automatiquement avant chaque run, donc la
   sortie correspond toujours au code (une édition manuelle d'un JSON généré, pour tester une
   recette + `/reload`, est transitoire : le prochain `runData` la remplace). Dépendances déjà
   en cache → ajouter `--offline` évite un accès réseau.
3. Les GameTest, **en deux processus** :
   - `./gradlew runFastGameTests` — 144 tests, ~27 s. C'est celui qu'on lance en boucle.
   - `./gradlew runWorldGameTests` — 21 tests de donjons, qui génèrent du monde.
   - `./gradlew runAllGameTests` — les deux à la suite.

   La séparation n'est pas cosmétique : dans une seule JVM, les tests de donjons accumulaient
   assez de monde chargé pour que la suite finisse par se figer. `runGameTestServer` n'existe
   plus, précisément pour que personne ne relance le chemin qui bloquait.
4. `./gradlew audit` : vérifie la cohérence code ↔ ressources générées ↔ dossier de design
   (blockstates et modèles manquants, loot table absente, incohérence tag d'outil /
   `requiresCorrectToolForDrops`, parité des traductions EN/FR, chiffres de
   `18-Etat-des-lieux.md`). Sort en erreur si quelque chose cloche.
5. **Sous Windows, cloner dans un chemin court.** Le plus long chemin du dépôt fait 148
   caractères (`.../advancement/recipes/building_blocks/veined_stone_brick_stairs_from_resonance_veined_stone_stonecutting.json`,
   un nom que la datagen vanilla compose toute seule). Au-delà de MAX_PATH, le checkout
   n'échoue pas franchement : il réussit **en partie**, en laissant des fichiers manquants et
   un `error: Filename too long` noyé dans la sortie. Si le dossier de destination est profond :

   ```
   git config --global core.longpaths true
   ```
6. `gradle.properties` : la version `neo_version=21.1.172` est celle utilisée pour valider le
   build. Si la résolution de dépendance échoue, vérifier la dernière version patch sur
   https://projects.neoforged.net/neoforged/neoforge.

## Conventions de code à respecter pour les machines suivantes

Elles ne sont pas décoratives — s'en écarter casse le socle générique :

- **Le dernier slot déclaré d'une machine est son (premier) slot d'augment.** Le socle en réserve
  `MAX_AUGMENT_SLOTS` (4) à la suite ; le nombre **actif** est réglable en config (`augmentSlots`,
  défaut 1 = comportement historique), avec une **règle de cumul** (FORBID/CAPPED/FREE) qui borne le
  cumul d'un même effet. C'est ce qui permet à `AbstractMachineBlockEntity` de gérer les augments
  sans code par machine.
- **Les slots de la machine sont ajoutés au menu avant l'inventaire du joueur**, donc l'indice
  d'un slot dans le menu est exactement son indice dans l'inventaire de la block entity.
- **Une entrée qui aura d'autres membres plus tard passe par un tag, pas par un item en dur**
  (voir `ModTags`) — ça transforme une évolution prévue en un ajout de datagen d'une ligne.

## Ce qui reste a faire

**La liste vit dans `veskorius-design/11-Development-Plan.md`**, avec les recettes exactes, les
chiffres d'equilibrage et les dependances entre taches. Et l'etat reel de chaque element est dans
`13-Registry-Index.md` et `18-Etat-des-lieux.md`.

Cette section listait autrefois les taches restantes elle-meme. Elle a fini par reclamer du travail
deja fait — Locator, Custode, Crystal Roost y figuraient comme « a coder » alors qu'ils tournaient
depuis longtemps. Une liste dupliquee derive, et une liste de taches qui derive fait recoder
l'existant. Elle ne sera pas retablie ici.

Les cinq paliers sont jouables de bout en bout. Ce qui reste tient de la finition : la passe
esthetique (voir l'encadre plus haut), le mode Tuner « retrait d'augment en place », et les
arbitrages de design encore ouverts, listes dans le dossier.
## Structure

```
src/main/java/com/veskorius/
├── Veskorius.java              ← point d'entrée, registres, onglet créatif
├── block/
│   ├── ModBlocks.java
│   ├── ResonanceStabilizerBlock.java
│   └── entity/                 ← AbstractMachineBlockEntity + machines
├── item/ModItems.java
├── menu/                       ← AbstractMachineMenu + menus de machines
├── client/                     ← écrans (Dist.CLIENT uniquement)
├── config/                     ← VeskoriusConfig (ModConfigSpec SERVER, réglages modpack)
├── entity/                     ← ModEntities + mobs (CrystalStrider) + événements (attributs/spawn)
├── energy/                     ← IResonanceField, ResonanceFieldManager, capabilities
├── recipe/                     ← MachineRecipe(Input/Serializer) + RecipeTypes/Serializers
├── worldgen/                   ← ModWorldGen (features + biome modifier, data-driven)
├── compat/jei/                 ← intégration JEI (chargée seulement si JEI présent)
├── datagen/                    ← providers + GatherDataEvent
├── gametest/                   ← tests joués par runFastGameTests / runWorldGameTests
└── tag/ModTags.java

src/main/resources/assets/veskorius/textures/  ← textures, produites par tools/{block,item,gui}-textures
src/main/resources/logo.png                    ← icône de la liste des mods, par tools/logo/genlogo.js
src/generated/resources/                       ← tout le reste, produit par runData (VERSIONNÉ)
src/main/templates/META-INF/neoforge.mods.toml ← généré vers le jar au build
```
