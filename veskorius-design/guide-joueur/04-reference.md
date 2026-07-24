# 4 — Référence : machines, objets, outils

Toutes les valeurs ci-dessous sont celles **par défaut** ; la plupart sont **réglables** par le
créateur de modpack (voir `14-Configuration.md`) et le contenu (recettes, loot, worldgen) est
**surchargeable par datapack**.

## Machines actives

| Machine | Tier | Recette de fonctionnement | Durée | Énergie |
|---|---|---|---|---|
| **Resonance Stabilizer** | T1 | Raw Crystal + Quartz *(ou Raw Flux)* → 1 Stable Crystal | 30 s | autonome |
| **Component Assembler** | T1 | Stable Crystal + 2 Fer → 2 Resonance Component | 5 s | **3 Osc/tick (champ requis)** |
| **Crystal Crusher** | T1 | 1 Stable Crystal → 3 Resonance Dust | 10 s | autonome |
| **Resonance Whetstone** | T1 | Outil endommagé + Stable Crystal → +25 % réparé | 8 s | autonome |
| **Field Emitter** | T2 | *(carburant)* brûle 1 Stable Crystal → +4000 Osc | — | émet un champ (portée 8, réserve 4000) |
| **Flux Purifier** | T2 | Stable Crystal + Redstone → 1 Refined Crystal | 45 s (22 s surchauffe) | 2 Osc/tick (4 en surchauffe) |
| **Crystal Roost** | T2 | 2 Quartz → 1 Raw Crystal *(Fileur ≤ 6 blocs requis)* | 600 s | autonome |
| **Tunable Field Emitter** | T2+ | *(comme le Field Emitter)* + **choix de bande** | — | émet un champ accordable |
| **Damping Array** | T2+ | absorbe la dissonance, consomme 1 agent → 1 `resonance_sludge` | ~5 s/cycle | autonome (0 Osc, à dessein) |

**Recette de construction** (le craft du bloc) ≠ recette de fonctionnement (ce qu'il fait). Exemples
de construction : Field Emitter = `4 Component + 2 Gold + 1 Stable Crystal` (+ blueprint) ; Flux
Purifier = `4 Fer + 2 Stable Crystal + 1 Bloc de Redstone` (+ blueprint) ; Damping Array = `4 Fer +
2 Refined Crystal + 1 Bloc de Redstone` (+ blueprint).

## Objets & matériaux

| Objet | Rôle |
|---|---|
| `raw_resonance_crystal` | Minerai de base (poches Y 0/−20). **Instable porté trop longtemps.** |
| `stable_resonance_crystal` | Brique universelle T1/T2 **et carburant** du champ (4000 Osc). |
| `refined_resonance_crystal` | Ressource raffinée T2 (Purifier). Augments, Damping Array, agent de damping. |
| `resonance_component` | Pièce intermédiaire (Assembler). Nécessaire à la plupart des machines. |
| `resonance_dust` | Poussière (Crusher) → branche alternative de Component. |
| `raw_flux_deposit` | Brossé sur les poches. Remplace le Quartz au Stabilizer (1:1). |
| `resonance_catalyst_core` | **Augment** : +15 % de vitesse (voir ci-dessous). |
| `resonance_sludge` | Déchet cristallisé (Damping Array). Débouchés à venir en Phase 2. |
| `resonance_spore` | Récolté sur la pierre veinée exposée (voir chapitre 5). Reproduit le Fileur. |
| `custode_alloy_fragment` | Drop du Custode. **Substitut 1:1 du fer** dans les recettes Veskorius. |
| `fossilized_ration` | Nourriture d'ambiance (loot de ruines). |
| `codex_fragment` | Lore lisible (parfois un indice). Ne débloque rien mécaniquement. |

## Outils

| Outil | Ce qu'il fait |
|---|---|
| **Resonance Locator** | Ping directionnel (modes **Ressources** / **Structures**, Maj+clic droit). Porté, active le **HUD de champ**. Batterie 100 Osc, 5/ping, recharge dans un champ ou via Storage Cell. |
| **Resonance Storage Cell** | Batterie portable 8000 Osc, se recharge dans un champ. Alimente les outils hors champ. |
| **Resonance Tuner** | Outil **à modes**, clic droit sur une machine : **Pivoter**, **Marche/Arrêt**, **Surchauffe**, **Cycle redstone**, **Accorder** (bande harmonique). Maj+clic droit : **démonter** un bloc (récupère son contenu). |
| **Resonance Codex** | Le **manuel en jeu** (voir plus bas). |

## Augments (slot d'augment)

Chaque machine a **au moins un slot d'augment** (le dernier de son inventaire). L'augment est **posé,
jamais consommé**.

- **Resonance Catalyst Core** (`2 Component + 1 Refined Crystal + Redstone`, + blueprint) :
  **+15 % de vitesse**.
- **Plusieurs slots** : le nombre de slots d'augment actifs est **réglable** (`augmentSlots`, défaut
  **1** = comme avant, jusqu'à 4). Avec plusieurs slots, une **règle de cumul** (`augmentStacking` :
  interdit / plafonné / libre) décide si plusieurs exemplaires du même effet s'additionnent. Le bonus
  de vitesse **se compose** (k cœurs → temps ÷ 1,15^k).
- *(Efficiency / Yield / Tuning / Damping Core viendront en Phase 2.)*

## Automatisation d'objets (mais jamais l'énergie)

**L'énergie n'a jamais de tuyaux — les objets, oui.** Chaque machine expose une capability
`ItemHandler` **par face**, façon four : par défaut, **sortie par le dessous**, **entrée par les
autres faces** → les **hoppers marchent tout de suite**.

Le **bouton « C »** du GUI ouvre un **panneau de configuration** : pour chaque face, cyclez
**Désactivé / Entrée / Sortie**, et activez l'**auto-entrée** / **auto-sortie** (la machine pousse
sa sortie / tire son entrée toute seule vers les inventaires adjacents). Les augments ne sont
**jamais** exposés à l'automatisation.

## Le Resonance Codex (manuel en jeu)

Un **guide qui s'écrit tout seul** : ses pages se **débloquent au fil de votre progression**
(fabriquer un objet, gagner un advancement, lire un fragment). Vous le recevez à la **première
connexion** (et une recette de secours existe : Livre + Raw Crystal). L'état de déblocage vit **sur
le joueur** — il **survit à la mort** et s'accumule même quand vous ne portez pas le Codex. Clic
droit pour l'ouvrir.

## Retours visuels & contrôle (rappel)

- **Glow** = la machine tourne (donc alimentée) ; **éteinte** = hors champ.
- **Boutons du GUI** : Marche/Arrêt, mode Redstone, Surchauffe (si supportée), Config faces.
- Le **Resonance Tuner** fait tout ça à distance, sans ouvrir le GUI.

➡️ Suite : **[Le monde, les créatures, les structures](05-monde-et-creatures.md)**.
