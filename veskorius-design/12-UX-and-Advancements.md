# 12 — UX Conventions & Advancements

Absent des versions précédentes. Nécessaire pour que 23 machines codées par plusieurs sessions
de travail (voir `11-Development-Plan.md`) restent cohérentes à l'usage, et pour que le
déblocage par fragment (répété dans tout le dossier) ait une implémentation technique concrète.

## Conventions d'interface (toutes les machines actives)

| Élément | Convention |
|---|---|
| Barre de progression | Toujours horizontale, gauche → droite, identique au four vanilla pour rester lisible immédiatement |
| Affichage de la consommation Osc | Toujours en haut à droite du GUI, format `X/Y Osc` (actuel/max), jamais une simple icône sans chiffre — cohérent avec la règle "jamais de chiffre vague" (`00`-`11`) |
| Mode surchauffe (Purifier, Chamber) | Icône flamme rouge clignotante sur la barre de progression quand actif, pas un texte séparé |
| Indicateur de dérive de calibration (Amplifier, Hub) | Barre secondaire fine sous la barre principale, jamais un pourcentage seul — doit rester visible sans ouvrir un tooltip |
| Machines sans recette encore débloquée | N'apparaissent pas du tout dans le creative tab tant que le fragment correspondant n'a pas été trouvé (voir Advancements ci-dessous) — cohérent avec pilier 2 |

## Boutons de contrôle dans le GUI (ajout du 2026-07-21, toutes les machines)

Absent des versions précédentes. Une colonne de trois boutons carrés, à gauche des slots, sur
**toutes** les machines actives. Portés par le socle (`AbstractMachineBlockEntity` +
`AbstractMachineScreen`), donc gratuits pour chaque nouvelle machine.

| Bouton | Icône / couleur | Effet |
|---|---|---|
| Interrupteur manuel | `I` vert (marche) / `O` rouge (arrêt) | Coupe/relance la machine à la main. Coupée = **pause** (progression conservée), jamais un reset |
| Contrôle redstone | `R`, gris (ignoré) / rouge vif (requiert un signal) / rouge sombre (requiert l'absence de signal) | Trois modes façon Thermal, dans cet ordre de défilement |
| Surchauffe | `H`, orange (active) / gris (inactive) | **Uniquement sur les machines à surchauffe** (Purifier, Chamber) ; masqué ailleurs |

Règles :
- La machine tourne si : interrupteur sur marche **ET** condition redstone satisfaite **ET**
  ingrédients présents **ET** énergie disponible. Les trois premières coupures mettent en pause ;
  seule l'absence d'ingrédient/de place en sortie réinitialise (cohérent avec la décision prise à
  la tâche 2).
- Le bouton surchauffe **double** le toggle prévu au Resonance Tuner (`05-Machines.md`) : les deux
  agissent sur le même état. Le Tuner arrive à la tâche 9 ; le bouton existe dès maintenant.
- Réseau : aucun packet custom. Le clic passe par le canal vanilla des boutons de menu
  (`clickMenuButton`), l'état revient au client par la `ContainerData` déjà en place pour la barre
  de progression.

Note : la ligne « Mode surchauffe : icône flamme sur la barre » du tableau ci-dessus reste valable
comme *indicateur d'état en cours* ; le nouveau bouton `H` est le *contrôle*. Les deux coexistent
(l'un montre, l'autre bascule) — à implémenter ensemble à la passe visuelle de la Phase 6.

## Resonance Tuner — outil à modes (révisé le 2026-07-21)

Voir `05-Machines.md` pour le craft. **Changement de modèle d'interaction** : la version
précédente faisait dépendre l'action du bloc ciblé, ce qui devenait ambigu sur une machine à la
fois orientée ET à surchauffe (le Purifier : pivoter ou surchauffer ?). Remplacé par un outil à
**modes**, plus lisible et sans ambiguïté :

- Le Tuner porte un **mode courant** (Data Component sur l'item, affiché dans le tooltip).
- **Clic droit** sur une machine : applique l'action du mode courant.
- **Shift-clic droit** (n'importe où) : passe au mode suivant, message en barre d'action.

| Mode | Action au clic droit | Cible |
|---|---|---|
| Pivoter (`ROTATE`) | Fait pivoter la face avant de 90° | Toute machine orientée (y compris le Field Emitter) |
| On/Off (`POWER`) | Bascule l'interrupteur manuel | Toute machine active |
| Surchauffe (`OVERHEAT`) | Bascule le mode surchauffe | Machines qui le supportent (message « pas de surchauffe » sinon) |
| Redstone (`REDSTONE`) | Fait défiler le mode de contrôle redstone | Toute machine active |

Modes à ajouter avec le contenu plus tardif (mêmes fentes, nouveaux modes) :
- **Priorité du Network Hub** (T4) : un mode qui ouvre l'écran de priorité.
- **Recalibration** (T4, Amplifier/Hub) : un mode qui remet la dérive à 100% (coûte 1 Resonance
  Component).
- **Retrait d'augment** (T2+, Catalyst Core, tâche 15) : un mode qui retire le Catalyst Core sans
  le détruire — remplace l'ancien « shift-clic droit », désormais réservé au changement de mode.

Note : le bouton `H` du GUI (couche de contrôle) et le mode Surchauffe du Tuner agissent sur le
même état — deux entrées pour la même bascule, voulu. Idem pour On/Off (bouton `I` / mode POWER)
et Redstone (bouton `R` / mode REDSTONE).

## Advancements — déblocage de recette par fragment

Chaque fragment de Codex qui débloque une recette (voir `03-Progression.md`) est implémenté
comme un `Advancement` NeoForge avec `RecipeUnlockedTrigger`, pas comme un flag custom stocké à
la main — réutilise le système vanilla de "recipe book" plutôt que d'en réinventer un.

| Advancement | Déclenché par | Débloque |
|---|---|---|
| `veskorius:tier1_awakening` | Ramasser un Raw Resonance Crystal pour la première fois | Toast d'intro, aucune recette (T1 déjà libre dès le départ) |
| `veskorius:tier2_field` | Lire le fragment de l'Avant-poste | Recette Field Emitter |
| `veskorius:tier3_relay` | Lire le fragment du Sigma Laboratory | Recette Resonance Relay |
| `veskorius:tier4_amplifier` | Lire le fragment de l'Archive Régionale | Recette Harmonic Amplifier + 3 Hyper Refined Crystal donnés directement par la structure (item, pas par l'advancement) |
| `veskorius:tier5_rift` | Poser un Rift Anchor fonctionnel pour la première fois | Recette Rift Core Extractor (si pas déjà connue) |
| `veskorius:rift_guardian_slain` | Tuer le Gardien de Faille | Toast de fin, statistique "Failles stabilisées" |

Toast affiché à chaque déclenchement (comportement vanilla par défaut d'un `Advancement` avec
`.display()` configuré) — pas de notification custom à coder, réutilisation directe de l'API.

## Ce que ce fichier ne couvre pas

Sons, musique, animations de modèle — hors périmètre (voir `11-Development-Plan.md`, Phase 6,
qui renvoie à un futur fichier dédié si besoin).

## Ouvert

- Faut-il un advancement séparé par machine T4 individuelle (Chamber, Array, Hub, Convergence
  Core) en plus du palier `tier4_amplifier` global ? Probablement non — la règle de déblocage
  transversale (`03-Progression.md`) dit qu'elles sont toutes libres une fois le palier
  atteint, un seul advancement de palier suffit donc côté implémentation.
