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
| Gating des machines de tier supérieur | **Aucune recette n'est masquée** (révisé 2026-07-22). Une machine non débloquée est visible partout (creative tab, JEI, recipe book) ; ce qui bloque, c'est qu'il manque le `resonance_blueprint` du tier dans sa recette (ingrédient rendu). Voir `03`/`08`. Filtrer le creative tab par advancement était bancal (pas de contexte joueur) et contraire au créatif — abandonné |

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
- **Clic droit sur une machine** : applique l'action du mode courant, **sans ouvrir le GUI**.
- **Clic droit dans le vide** : passe au mode suivant, message en barre d'action.
- **Shift + clic droit sur un bloc-entité** : le **démonte** — rend le bloc et tout son contenu
  au joueur (priorité à l'inventaire, sol si plein). Valable sur n'importe quel bloc doté d'une
  block entity, y compris d'autres mods.

Détail technique important : l'action et le démontage passent par
`PlayerInteractEvent.RightClickBlock`, **pas** par `Item.useOn`. Sinon, sur un clic droit sans
shift, l'interaction du bloc (ouverture du GUI de la machine) a la priorité et l'action du Tuner
ne se déclenche jamais. L'événement se produit avant la résolution bloc/item ; le Tuner l'annule
pour prendre la main.

Le démontage lit le contenu du bloc via, dans l'ordre : la capability ItemHandler (autres mods +
Field Emitter), l'inventaire direct des machines du mod (qui ne l'exposent pas), puis l'interface
`Container` vanilla. À surveiller : outil puissant (retrait instantané avec contenu, sans outil
requis) — sur un serveur, une intégration avec les mods de protection reste à faire.

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
  le détruire (le shift-clic droit étant désormais pris par le démontage).

Note : le bouton `H` du GUI (couche de contrôle) et le mode Surchauffe du Tuner agissent sur le
même état — deux entrées pour la même bascule, voulu. Idem pour On/Off (bouton `I` / mode POWER)
et Redstone (bouton `R` / mode REDSTONE).

## Advancements — feedback, pas gating (révisé 2026-07-22)

**Changement de modèle.** Le gating n'est plus un advancement qui « débloque une recette » (v1) :
il est **physique** (le `resonance_blueprint` du tier, ingrédient rendu — voir `03`/`08`). Les
advancements ne débloquent donc **plus rien** ; ils servent uniquement de **feedback** (toast +
progression narrative), déclenchés quand le joueur obtient le blueprint ou franchit une étape.

| Advancement | Déclenché par | Rôle |
|---|---|---|
| `veskorius:tier1_awakening` | Ramasser un Raw Resonance Crystal pour la première fois | Toast d'intro |
| `veskorius:tier2_field` | Obtenir le `resonance_blueprint` T2 (console de l'Avant-poste) | Toast « Réseau court restauré » |
| `veskorius:tier3_relay` | Obtenir le blueprint T3 (Sigma Laboratory) | Toast |
| `veskorius:tier4_amplifier` | Obtenir le blueprint T4 (Archive Régionale) | Toast |
| `veskorius:tier5_rift` | Poser un Rift Anchor fonctionnel | Toast |
| `veskorius:rift_guardian_slain` | Tuer le Gardien de Faille | Toast de fin, statistique "Failles stabilisées" |

Toast affiché à chaque déclenchement (comportement vanilla par défaut d'un `Advancement` avec
`.display()`) — pas de notification custom à coder. Les advancements sont accordés en code au bon
moment (ex. le `tier2_field` par la console d'attunement).

## Ce que ce fichier ne couvre pas

Sons, musique, animations de modèle — hors périmètre (voir `11-Development-Plan.md`, Phase 6,
qui renvoie à un futur fichier dédié si besoin).

## Ouvert

- Faut-il un advancement séparé par machine T4 individuelle (Chamber, Array, Hub, Convergence
  Core) en plus du palier `tier4_amplifier` global ? Probablement non — la règle de déblocage
  transversale (`03-Progression.md`) dit qu'elles sont toutes libres une fois le palier
  atteint, un seul advancement de palier suffit donc côté implémentation.
