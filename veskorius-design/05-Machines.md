# 05 — Machines (23 blocs + 2 outils/augment transversaux)

## Deux tableaux à ne pas confondre

Un bloc actif (Stabilizer, Assembler, Purifier, etc.) a **deux** recettes distinctes :
1. Sa **recette de construction** (une fois, pour poser le bloc) — tableau juste en dessous.
2. Sa **recette de fonctionnement** (répétée à chaque cycle une fois le bloc posé) — tableau
   "Vue d'ensemble" qui suit. Les versions précédentes de ce fichier ne montraient que #2, ce qui
   ne suffit pas pour coder quoi que ce soit : on ne peut pas fabriquer un Stabilizer sans savoir
   ce qu'il coûte à poser. Corrigé ici.

Les blocs passifs (Field Emitter, Relay, Amplifier, Driller, Hub, Anchor...) n'ont qu'une seule
recette (leur construction) puisqu'ils ne "traitent" rien — elle reste directement dans le
tableau "Vue d'ensemble", précédée de `craft:`.

## Recettes de construction (blocs actifs uniquement)

| Machine | Recette de construction |
|---|---|
| Resonance Stabilizer | 4 Cobblestone + 2 Copper Ingot + 1 Raw Resonance Crystal |
| Component Assembler | 3 Iron Ingot + 2 Stable Resonance Crystal + 1 Redstone |
| Resonance Whetstone | 2 Cobblestone + 1 Iron Ingot + 1 Stable Resonance Crystal |
| Flux Purifier | 4 Iron Ingot + 2 Stable Resonance Crystal + 1 Redstone Block |
| Veskorian Alloy Forge | 4 Stone Bricks + 2 Refined Resonance Crystal + 2 Iron Ingot |
| Structural Synthesizer | 6 Veskorian Alloy Ingot + 2 Refined Crystal + 1 Diamond |
| Deep Synthesis Chamber | 6 Veskorian Alloy Ingot + 2 Refined Crystal + **1 Hyper Refined Crystal (installé comme catalyseur permanent, voir section Bootstrap T4 ci-dessous)** |
| Rift Core Extractor | 4 Hyper Refined Crystal + 2 Veskorian Alloy Block + 1 Diamond Block — pose valide uniquement à l'intérieur d'une Faille ancrée |
| Rift Ward Emitter | 2 Rift Essence + 4 Veskorian Alloy Block |
| Slag Vent | 4 Iron Ingot + 1 Redstone + 1 Resonance Component |
| Crystal Roost | 4 Planches (peu importe l'essence) + 2 Stable Resonance Crystal + 1 Botte de Foin |
| Crystal Crusher | 3 Cobblestone + 1 Iron Ingot (voir Vue d'ensemble, #22) |
| Flux Compressor | 4 Iron Ingot + 2 Veskorian Alloy Ingot + 1 Redstone Block (#23) |
| Convergence Core | 12 Veskorian Alloy Block + 6 Harmonic Lattice + 4 Hyper Refined Crystal + 4 Concentrated Flux |

## Vue d'ensemble (recettes de fonctionnement + blocs passifs)

| # | Machine | Tier | Input (par cycle) | Output | Temps | Énergie |
|---|---|---|---|---|---|---|
| 1 | Resonance Stabilizer | T1 | Raw Crystal + Quartz | Stable Crystal | 30s | autonome |
| 2 | Component Assembler | T1 | Stable Crystal + 2 Iron Ingot | 2 Resonance Component | 5s | 3 Osc/tick |
| 3 | Resonance Whetstone | T1 | Outil endommagé + Stable Crystal | Outil réparé de 25% | 8s | autonome |
| 4 | Field Emitter | T2 | craft: 4 Component + Stable Crystal + 2 Gold Ingot | champ, portée 8 | — | réserve 4000 Osc |
| 5 | Flux Purifier | T2 | Stable Crystal + Redstone | Refined Crystal | 45s (22s en surchauffe) | 2 Osc/tick (4 en surchauffe) |
| 6 | Resonance Storage Cell | T2 | craft: 2 Component + 1 Stable Crystal | stocke 8000 Osc, portable | — | passif |
| 7 | Resonance Locator | T2 | craft (outil, non consommable) | ping directionnel, **outil à modes** (Ressources / Structures ; maj+clic droit change de mode) | — | 5 Osc/utilisation — *révisé `16` §1* |
| 8 | Crystal Roost | T2 | passif, nourri au Quartz (4 tous les jours MC) | 1 Raw Crystal / 600s si Fileur de Cristal à proximité | continu | aucune |
| 9 | Resonance Relay | T3 | craft: 4 Refined Crystal + 2 Veskorian Conductive Alloy Ingot + 1 Diamond | portée 20, chaînable | — | 1 Osc/tick (champ) |
| 10 | Veskorian Alloy Forge | T3 | 2 Refined Crystal + 2 **Iron** Ingot **(ou 2 Gold Ingot, voir branche)** | 1 Alloy Ingot **(ou Conductive Alloy Ingot)** + 1 Flux Slag | 20s | 4 Osc/tick |
| 11 | Structural Synthesizer | T3 | 4 Alloy Ingot + 8 Stone | 4 Veskorian Alloy Block + 1 Synthesis Residue | 60s | via champ (Osc/tick à fixer) — *révisé `16` §0 : plus « via Relay uniquement »* |
| 12 | Deep Crystal Driller | T3 | craft: 6 Component + 2 Alloy Ingot | mine automatiquement une veine détectée (Y < -40) | continu | 6 Osc/tick |
| 13 | Slag Vent | T3 | craft (passif) | vente 1 Flux Slag / 10s par Forge dans un rayon de 8 blocs | continu | 1 Osc/tick |
| 14 | Harmonic Amplifier | T4 | craft: Harmonic Lattice + 2 Refined Crystal | double la portée reçue (dérive, voir Calibration) | — | 2 Osc/tick prélevé |
| 15 | Deep Synthesis Chamber | T4 | 2 Refined Crystal (catalyseur déjà installé au craft) | Hyper Refined Crystal | 90s (45s en surchauffe) | 8 Osc/tick (16 en surchauffe) |
| 16 | Automated Extraction Array | T4 | craft: 4 Alloy Ingot, synchronise les Driller déjà posés | automatise plusieurs Driller | continu | 10 Osc/tick |
| 17 | Resonance Network Hub | T4 | craft: 4 Component + 2 Harmonic Lattice | priorité de répartition (dérive, voir Calibration) | — | passif |
| 18 | Convergence Core | T4→T5 | multi-bloc (voir section dédiée) | champ fixe portée 40, intensité max | — | 12 Osc/tick |
| 19 | Rift Anchor | T5 | 4 Hyper Refined Crystal + 4 Alloy Block, posé en bordure de Faille | stabilise la Faille | pose unique | 20 Osc/tick en continu |
| 20 | Rift Core Extractor | T5 | placé dans une Faille ancrée | 1 Rift Essence (+ 15% chance de 1 Corrupted Alloy Ingot) | 120s / extraction, 6 max par Faille | 15 Osc/tick |
| 21 | Rift Ward Emitter | T5 | craft: 2 Rift Essence + 4 Alloy Block | annule la corrosion ambiante, rayon 12 blocs | — | 5 Osc/tick |
| 22 | Crystal Crusher | T1 | Raw Crystal | 3 Resonance Dust | 10s | autonome |
| 23 | Flux Compressor | T3 | 4 Refined Crystal | 1 Concentrated Flux | 30s | 6 Osc/tick |

**Outil transversal — Resonance Tuner** (T2, non compté dans les 23) : craft 2 Iron Ingot + 1
Resonance Component + 1 Redstone. **Outil à modes** (révisé, voir `12-UX-and-Advancements.md`) :
il porte un mode courant, le clic droit sur une machine applique ce mode, le shift-clic droit
change de mode. Modes codés : Pivoter, On/Off, Surchauffe, Redstone. Modes à venir avec leur
contenu : priorité du Network Hub, recalibration Amplifier/Hub (coûte 1 Resonance Component),
retrait d'un Catalyst Core sans le détruire (ce dernier remplace l'ancien « shift-clic droit »,
désormais utilisé pour changer de mode). Un seul outil pour toutes les interactions de
configuration.

**Augment transversal — Resonance Catalyst Core** (T2, item, non compté dans les 23) : craft 2
Resonance Component + 1 Refined Crystal + 1 Redstone. Ne se pose pas seul — s'insère dans les
slots d'augment que possède désormais chaque machine active de la liste ci-dessus (slots
génériques côté code, valables pour toutes sans exception ; ce sont les mêmes slots que ceux
utilisés par l'intégration Thaumcraft du Cristal de Vis, voir `10-Mod-Integrations.md` — un seul
jeu de slots annexes par machine, pas deux systèmes séparés). Effet : +15% de vitesse de cycle en
permanence, jamais consommé ni sujet à la dérive de calibration —
*(**révisé 2026-07-23** : le slot unique devient **N slots configurables**, avec des règles de cumul
réglables en config — voir la section « Slot d'augment → slots d'augment » plus bas et `14`.)*
contrairement à celle-ci, qui touche uniquement l'Amplifier/le Hub, le Catalyst Core est un
choix d'investissement ponctuel, pas un entretien.

## Ajouts de la révision harmonique (2026-07-23, voir `06` et `16`)

Ces machines ne renumérotent pas les 23 ci-dessus : elles s'ajoutent avec le système Harmoniques &
Dissonance.

| Machine | Tier | Rôle |
|---|---|---|
| **Émetteur Accordable** (`tunable_field_emitter`) ✅ codé | T2+ | Field Emitter dont on **choisit la bande** harmonique (Tuner, mode Accorder). C'est lui qui introduit le choix de fréquence (le Field Emitter T2 de base reste mono-bande, sans décision). Craft : Field Emitter + 2 Refined Crystal (+ blueprint rendu) — un *upgrade*, pas une machine de plus. |
| **Damping Array** (`damping_array`) | T3 | **Absorbe la dissonance** d'un champ. Se **sature** (container à purger) et consomme du **Concentrated Flux**. La gestion de dissonance est de l'infrastructure, pas un slot. |
| **Reclaimer** (`reclaimer`) | T3 | **Re-stabilise les déchets** (`resonance_sludge`, `flux_slag`) en une fraction de matériaux ou en bloc de construction. Ferme la boucle économique. |
| **Advanced Assembler** (`advanced_assembler`) | T3 | Compose le **`resonance_matrix`** (Component + alliage conductif), pièce intermédiaire requise par les machines T4. |

### Bandes harmoniques sur les machines

À partir du T3, une machine porte une **bande** (voir `06`). Réglée au **Resonance Tuner**, nouveau
mode **« Accorder »** (l'outil à modes existe déjà — `12`). Les machines **T1 restent universelles**
(aucune bande, acceptent n'importe quel champ) : la boucle de départ ne gagne aucune complexité.

Le **glow** d'une machine en marche prend la couleur de sa bande — le diagnostic est visuel.

### Recettes increvables — `stable`

Les recettes de fonctionnement (`MachineRecipe`, data-driven) gagnent un champ **`stable`** :

- `stable: true` → la recette **réussit toujours**, quels que soient le désaccord et la dissonance
  de la machine. **Toutes les recettes T1 le sont par défaut.**
- La **surchauffe** conserve son risque même sur une recette stable (c'est un pari que le joueur
  choisit d'activer), sauf réglage contraire en config.

Levier direct pour un modpack maker : rendre increvable ce qu'il veut, sans toucher au code.

### Slot d'augment → slots d'augment

Le slot unique devient **N slots configurables** (par machine ou par tier), avec des **règles de
cumul** réglables : un même effet est-il cumulable dans un slot / entre slots, avec quel plafond ?
Tout est en config (`14`). Nouveaux augments prévus, au-delà du Catalyst Core (+15 % vitesse) :

| Augment | Effet |
|---|---|
| `resonance_catalyst_core` | +15 % vitesse (existant) |
| **Efficiency Core** | −20 % d'Osc consommé |
| **Yield Core** | ~10 % de chance de doubler la sortie |
| **Tuning Core** | verrouille la bande de la machine (résiste à la dérive harmonique) |
| **Damping Core** | réduit la dissonance émise par la machine |

## Bootstrap du T4 — pourquoi 3 Hyper Refined Crystal et pas 2 ni 4

L'Archive Régionale donne exactement **3** Hyper Refined Crystal (voir `08-Structures.md`) :
2 sont consommés dans le Harmonic Lattice (premier Harmonic Amplifier), le 3e est consommé au
moment de construire la Deep Synthesis Chamber elle-même (il devient son catalyseur permanent,
jamais listé comme input de cycle après ça). Une fois la Chamber posée, elle produit du Hyper
Refined Crystal en continu (2 Refined Crystal → 1 Hyper Refined, 90s) — la ressource devient
renouvelable à partir de ce point. Avec 2 crystal, le joueur ne pourrait jamais construire la
Chamber (resterait bloqué à un seul Amplifier, sans façon d'en refaire) ; avec 4, la Chamber
serait accessible sans avoir à faire le choix structurant "je construis d'abord mon premier
Amplifier, ou je sécurise ma production long terme ?". 3 est le nombre qui force ce choix sans
bloquer personne.

## Nouveaux styles de craft/process (au-delà du simple input → attends → output)

Les six machines suivantes introduisent chacune un mécanisme différent, pour qu'un joueur qui a
vu les dix premières machines ne se dise pas "encore un four" en voyant les suivantes.

1. **Surchauffe (risque/récompense)** — Flux Purifier et Deep Synthesis Chamber ont un mode
   optionnel (bascule via Resonance Tuner) : temps divisé par 2, consommation Osc doublée, mais
   20% de chance par cycle que l'input soit détruit sans produire l'output. Choix actif du
   joueur entre vitesse et sécurité, pas une amélioration strictement meilleure.
2. **Sous-produit à gérer (maintenance)** — le Veskorian Alloy Forge produit 1 Flux Slag par
   cycle en plus de l'Alloy Ingot, dans un tampon interne de 16. Plein, la Forge s'arrête jusqu'à
   évacuation (manuelle, ou automatique via un Slag Vent à portée). Clin d'œil de lore
   intentionnel : le Flux Slag est chimiquement la même substance qui, à l'échelle régionale, a
   participé à la sur-résonance de l'Effondrement (`02-Lore.md`) — le joueur reproduit à petite
   échelle, et de façon gérable, le même phénomène qui a détruit la civilisation qu'il restaure.
3. **Élevage passif (faune + machine)** — le Crystal Roost ne "fabrique" rien lui-même : il
   attire et nourrit des Fileur de Cristal (`09-Entities.md`), qui génèrent alors une ressource
   lentement en continu. Premier mécanisme du mod qui mélange gestion de faune et production.
4. **Dérive / calibration (le réseau vieillit)** — Harmonic Amplifier et Resonance Network Hub
   perdent 1% d'efficacité par jour Minecraft d'utilisation continue (plafond -30%), remise à
   niveau par le Resonance Tuner. Rend le pilier "le réseau est vivant" littéral : un réseau
   avancé demande un entretien actif, pas juste un investissement initial.
5. **Multi-bloc capstone** — le Convergence Core n'est pas un bloc unique mais une structure
   validée (voir section dédiée ci-dessous). Premier vrai projet de construction du jeu plutôt
   qu'un simple bloc à poser.
6. **Catalyseur non consommé au cycle, mais consommé à la construction** — Deep Synthesis
   Chamber (voir Bootstrap ci-dessus) : le joueur "paie" une fois pour une capacité permanente,
   contrairement à toutes les machines à input consommé à chaque cycle.
7. **Branchement de recette selon le métal fourni** — le Veskorian Alloy Forge produit un
   alliage structurel avec du Iron Ingot, ou un alliage conductif avec du Gold Ingot, sans
   changer de bloc ni de recette de construction. Première machine du mod où le choix de
   l'input change la nature du résultat plutôt que sa seule quantité.
8. **Augment modulaire permanent** — le Resonance Catalyst Core (voir ci-dessus) s'installe une
   fois, ne se dégrade jamais, et fonctionne indépendamment de la dérive de calibration. Premier
   système d'amélioration de machine qui n'est ni consommable à chaque cycle ni sujet à
   l'entretien — un pôle stable face aux mécaniques 1, 2 et 4 qui demandent toutes une attention
   répétée.

## Convergence Core (multi-bloc, T4 → T5)

Structure requise : le bloc Convergence Core au centre, entouré d'un anneau de 8 Resonance Relay
**ou** Harmonic Amplifier (mélange autorisé) posés à exactement 5 blocs de distance, tous avec
ligne de mire directe vers le Core (validation par ray-cast au moment de la pose du dernier
élément de l'anneau). Une fois validée, le Core émet un champ fixe de portée 40 blocs à
intensité maximale — volontairement une exception à la règle de non-stacking (`06-Energy.md`) :
c'est la seule façon en jeu de reproduire, à petite échelle et en sécurité, un réseau de la
puissance de l'âge d'or. Sert avant tout à alimenter un Rift Anchor sans lui dédier une base
entière de relais.

## Problèmes / Alternatives rejetées

- **Rejeté : un four "universel" qui ferait tout.** Dilue la lecture spatiale du monde
  (pilier 2) — chaque machine doit rester associée à un lieu/âge précis de découverte.
- **Rejeté : Rift Essence renouvelable via une machine de régénération.** Romprait la seule
  vraie ressource finie du mod.
- **Rejeté : rendre la surchauffe strictement meilleure (aucun risque réel).** Annulerait
  l'intérêt du choix — sans risque de perte d'input, tout le monde l'activerait en permanence et
  ce serait juste "la vraie vitesse" du Purifier/Chamber.
- **Rejeté : calibration qui arrête complètement la machine à 0% plutôt qu'un plafond à -30%.**
  Rejeté — un arrêt dur serait punitif plutôt qu'un entretien ; le plafond garde la machine
  toujours utile même négligée longtemps.
- **Rejeté : un bloc séparé pour l'Alloy Forge conductif.** Rejeté — un bloc distinct par
  variante d'alliage doublerait le nombre d'entrées sans ajouter de décision de gameplay ; la
  branche de recette sur un seul bloc suffit et reste plus simple à retenir.
- **Rejeté : plusieurs Resonance Catalyst Core cumulables sur une même machine.** Rejeté — un
  cumul illimité rendrait la vitesse de traitement incontrôlable en fin de jeu ; un seul par
  machine garde l'augment comme un choix notable plutôt qu'un simple multiplicateur à empiler.

## Ouvert

- L'écart T2→T3 identifié dans une version précédente est maintenant comblé par quatre machines
  supplémentaires à construire avant le Relay (Crystal Roost, Slag Vent, Crystal Crusher, Flux
  Compressor). Vérifier en playtest que ça ne surcharge pas au contraire cette fenêtre de jeu.
