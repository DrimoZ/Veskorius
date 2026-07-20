# 09 — Entities

Règle transversale (pilier 4 + `02-Lore.md`, strate Custodes) : tout mob gardien est **réactif**,
jamais agressif par défaut. Un joueur qui reste hors d'une zone de détection définie ne se fait
jamais attaquer.

## Vue d'ensemble

| Mob | Rôle | PV | Dégâts | Où | Agressivité |
|---|---|---|---|---|---|
| Custode | Garde standard | 30 | 6 | Poste de Garde, Avant-poste (rare) | Réactif, rayon 6 blocs |
| Custode Lourd | Garde renforcé | 60 | 9 | Sigma Laboratory, Archive Régionale | Réactif, rayon 8 blocs |
| Fileur de Cristal | Faune neutre | 10 | 0 (fuit) | Près des poches de cristal, Y 0 à -40 | Neutre, jamais hostile |
| Custode Archiviste (mini-boss optionnel) | Gardien d'élite | 150 | 12 | Archive Régionale, salle profonde optionnelle | Réactif, rayon 10 blocs |
| Gardien de Faille (boss final) | Boss endgame | 300, 3 phases | 14-20 selon phase | Cœur de Faille, après pose du Rift Anchor | Déclenché, pas réactif |

## Custode (garde standard)

IA : patrouille un point fixe, attaque seulement si le joueur entre dans son rayon de détection
ou endommage une machine du site. Peut être évité en restant à distance — cohérent avec le refus
explicite de combat obligatoire au Poste de Garde (`08-Structures.md`). Drop de combat : 2-4
`custode_alloy_fragment` (voir `04-Materials.md` — substitut 1:1 de l'Iron Ingot dans toute
recette Veskorius, récompense un style de jeu combat plutôt que minage pur).

## Custode Lourd (variante d'élite)

Même comportement réactif que le Custode standard, mais rayon de détection plus large et
capacité à alerter un second Custode Lourd à moins de 16 blocs (justifie la difficulté accrue du
Sigma Laboratory/Archive Régionale sans introduire d'agressivité non motivée).

## Fileur de Cristal (faune neutre)

Petite créature passive qui se nourrit de résidus de Résonance près des poches de cristal brut.
Ne combat jamais. Peut être "traite" (clic droit, cooldown 5 minutes) pour obtenir 1 Raw
Resonance Crystal sans miner — source mineure alternative, volontairement plus lente que le
minage direct pour ne pas remplacer l'exploration. Se reproduit selon le mécanisme de reproduction
animale vanilla : donner un `resonance_spore` (récolté sur du Resonance Veined Stone en faible
luminosité, voir `04-Materials.md`) à deux adultes à proximité produit un bébé Fileur — permet à
un joueur d'établir un cheptel stable plutôt que de dépendre de rencontres aléatoires en
exploration. Peut aussi être attiré et gardé près d'un
**Crystal Roost** (T2, voir `05-Machines.md`) : tant qu'au moins un Fileur reste à moins de 6
blocs d'un Roost nourri, celui-ci génère passivement 1 Raw Resonance Crystal toutes les 600
secondes — un deuxième mécanisme d'obtention, tout aussi lent, pensé comme complément et non
remplacement du minage.

## Custode Archiviste (mini-boss optionnel)

Gardien d'élite qui protège la salle profonde de l'Archive Régionale, séparée de la salle de
lecture principale (qui elle donne accès au fragment obligatoire via puzzle, sans combat — voir
`08-Structures.md`). Le combattre est optionnel et donne une récompense bonus : 2 Hyper Refined
Crystal supplémentaires (utile mais non bloquant pour la progression T4).

- Phases : aucune — combat à une seule phase, pensé comme un "check" de fin de T3/début T4, pas
  un boss narratif majeur.
- Attaque signature : projette un champ de Résonance instable qui inflige des dégâts sur la
  durée si le joueur y reste (force le mouvement, pas un simple tank-and-spank).

## Gardien de Faille (boss final)

Apparaît uniquement après la pose d'un Rift Anchor fonctionnel sur une Faille — pas une
rencontre aléatoire. Trois phases, liées au lore de la sur-résonance (`02-Lore.md`, Âge 4) :

1. **Phase Écho** (300-200 PV) : attaques à distance basées sur des échos de Résonance
   déphasée ; le joueur doit interrompre 3 "points d'écho" au sol pour passer en phase 2.
2. **Phase Rupture** (200-80 PV) : le combat se déplace en partie dans le vide de la Faille
   (le sol devient partiellement instable, chute possible sans dégât létal — juste retour au
   bord).
3. **Phase Stabilisation** (80-0 PV) : le boss tente de refermer la Faille ; le joueur doit
   maintenir le combat près du Rift Anchor pour l'empêcher.

Victoire : la Faille devient définitivement stable (les dégâts de déphasage disparaissent même
sans Rift Anchor actif), le Rift Core Extractor devient utilisable pour les 6 extractions
prévues. Drop garanti à la mort : 3 Corrupted Veskorian Alloy Ingot (voir `04-Materials.md`,
Rift-Ward Plate). Pas de répétition du combat sur la même Faille une fois vaincu — cohérent avec
le refus d'un endgame basé sur la répétition infinie.

## Problèmes / Alternatives rejetées

- **Rejeté : agressivité par défaut pour tous les Custodes, y compris hors structure.** Rejeté —
  romprait le pilier 4 (danger toujours motivé) sans raison de lore : les Custodes gardent des
  sites, pas des territoires.
- **Rejeté : Gardien de Faille repopulé après un temps, façon boss de raid répétable.** Rejeté —
  la Faille est une ressource finie assumée (voir `03-Progression.md`, `05-Machines.md`) ; un
  boss répétable romprait cette rareté volontaire.

## Ouvert

- Faut-il une variante affaiblie du Custode Lourd trouvable dans l'Avant-poste (actuellement
  seulement Custode standard) pour marquer une transition de difficulté plus douce vers Sigma
  Laboratory ? Idée notée, non tranchée.
