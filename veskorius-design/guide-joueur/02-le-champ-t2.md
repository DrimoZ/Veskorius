# 2 — Le champ (T2) : l'énergie sans câbles

Le tier 2 introduit **la Résonance en tant qu'énergie** : des champs, une réserve, des machines qui
consomment. C'est le moment où le mod devient lui-même.

## Ouvrir le T2 : l'Avant-poste

1. Trouvez un **Avant-poste** (ruine souterraine, gardée par un **Custode**).
2. À l'intérieur, une **console d'attunement** (`attunement_console`) — une « machine morte ».
   **Clic droit** dessus : elle se réveille et vous remet le **plan T2** (`resonance_blueprint`).
3. Le **coffre** de l'Avant-poste contient l'**amorçage garanti** : **4 Resonance Component + 2 Gold**
   — exactement de quoi fabriquer votre **premier Field Emitter**.

> **Le blueprint est une clé physique, pas une case cochée.** Il est **rendu** dans chaque recette
> T2 (il ressort du craft, il n'est pas consommé) : rien n'est masqué dans JEI, ce qui « bloque »,
> c'est simplement de ne pas encore avoir le plan. Le Custode qui garde le site n'est **pas**
> obligatoire à vaincre — restez hors de sa portée (6 blocs) et il vous ignore.

## Poser votre premier champ

### Field Emitter
- **Recette** : `4 Resonance Component + 2 Gold + 1 Stable Crystal` (+ blueprint rendu).
- **Portée** : 8 blocs (sphère). **Réserve** : 4000 Osc.
- **Carburant** : il **brûle des Stable Crystals** — 1 cristal = **+4000 Osc** (comme du charbon dans
  un four). Mettez des Stable Crystals dans son slot de carburant ; il fait le plein tout seul quand
  la réserve baisse.
- Un **GUI** montre la jauge de réserve (`X / 4000 Osc`).

> **Pas d'énergie infinie.** Produire de l'Osc coûte des Stable Crystals — donc du minage et du temps
> de Stabilizer. L'énergie est bornée par vos ressources.

### Comment savoir si une machine est alimentée ?
**Regardez-la.** Une machine qui tourne **brille** (glow). Une machine qui a ses ingrédients mais
reste **éteinte** est **hors champ**. C'est le seul retour « pas d'énergie » — et un test de
couverture gratuit : posez la machine, si elle s'allume, elle est dans le champ.

Le Field Emitter actif trace aussi une **coupole** de particules sur sa sphère de portée : vous
*voyez* jusqu'où le champ porte.

## Les machines T2

### Component Assembler (enfin alimenté)
Une fois dans le champ, l'Assembler tourne (3 Osc/tick) et produit vos Resonance Component en série.
La boucle est bouclée : le champ nourrit l'Assembler, l'Assembler fabrique de quoi étendre le réseau.

### Flux Purifier
- `Stable Crystal + Redstone` → **1 Refined Crystal**, ~45 s, **2 Osc/tick**.
- Le Refined Crystal est la ressource « raffinée » du T2 (augments, Damping Array, futurs T3).
- **Mode surchauffe** (bouton du GUI ou Resonance Tuner) : **2× plus rapide**, mais **2× plus
  gourmand** en Osc et **20 % de risque par cycle** de perdre l'entrée sans produire. Un pari que
  *vous* choisissez d'activer.
- C'est aussi la **seule machine accordable avant le T3** (voir [chapitre 3](03-harmoniques-et-dissonance.md)).

### Resonance Storage Cell
- `2 Component + 1 Stable Crystal` → une **batterie portable** (objet), capacité **8000 Osc**.
- Se **recharge dans un champ** : tant qu'elle est dans votre inventaire et que vous êtes couvert
  par un émetteur, elle absorbe des Osc (≤ 20/tick). Sert de réserve pour vos outils.

### Resonance Locator
- Outil : `Stable Crystal + 2 Component + Fer` (+ blueprint). **Batterie interne 100 Osc**, **5 Osc
  par ping** (~20 pings), rechargée dans un champ **ou** en puisant sur une Storage Cell portée.
- **Clic droit** : ping directionnel (direction + distance) vers la **source la plus proche**.
- **Outil à modes** (**Maj + clic droit** pour changer) :
  - **Ressources** : poche de cristal (à courte portée) ou signature de champ (émetteur actif).
  - **Structures** : la structure `#veskorius:locatable` la plus proche (Avant-poste, Habitation) —
    équivaut à un `/locate` intégré. C'est votre outil de repérage post-T2.
- **Bonus** : porter le Locator (inventaire, ou slot **Curios** si le mod est présent) active le
  **HUD de champ** (voir chapitre 3).

### Crystal Roost
- Production passive : `2 Quartz` → **1 Raw Crystal**, 600 s, **à condition qu'un Fileur de Cristal**
  soit à moins de 6 blocs. **Autonome.** Une alternative lente au minage, une fois les poches
  épuisées et un Fileur apprivoisé (voir [chapitre 5](05-monde-et-creatures.md)).

## Le récapitulatif du T2

Vous avez : une source d'énergie (le champ), de quoi la stocker (Storage Cell), de quoi raffiner
(Purifier), de quoi automatiser (Assembler), et de quoi explorer (Locator). Le réseau est en place —
il ne reste qu'à apprendre à le **router** et à l'**entretenir** : c'est le système-signature du mod.

➡️ Suite : **[Harmoniques & Dissonance](03-harmoniques-et-dissonance.md)**.
