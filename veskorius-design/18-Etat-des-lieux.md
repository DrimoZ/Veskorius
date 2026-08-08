# Veskorius — inventaire de ce qui est codé

Généré à partir du code, pas du dossier de design. Une case cochée ici veut dire
« enregistré, texturé, traduit, testé », pas « écrit dans un .md ».

NeoForge 1.21.1 · Java 21 · **181 fichiers Java, ~23 800 lignes** · **38 blocs, 55 items**
· **138 GameTest**.

---

## 1. Machines (23 au dossier — **13 codées**)

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
| 9 | **Resonance Relay** | T3 | ✅ | Portée 20, chaînable, 1 Osc/t. Tampon, jamais un fil |
| 10 | **Veskorian Alloy Forge** | T3 | ✅ | 2 Refined + 2 lingots → alliage **+ scorie**, 20 s |
| 11 | **Structural Synthesizer** | T3 | ✅ | 4 lingots + 8 pierres → 4 blocs **+ résidu**, 60 s |
| 12 | **Deep Crystal Driller** | T3 | ✅ | Récolte les amas sous Y −40, 6 Osc/t |
| 13 | **Slag Vent** | T3 | ✅ | Vide 1 scorie / 10 s par forge dans 8 blocs |
| 23 | **Flux Compressor** | T3 | ✅ | 4 Refined → 1 Flux Concentré, 30 s, 6 Osc/t |
| — | Damping Array | T3 | ✅ | Purge la dissonance d'un champ |
| 14 | Harmonic Amplifier | T4 | ⬜ | |
| 15 | Deep Synthesis Chamber | T4 | ⬜ | |
| 16 | Automated Extraction Array | T4 | ⬜ | |
| 17 | Resonance Network Hub | T4 | ⬜ | |
| 18 | Convergence Core | T4→T5 | ⬜ | Multi-bloc |
| 19–21 | Rift Anchor / Extractor / Ward | T5 | ⬜ | |

**Le T3 est terminé.** Le T4 et le T5 sont entièrement à faire.

### Le socle commun
Toute machine à cycle hérite d'`AbstractMachineBlockEntity` : progression, slot
d'augment (Catalyst Core, +15 %), On/Off, mode redstone, surchauffe, faces
configurables (entrée/sortie/désactivé), bande harmonique, glow lisible sur la façade.
Neuf types de recette **data-driven** — un datapack change les valeurs sans recompiler.

---

## 2. Énergie & réseau

- **Champ de Résonance** : aucune machine ne se branche ; elle demande « y a-t-il de
  l'énergie ici ? » à un index global par dimension.
- **Anti-stacking** : deux émetteurs superposés n'additionnent rien.
- **Harmoniques** (3 bandes) : une machine désaccordée tourne quand même, coûte plus
  cher, et **injecte de la dissonance** dans le champ.
- **Dissonance** en trois étapes visibles : la coupole grisaille → le champ devient
  intermittent → **décharge AoE**. Le Damping Array la purge, le Relay la renvoie en amont.
- Coupole de particules qui trace la portée réelle. HUD de champ (Locator porté).

## 3. Monde

- **Poches de cristal** (Y −20 à 0) : amas + coquille de pierre veinée.
- **Dépôts de flux** brossables.
- **8 structures jigsaw**, toutes générées par datagen (aucun NBT écrit à la main) :
  Avant-poste (3 niveaux), Hameau + 4 variantes de maison, Sigma Laboratory,
  Guard Post (tour inversée), Drill Shaft, Archive Régionale, Ruin Marker, Sunken Chamber.
- **21 pièces NBT**, 10 pools, processeurs de pourrissage sur **liste blanche**.
- Doctrine dans `17-Dungeons.md` (R1–R11). Tests de **traversabilité réelle** (BFS avec
  tolérances de pas) sur chaque donjon, plus « rien ne flotte ».

## 4. Progression

- Blueprint T2 (Console de l'Avant-poste) et T3 (Console du Sigma), **rendus au craft**.
- Le palier est vérifié par data component — corrigé récemment : jusque-là le plan T2
  ouvrait tout le mod.
- Trois châssis (Fracturé / Accordé / Veskorien), chacun contenant le précédent.
- 7 advancements.

## 5. Énigmes de donjon

- **Sigma** : deux relais endommagés à réparer, la simultanéité naît d'une contrainte de
  trajet (90 s d'autonomie, le second est hors de portée de l'émetteur).
- **Archive** : quatre socles à activer dans l'ordre d'un texte de Codex.
- **Sas** : une porte s'ouvre toujours par un champ. Jamais de serrure.

## 6. Entités

Fileur de Cristal (passif, nourri au spore) · Custode (gardien, alerte de groupe,
lâche du fragment d'alliage — substitut de fer).

## 7. Interface

- **Resonance Codex** : manuel en jeu qui s'écrit tout seul, 35 entrées, 8 catégories.
  L'état de déblocage vit sur le **joueur** et survit à la mort.
- Resonance Tuner : outil à modes (Pivoter / On-Off / Surchauffe / Redstone).
- 12 GUI dessinés par générateur, 61 textures de bloc, palettes indexées.
- **JEI** : toutes les catégories, y compris Forge, Compresseur, Synthétiseur et
  agents du Damping Array — ces deux dernières manquaient encore ce matin.
- Jade, Curios. Lang EN + FR complets.

## 8. Configuration

Cinq fichiers TOML par thème (basics, machines, generation, mobs, harmonics), type
SERVER, surchargeables par modpack.

---

## Ce qui reste

1. **T4 et T5** — 9 machines, la Faille, le multi-bloc Convergence Core.
2. **Structures** — Cœur de Faille (T5), Station Relais (débloquée maintenant que le
   `resonance_relay` existe).
3. **Outils et armure d'alliage** — annoncés au dossier, pas commencés.
4. **Débouchés des déchets** — le résidu de synthèse n'a encore aucun exutoire (voulu :
   c'est ce que le T4 doit résoudre).
