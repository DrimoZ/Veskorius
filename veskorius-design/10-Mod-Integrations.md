# 10 — Mod Integrations

## Principe transversal

Toute intégration est un **bonus optionnel**, jamais une dépendance dure (détection via
`ModList`, comportement par défaut = sans le mod tiers, testé en premier). Aucune conversion
d'énergie ou de ressource 1:1 cachée — chaque pont a son propre coût, cohérent avec
`06-Energy.md`.

## Thaumcraft

| Pont | Effet | Condition |
|---|---|---|
| Cristal de Vis dans le Resonance Stabilizer | Temps 30s → 18s | **Mêmes slots d'augment que le Resonance Catalyst Core** (voir `05-Machines.md`) — un Cristal de Vis se consomme en fin de cycle (bonus temporaire), un Catalyst Core reste en place (bonus permanent) ; ils se disputent les mêmes slots, le joueur arbitre entre investir une fois ou consommer au cas par cas. *(Révisé 2026-07-23 : N slots + règles de cumul en config — le Vis entre dans ce cadre commun.)* |
| Nœud de Vis à < 4 blocs d'un Field Emitter | -1 Osc/tick prélevé sur réserve | Détection passive, pas de craft requis |

Justification lore : les Architectes n'ont jamais utilisé la magie Thaumcraft — c'est une
découverte du joueur, pas un savoir restauré (seule exception assumée au pilier 1, voir
`01-Vision-Pillars.md`).

## Create

| Pont | Effet | Condition |
|---|---|---|
| Rotation mécanique (Rotational Power) entraînant un Component Assembler | Réduit le temps de 5s à 3s | Adaptateur dédié `create_rotational_adapter`, pas de rotation directe sur le bloc de base |
| Cristal stable placé dans un Depot puis traité par un Mechanical Press | Produit un Resonance Component à la place de l'Assembler (chaîne alternative, même output) | Recette custom enregistrée via le `RecipeType` de Create, pas une modification du Press lui-même |

Logique : Create gère l'automatisation mécanique mieux que ne le ferait Veskorius en la
réimplémentant (Ecosystem First, voir skill `minecraft-mod-dev`) — Veskorius fournit des points
d'entrée (adaptateur, recette custom), jamais un système de rotation parallèle.

## Applied Energistics 2

| Pont | Effet | Condition |
|---|---|---|
| Export/Import bus AE2 sur un Field Emitter | Permet de piloter le stock de Stable/Refined Crystal comme n'importe quel item AE2 | Aucune capacité énergie AE2 exposée — uniquement le stockage d'objets, jamais l'énergie de Résonance elle-même |
| Terminal AE2 affichant les machines Veskorius comme "processing pattern" (Refined = pattern de craft) | Automatisation de la chaîne T2-T3 depuis un réseau AE2 existant | Nécessite un `IGridNodeListener` dédié, pas une réimplémentation du système de stockage |

Refus explicite : pas de capacité d'énergie AE2 (`IEnergyService`) reliée aux Osc — l'énergie de
Résonance reste un système à part, cohérent avec le refus de conversion 1:1 déjà posé pour le
FE dans `06-Energy.md`.

## Mekanism

| Pont | Effet | Condition |
|---|---|---|
| Machine de conversion dédiée `osc_fe_converter` (T3, coûte un Refined Crystal à construire) | Convertit Osc → FE et FE → Osc à un taux volontairement défavorable (2 Osc pour 1 FE) | Bloc distinct, jamais un flag caché sur les machines existantes |

Le taux défavorable est voulu : cette machine est un pont de dépannage pour connecter un système
Mekanism existant, pas une façon rentable de remplacer le réseau de champ par du câblage FE.

## Curios (ajouté 2026-07-23)

| Pont | Effet | Condition |
|---|---|---|
| Objet de lecture de champ (Locator / « Attunement Lens ») placé dans un **slot Curios** | Active le **HUD de champ** (bande, réserve, dissonance — voir `12`) exactement comme s'il était dans l'inventaire | **Dépendance douce** : détection via `ModList.isLoaded("curios")`. Sans Curios, le HUD s'active simplement en ayant l'objet **dans l'inventaire** — aucune fonctionnalité n'est réservée au mod tiers |

Règle respectée : Curios est un **confort d'ergonomie** (libérer une case d'inventaire), jamais un
prérequis. Le comportement par défaut (sans le mod) reste complet et testé en premier.

> **Codé le 2026-07-23** (`compat/curios/CuriosCompat`). L'objet de lecture est le **Resonance
> Locator**. Le pont passe par **réflexion**, pas par une dépendance de compilation : Veskorius
> n'ajoute aucune dépendance de build (les validations tournent hors ligne), et un changement d'API
> chez Curios ne peut pas casser la compilation du mod. À la première erreur, l'intégration se
> **désactive définitivement** avec un avertissement unique et le HUD retombe sur l'inventaire —
> le chemin par défaut, seul chemin testé en GameTest (`fieldHudOnlyForCarriers`).
>
> Contrepartie assumée : le slot Curios ne peut pas être vérifié en test automatisé (Curios n'est
> pas chargé dans `runGameTestServer`). Il se valide à la main, en `runClient` avec le mod installé.

## JEI / EMI

| Élément | Implémentation |
|---|---|
| Recettes des machines T1-T3 (Stabilizer, Assembler, Purifier, Alloy Forge, Synthesizer) | `IRecipeCategory` / `EmiRecipeCategory` standard, affichage input→output classique |
| Recettes T4-T5 (Deep Synthesis, Rift Core Extractor) | Affichées mais marquées "nécessite déblocage en exploration" — pas de recette visible tant que le fragment correspondant n'a pas été trouvé en jeu, pour ne pas spoiler la découverte (compromis avec le pilier 2 : JEI reste utile sans révéler where-to-find) |

**État du code (2026-07-21)** : une intégration JEI de base est déjà en place (`com.veskorius.compat.jei`),
en avance sur la Phase 5, parce qu'elle sert d'outil de vérification des recettes pendant le
développement. Elle couvre les 4 machines actuelles (Stabilizer, Assembler, Purifier, Whetstone)
avec une catégorie chacune (input→output + temps + Osc). JEI est une dépendance de dev
(`compileOnly` API + `localRuntime`), pas exportée. Restent à faire en Phase 5 : EMI (miroir
d'implémentation), le masquage des recettes T4-T5 non débloquées, et le transfert de recette
(remplissage auto du GUI depuis JEI).

## Problèmes / Alternatives rejetées

- **Rejeté : capacité d'énergie universelle Forge Energy exposée nativement sur toutes les
  machines Veskorius.** Rejeté — banaliserait immédiatement la Résonance ; toute compatibilité
  passe par les machines de conversion dédiées ci-dessus.
- **Rejeté : afficher toutes les recettes T4-T5 sans restriction dans JEI dès le premier
  lancement.** Rejeté partiellement — compromis retenu ci-dessus (visible mais marqué verrouillé)
  plutôt qu'un masquage total, qui frustrerait inutilement les joueurs qui utilisent JEI pour
  comprendre une chaîne de craft déjà partiellement débloquée.

## Ouvert

- Faut-il une intégration Botania (fleurs générant du Flux magique) sur le modèle du pont
  Thaumcraft ? Non traité pour l'instant — pas mentionné dans le brainstorm initial, à ajouter
  seulement si une demande précise apparaît.
