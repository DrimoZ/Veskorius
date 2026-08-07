# 17 — Donjons (doctrine, grammaire, croquis)

> Ce fichier **ne remplace pas** `08-Structures.md`, il le complète : `08` dit *quelle* structure
> existe, à quelle strate, avec quel loot et quelle porte de tier. `17` dit **à quoi ressemble
> l'intérieur** et **quelle mécanique on y joue**. Quand les deux se contredisent, `08` gagne sur
> le contenu (loot, gate, strate), `17` gagne sur la forme (plan, salles, assemblage).

---

## 0. Pourquoi ce fichier existe (le constat du 2026-08-07)

La migration A7 a donné de **vraies** `Structure` vanilla : `/locate` marche, le Locator les
trouve, les pièces sont générées par datagen. C'était le bon socle. Mais l'intérieur, lui, est
resté un placeholder, et l'audit du code l'a montré noir sur blanc :

| # | Constat | Preuve |
|---|---|---|
| 1 | **Le jigsaw est nominal.** Aucune pièce n'émet de bloc jigsaw ; `JigsawStructure` est construite avec une profondeur de **1**. Ajouter une pièce au pool ne rallonge donc rien, ça **remplace** le bâtiment. La promesse « agrandir = ajouter une pièce » (`16` §2) était fausse en l'état. | `ModStructures`, argument `1 // profondeur` |
| 2 | **Variance nulle.** `collapse()` tire sur une graine constante : tous les Avant-postes du monde sont *byte-identiques*, effondrement compris. Le deuxième est mort narrativement. | `collapse(w, h, d, 0x5EED3)` |
| 3 | **Aucun `StructureProcessor`.** Le levier vanilla de l'usure (RuleProcessor, BlockRot) n'était pas câblé. | pas de `Registries.PROCESSOR_LIST` au datagen |
| 4 | **Pas de 3D.** `room()` fabrique une boîte, hauteur constante, tout le mobilier à `y = 1`. Un donjon se lit en Y. | `outpost()`, `h = 9` |
| 5 | **Pas de langue architecturale.** 8 blockstates, dont **un seul** du mod. Une civilisation de la Résonance bâtissait donc… en deepslate poli vanilla. | constantes `FLOOR`/`PILLAR`/`RUBBLE` |
| 6 | **Éclairage = lanterne.** Anachronique (pilier 1) et pauvre : une lanterne par salle = boîte noire. | `hangingLamps` |
| 7 | **Aucun contrat avec le terrain.** `TerrainAdjustment.NONE` + Y uniforme : la structure est tranchée par les grottes, parfois suspendue dans un ravin. `doorway()` perce un bloc puis s'arrête dans la roche. | `ModStructures`, `jigsaw()` |
| 8 | **Pas de `StructureSpawnOverride`.** Les zombies vanilla spawnent dans le noir de nos ruines et contredisent l'ambiance « gardiens réactifs » de `09`. | `Map.of()` |
| 9 | **⚠️ Le vrai problème.** Un joueur qui traverse l'Avant-poste fait exactement ce qu'il fait dans un donjon vanilla : marcher, taper, ouvrir un coffre. La seule mécanique Veskorius qu'il touche est **un clic droit**. | — |

Le point 9 est celui qui compte : on pourrait poser quarante belles salles et rester **un mod de
structures de plus**. Les huit autres sont de la plomberie ; celui-là est une faute de conception.

### 0 bis. Le second constat (même jour) — « ça se voit que c'est un ordinateur »

La première refonte a corrigé les neuf points ci-dessus et **a quand même raté**. Verdict du
porteur, et il est juste : *des salles cubiques posées côte à côte*. Trois causes, distinctes de
toutes les précédentes :

| # | Constat | Ce que ça produisait |
|---|---|---|
| A | **On construisait une boîte au lieu de creuser une masse.** Chaque structure était une coquille rectangulaire pleine d'air, cloisonnée en salles rectangulaires. | Un plan d'appartement dans un pavé. Le pire : de l'**air** entre les salles là où il devrait y avoir de la **roche**. |
| B | **Tout était plat et à angle droit.** Plafonds plats, hauteurs identiques d'une pièce à l'autre, quatre angles droits par salle, rampes droites. | Aucun geste de bâtisseur nulle part. Un plafond plat est le signe le plus sûr qu'un algorithme est passé. |
| C | **La ruine était du bruit.** Des blocs remplacés au hasard. | « C'est sale », jamais « c'est tombé ». Un tas de gravats ne raconte rien s'il n'est pas **sous le trou d'où il vient**. |

Et un quatrième, de rythme : **le monde n'avait que deux ruines, toutes deux grandes**. Une
civilisation effondrée ne laisse pas deux bâtiments — elle laisse surtout des miettes. Sans les
miettes, une vraie structure n'arrive jamais « au terme d'une piste », elle apparaît de nulle part.

---

## 1. Doctrine — « le donjon est une machine morte, l'explorer c'est la rallumer »

Cinq règles. Aucune n'invente de doctrine : chacune est la conséquence d'un pilier de `01` qu'on
n'avait simplement jamais appliqué à l'architecture.

### R1 — La clé est toujours un champ, jamais un objet
Le verrouillage à la Zelda (lock & key) est le bon outil, mais la clé n'est **pas une clé** : c'est
**de la Résonance qu'il faut amener quelque part**. Un sas (`resonance_bulkhead`) ne s'ouvre que
dans un champ actif. C'est *littéralement* le pilier 3 rendu spatial — et c'est ce qu'aucun autre
mod ne fait : les autres posent des keycards, des boutons ou des mobs à tuer.

> **Corollaire non négociable.** Un sas ne se mine pas, ne se fait pas sauter, et ne s'ouvre pas
> à la redstone. Sinon la règle n'existe pas : elle devient un péage qu'on contourne à la pioche.

### R2 — La lumière raconte l'état du réseau
Des lignes de conduit dans les murs : **froides et éteintes** sur une branche morte, **allumées à
la couleur de la bande** sur une branche alimentée. Un seul bloc, et on récupère d'un coup le
wayfinding sans carte, l'ambiance, et le retour de progression. C'est aussi la réponse à
« comment le joueur sait-il qu'il a réussi ? » : **le donjon s'allume derrière lui.**

### R3 — Le danger principal est la dissonance, pas le mob
`06` a déjà tout : dissonance croissante, décharge AoE, Damping Array, `resonance_sludge`. Une
salle où un émetteur tourne **désaccordé depuis neuf siècles** est une zone létale que le joueur
*répare ou contourne* au lieu de la tanker. Pilier 4 satisfait **par construction** : la cause est
écrite dans `02-Lore.md` depuis le premier jour (la sur-résonance de l'Âge 4).

### R4 — Aucune salle vide
Chaque salle **enseigne**, **pique** ou **récompense**. Le remplissage est le rôle des couloirs,
et le couloir a le droit d'être court. Une salle qui ne fait aucune des trois choses ne va pas
dans le pool.

### R8 — Une civilisation se lit à ses proportions, jamais à son mobilier
*(Ajoutée à la troisième passe, sur le constat « des micro-salles hyper-chargées en blocs pas
utiles ».)* Une salle de 27 mètres à double colonnade et huit mètres sous voûte dit « ils étaient
nombreux et ils bâtissaient ». La même salle réduite à 9 mètres et remplie de tonneaux, d'établis
et de pots de fleurs dit « un ordinateur a rempli une case ». Donc, dans cet ordre :

1. **De la hauteur et du vide** avant tout. Huit blocs sous voûte pour une salle d'apparat.
2. **Un ordre** : colonnades, arcades aveugles, pilastres engagés, frises, gradins. C'est la
   **répétition verticale** qui donne l'impression de hauteur, pas la hauteur réelle.
3. **Au plus deux ou trois meubles par salle.** Un meuble de plus ne remplit pas un vide, il
   supprime une proportion.
4. **Un écart de traitement entre les salles.** Une salle de service a le droit d'être nue ; elle
   n'a pas le droit d'avoir la même section qu'une salle d'apparat, sinon la salle d'apparat n'a
   plus d'échelle. C'est le contraste qui fait l'effet, jamais la taille absolue.

> **Pilastres et frises sont *engagés dans le mur*, jamais posés sur la rangée intérieure.** Ils
> restent parfaitement lisibles à plat (c'est la texture de colonne qui les fait lire, pas le
> relief), et posés en saillie ils mangeaient un bloc d'espace jouable tout autour de chaque
> salle — dont, une fois, pile devant une sortie de galerie, ce qui **murait le donjon**.

### R9 — Rien ne flotte, et rien ne coule
- **Toute décoration murale REMPLACE un bloc de mur**, elle ne s'y accole pas. Lampes et conduits
  posés sur la case intérieure adjacente formaient des rangées de blocs en lévitation le long de
  chaque paroi — le défaut le plus visible et le plus bête des versions précédentes. Ce qui doit
  éclairer le **centre** d'une salle se suspend à la clé de voûte par une **chaîne**.
- **Aucun bloc à gravité.** Le gravier posé dans une voûte crevée s'effondre au premier chargement
  de chunk : la ruine se dégrade toute seule, jamais deux fois pareil, et le dessin est perdu.
- **Aucune source d'eau.** Décorative sur le papier, une flaque devient une inondation dès qu'un
  bloc voisin manque. Le point bas se raconte par le **dépôt**, pas par le liquide.

### R6 — On creuse une masse, on ne pose pas une boîte
Une architecture souterraine **évide de la roche**. Chaque salle est donc creusée et chemisée
séparément, et **il reste de la roche du monde entre les salles** ; on circule par des galeries,
jamais par une porte percée dans une cloison. Conséquences gratuites : la silhouette extérieure
cesse d'être un pavé, le plan cesse d'être une grille, et une grotte qui débouche dans une ruine
devient un accident crédible au lieu d'un bug.

### R7 — Trois gestes, et ils sont non négociables
**La voûte, l'angle coupé, le cône d'éboulis.** Aucun ornement de mur ne compense leur absence :

| Geste | Ce qu'il remplace | Pourquoi il gagne |
|---|---|---|
| **Voûte en berceau** (escaliers retournés, par ressauts) | un plafond plat | un plafond plat est la signature d'un algorithme ; une voûte est la signature d'une main |
| **Angle coupé** (chanfrein d'un ou deux blocs) | quatre angles droits | un angle droit de plus de deux blocs de haut se lit comme un carton d'emballage |
| **Effondrement causal** : le trou dans la voûte, et la matière manquante **en cône exactement dessous** | des blocs remplacés au hasard | c'est la correspondance trou/tas qui se lit comme « ça s'est écroulé ». Sans elle, même très dense, ça se lit « c'est sale » |

S'y ajoutent, pour ce qui doit être **remarquable** : la **rotonde octogonale à coupole** (un plan
non rectangulaire est lu comme important avant qu'on ait rien écrit dedans) et l'**escalier en
vis** (une rampe droite de quinze mètres se lit comme un tapis roulant).

### R5 — Un donjon veskorien se lit vers le bas
Descendre = plus vieux = plus dangereux, cohérent avec les strates Y de `07`. Toute structure de
plus d'une salle a donc au moins **deux niveaux**, et son plan se comprend en coupe autant qu'en
plan.

---

## 2. Grammaire d'assemblage (la partie technique)

### 2.1 Module et gabarits
Toutes les pièces s'alignent sur un **module de 5 blocs**. Couloirs en hauteur intérieure 3
(gabarit 5), salles en 5 / 9 / 13. Une pièce qui ne respecte pas le module ne se connecte pas —
c'est la seule discipline qui rend le jigsaw fiable.

### 2.2 Quatre couches de pools
```
skeleton/   la pièce de départ, GARANTIE, qui contient le chemin critique
corridor/   segments, coudes, T, escaliers descendants, éboulis
room/       les set-pieces, pondérées
terminator/ les bouchons de fin de branche
decor/      le mobilier, injecté DANS les salles par un jigsaw interne
```

Deux conséquences qui font tout le travail :

- **`terminator/` est le `fallback` de *chaque* pool.** Quand le jigsaw arrive à sa profondeur
  maximale, il pose un bouchon au lieu de laisser un trou béant sur la roche. Sans ça, une
  structure sur deux a une salle ouverte sur le vide.
- **`decor/` transforme le mobilier en entrée de pool.** Une salle × quatre habillages = quatre
  rendus, sans dupliquer la salle. C'est le multiplicateur de variété le moins cher du système.

### 2.3 Nommage des connecteurs
| `name` (sur la pièce) | `target` (sur le voisin) | Usage |
|---|---|---|
| `veskorius:corridor` | `veskorius:corridor` | raccord couloir ↔ couloir / salle |
| `veskorius:room` | `veskorius:room` | entrée de salle |
| `veskorius:stair_down` | `veskorius:stair_up` | changement de niveau (asymétrique **exprès** : on ne remonte pas par erreur) |
| `veskorius:decor` | `veskorius:decor` | ancre de mobilier, à l'intérieur d'une salle |

`joint: aligned` partout sauf sur `decor` (rollable, pour que le mobilier tourne).

### 2.4 Processors
Trois listes, appliquées par pièce :

| Liste | Contenu | Pour qui |
|---|---|---|
| `intact` | rien | consoles, sas, salles du chemin critique |
| `worn` | `RuleProcessor` : veined → cracked 8 %, briques → fissurées 12 % | tout le reste |
| `ruined` | `worn` + `BlockRotProcessor(0.92)` | ailes facultatives, impasses |

**Le pourrissage marche sur une liste blanche, pas une liste noire.** `BlockRotProcessor` reçoit
le tag `#veskorius:structure_rottable` : il ne peut retirer **que** la maçonnerie qui y figure.

> **Correction de conception, faite au codage (2026-08-07).** La première version de cette section
> prévoyait un `ProtectedBlockProcessor` sur un tag `#veskorius:structure_protected`. C'était une
> erreur sur deux plans. D'abord techniquement : `ProtectedBlockProcessor` protège les blocs **du
> monde** contre le remplacement par la structure, il ne protège pas les blocs de la structure
> contre le pourrissage — il n'aurait rien fait. Ensuite conceptuellement : une liste noire est
> fragile par nature, puisque le premier bloc critique ajouté et oublié dedans devient effaçable.
> Avec une liste blanche, l'oubli est inoffensif — un bloc absent est simplement épargné. Et un
> Avant-poste dont la console a « pourri » est une progression bloquée : c'est exactement la classe
> de bug déjà trouvée deux fois sur le loot d'amorçage.

La pièce de départ de l'Avant-poste, en plus, n'a **aucun** processor : le chemin critique n'est
même pas exposé à l'usure cosmétique.

### 2.5 Réglages de `Structure` à corriger
| Réglage | Avant | Après | Pourquoi |
|---|---|---|---|
| profondeur jigsaw | 1 | 7 | sans ça, rien ne s'assemble |
| `maxDistanceFromCenter` | 80 | 116 | 80 tronque un donjon à trois niveaux |
| `StructureSpawnOverride` MONSTER | absent | liste **vide**, portée `PIECE` | interdit les spawns vanilla dans nos ruines (`09` : gardiens réactifs, pas une salle à zombies) |
| `LiquidSettings` | `APPLY_WATERLOGGING` | `IGNORE_WATERLOGGING` | un intérieur scellé ne se remplit pas parce qu'un aquifère passe à côté |

---

## 3. L'invariant du chemin critique

> **Le chemin critique d'un tier (console, sas, blueprint, lot d'amorçage) vit toujours dans la
> pièce de départ, jamais dans un pool optionnel.**
>
> **Et le donjon se traverse réellement, de l'entrée au chemin critique.** (Ajouté 2026-08-07,
> seconde passe.)

Le second invariant a l'air d'une évidence ; c'est le plus dur à tenir. Un donjon écrit par code
peut être **parfaitement valide et infranchissable** : une galerie qui chemise ses propres bouts et
ne perce donc aucun des deux murs qu'elle relie ; un escalier en vis dont la première marche tombe
à l'opposé de la galerie qui y mène, si bien qu'on en sort **dans le vide de la cage** ; une salle
murée par le pan de voûte qu'on vient d'y faire tomber. Rien ne casse, rien ne lève d'exception,
la structure se génère magnifiquement — et le joueur se retrouve devant de la roche.

**Les trois exemples ci-dessus étaient réellement présents**, et aucune relecture ne les avait vus.
C'est un **parcours automatisé** (`outpostIsWalkableFromEntranceToConsole` : on explore les cases où
un joueur tient debout, avec les tolérances d'un pas — monter d'un bloc, descendre de trois) qui les
a trouvés. À écrire pour **toute** structure de plus d'une salle : le dessin se relit, la
circulation non.

`08` l'énonçait déjà en prose, à une époque où la profondeur valait 1 — donc où le risque était
nul. Avec un jigsaw réel, **c'est devenu une régression possible à chaque ajout de pièce**. La
règle est donc gardée par un GameTest, au même titre que le lot d'amorçage.

Le jigsaw sert à ce pour quoi il est bon : **des ailes facultatives**. Du bonus, jamais du chemin.

---

## 4. Le vocabulaire architectural (blocs à ajouter)

On ne fait pas un donjon mémorable avec huit blockstates vanilla. Les deux tiers ci-dessous sont
du bloc-avec-texture pur (générateur `tools/block-textures/`).

| Bloc | Rôle | Poids |
|---|---|---|
| `resonance_bulkhead` | **Le sas.** S'ouvre seulement dans un champ actif. La clé du donjon. | moyen |
| `conduit_line` (+ coudes) | Ligne murale, état `powered`, glow à la couleur de bande. Le fil d'Ariane. | moyen |
| `veined_stone_bricks` / `_cracked` / `_chiseled` + dalle / escalier / mur | **La maçonnerie.** Sans elle, aucune architecture n'est possible. | faible |
| `resonance_lamp` | L'éclairage veskorien. Remplace la lanterne. | faible |
| `dissonance_bloom` | La moisissure de l'Effondrement : contact = dégâts légers, se brosse / se dampe | faible |
| `ancient_emitter` | Émetteur de structure, à sec, non lootable. Le « geste » de l'Avant-poste. | moyen |
| `custode_alcove` | Niche de dock. Un Custode **rangé** qui s'extrait vaut dix Custodes plantés au sol. | moyen |
| `sealed_vault` | Coffre scellé, ouvert par champ. La récompense facultative. | moyen |

---

## 5. Croquis

### 5.1 Avant-poste (T2) — « la Salle des Machines »

Aujourd'hui : 21×9×21, quatre boîtes à plat. Cible : **trois paliers en spirale autour d'un puits
central**, la pièce de départ portant le chemin critique (§3), les ailes en jigsaw.

```
                    ~~~~~~ grotte / surface ~~~~~~
                          │  CHEMINÉE D'AÉRATION
                          │  (conduits brisés qui pendent)
                          ▼   ← LE TELL. On tombe dessus en explorant,
  N0  ┌───────────────────────────┐  Y-4      pas seulement au /locate.
      │  Vestibule   ▓▓ effondré  │
      │  [alcôve Custode — VIDE]  │  ← vide. C'est un AVERTISSEMENT,
      └──────┬────────────────────┘     pas encore une menace.
             │ escalier tournant
  N1  ┌──────┴────────────────────┐  Y-9   PALIER DE VIE
      │ Réfectoire │  Dortoirs    │   loot quotidien, lore du peuple,
      │   ▒▒▒▒▒    │  ░  ░  ░     │   1er Custode ACTIF (contournable
      │            │              │   par la coursive)
      │  ╔══════ PUITS ══════╗    │  ← 12 blocs de vide traversant.
      └──╨────────────────────╨───┘    D'EN HAUT ON VOIT LA CONSOLE,
             │ échelle rompue            éteinte, deux étages plus bas.
             │ (détour obligé par l'aile est)
  N2  ┌──────┴────────────────────┐  Y-16  SALLE DES MACHINES
      │  ATELIER   ║  SALLE DE    │
      │  établis   ║   CHARGE     │
      │  châssis   ║ [ÉMETTEUR    │  ← émetteur ancien, à sec.
      │            ║   ANCIEN]    │    Non minable, non lootable.
      ├═════[ SAS DE RÉSONANCE ]══┤  ← BULKHEAD. Fermé. Indestructible.
      │     CHAMBRE DE CONSOLE    │
      └───────────────────────────┘
```

**Le geste**, qui remplace « clic droit sur une console dans une boîte » :

1. Le joueur descend, trouve le **sas fermé**. La console est derrière, visible par une baie.
2. À deux mètres : **l'émetteur ancien**, à sec, avec son slot de carburant.
3. Il faut lui donner **un Stable Resonance Crystal**.
   > ✅ **Vérifié : aucune dépendance circulaire réintroduite.** Le Resonance Stabilizer est
   > autonome (0 Osc, aucun champ requis, `06`), donc un joueur T1 peut fabriquer un Stable
   > Crystal sans rien débloquer. Et pour celui qui arrive les mains vides, un **coffre-réserve
   > garanti dans la même salle** contient exactement un cristal : le puzzle devient « trouve le
   > carburant ici », un vrai small-key physique. C'est la même classe d'erreur que le lot
   > d'amorçage T2 ; elle est traitée d'avance, et gardée par un GameTest.
4. Le champ s'allume. **Le sas s'ouvre, et la salle des machines s'allume** — conduits et lampes,
   d'un coup. Les deux Custodes en alcôve se réveillent : le chemin du retour n'est plus celui de
   l'aller.
   > *Portée, corrigée au codage (2026-08-07)* : un Field Emitter porte à **8 blocs**, et
   > l'émetteur ancien en est un vrai — c'est même tout l'intérêt (voir plus bas). Ce qui se
   > réveille est donc **la salle des machines**, pas l'édifice entier comme l'annonçait la
   > première rédaction. Les conduits des deux paliers du dessus restent morts, et c'est mieux
   > ainsi : le contraste entre la branche vivante et les branches coupées est précisément ce que
   > la règle R2 veut rendre lisible. Donner une portée spéciale à l'émetteur ancien aurait exigé
   > une block entity dédiée — donc une machine qui peut diverger de la vraie, ce qu'on refuse.
5. Console → blueprint T2.

Ce que ça gagne, pour un coût de conception nul :
- Le joueur **apprend ce qu'est un champ dans la salle où il l'a vu**, avant de posséder la
  moindre machine T2. Pilier 2, appliqué pour la première fois de façon non décorative.
- Le rapport risque/récompense s'inverse : **allumer, c'est armer le donjon**. Le joueur choisit.
- Le coffre d'amorçage (4 Component + 2 Gold garantis) ne bouge pas : les GameTest existants
  restent valides.
- Ça répond à la question « Ouvert » de `08` et `07` (« faut-il une variante de surface ? ») :
  **oui, mais comme une entrée** — la cheminée d'aération — pas comme une seconde structure.

### 5.2 Habitation Modeste → **Hameau creusé**

Une maison isolée ne raconte pas un peuple. Un hameau, si — et le jigsaw le rend gratuit.

```
        ┌───┐    ┌───┐          ▫ logis (2 plans × 4 décors = 8 rendus)
        │ ▫ │────│ ▫ │          ● puits commun
        └─┬─┘    └─┬─┘          ═ galerie
   ┌───┐  │        │  ┌───┐     ✕ logis effondré : impasse, il faut
   │ ▫ │══╪════●═══╪══│ ▫ │         creuser pour entrer (1/3)
   └───┘  │        │  └───┘
        ┌─┴─┐    ┌─┴─┐          Taille variable 3 → 6 logis :
        │ ▫ │    │ ✕ │          c'est le pool qui décide, pas nous.
        └───┘    └───┘
```

Bonus à cinq lignes : **un hameau sur six a une galerie murée**, derrière laquelle une cache
d'avant l'Effondrement. Récompense la curiosité, ne garde aucune progression.

### 5.3 Poste de Garde (T2, à créer) — **la Tour Inversée**

L'image la plus marquante pour le moins cher : une garnison qui descend.

```
 Y-2   ┌─────┐   entrée = une meurtrière au ras d'une grotte
       │  ▲  │
 Y-7   │ ░▒░ │   niveau 1 — alcôves VIDES            ← noir
 Y-12  │ ░█░ │   niveau 2 — 1 Custode en veille      ← noir
 Y-17  │ ▓█▓ │   niveau 3 — 2 Custodes, conduits     ← ça s'allume
       │     │                VIVANTS
 Y-22  │ ███ │   niveau 4 — ARSENAL, 2 sealed_vault  ← plein jour
       └─────┘
```

Deux choix portent toute la salle : **l'escalier en spirale est ouvert sur le vide** (le combat
devient positionnel au lieu d'être un échange de coups) ; et **la lumière augmente en descendant**
— l'inverse de l'intuition, et ça *dit* au joueur où le réseau a survécu.

### 5.4 Sigma Laboratory (T3) — la roue

Le puzzle « deux relais simultanés » existe dans `08` ; il lui manquait une forme.

```
            ┌────── AILE NORD : SERRES ───────┐
            │ cultures résonantes mortes,     │
            │ verrières brisées SUR LE VIDE   │  ← la carte postale du mod
            │         [ RELAIS A ]            │
            └────────────────╥────────────────┘
   ┌───────────────┐     ╔═══╩═══╗     ┌───────────────┐
   │  AILE OUEST   │     ║ HALL  ║     │   AILE EST    │
   │  CHAMBRE DE   ╞═════╣SCELLÉ ╠═════╡  DORTOIRS +   │
   │  DISSONANCE ☠ │     ║ ▓▓▓▓▓ ║     │  INFIRMERIE   │
   │ (loot optionn.)│    ╚═══╦═══╝     │  (lore, loot) │
   └───────────────┘         ║         └───────────────┘
            ┌────────────────╨────────────────┐
            │   AILE SUD : SALLE DES POMPES   │
            │   [ RELAIS B ]  Custodes Lourds │
            └─────────────────────────────────┘
```

- **Le vrai puzzle en une ligne** : un relais réparé ne tient que ~90 s. Nord ↔ Sud fait le tour
  de la roue → le joueur doit **planifier son trajet** et dégager les Custodes du sud *avant* de
  réparer le nord. Aucune mécanique nouvelle (le relais a déjà une réserve), difficulté réelle.
- **La Chambre de Dissonance** (ouest) : un émetteur jamais coupé, dérive séculaire, dissonance au
  plafond, décharge AoE périodique. Le meilleur loot facultatif est dedans. On y entre en gérant
  une mécanique du mod — poser un Damping Array à 16 blocs (la décharge porte à 6, `06` garantit
  donc qu'on peut toujours nettoyer à distance sûre), couper l'émetteur, ou courir.

### 5.5 Archive Régionale (T4) — la bibliothèque en puits

Le puzzle des quatre fragments à ordonner existe ; il lui manque un volume. **Un cylindre de
rayonnages**, cinq balcons annulaires autour du vide, salle de lecture au fond. Un fragment par
étage, chacun portant un glyphe ordinal ; la porte de la salle de lecture est un **cadran de
quatre lampes de résonance**. On lit en montant, on compose en descendant.

### 5.6 Cœur de Faille (T5) — pas une salle, une **approche**

Ce qui manque n'est pas la bulle, c'est les cent blocs qui la précèdent : pierre déformée,
conduits morts pointant tous vers l'intérieur, sludge, dissonance qui monte à chaque pas. Un
couloir d'effroi. Ça coûte une *feature*, pas une structure.

---

## 6. Structures neuves justifiées

### 6 bis. L'échelle de ruines — ✅ faite le 2026-08-07

Le monde n'avait que **deux** structures, toutes deux grandes. Il en a maintenant quatre, réparties
sur une échelle, et c'est ce qui donne enfin une profondeur de champ : une vraie structure se
distingue **par contraste** au lieu d'apparaître de nulle part.

| Structure | Taille | Densité | Rôle |
|---|---|---|---|
| **Borne de conduit** (`ruin_marker`, variante pilier) | 5×7×5 | ~1 / 130 blocs | Un fût brisé, une base dallée, un bout de conduit. **Aucune récompense.** Son seul rôle est qu'on croise du veskorien tout le temps. |
| **Bout de galerie enseveli** (`ruin_marker`) | 7×6×7 | idem | Trois mètres de voûte et un tas. On comprend qu'il y avait un réseau. |
| **Chambre engloutie** (`sunken_chamber`) | 13×10×13 | ~1 / 350 blocs | Une salle voûtée à demi comblée, un coffre. Le palier « on a trouvé quelque chose » sans être un donjon. |
| **Hameau** / **Avant-poste** | 13→33 | 1 / 320 · 1 / 512 | Les lieux. |

Les deux petites sont **volontairement hors du tag `#locatable`** : les y mettre noierait le mode
Structures du Locator sous des bornes de trois blocs et lui ferait perdre ce qui fait sa valeur.

| Structure | Tier | Strate sociale | Ce qu'elle répare |
|---|---|---|---|
| ⭐ **Puits de Forage abandonné** ✅ | T1 | Peuple du réseau | **Le T1 n'avait aucune structure à lui.** Un fût de vingt blocs, six plateformes décalées, le foreur brisé au fond sur la poche de cristal qu'il cherchait. Aucun déblocage : il ENSEIGNE « descendre = cristaux » dans la première heure. Densité ~1 / 260 blocs — c'est la première ruine du joueur, elle doit se croiser sans la chercher. |
| ⭐ **Relais Station** | T2-T3 | Architectes | Déjà proposée en `16` §5 : petite, commune, gate alternatif au Sigma. |
| **Dépôt de Slag** | T3 | Architectes (fin d'âge) | Ancienne décharge : blooms de dissonance, sludge, beaucoup d'alliage. **Une zone hostile sans un seul mob** — la démonstration de R3. |
| **Nécropole des Custodes** | T4, rare | Custodes | Trente alcôves alignées, **trois occupées**. Toute la tension est dans « lesquelles ». |

Chacune respecte la règle de `07` : strate Y **et** strate sociale choisies avant d'écrire quoi
que ce soit.

---

## 7. Ordre d'implémentation

| Phase | Contenu | Statut |
|---|---|---|
| **A** | Déblocage : `jigsaw()` dans le `TemplateBuilder`, profondeur 5, `maxDistance` 116, spawn overrides, `IGNORE_WATERLOGGING`, `ModProcessorLists` | ✅ 2026-08-07 |
| **B** | La langue : maçonnerie de pierre veinée (brique / fissurée / gravée / dalle / escalier / muret), lampe, conduit, efflorescence | ✅ 2026-08-07 |
| **C** | Les trois mécaniques : sas, conduits alimentés, émetteur ancien | ✅ 2026-08-07 |
| **D** | Avant-poste à trois paliers + Hameau creusé | ✅ 2026-08-07 |
| **E** | Poste de Garde ✅, Puits de Forage ✅ — Relais Station à faire | partielle |
| **F** | Sigma Laboratory | à faire |

**Ce que A→D a livré** (112 GameTest au vert) : `FieldSensitiveBlock` (socle commun de la lampe,
du conduit et du sas), `ResonanceBulkheadBlock`, `DissonanceBloomBlock`, onze blocs
d'architecture avec leurs textures générées, dix pièces de structure NBT réparties en six pools,
et cinq tests d'invariants — dont `outpostStartPieceCarriesTheWholeT2Gate`,
`optionalWingsNeverCarryTheCriticalPath` et `piecesExposeRealJigsawConnectors`, qui gardent
respectivement §3 dans les deux sens et la réalité du jigsaw.

**Troisième passe (même jour) : le monumental.** Constat du porteur — « des micro-salles
hyper-chargées en blocs pas utiles », « toujours trop carré », « pas assez comme une civilisation »,
et des blocs qui flottent. D'où R8 et R9, et une refonte de **toutes** les pièces, pas seulement du
Hameau :

| | Avant | Après |
|---|---|---|
| Avant-poste | 33×26×33, 2 niveaux | **39×31×39**, deux niveaux à **huit blocs sous voûte**, nef de **31 m à double colonnade**, rotonde à coupole et colonnes engagées |
| Hameau | une placette + 4 cabanes détachées (un lotissement) | **une halle à colonnade de 19 m**, logis **taillés dans les bas-côtés** sous arcade, escalier d'apparat, foyer commun |
| Petites ruines | 5→13 de côté | 7→17, voûtées, à colonnade pour la chambre engloutie |
| Palette | maçonnerie du mod seule | + **accents vanilla** : deepslate poli (sol), carrelage (bordures), **cuivre patiné** et **grilles de cuivre** (frises), **chaînes** (lustres) |
| Nouveau bloc | — | **colonne cannelée** (à axe) ; le conduit devient **à axe** lui aussi, sinon un tracé vertical a l'air haché en travers |

Nouveau garde-fou :  balaie **les douze pièces** et refuse tout bloc dont
les six voisins sont de l'air.

**Seconde passe du même jour** (suite au verdict « ça se voit que c'est un ordinateur ») : tout le
dessin est repris sur le vocabulaire de `Masonry` — voûtes, chanfreins, bandeaux, pilastres,
rotonde à coupole, escalier en vis, effondrement causal, brèches de mur, flaques, concrétions.
L'Avant-poste passe de trois boîtes empilées (21×20×21) à **deux niveaux voûtés reliés par une vis,
plus une rotonde à coupole** (33×26×33), chaque salle creusée séparément dans la masse. Le Hameau
gagne une citerne et des logis de hauteurs différentes. Et **trois nouvelles petites ruines**
peuplent le monde (§6 bis). 114 GameTest, dont le parcours de bout en bout.

**`dataMarker()` n'a pas été écrit**, contrairement au plan initial : il servait à faire tirer les
entités à la génération plutôt qu'à les cuire dans le NBT. Les Custodes sont posés directement
dans les pièces, en alcôve — ce qui est **voulu** (leur place raconte quelque chose, elle ne doit
pas être aléatoire) et rend le marqueur inutile tant qu'aucune pièce n'a de spawn variable. À
reprendre si le Poste de Garde en demande.

---

## Problèmes / Alternatives rejetées

- **Rejeté : ouvrir les sas avec une clé-objet (keycard, « fragment de porte »).** C'est ce que
  font tous les autres mods, et ça rendrait le pilier 3 décoratif : le champ redeviendrait une
  mécanique d'usine qu'on laisse à la base. La clé doit être le champ lui-même.
- **Rejeté : rendre le sas minable au bout d'un certain tier.** Un verrou qu'on finit par percer
  n'est pas un verrou, c'est un péage. Le sas reste un sas ; la récompense derrière est calibrée
  pour ça.
- **Rejeté : mettre la console dans un pool de salles tirées au sort.** Une partie des
  Avant-postes n'en aurait pas et la progression redeviendrait suspendue à un tirage — la classe
  de bug déjà trouvée deux fois (loot d'amorçage, châssis T2). Voir §3.
- **Rejeté : générer les donjons dans une dimension dédiée.** Même raison qu'en `07` pour les
  Failles : coût technique sans gain de sensation.
- **Rejeté : faire de la dissonance des salles un simple champ de dégâts.** Ce serait un piège
  vanilla repeint. Elle doit être **gérable par les outils du mod** (Damping Array, couper
  l'émetteur) — sinon R3 n'est qu'un habillage.
- **Rejeté : `BlockRotProcessor` piloté par une liste noire.** Il finit statistiquement par manger
  la console. Voir §2.4.
- **Rejeté : donner une portée spéciale à l'émetteur ancien** pour que tout le donjon s'allume.
  Il faudrait une block entity dédiée, donc une machine qui peut diverger de celle que le joueur
  fabrique — et c'est justement l'identité stricte entre les deux qui fait la valeur pédagogique
  de la scène. Voir §5.1, note de portée.
- **Rejeté : meubler pour remplir.** Le réflexe devant une grande salle vide est d'y poser des
  tonneaux. C'est l'inverse qu'il faut faire : agrandir encore et enlever le reste. Voir R8.
- **Rejeté : le gravier et l'eau comme matières de ruine.** Voir R9 — l'un s'effondre, l'autre
  inonde. Les deux ont l'air d'une bonne idée jusqu'au premier chargement de chunk.
- **Rejeté : une coquille englobante par structure.** C'est la cause racine du « ça se voit que
  c'est un ordinateur » : une boîte creuse cloisonnée donne un plan d'appartement dans un pavé, et
  met de l'air là où il devrait y avoir de la roche. Voir R6.
- **Rejeté : les plafonds plats, même pour les pièces de service.** Une salle de service a le droit
  d'être nue ; elle n'a pas le droit d'avoir la même section qu'une grande salle. Sans écart de
  hauteur et de traitement, la grande salle n'a plus d'échelle — c'est le contraste qui fait
  l'effet, pas la taille absolue.
- **Rejeté : plus de bruit dans la ruine pour « faire vieux ».** Densifier du remplacement
  aléatoire ne rapproche jamais de « c'est tombé ». Seule la correspondance trou-dans-la-voûte /
  cône-au-sol y arrive. Voir R7.
- **Rejeté : les lits vanilla comme mobilier de couchage.** Outre le pilier 1 (une civilisation de
  la Résonance ne dort pas sur de la laine teinte), un lit occupe deux blocs et une moitié posée
  seule est **retirée** par la mise à jour de voisinage : les trois lits de l'Avant-poste
  disparaissaient sans un mot. Remplacés par des couchettes de dalle et de pierre gravée.

## Ouvert

- Le hameau doit-il avoir une **variante de surface** (village en ruine à ciel ouvert) ? Ça
  donnerait un premier contact avant même de creuser, mais ça empiète sur la lisibilité
  « le veskorien est souterrain ». Non tranché.
- Les alcôves de Custode doivent-elles **se refermer** si le joueur coupe le champ (donc un moyen
  de désamorcer le donjon en repartant) ? Séduisant, mais ça rend le combat entièrement
  optionnel — à arbitrer en playtest.
- Faut-il un **second sas** dans l'Avant-poste, purement facultatif, gardant un `sealed_vault` ?
  Ça enseignerait la règle deux fois. Probablement oui, à la Phase E.
