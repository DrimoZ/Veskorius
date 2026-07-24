# Veskorius — Dossier de conception

Douze fichiers, à lire dans l'ordre si besoin de contexte complet, ou à piocher directement selon
le besoin. Chacun est autonome et dense — pas de remplissage, pas de discours marketing. Chaque
affirmation de gameplay est accompagnée d'un chiffre ou d'une règle vérifiable.

1. `01-Vision-Pillars.md` — ce que le mod doit faire ressentir, et les règles de tranchage rapide.
2. `02-Lore.md` — chronologie et société veskorienne (support des décisions de 3 à 10).
3. `03-Progression.md` — arbre complet T1 → T5, tous les déblocages, bootstrap T4 expliqué.
4. `04-Materials.md` — tous les matériaux (bruts, naturels, obtenus par action du joueur, ou
   sous-produits de fabrication), 4 groupes distincts.
5. `05-Machines.md` — 23 blocs + 2 outils/augment transversaux, recettes de construction ET de
   fonctionnement, 8 styles de craft/process différents au-delà du simple input→output.
6. `06-Energy.md` — le système de Résonance, formules et constantes (y compris surchauffe,
   dérive de calibration).
7. `07-World-Generation.md` — biomes, strates Y, règles de génération, densité.
8. `08-Structures.md` — 6 structures détaillées + règles de variation.
9. `09-Entities.md` — mobs, gardiens, 1 mini-boss optionnel, 1 boss final en 3 phases.
10. `10-Mod-Integrations.md` — Thaumcraft, Create, AE2, Mekanism, JEI/EMI.
11. `11-Development-Plan.md` — le plan de développement complet, par phases, à partir d'ici.
12. `12-UX-and-Advancements.md` — conventions d'interface et système d'avancements (déblocage
    de recette par fragment, implémenté avec l'API vanilla plutôt que réinventé).
13. `13-Registry-Index.md` — index consolidé de tous les noms de registre (matériaux, blocs,
    entités, structures), avec statut "codé / à coder" — référence rapide pendant le
    développement, évite les collisions de nom.
14. `14-Configuration.md` — politique de configuration (datapack + config TOML SERVER), constantes
    d'équilibrage exposées aux modpack makers.
15. `15-Codex-Guidebook.md` — le Codex de Résonance (manuel en jeu qui s'écrit tout seul) :
    architecture, déblocage sur le joueur, synchronisation.
16. `16-Revision-and-Expansion.md` — passe de révision (2026-07-23) : Locator à modes/index, vraies
    Structures + configs, biome profond + gaz, système déchets/calibration, expansion de contenu.
    **Prime sur 01-15 là où il les contredit**, répercuté au fil de l'implémentation.

Le fichier 11 est la synthèse de tout ce qui précède : il ne réexplique rien, il transforme les
autres fichiers en tâches ordonnées.

## Guide du joueur (nouveau, 2026-07-24)

Les 16 fichiers ci-dessus sont la référence **de conception** (dev-facing). Pour la traduction
**jouable** de ce qui est réellement en jeu aujourd'hui (Phase 1 + bloc A), voir le sous-dossier
**[`guide-joueur/`](guide-joueur/README.md)** : premiers pas T1, le champ T2, Harmoniques &
Dissonance, la référence des machines/objets, le monde et les créatures — du point de vue du joueur.

## Ce qui a été corrigé lors de la relecture

- Les machines n'avaient que leur recette de fonctionnement, jamais leur recette de
  construction — corrigé dans `05-Machines.md`.
- Le Harmonic Lattice (T4) dépendait d'une ressource qu'une seule machine T4 pouvait produire —
  boucle fermée sans solution. Corrigé par un compte exact de 3 Hyper Refined Crystal fournis
  par l'Archive Régionale (voir `03` et `05`, section Bootstrap).
- Toutes les machines suivaient le même schéma "attends puis produis" — ajout de 6 styles
  différents (surchauffe risque/récompense, sous-produit à maintenir, élevage passif, dérive de
  calibration, multi-bloc capstone, catalyseur permanent) pour casser la répétition.
- Aucun système d'avancement n'existait pour le déblocage de recette par fragment, pourtant
  mentionné partout — ajouté dans le nouveau fichier `12`.
- Les matériaux se limitaient à la chaîne de cristaux et deux alliages — étoffés avec des
  matériaux naturels (pierre veinée, dépôt brossable, graine ancienne, éclat météorique),
  obtenus par action du joueur (drop de combat, élevage du Fileur de Cristal, agriculture), et
  de procédé (branchement de recette selon le métal, sous-produits utiles ou à entretenir,
  augment permanent, compression de ressources). Deux nouvelles machines (Crystal Crusher,
  Flux Compressor) et un événement météo (Orage de Résonance) en découlent.
- Passe de validation complète : un déséquilibre trouvé (Rift-Ward Plate demandait 4× plus de
  Corrupted Alloy Ingot qu'une seule Faille n'en rapporte — réduit à une pièce unique) et un
  chevauchement de mécanique corrigé (le slot Vis de Thaumcraft et le slot d'augment généraient
  deux emplacements différents sur le même bloc — fusionnés en un seul). Le dossier
  `veskorius-mod/` avait aussi un `TECH-SPEC.md` obsolète qui contredisait ce dossier — supprimé,
  remplacé par un renvoi direct ici (une seule source de vérité).
