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

## Resonance Tuner — interactions complètes

Voir `05-Machines.md` pour le craft. Table des actions selon la machine ciblée :

| Machine ciblée | Clic droit avec Tuner |
|---|---|
| Toute machine avec orientation (Assembler, Purifier, Forge, Synthesizer...) | Fait pivoter la face avant de 90° |
| Flux Purifier, Deep Synthesis Chamber | Bascule mode surchauffe on/off |
| Resonance Network Hub | Ouvre l'écran de priorité (glisser-déposer les machines du champ) |
| Harmonic Amplifier, Resonance Network Hub | Si dérive > 0%, recalibre à 100% (consomme 1 Resonance Component) |
| Toute autre machine | Aucun effet (pas de message d'erreur intrusif, juste rien ne se passe) |

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
