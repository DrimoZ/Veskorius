# 07 — World Generation

## Principe

Le contenu de Veskorius se lit par strates Y, pas par biome — cohérent avec le lore (les âges
les plus anciens sont les plus profonds). Le biome de surface reste indifférent la plupart du
temps ; c'est la profondeur qui porte le sens.

## Strates verticales

| Strate (Y) | Contenu | Tier associé |
|---|---|---|
| 0 à -20 | Poches de Raw Resonance Crystal, Avant-postes en sous-sol peu profond | T1-T2 |
| -20 à -40 | Avant-postes plus rares, premiers signes du Sigma Laboratory à distance (signature de champ détectable au Resonance Locator dès 40 blocs) | T2-T3 |
| -40 à -55 | Sigma Laboratory, veines profondes de cristal (nécessitent Deep Crystal Driller pour être exploitées sans le gaz de Résonance résiduel, voir `06-Energy.md`) | T3-T4 |
| -55 à -64 (bedrock) | Archive Régionale, premières Failles | T4-T5 |

## Matériaux naturels associés aux poches de cristal

- **Resonance Veined Stone** : génère en coquille de 3-5 blocs autour de chaque poche de
  `raw_resonance_crystal`, à toutes les strates. Sert de "tell" visuel avant même de creuser
  jusqu'à la poche (voir `04-Materials.md`).
- **Raw Flux Deposit** : ~15% des blocs de paroi situés à moins de 2 blocs d'une poche portent
  cette croûte brossable (mécanique de brosse vanilla, aucun nouvel outil à coder). Alternative
  de collecte silencieuse pour un joueur qui préfère observer plutôt que miner à l'aveugle.

## Événement météo — Orage de Résonance

Actif uniquement une fois le T3 débloqué (cohérent avec le lore : les premiers signes de
l'Effondrement ne prennent sens qu'une fois le Sigma Laboratory visité). Vérification aléatoire
~1 fois tous les 5-7 jours Minecraft ; s'il se déclenche, dure 10 minutes, visuellement proche
d'un orage vanilla avec une teinte d'aurore supplémentaire. Pendant l'orage, des
`meteoric_resonance_shard` se posent en petits cratères sur les blocs de surface exposés,
ramassables à la main ; tout fragment non récupéré avant la fin de l'orage disparaît — aucun
stock à faire indéfiniment, juste une fenêtre à saisir.

## Densité et rareté (référence de départ, à valider en playtest)

| Structure | Fréquence approx. | Strate |
|---|---|---|
| Avant-poste | 1 / 1500 blocs | 0 à -40 |
| Sigma Laboratory | 1 / 6000 blocs | -40 à -55 |
| Archive Régionale | 1 / 12000 blocs | -55 à -64 |
| Faille | 1 / 15000 blocs | -60 à -64, ou poche isolée générée hors strate normale (voir ci-dessous) |
| Poste de garde (Custode) | 1 / 3000 blocs | 0 à -40, souvent proche d'un Avant-poste |

## Les Failles — génération spéciale

Une Faille n'est pas une structure classique : c'est une **poche de vide partiel**, générée
comme une bulle sphérique (rayon 5-9 blocs) dans laquelle la génération normale du monde est
supprimée, remplacée par un vide semi-translucide avec un noyau flottant (le Rift Core). Elle
peut apparaître à n'importe quelle profondeur sous Y -60, indépendamment du biome de surface —
cohérent avec le lore (une Faille est un accident de sur-résonance, pas un site construit).

Détection : invisible au Resonance Locator classique (elle n'émet pas de champ de Résonance
"normal", elle en déphase un). Nécessite un fragment d'Archive Régionale expliquant comment
reconnaître les signes avant-coureurs (fissures de pierre déformée générées en surface de la
bulle) — dernier maillon du pilier 2 (connaissance spatiale) avant l'endgame.

## Exploration : ce que le joueur utilise, dans l'ordre

1. Rien (T1) — creuse au hasard, trouve des poches par chance.
2. Resonance Locator (T2, voir `05-Machines.md`) — direction approximative vers structure non
   explorée, portée 200 blocs.
3. Signature de champ à distance (T3) — une fois qu'un joueur a visité un Sigma Laboratory, son
   Resonance Locator affiné détecte les grandes structures dès 400 blocs.
4. Lecture des fissures de surface de Faille (T4-T5) — compétence acquise par fragment, pas par
   objet.

## Règle de cohérence pour toute nouvelle structure

Avant de coder une nouvelle structure : d'abord choisir sa strate Y (détermine le tier de
contenu autorisé), puis sa strate sociale (`02-Lore.md`, détermine le loot cohérent). Une
structure qui ne peut pas répondre aux deux n'est pas prête à être écrite.

## Problèmes / Alternatives rejetées

- **Rejeté : lier le contenu à des biomes spécifiques (ex : Sigma Laboratory seulement sous
  désert).** Rejeté — romprait la lecture "par profondeur" qui est la colonne vertébrale du
  monde, et compliquerait inutilement la génération pour un gain narratif faible.
- **Rejeté : rendre les Failles détectables au Resonance Locator classique.** Rejeté — la Faille
  doit rester un secret de fin de jeu découvert par l'observation directe (fissures), pas par un
  outil qu'on a déjà en T2.
- **Rejeté : faire des Failles une dimension séparée (façon Nether/End).** Rejeté pour contenir
  la portée du projet — une poche in-world (bulle sphérique) donne la même sensation de rupture
  avec le monde normal, pour une fraction du travail technique (pas de nouveau `DimensionType`,
  pas de portail, pas de génération de chunk dédiée).

## Ouvert

- Faut-il une variante de surface des Avant-postes (actuellement possibles en sous-sol peu
  profond seulement) pour varier le rythme d'exploration en début de partie ? Probablement oui,
  à trancher lors de l'écriture détaillée dans `08-Structures.md` v0.2.
