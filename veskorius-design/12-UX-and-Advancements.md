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

## Retours visuels dans le monde (ajout du 2026-07-22)

Le pilier 3 (« pas de câbles, des champs invisibles ») laissait le joueur sans aucun signal hors GUI :
une machine hors champ ne bougeait simplement pas sa barre. Trois retours **dans le monde**, portés par
le socle donc gratuits pour les machines à venir :

| Retour | Convention | Où |
|---|---|---|
| Glow « en marche » | Une machine active **rayonne** (lumière 7) uniquement le temps qu'elle avance un cycle ; noire à l'arrêt. Comme le glow suit l'énergie *réelle*, une machine consommatrice d'Osc **hors champ reste éteinte** — c'est le retour « pas d'énergie » lisible sans ouvrir le GUI. | blockstate `LIT` sur `AbstractMachineBlock`, piloté par `AbstractMachineBlockEntity.setLit` |
| Coupole de champ | Le Field Emitter actif sème quelques particules éparses sur sa **sphère de portée**, traçant le dôme au fil du temps : on voit jusqu'où le champ porte. | `FieldEmitterBlockEntity.pulseFieldDome`, purement client |
| Indice d'onboarding | Ligne de tooltip **grisée** sur les objets clés du début (chaîne T1), pointant l'étape suivante sans tout dévoiler. | `ItemHintHandler` (client), clés `item.veskorius.<id>.hint` |

Règle pour la suite : toute nouvelle machine active hérite du glow sans code ; une source de champ future
(Relay, Amplifier, Convergence Core) devrait réutiliser le motif de coupole avec sa propre portée.

## Harmoniques : la couleur comme interface (2026-07-23, voir `06`)

Convention centrale du système Harmoniques & Dissonance : **la bande harmonique est une couleur**, et
elle réutilise les deux visuels déjà codés — **aucun GUI supplémentaire à apprendre**.

| Élément | Convention |
|---|---|
| Coupole de l'émetteur | prend la couleur de la **bande du champ** |
| Glow d'une machine en marche | prend la couleur de la **bande de la machine** |
| Machine désaccordée | glow qui **clignote entre les deux couleurs** (le « ça grince » visuel) |
| Champ dissonant | coupole **désaturée / grésillante**, puis intermittente |

Règle de lisibilité : un joueur doit pouvoir **diagnostiquer sa base en la regardant**, sans ouvrir
un écran. Pas de chiffre de fréquence exposé — des couleurs.

> **État du code (2026-07-23) : les quatre lignes du tableau sont codées.** Le glow d'une machine
> est rendu par de fines particules de la couleur de sa bande au-dessus du bloc, émises tant que le
> blockstate `LIT` est vrai (donc tant qu'elle avance *réellement* un cycle). Une machine
> **universelle n'émet rien du tout** : la couche harmonique reste littéralement invisible tant que
> le joueur n'a rien accordé — c'est ce qui tient la promesse « la T1 ne gagne aucune complexité ».

## HUD de champ (lecture globale)

Overlay discret en coin d'écran, affichant le champ **où se tient le joueur** :
- la **bande** (pastille de couleur + nom),
- la **réserve** de l'émetteur qui le couvre (`X/Y Osc`),
- le **niveau de dissonance** (petite jauge, qui vire au rouge « champ instable » au seuil).

**Conditions d'affichage** : le joueur porte l'objet dédié **dans son inventaire** — **ou dans un
slot Curios si le mod est détecté** (dépendance douce, voir `10`). Alimenté par un petit paquet
serveur→client périodique, envoyé uniquement aux joueurs porteurs.

> **Codé le 2026-07-23** (`FieldHudPayload`, `FieldHudHandler`, `ClientFieldData`,
> `FieldHudOverlay`). Décisions prises au codage :
> - **L'objet de lecture est le Resonance Locator**, pas un nouvel item : il est déjà l'outil de
>   détection, se recharge déjà dans le champ, et n'ajoute rien à la progression. Son tooltip
>   annonce le HUD, sinon rien ne l'expliquerait.
> - **Le serveur n'envoie rien hors champ** ; c'est la *péremption* de la dernière lecture (2 s)
>   qui efface le HUD côté client. Un paquet « rien à signaler » deux fois par seconde et par
>   joueur aurait coûté plus que le HUD ne vaut.
> - **Masqué** quand le GUI est caché (F1) ou l'écran de debug ouvert (F3) : c'est un instrument,
>   jamais un obstacle.
> - Rendu **minimal** (texte + rectangles pleins) : ses textures viennent avec la passe visuelle
>   de la Phase 6, comme le reste des GUI.

## Resonance Tuner — mode « Accorder »

Le Tuner (outil à modes déjà en place) gagne le mode **Accorder** : clic droit sur une machine T3+
règle sa **bande harmonique**. Il rejoint la liste des modes existants (Pivoter, On/Off, Surchauffe,
Redstone) sans nouvelle interaction à apprendre.

**Le cycle repasse par l'universel** (universelle → violet → cyan → ambre → universelle, borné par
`bandCount`). Sans ce retour, accorder serait un geste à **sens unique** : une machine réglée sur la
mauvaise bande resterait définitivement moins bonne qu'avant qu'on y touche — un piège pour le joueur
qui essaie l'outil par curiosité. Une machine T3 qui *doit* porter une bande pourra le refuser
(`allowsUniversal`).

## Automatisation d'objets — capability sidée + config par face (2026-07-23)

**Distinction cardinale : l'ÉNERGIE n'a jamais de tuyaux** (champs de résonance, pilier 3) — mais les
**OBJETS**, eux, circulent par hopper/automatisation, comme dans tout mod technique. Les deux ne se
mélangent pas : l'énergie ne passe jamais par une capability.

Chaque machine active expose une capability `ItemHandler` **par face**, via une vue sidée
(`MachineItemHandler`) pilotée par un mode de face (`SideMode`) :

| Mode | Effet sur la face |
|---|---|
| `DISABLED` | aucune capability exposée (rien ne peut entrer ni sortir par là) |
| `INPUT` | insertion autorisée dans les **slots d'entrée** (filtrée par recette via `isItemValid`) ; extraction interdite |
| `OUTPUT` | extraction autorisée du **slot de sortie** ; insertion interdite |

Le **slot d'augment n'est jamais exposé** : l'automatisation ne peut pas voler le Catalyst Core.
Défaut « façon four » pour marcher immédiatement avec un hopper, sans config : **sortie sous le bloc,
entrée par les autres faces**. Le socle (`AbstractMachineBlockEntity`) porte tout : chaque nouvelle
machine hérite du système sans code (elle déclare juste ses slots d'entrée/sortie).

**Auto-I/O par bloc** : deux bascules opt-in, `autoInput` / `autoOutput`, tickées à débit réduit
(1/8 tick). `autoOutput` pousse la sortie vers l'inventaire adjacent des faces `OUTPUT` ; `autoInput`
tire depuis l'adjacent des faces `INPUT` (toujours filtré par recette). État (modes des 6 faces +
bascules) persisté en NBT ; la capability en cache est invalidée à chaque changement de mode.

**UI de config (2026-07-23, fonctionnelle).** Un **bouton « C »** dans la colonne de contrôle ouvre un
**panneau de config** intégré au GUI de la machine (pas un écran séparé, pour ne pas fermer le
conteneur) : 6 boutons de face (cycle Désactivé/Entrée/Sortie, couleur + tooltip) + 2 bascules auto
(auto-entrée ↓ / auto-sortie ↑). Tout passe par le **canal vanilla des boutons de menu**
(`clickMenuButton`, aucun packet custom) ; l'état des 6 faces + 2 bascules est synchronisé au client
par la `ContainerData` déjà en place (comme la barre de progression). Le socle serveur est complet et
testé (comportement sidé, filtrage recette, augment protégé, auto-push/pull, et le câblage
bouton→état). **Reste (passe visuelle Phase 6) :** un vrai patron de faces en croix plutôt qu'une
grille placeholder, et des textures. Voir aussi le démontage au Tuner, qui lit volontairement
l'inventaire **interne complet** de nos machines (et non la capability sidée, qui ne rendrait pas les
entrées ni l'augment).

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

**Il ne l'annule que sur une cible qu'il sait traiter** *(corrigé le 2026-08-06)* : une block
entity de Veskorius pour l'application d'un mode, n'importe quelle block entity pour le démontage
en shift. Tout le reste garde son interaction normale. L'annulation était auparavant
inconditionnelle, avant même de regarder la cible : Tuner en main, on ne pouvait plus ouvrir un
coffre, un four ou une porte, ni poser un bloc — un outil de configuration ne doit pas confisquer
le clic droit du joueur.

Le démontage lit le contenu du bloc via, dans l'ordre : **l'inventaire interne direct de nos
machines d'abord** (surtout pas leur capability sidée, volontairement insert-only sur les entrées,
qui ne rendrait ni les entrées ni l'augment), puis la capability ItemHandler pour tout le reste
(autres mods, Field Emitter), puis l'interface `Container` vanilla. *(Ordre corrigé le 2026-08-06 :
ce paragraphe donnait l'inverse depuis l'origine.)*

Le **bloc lui-même** est rendu via sa **table de butin** (`Block.getDrops`), pas fabriqué à partir
du bloc. *(Corrigé le 2026-08-06.)* La version précédente faisait `new ItemStack(block)`, ce qui
ignorait toute règle de butin : le Tuner rendait en survie des blocs-entités que le jeu ne donne
jamais (spawner, trial_spawner, vault). Passer par la table respecte les règles de chaque bloc,
vanilla comme moddé.

À surveiller : outil puissant (retrait instantané avec contenu, sans outil requis) — sur un
serveur, une intégration avec les mods de protection reste à faire.

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
