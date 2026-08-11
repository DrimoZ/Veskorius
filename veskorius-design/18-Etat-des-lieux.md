# Veskorius — inventaire de ce qui est codé

Généré à partir du code, pas du dossier de design. Une case cochée ici veut dire
« enregistré, texturé, traduit, testé », pas « écrit dans un .md ».

NeoForge 1.21.1 · Java 21 · **244 fichiers Java, ~34 200 lignes** · **58 blocs, 88 items**
· **174 GameTest en deux processus** (`runFastGameTests` 153 en ~28 s / `runWorldGameTests` 21 donjons ; `runAllGameTests` pour les deux), dont un qui vérifie que chaque machine a une recette réellement
chargée — une recette de plus de 9 ingrédients est écartée au chargement du monde, sans
que rien d'autre ne le signale.

---

## 1. Machines (25 au dossier — **25 codées**)

| # | Machine | Palier | État | Ce qu'elle fait |
|---|---|---|---|---|
| 1 | Resonance Stabilizer | T1 | ✅ | Raw + Quartz → Stable, 30 s, autonome |
| 2 | Component Assembler | T1 | ✅ | Stable + 2 Fer → 2 Component, 5 s, 3 Osc/t |
| 3 | Resonance Whetstone | T1 | ✅ | Répare un outil de 25 %, 8 s, autonome |
| 22 | Crystal Crusher | T1 | ✅ | Raw → 3 Dust, 10 s, autonome |
| 4 | Field Emitter | T2 | ✅ | Champ portée 8, réserve 4000 Osc, carburant data-driven |
| — | Émetteur Accordable | T2 | ✅ | Même émetteur, bande harmonique choisie |
| 5 | Flux Purifier | T2 | ✅ | Stable + Redstone → Refined, 45 s (22 s en surchauffe) |
| 6 | Resonance Storage Cell | T2 | ✅ | Batterie portable 8000 Osc |
| 7 | Resonance Locator | T2 | ✅ | Outil à modes (Ressources / Structures) ; active le HUD de champ |
| 8 | Crystal Roost | T2 | ✅ | 1 Raw / 600 s si un Fileur est à proximité |
| 9 | Resonance Relay | T3 | ✅ | Portée 20, chaînable, 1 Osc/t. Tampon, jamais un fil |
| 10 | Veskorian Alloy Forge | T3 | ✅ | 2 Refined + 2 lingots → alliage **+ scorie**, 20 s |
| 11 | Structural Synthesizer | T3 | ✅ | 4 lingots + 8 pierres → 4 blocs **+ résidu**, 60 s |
| 12 | Deep Crystal Driller | T3 | ✅ | Récolte les amas sous Y −40, 6 Osc/t |
| 13 | Slag Vent | T3 | ✅ | Vide 1 scorie / 10 s par forge dans 8 blocs |
| 23 | Flux Compressor | T3 | ✅ | 4 Refined → 1 Flux Concentré, 30 s, 6 Osc/t |
| — | **Reclaimer** | T3 | ✅ | 4 scories → 1 gravier, 4 boues → 1 poussière. Ferme la boucle |
| — | **Advanced Assembler** | T3 | ✅ | 4 Composants + 2 lingots conducteurs → 1 Matrice, exigée par les 4 machines T4 |
| — | Damping Array | T3 | ✅ | Purge la dissonance d'un champ |
| 14 | **Harmonic Amplifier** | T4 | ✅ | Élargit un champ existant ; ne fabrique rien |
| 15 | **Deep Synthesis Chamber** | T4 | ✅ | Consomme un Hyper Refined à la construction, à demeure |
| 16 | **Automated Extraction Array** | T4 | ✅ | Extraction sans joueur |
| 17 | **Resonance Network Hub** | T4 | ✅ | Coordination de réseau |
| 18 | **Convergence Core** | T4→T5 | ✅ | Multi-bloc : anneau de 8 relais à 5 blocs, portée 40, force 1000 |
| 19–21 | **Rift Anchor / Extractor / Ward** | T5 | ✅ | La Faille : ancrer, purger, extraire 6 fois — et le Cœur DIT ce qu'il lui reste |

**Les cinq paliers sont jouables de bout en bout.**

### Le socle commun
Toute machine à cycle hérite d'`AbstractMachineBlockEntity` : progression, slot
d'augment (Catalyst Core, +15 %), On/Off, mode redstone, surchauffe, faces
configurables (entrée/sortie/désactivé), bande harmonique, glow lisible sur la façade.
**14 types de recette** data-driven — un datapack change les valeurs sans recompiler.

---

## 2. Énergie & réseau

- **Champ de Résonance** : aucune machine ne se branche ; elle demande « y a-t-il de
  l'énergie ici ? » à un index global par dimension.
- **Anti-stacking** : deux émetteurs superposés n'additionnent rien.
- **La source la plus forte l'emporte** — règle écrite dans `06-Energy.md` mais que le
  code n'a honorée qu'à l'arrivée du Convergence Core, première source dont la force
  n'était pas 100. Elle était invisible tant que toutes les sources se valaient.
- **Harmoniques** (3 bandes) : une machine désaccordée tourne quand même, coûte plus
  cher, et **injecte de la dissonance** dans le champ.
- **Dissonance** en trois étapes visibles : la coupole grisaille → le champ devient
  intermittent → **décharge AoE**. Le Damping Array la purge, le Relay la renvoie en amont.
- Coupole de particules qui trace la portée réelle. HUD de champ (Locator porté).

## 3. Monde

- **Poches de cristal** (Y −20 à 0) : amas + coquille de pierre veinée.
- **Dépôts de flux** brossables.
- **Orage de Résonance** : un tirage par jour MC après le T3, 10 min, sème des cratères
  météoriques ramassables — et efface au sol tout ce qui n'a pas été pris.
- **Failles** sous Y −60 : bulle de vide sphérique (rayon 5–9), coquille de pierre
  déformée, cœur flottant. Posées en `UNDERGROUND_DECORATION` et non en `ORES` — elles
  creusent au lieu de déposer.
- **8 structures jigsaw**, toutes générées par datagen (aucun NBT écrit à la main) :
  Avant-poste (3 niveaux), Hameau + 4 variantes de maison, Sigma Laboratory,
  Guard Post (tour inversée), Drill Shaft, Archive Régionale, Ruin Marker, Sunken Chamber.
- **21 pièces NBT**, 10 pools, processeurs de pourrissage sur **liste blanche**.
- Doctrine dans `17-Dungeons.md` (R1–R11). Tests de **traversabilité réelle** (BFS avec
  tolérances de pas) sur chaque donjon, plus « rien ne flotte ».

## 4. Progression

- Blueprint T2 (Console de l'Avant-poste), T3 (Console du Sigma) et T4 (Console de
  l'Archive), **rendus au craft**. Le T5 n'a pas de plan : on y entre en trouvant une Faille.
- Le palier est vérifié par data component — jusque-là le plan T2 ouvrait tout le mod.
- Trois châssis (Fracturé / Accordé / Veskorien), chacun contenant le précédent.
- **17 advancements** : la colonne des cinq paliers, PLUS six branches pour le facultatif
  (le multi-bloc, le mini-boss de l'Archive, l'orage, l'agriculture, le verre lumineux, la
  boucle des déchets). Un arbre qui ne montre qu'un chemin dit qu'il n'y en a qu'un.
  Le critère de palier lit lui aussi le data component : sans ça, les quatre plans
  décerneraient les quatre paliers d'un coup.

## 5. Énigmes de donjon

- **Sigma** : deux relais endommagés à réparer, la simultanéité naît d'une contrainte de
  trajet (90 s d'autonomie, le second est hors de portée de l'émetteur).
- **Archive** : quatre socles à activer dans l'ordre d'un texte de Codex.
- **Sas** : une porte s'ouvre toujours par un champ. Jamais de serrure.

## 6. Entités

Gardien de Faille (boss final, 3 phases aux comportements réellement distincts) ·
Custode Archiviste (élite de la salle profonde, marque le sol et détone) ·
Custode Lourd (garde du Sigma et de l'Archive, posté par paires : il en alerte un autre à 16 blocs) ·
Custode (gardien, alerte de groupe, lâche du fragment d'alliage — substitut de fer) ·
Fileur de Cristal (passif, nourri au spore).

## 7. Équipement

Épée et pioche d'alliage (niveau netherite au minage, dégâts diamant, 1873 usages) ·
armure d'alliage complète (protection diamant, **panoplie : dégâts de phase divisés par
deux**) · Plastron de Garde (dégâts de phase **à zéro**, mais il remplace le plastron
d'alliage et coûte donc le bonus de panoplie).

## 8. Interface

- **Resonance Codex** : manuel en jeu qui s'écrit tout seul, **78 entrées**, 9 catégories,
  couvrant les cinq paliers, les entités et l'équipement. Affiche les **recettes**
  (établi et machine, lues du RecipeManager chargé), pagine le texte, cherche dans tout
  le livre, s'adapte à la taille de la fenêtre.
  **Tout est lisible avant de posséder l'objet** — seuls les fragments de lore cachent
  leur texte, parce qu'un manuel qui ne se lit qu'après coup n'aide pas à progresser.
  L'état de déblocage vit sur le **joueur** et survit à la mort.
- Resonance Tuner : outil à modes (Pivoter / On-Off / Surchauffe / Redstone).
- 12 GUI dessinés par générateur, 101 textures de bloc, palettes indexées.
- **Verre à textures connectées** : aucun joint ni cadre entre deux plaques. Un cube sans
  bordure, plus une baguette de cadre par ARÊTE ouverte, assemblés en blockstate multipart
  — douze pièces au lieu des 64 modèles qu'auraient demandé six booléens.
- **Châssis à cadre connecté** (les trois paliers) : un caisson isolé montre son cadre
  métallique sur ses douze arêtes ; accolés, le cadre ne subsiste qu'autour du groupe, qui
  se lit comme *un* panneau. Les trois paliers ne se connectent pas entre eux — le palier est
  une information qui doit rester visible sur le bâtiment.

  **Le bloc ne porte AUCUNE propriété de blockstate.** Une première version en portait six —
  une par face — tenues par `updateShape`. Elle butait sur le **coin rentrant** : dans une
  disposition en L, le bloc de l'angle touche ses deux voisins, ne dessine donc aucune
  bordure, et son coin restait nu. Le combler demande de connaître les **diagonales**, soit
  dix-huit booléens, soit **262 144 états par bloc** au lieu de 64. Le voisinage est donc lu
  au moment où le chunk se construit, par `ConnectedChassisModel` (`getModelData`), qui a
  accès au monde. Bénéfice qu'on n'avait pas cherché : plus rien n'étant stocké, **un mur
  bâti avant cette fonctionnalité se connecte de lui-même** — la version à propriétés laissait
  les anciens murs figés sur l'état par défaut, `updateShape` n'étant pas appelé au
  chargement d'un monde.

  La règle vit dans `ChassisFrame`, sans rendu et sans client, pour être testable : une
  baguette sur l'arête `a`/`b` si ni `a` ni `b` n'a de voisin ; un quart de cadre dans le coin
  `p`/`q` de la face `f` si `f` est dégagée, si `p` et `q` ont un voisin, et si la diagonale
  `p+q` n'en a pas. **La troisième condition n'est pas une précaution** : sans elle, le bloc
  central d'un mur plein poserait quatre quarts de cadre au milieu d'une surface lisse.

  Quatre pièges, tous trouvés en jeu et aucun en relecture :
  1. **Les baguettes portent la texture du caisson**, pas un métal à part : l'UV automatique
     fait tomber chacune sur la bordure qu'elle représente, si bien qu'un bloc isolé
     redessine exactement le bloc en main. Une texture dédiée donnait deux objets différents
     pour le même bloc.
  2. **Le cadre peint tient dans trois pixels**, l'épaisseur exacte des baguettes
     (`CASING_WIDTH`, partagé avec le générateur de textures). Au-delà, la partie qui déborde
     n'existe que dans l'inventaire.
  3. **Les UV sont bornées à la main.** Les baguettes débordent de 0,02 px pour ne pas être
     coplanaires avec la plaque — et l'UV automatique se déduisant des coordonnées, ce débord
     les faisait lire la tuile VOISINE dans l'atlas : un liseré clair sur chaque bord.
  4. **La plaque est un cube plein, de 0 à 16.** L'avoir reculée de 0,05 px pour la départir
     des baguettes ouvrait une fente de 0,1 px entre deux caissons, que rien ne fermait
     puisque les faces partagées sont cullées : on voyait **le ciel à travers le mur**.

  Les **machines gardent un cadre peint** dans leur texture, et ne se connectent pas : leurs
  silhouettes sont creusées, à étages, parfois traversantes, et une baguette posée sur
  l'arête d'un cube y flotterait dans le vide.
- **JEI** : toutes les catégories, plugin piloté par table pour qu'une machine ajoutée
  sans sa catégorie se voie.
- Jade, Curios. **Lang EN + FR à parité stricte** — 404 clés de chaque côté, vérifié au datagen.
  Quarante-huit manquaient en français (tout le Codex T3-T4-T5 et les libellés du livre) :
  une clé absente n'est pas une erreur pour Minecraft, il affiche l'identifiant et continue.

## 9. Garde-fous hors du jeu

`./gradlew audit` — vérifie registre ↔ ressources générées ↔ dossier : modèle manquant,
blocs sans butin, clé sans traduction française, entrée absente de `13-Registry-Index.md`,
chiffres faux dans ce fichier. Aucune de ces pannes ne plante ; toutes se découvrent en
jeu, ou jamais. Les GameTest ne peuvent pas les voir — ils tournent dans le jar.

## 10. Configuration

Cinq fichiers TOML par thème (basics, machines, generation, mobs, harmonics), type
SERVER, surchargeables par modpack.

---

## Ce qui reste

Tous les matériaux du dossier sont codés. **Il ne reste aucun poste ouvert** — les
questions marquées « Ouvert » dans le dossier sont des décisions de design, pas du travail
en attente. La branche décorative est complète : agriculture → extrait → verre teint. Aucun n'est sur le chemin critique, et c'est
désormais le seul poste ouvert du dossier.

Le Cœur de Faille n'est PAS de cette liste : c'est la `RiftFeature`, et elle fait déjà ce
que `08-Structures.md` décrit — bulle sphérique de 5 à 9 blocs, coquille de pierre
déformée, noyau flottant. Aucune « Station Relais » n'existe non plus dans le dossier ;
elle ne figurait que dans une version antérieure de ce document.

Et les déchets ont tous leur sortie : la scorie par le Slag Vent qui la détruit, le
résidu par compression en bloc de construction. Le test `everyByproductHasAnOutlet`
garde la seconde ; la première en est exclue exprès, sa corvée EST la contrainte.
