# 16 — Révision & Expansion (corrections Phase 1 + conception Phase 2)

Ce document consolide une passe de conception demandée par le porteur du projet (2026-07-23) après
l'audit de cohérence Phase 1/2. Il **prime** sur les fichiers 01-15 là où il les contredit ; les
changements sont répercutés dans les fichiers canoniques au fur et à mesure de l'implémentation
(section 9 : liste des répercussions). Les piliers (`01-Vision-Pillars.md`) restent la contrainte
absolue : **pas de tuyaux pour l'énergie**, lecture spatiale du monde, 5 tiers, thème de restauration.

Chaque proposition suit le format : **problème → proposition → pourquoi (piliers) → décisions ouvertes**.

---

## 0. Statut des décisions — CANONIQUE vs OUVERT (2026-07-23)

Ce tableau tranche ce qui **n'est plus une décision** (canonique, à répercuter dans les docs et à
coder) et ce qui **reste à trancher** (phase de réflexion à venir). Le porteur du projet a validé ces
directions.

| # | Sujet | Statut | Ce qui est verrouillé | Ce qui reste ouvert |
|---|---|---|---|---|
| 1 | Locator | **CANONIQUE** | Outil **à modes** (Ressources / Structures) ; **efficacité** algorithmique (fin du scan de cube) ; détection de structures via l'**API de structure vanilla** | Gating « mode Structures au tier 2 de l'outil » ; portées exactes |
| 2 | Expansion de contenu | **CANONIQUE (cadre)** | Le contenu découle du système **Harmoniques & Dissonance** (`06`) plutôt que d'une liste de machines « filler » : Émetteur Accordable, Damping Array, Reclaimer, Advanced Assembler, augments variés | Contenu additionnel au-delà de ce noyau |
| 3 | Gaz → biome | **CANONIQUE (direction)** | Le gaz vit dans un **biome profond custom rare** où spawnent les structures profondes (fin de la boucle punitive) ; intérieurs scellés ; armure réduit le gaz | Nom du biome, rareté, effet exact (MobEffect vs dégâts) |
| 4 | Placement Custode Lourd | **CANONIQUE** | Custode Lourd ↔ Sigma/Archive ; Custode standard ↔ Avant-poste/Poste de Garde | Variété/densité de mobs, `resonance_wisp`, variante affaiblie |
| 5 | Déchets / calibration | **CANONIQUE** | Remplacé par **Harmoniques & Dissonance** (`06`) : la « calibration » devient dérive/désaccord harmonique, le déchet devient **dissonance cristallisée** (substance de l'Effondrement, instable si non contenue), la gestion passe par une **infrastructure de damping** (container à saturer) et non un slot. Le désaccord **ne bloque jamais** ; flag de recette `stable`. Tout modulable en config, jusqu'à OFF | Valeurs d'équilibrage |
| 6 | Synthesizer « via Relay » | **CANONIQUE** | **Relâché** : alimenté par n'importe quel champ (comme les autres machines) | — |
| 7 | Usages Concentrated Flux | **CANONIQUE** | **Consommable de damping** du Damping Array (son usage T3), + carburant premium de l'émetteur, + Convergence Core en Phase 4 | — |
| 8 | Vraies structures + configs | **CANONIQUE** | Migration vers le système `Structure` vanilla, **en jigsaw** (seul choix qui scale au massif — cf. `08`), **configs de spawn data-driven** (spacing/separation/biomes/strate), vrais layouts | — |
| 9 | Gating par blueprint | **CONTESTÉ — à revisiter** | — | Le porteur n'aime pas le modèle blueprint. Reste en vigueur (codé/testé) mais **ne rien bâtir dessus** ; session dédiée à prévoir (voir `03`) |
| 10 | Configs par thème | **CANONIQUE** | Découpage en specs thématiques (`basics`/`machines`/`harmonics`/`generation`/`structures`/`mobs`), **interrupteur maître par système**, doctrine « tout doit pouvoir se désactiver » (`14`) | — |
| 11 | Augments multi-slots | **CANONIQUE** | N slots configurables + **règles de cumul** (dans un slot / entre slots) en config ; nouveaux augments (Efficiency/Yield/Tuning/Damping Core) | Valeurs |

**Ordre de chantier (tout est désormais canonique) :**
1. **Découpage des configs par thème** (`14`) — fondation : chaque système arrive avec son
   interrupteur maître.
2. **Harmoniques & Dissonance** (spec complète dans `06`) — bandes, accord/désaccord, dissonance
   spatiale, Damping Array, flag `stable`, HUD de champ.
3. **Migration Structures en jigsaw** (`08`) + configs de spawn → allume le mode Structures du Locator.
4. **Biome `resonant_deeps` + gaz par strate** (`07`) — scaling et ordre imposé.
5. Contenu T3 (Alloy Forge/Slag Vent, Relay, Synthesizer, Driller, Compressor, Advanced Assembler,
   Reclaimer) puis structures/mobs.

⚠️ Seul point **non tranché** : le **gating par blueprint** (§9 ci-dessus) — contesté, à revisiter en
session dédiée ; ne rien bâtir de nouveau dessus.

> **✅ Fait (2026-07-23) — Locator à modes.** L'outil est désormais à modes (`LocatorMode`
> RESOURCES / STRUCTURES, Data Component `locator_mode`, maj+clic droit change de mode). Le mode
> Ressources garde le scan borné + l'index des émetteurs ; le mode Structures interroge l'**API de
> structure vanilla** (`findNearestMapStructure` sur le tag `#veskorius:locatable`) — **aucun scan de
> blocs**, et se remplit automatiquement à la migration vers de vraies Structures (tag vide → retourne
> proprement « aucune structure »). Tooltip du mode, i18n en/fr, 2 GameTest (cycle + no-op gracieux).
> 78 tests verts. Reste Phase 2 : la migration Structures elle-même (qui allume le mode).

---

## 1. Locator — outil à modes + tiers + index O(n)

**Problème.** `07` promet une détection de structures à 200/400 blocs ; le code ne détecte que
cristaux (16 bl) + émetteurs (~40 bl), par un scan de cube coûteux. Aucun repérage de structure.

**Proposition.**
- **Outil à modes** (comme le Tuner) : le Locator porte un *mode courant* stocké en Data Component.
  - `RESSOURCES` — poches de cristal, dépôts de flux (radiation courte).
  - `STRUCTURES` — grandes structures indexées (Avant-poste, Sigma Lab, Archive…).
  - Clic droit dans le vide = mode suivant ; clic droit = ping du mode courant.
- **Tiers de l'objet (upgrade)** : le Locator se raffine.
  - **T1 (base)** : mode `RESSOURCES` seul, portée courte.
  - **T2 (raffiné)** : débloque le mode `STRUCTURES` et augmente la portée. Upgrade par craft
    (Locator + une *Lentille de Résonance*, nouvel item, ou Refined Crystal + Component). Idée
    alternative retenue de `07` : le mode structure s'affine encore une fois une structure d'un type
    **déjà visitée** (portée ×2 sur ce type) — la connaissance améliore l'outil.
- **Efficacité — le point clé** : **remplacer tout scan de blocs par un index O(n)**. On enregistre
  à la génération, dans une `SavedData` par dimension (`ResonanceSurveyData`), le **centre de chaque
  poche de cristal** ET la **position de chaque structure** (avec son type). Le Locator ne scanne
  plus rien : il interroge l'index (« la poche / structure de type X la plus proche dans le rayon »).
  Gain : de O(r³) (~275k blocs/ping historiquement) à O(nombre d'entrées), et une détection de
  structure enfin possible.
  - Migration : les poches et structures étant générées par nous, on les inscrit dans l'index au
    moment de leur `place()`. Rétro-compat des mondes existants : un scan de chunk paresseux au
    chargement peut ré-indexer, ou on assume que seul le contenu nouvellement généré est indexé
    (acceptable en dev).

**Pourquoi.** Rend le pilier 2 (lecture spatiale) littéral et *performant* ; l'upgrade de l'outil
est une micro-progression satisfaisante ; l'index sert aussi la Phase 2 (trouver un Sigma Lab).

**Décisions ouvertes.** Coût exact de l'upgrade T2 ; la portée par mode ; faut-il un 3ᵉ mode
`FAILLES` en T5 (probablement non — les Failles se lisent par les fissures, pas par l'outil, `07`).

---

## 2. Structures réelles (système vanilla) + configuration de spawn

> **✅ FAIT le 2026-07-23 (A7, suite : 96 GameTest).** L'Habitation Modeste et l'Avant-poste sont
> désormais de **vraies `Structure` vanilla en jigsaw** (`ModStructures` : `template_pool` →
> `structure` → `structure_set`), leurs pièces sont des **NBT générés par datagen**
> (`ModStructurePieceProvider`, aucune ressource écrite à la main), et le tag `#veskorius:locatable`
> les référence → **`/locate` fonctionne et le mode Structures du Locator s'allume**. La
> `RuinFeature`/`RuinConfiguration` sont **supprimées** (code mort retiré). Choix retenu : **jigsaw à
> pièce unique** — on honore « structures en jigsaw » (agrandir = ajouter des pièces au pool) sans
> encore authorer de connecteurs sur des pièces placeholder. **Décisions prises au codage** (à
> ✅ confirmées par le porteur le 2026-07-24) : (a) le **tell de surface** (pilier) est abandonné — sa raison d'être était
> « repérer une fois le T2 acquis », ce que `/locate` + Locator couvrent mieux ; (b) **pas de
> `veskorius-structures.toml`** : fréquence/espacement/biomes vivent dans le JSON `structure_set`/
> `structure`, déjà surchargeable par datapack — un TOML redondant violerait la règle « pas de clé
> qui ne fait rien » de `14`. Reste Phase 6 : vrais layouts multi-pièces (aujourd'hui une salle
> meublée).

**Problème (résolu).** Nos structures étaient des `RuinFeature` (boîtes creuses posées comme une
*feature*). Pas de `/locate`, pas de vraies pièces, placement approximatif. Le porteur veut **de
vraies structures façon village/forteresse**.

**Proposition.** Migrer du système *feature* vers le **système `Structure` vanilla** :
- `StructureType` + `Structure` custom (ou basées sur des templates NBT via `TemplatePool`/jigsaw),
  avec `StructurePiece`s réelles → vraies salles, couloirs, décor, `/locate` gratuit, et intégration
  propre à l'index du Locator (on peut lire les références de structure d'un chunk).
- **Configuration de spawn data-driven** via `StructureSet` + `RandomSpreadStructurePlacement`
  (spacing / separation / salt) dans un JSON de datapack — donc **fréquence, espacement, biomes
  autorisés, strate Y** tous réglables sans recompiler. C'est la brique qui manquait.
- **Layouts réels** (au-delà de Phase 6 « textures ») : chaque structure gagne une vraie forme :
  - *Habitation Modeste* : 2-3 petites pièces, mobilier (déjà amorcé par `decorateInterior`, à
    porter en pièces de structure).
  - *Avant-poste* : atelier + réserve + salle de la console.
  - *Sigma Laboratory* : entrée, deux ailes verrouillées (les 2 Relais), salle centrale scellée.
  - *Poste de Garde* : corps de garde + cellule + tourelle.
- **Compromis assumé** : on réintroduit la complexité jigsaw/Structure que la Phase 1 avait évitée —
  mais c'est exactement ce qu'exige « de vraies structures localisables ». On garde `RuinFeature`
  seulement pour les micro-formations (amorces de surface), pas pour les bâtiments.

**Pourquoi.** Piliers 2 & 5 (le monde se lit, chaque tier change ce qu'on voit). Des structures
mémorables > des boîtes. Le `/locate` + l'index rendent l'exploration T2-T3 fonctionnelle.

**Décisions ouvertes.** Jigsaw (modulaire, plus de travail, plus de variété) **vs** templates NBT
fixes (plus simple, moins varié) — je recommande **templates NBT** pour un premier jet fiable, puis
jigsaw si on veut de la variété procédurale. Fréquences de départ (reprendre `07`).

---

## 3. Biome profond custom + gaz de Résonance (résout le pic de difficulté)

**Problème.** `06` : gaz partout sous Y-40 (dégâts + fatigue) sans Driller ; or le Driller anti-gaz
se débloque *au* Sigma Lab, lui-même dans la zone de gaz → boucle punitive, risque de blocage.

**Proposition.**
- **Créer un biome souterrain custom rare**, p.ex. `resonant_deeps` (« Profondeurs Résonantes »),
  généré en poches profondes (façon Lush Caves/Dripstone, par paramètres de bruit), **rare**. Le
  **gaz de Résonance est une propriété de CE biome**, pas de tout Y<-40. Le minage courant en
  profondeur redevient sûr ; le danger est localisé et *thématique*.
- **Les structures profondes (Sigma Lab, Archive, veines profondes) spawnent dans ce biome** → il
  devient un objectif d'exploration reconnaissable (ambiance, particules, son), et le gaz raconte
  enfin son lore là où il a du sens.
- **Gestion du gaz, non bloquante** :
  - Intensité *tankable* à l'entrée (fatigue + léger chip) — on peut atteindre le premier Sigma Lab
    avec de la nourriture/potions, comme un donjon hostile.
  - **Intérieurs de structure « scellés »** : l'air *à l'intérieur* d'une structure du biome ne gaze
    pas (on peut le déterminer via la pièce de structure, ou un bloc d'ambiance qui « purge » un
    rayon). Le joueur souffre en creusant vers/dans le biome, respire dans les salles.
  - **Driller** : supprime le gaz dans un rayon pour l'exploitation durable (base profonde).
  - **Armure en Alliage Veskorien** : réduit les dégâts de gaz (nouvelle utilité T3, boucle de
    récompense — au lieu de seulement « -déphasage » qui ne sert qu'en Phase 4).
- **Repérage** : le biome + les structures sont indexés (section 1) → le Locator T2 les trouve.

**Pourquoi.** Contient le danger (pilier 4 : danger *motivé*, ici par un lieu identifiable) ; ajoute
de la richesse worldgen ; donne un rôle T3 à l'armure ; supprime la boucle punitive.

**Décisions ouvertes.** Nom du biome ; rareté ; le gaz est-il un `MobEffect` custom (visible dans
l'HUD) ou des dégâts directs ? (recommandé : un effet custom `resonance_sickness` lisible).

---

## 4. Système de déchets + calibration (upkeep des machines évoluées)

> **⚠️ Section dépassée — remplacée par « Harmoniques & Dissonance » (`06-Energy.md`).** La v1
> ci-dessous (calibration générique par catalyseur + déchet) a été jugée trop générique (« ça ne se
> démarque pas d'un junk gameplay »). Elle est conservée comme trace du raisonnement ; **la spec qui
> fait foi est celle de `06`** : la calibration devient une **dérive harmonique**, le déchet devient
> de la **dissonance cristallisée**, et la gestion passe par une **infrastructure de damping**
> spatiale et visible plutôt qu'un compteur caché.

**Problème / demande.** Introduire un vrai système de déchets généré par certaines machines (pas
toutes), avec compteur interne, et une mécanique de **calibration par catalyseur consommé** ; sans
catalyseur → double conso + déchet + fail craft (input mangé). Trouver des **usages aux déchets**.

**Proposition — système transversal, opt-in par machine.**
- **Calibration** (T3+ « machines lourdes » uniquement, via une interface `Calibratable`) :
  - Une **charge de calibration** interne décroît (par cycle ou par jour MC).
  - Un **catalyseur de calibration** (item) la recharge, **consommé lentement** (une fraction par
    cycle). Candidat idéal : le **Concentrated Flux** (résout la machine orpheline, section 7) —
    ou un nouvel item `calibration_matrix`.
  - **Calibrée** : fonctionnement propre.
  - **Décalibrée (charge 0)** : la machine tourne encore mais *sale* → **conso Osc ×1.5**, **produit
    1 déchet / N cycles** (compteur interne), et **X% de fail craft** (input consommé, pas de sortie).
- **Déchets — items** :
  - `flux_slag` (déjà prévu : sous-produit du Alloy Forge, nuisance à venter).
  - `resonance_sludge` (nouveau) : déchet générique des machines décalibrées **et** produit à la
    place de l'input perdu en **surchauffe** (au lieu de simplement le détruire — la surchauffe
    gagne un sous-produit, connecte les deux systèmes).
- **Compteur interne** : un accumulateur `wasteAccumulator` sur la machine ; quand il franchit un
  seuil, un déchet est poussé en sortie (ou au sol si plein). Léger, pas de nouvel inventaire.
- **Usages des déchets** (essentiels pour que ce ne soit pas juste de la suppression) :
  - **Engrais** : `resonance_sludge` accélère `ancient_seed`/`resonance_bloom` (comme
    `resonance_dust`), voire fertilise des cultures vanilla.
  - **Recyclage** : une machine **Reclaimer** (T3, section 5) transforme un lot de déchets en une
    *fraction* de matériaux de base (slag → un peu de pierre/gravier « scorie », sludge → poussière),
    ou en bloc de construction terne.
  - **Construction** : `synthesis_residue` compressé en bloc (déjà prévu) ; scorie en bloc « brut ».
  - **Sink d'endgame** (option) : stabiliser une Faille consomme du déchet (le lore boucle :
    l'Effondrement était une sur-résonance ; on « nourrit » la fermeture avec les résidus).

**Pourquoi.** Rend le pilier « le réseau est vivant » littéral (entretien actif) ; la surchauffe et
la calibration deviennent des choix économiques (vitesse vs propreté vs upkeep) ; les déchets bouclent
dans l'économie au lieu d'être punitifs.

**Décisions ouvertes.** Catalyseur = Concentrated Flux ou item dédié ? Seuils (charge, N cycles, X%) →
à équilibrer et à mettre dans `VeskoriusConfig`. Quelles machines exactement sont `Calibratable`
(proposé : Synthesizer, Deep Synthesis Chamber, Extraction Array, Alloy Forge — les « lourdes »).

---

## 5. Expansion de contenu — anti-linéarité & variété de gates

**Demande.** Beaucoup plus de contenu (machines, procédés, items) pour donner à faire au joueur et
varier les façons de passer les gates. Ci-dessous, des propositions **classées par tier**, toutes
compatibles piliers (aucune ne transporte de l'énergie par tuyau). Marquées ⭐ = fort intérêt.

### T1-T2 — réduire la linéarité du début
- ⭐ **Variété d'augments** (le slot d'augment devient un vrai choix) : au-delà du Catalyst Core
  (+15% vitesse) → *Efficiency Core* (−20% Osc), *Yield Core* (~10% de double sortie), *Purity Core*
  (annule la production de déchet / le fail). Un seul slot → arbitrage réel.
- **Lentille de Champ** (augment du Field Emitter) : troque portée contre conso, ou concentre le
  champ dans une direction (planification de base).
- **Fileur — produit secondaire** : à la traite, faible chance d'un `resonance_down` (duvet) pour une
  branche décorative/textile mineure, ou un consommable (petit buff). Donne une raison d'élever au-delà
  du Roost.
- **Résonance & agriculture tôt** : autoriser `resonance_dust` comme engrais dès le T1 (déjà prévu),
  et amorcer une petite culture avant le T3 pour occuper l'early game.
- **Chemins T1 alternatifs** : le Crusher (poussière) et le brossage de flux existent ; ajouter un
  petit procédé (p.ex. *Résonance par décantation* : un bloc passif qui, dans un champ, convertit
  lentement de la poussière en éclats) — optionnel, pour multiplier les voies.

### T3 — largeur (le cœur de la demande Phase 2)
- ⭐ **Advanced Assembler / Matrix Weaver** : compose un `resonance_matrix` (composant composé) à
  partir de Component + alliage conductif → **intermédiaire requis pour les machines T4**. Ajoute une
  étape de fabrication et un item de procédé.
- ⭐ **Reclaimer (recyclage)** : déchets → fraction de matériaux / blocs (voir section 4). Boucle
  l'économie de déchets.
- ⭐ **Field Surveyor** (outil T3) : affiche la **couverture de champ** en particules sur une zone
  (planification de réseau) — prolonge la coupole d'émetteur déjà codée.
- **Variantes de Relais** : *Focusing Relay* (portée longue, cône étroit) vs *Diffuse Relay* (courte,
  large) — choix de topologie de réseau, pas juste « plus de portée ».
- **Émetteur de Champ Portable** (item déployable) : pose un champ temporaire pour un chantier
  distant (mine profonde), consomme des cristaux — mobilité sans casser « pas de câble ».
- **Alliages spécialisés** : *Dampening Alloy* (manipulation de déchets/gaz : composant du Slag Vent,
  de l'armure anti-gaz) — sépare encore les chaînes de matériaux (choix de planification, cf. `04`).
- **Blocs de construction** : `resonance_glass` teignable, variantes d'`veskorian_alloy_block`
  (dalles/escaliers/murs), `ancient_conduit_stone` (tell des structures Architectes, minable pioche
  T3) — build-out esthétique + tells.

### Variété de gates (passer les paliers de plusieurs façons)
Le modèle « une clé physique par tier » (`03`) reste, mais on peut **varier l'acquisition** et
**doubler les sources** sans casser le pilier 2 :
- Le blueprint T3 vient du Sigma Lab (réparer 2 Relais). **Alternative** : une petite structure
  *Relais Station* (plus commune) où réparer **un seul** grand relais donne un *blueprint partiel*
  qui, combiné à un second trouvé ailleurs, forme le T3 — pour ceux qui n'ont pas trouvé de Sigma
  Lab. (Optionnel ; garde la découverte physique.)
- **Objectifs secondaires** dans les structures (loot bonus, mini-défis) pour récompenser
  l'exploration au-delà du strict blueprint.

**Décisions ouvertes.** Lesquels retenir pour la v1 Phase 2 (je recommande le noyau ⭐ :
augments variés, Advanced Assembler, Reclaimer, Field Surveyor) vs différer. Le reste enrichit sans
bloquer.

---

## 6. Entités — variété & placement

**Problème.** Dev-plan : « Poste de Garde + Custode **Lourd** », mais `08`/`09` : Poste de Garde =
Custode **standard**, Custode Lourd = Sigma/Archive.

**Proposition.**
- **Corriger le placement** : Custode Lourd ↔ Sigma Lab/Archive ; Custode standard ↔ Avant-poste/
  Poste de Garde.
- **Custode Lourd** (60 PV / 9 dég., rayon 8, alerte un pair à ≤16 bl) — réutilise le pattern Custode.
- **Plus de variété dans les structures** :
  - **Custode Archiviste** (mini-boss, `09`) dans la salle profonde optionnelle de l'Archive.
  - **Variante affaiblie de Custode Lourd** dans l'Avant-poste (rampe de difficulté douce, question
    ouverte de `09`).
  - **Faune/ambiance** : `resonance_wisp` (particule vivante inoffensive dans les structures
    résonantes) pour habiter les lieux sans combat.
  - **Densité** : plusieurs Custodes standard dans un Poste de Garde (patrouille), pas un seul.
- **Cohérence pilier 4** : tous réactifs, jamais agressifs à distance (règle transversale `09`).

**Décisions ouvertes.** Ajouter le `resonance_wisp` (ambiance) maintenant ou différer ? Le Custode
Archiviste est-il Phase 2 (avec l'Archive… qui est Phase 3) — **non**, il vient avec l'Archive en
Phase 3 ; seul le Custode Lourd est Phase 2.

---

## 7. Concentrated Flux & sous-produits — usages

**Problème.** Le Flux Compressor (T3) produit du Concentrated Flux qui **ne sert qu'en Phase 4**.

**Proposition — donner un rôle T3 au Concentrated Flux :**
- ⭐ **Catalyseur de calibration** (section 4) : c'est son usage T3 principal — il *entretient* les
  machines lourdes. Boucle propre : compresser du flux pour garder son réseau calibré.
- **Booster de carburant** : inséré dans un Field Emitter, vaut plus d'Osc qu'un Stable Crystal
  (carburant premium, via le `RecipeType veskorius:fueling` déjà data-driven).
- **Composant d'alliage/matrix** avancé.
- **Flux Slag → utilité** : recyclable en scorie de construction (Reclaimer) ; lien de lore avec
  l'Effondrement conservé (nuisance qu'on *doit* gérer, mais valorisable).

**Pourquoi.** Une machine T3 doit avoir un intérêt T3. La calibration est le liant qui connecte
Compressor → déchets → upkeep → agriculture.

---

## 8. Corrections Phase 1 (à répercuter)

Points Phase 1 impactés par cette révision :
- **Locator** : deviendra outil à modes + index (section 1). L'implémentation courante (scan 16 bl +
  émetteurs) est la base ; on ajoute l'index et les modes. `07` (portée 200/400) sera réécrit pour
  décrire le modèle final.
- **Structures Phase 1** (Avant-poste, Habitation) : à **migrer vers de vraies Structures** (section
  2) pour être localisables et avoir de vraies pièces. `decorateInterior` (mobilier) sera porté en
  pièces de structure. `08` sera mis à jour.
- **Surchauffe** : gagnera un sous-produit `resonance_sludge` (section 4) au lieu de détruire l'input
  — petit changement, gros liant systémique. `05`/`06` à noter.
- **Armure Veskorien** : gagnera « réduit le gaz de Résonance » (section 3) en plus du déphasage.
  `04` à mettre à jour (utilité dès le T3).
- **Field Emitter** : la question ouverte de `06` (indicateur de bord de champ) est **déjà résolue**
  (coupole de particules) — à acter dans `06`.

---

## 9. Répercussions dans les docs canoniques (checklist)

À appliquer quand chaque brique est validée/codée (le dossier reste la source de vérité) :
- `03-Progression` : variété de gates (section 5), Locator tiers.
- `04-Materials` : `resonance_sludge`, `resonance_matrix`, `resonance_down`, augments variés,
  Dampening Alloy, armure anti-gaz, usages Concentrated Flux/Flux Slag.
- `05-Machines` : Advanced Assembler, Reclaimer, variantes de Relais, calibration (`Calibratable`),
  surchauffe→déchet.
- `06-Energy` : gaz = biome (section 3), résoudre la question du bord de champ, Emitter portable.
- `07-World-Generation` : biome `resonant_deeps`, migration Structures + configs de spawn, Locator
  index/modes (réécrire la portée 200/400).
- `08-Structures` : layouts réels, Structures vanilla, placement data-driven, densité de mobs.
- `09-Entities` : placement Custode Lourd (Sigma/Archive), variantes, `resonance_wisp`.
- `11-Development-Plan` : réordonner la Phase 2 avec ces briques ; corriger « Poste de Garde + Custode
  Lourd » → Custode standard.
- `12-UX` : Locator à modes (tooltip mode), écran de config item I/O (déjà amorcé), HUD de gaz.
- `13-Registry-Index` : tous les nouveaux registry names.
- `14-Configuration` : seuils de calibration/déchets, fréquences de structures, rareté du biome.

---

## Ordre d'implémentation Phase 2 révisé (proposé)

0. **Fondations transversales d'abord** (débloquent le reste) :
   a. **Index de structures/ressources** (`ResonanceSurveyData`) + **Locator à modes/tiers**.
   b. **Migration vers de vraies Structures** (framework + configs de spawn) — porter Avant-poste &
      Habitation, préparer Sigma Lab/Poste de Garde.
   c. **Biome `resonant_deeps` + gaz** (effet custom).
   d. **Système calibration/déchets** (interface `Calibratable`, `resonance_sludge`, surchauffe→déchet).
1. **Matériaux T3** (+ nouveaux : matrix, sludge, dampening alloy, glass, conduit stone).
2. **Alloy Forge** (branche Iron/Gold, compteur Slag) → **Slag Vent**.
3. **Relay** (+ chaînage/LOS du field manager) → **variantes** (option).
4. **Structural Synthesizer** (+ residue) ; **Advanced Assembler** (matrix) ; **Reclaimer** (déchets).
5. **Deep Crystal Driller** (+ veines profondes dans le biome, suppression du gaz).
6. **Flux Compressor** (+ rôle catalyseur de calibration).
7. **Sigma Laboratory** (Structure réelle, puzzle 2 Relais, Custode Lourd) ; **Poste de Garde**
   (Custode standard).
8. **Outils/armure Veskorien** (dont armure anti-gaz) ; **Field Surveyor**.

**Critère de sortie** (inchangé) : Alloy Forge saturée s'arrête proprement puis reprend avec un Slag
Vent ; un joueur atteint et vide un Sigma Laboratory.
