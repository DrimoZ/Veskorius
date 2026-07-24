# Guide du joueur — Veskorius

> Ce guide décrit **ce qui est réellement jouable aujourd'hui** dans le mod (Phase 1 complète
> + les fondations transversales du « bloc A »). Il est écrit du **point de vue du joueur** : ce
> qu'on voit, ce qu'on fabrique, comment on progresse. Les documents `01`–`16` de ce dossier, eux,
> sont la référence *de conception* (pour le développement) — ce guide-ci en est la traduction
> jouable.
>
> Contenu **pas encore en jeu** (Phase 2 et au-delà) : le réseau régional T3 (Relais, Alliages,
> Advanced Assembler, Reclaimer…), le biome profond et son gaz, les Failles T5. Ils ne sont pas
> décrits ici.

## En une phrase

Vous réveillez une technologie oubliée fondée sur la **Résonance** : une énergie qui se propage
**sans le moindre câble**, en **champs** autour d'émetteurs. Tout le sel du mod est là — apprendre à
lire, router et entretenir un réseau qu'on ne peut pas voir avec des fils, mais qu'on lit avec des
**couleurs** et de la **lumière**.

## La règle d'or : pas de câbles

L'énergie (les **Oscillations**, `Osc`) ne circule **jamais** dans un tuyau. Une machine fonctionne
si elle **se tient dans un champ** actif, un point c'est tout. En revanche, les **objets**, eux,
circulent normalement (hoppers, automatisation) — les deux ne se mélangent jamais.

C'est un choix de design fondateur : à la place des câbles, vous avez les **bandes harmoniques** (des
couleurs) pour choisir *ce que vous alimentez*. C'est le cœur du mod, expliqué dans
[03 — Harmoniques & Dissonance](03-harmoniques-et-dissonance.md).

## Sommaire

1. **[Premiers pas (T1)](01-premiers-pas-t1.md)** — trouver le cristal, la première boucle de
   fabrication, sans aucune énergie à gérer.
2. **[Le champ (T2)](02-le-champ-t2.md)** — réveiller un Avant-poste, poser votre premier champ,
   alimenter des machines à distance.
3. **[Harmoniques & Dissonance](03-harmoniques-et-dissonance.md)** — le système-signature :
   les couleurs, l'accord, la dissonance, l'entretien du réseau.
4. **[Référence : machines, objets, outils](04-reference.md)** — la fiche de chaque bloc et objet
   codé, avec ses valeurs.
5. **[Le monde, les créatures, les structures](05-monde-et-creatures.md)** — génération, faune,
   ruines, repérage.

## Parcours express (de zéro au champ)

1. **Creusez** entre Y 0 et −20 : cherchez la **pierre veinée** (`resonance_veined_stone`), la
   coquille bleuâtre qui enrobe une **poche de cristal brut**. Minez le cristal.
2. **Stabilisez-le** : Raw Crystal + Quartz au **Resonance Stabilizer** → **Stable Crystal**. (Le
   Quartz peut être remplacé par du **Raw Flux Deposit**, brossé sur les croûtes des poches.)
3. **Explorez / creusez** jusqu'à une **ruine**. Une **Habitation Modeste** vous donne du lore et un
   indice ; un **Avant-poste** contient une **console** : cliquez-la → elle vous remet le **plan
   (blueprint) T2**.
4. Avec le plan, fabriquez un **Field Emitter**, chargez-le d'un **Stable Crystal** : il émet un
   **champ** de 8 blocs. Posez une machine consommatrice dedans — quand elle **s'allume**, elle est
   alimentée.
5. À partir de là, le mod s'ouvre : purifier des cristaux, stocker de l'énergie portable, localiser
   les ressources, et — quand vous serez prêt — jouer avec les **couleurs harmoniques**.

## Comment « lire » votre base d'un coup d'œil

Le mod mise sur des signaux **visuels**, jamais sur un menu caché :

| Ce que vous voyez | Ce que ça veut dire |
|---|---|
| Une machine **brille** (glow) | Elle tourne *vraiment* ce tick — donc elle est alimentée |
| Une machine **éteinte** alors qu'elle a ses ingrédients | Elle est **hors champ** (pas d'énergie) |
| Une **coupole** de particules colorées | La portée d'un champ actif, et sa **bande** (couleur) |
| Le glow d'une machine **clignote** entre deux couleurs | Elle est **désaccordée** (tourne quand même, mais coûte plus et salit) |
| La coupole **grisaille** | Le champ accumule de la **dissonance** — il faut l'entretenir |

Tout est réglable par le créateur de modpack (voir `14-Configuration.md`), jusqu'à désactiver
entièrement un système.
