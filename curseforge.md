# Veskorius

> **Alpha 0.1.0 — NeoForge 1.21.1.** Playable end to end, but expect rough edges and
> world-breaking changes between alphas. Do not start a world you care about.

*(Version française plus bas — [Français](#français).)*

---

## Energy without a single cable

Something was here before you. It ran on **Resonance** — a form of energy that never
travelled through a wire. It filled the air.

Veskorius is a tech-progression mod built on that one idea, followed honestly to its
consequences. A machine is not *connected* to anything. It works if it **stands inside an
active field**, and it stops when it doesn't. There is no cable, no pipe, no conduit, and no
network to lay out — there is a **volume**, and things either sit in it or they don't.

Items still move normally. Hoppers, chests, automation: all of that behaves exactly as you
expect. Only the *energy* is invisible.

## Reading a base you can't see

Cables have one virtue: you can look at a wire and know where the power goes. Take them away
and you need something else, so Veskorius gives you **colour**.

Every emitter broadcasts on a **harmonic band**. Every machine is tuned to one. Run a machine
on the wrong band and it still works — it just costs more and it dirties the field. That dirt
is **dissonance**, it accumulates, and a saturated field starts to misbehave. Left alone long
enough, it discharges.

None of this hides in a menu. You read your base by looking at it:

| What you see | What it means |
|---|---|
| A machine **glows** | It is genuinely running this tick — so it has power |
| A machine sits **dark** with a full input slot | It is **outside** any field |
| A **dome** of coloured particles | The reach of a field, and its band |
| The glow **flickers** between two colours | That machine is **detuned** — running, but paying for it |
| The dome turns **grey** | Dissonance is building up. Maintain it |

## Five tiers, twenty-five machines

You start with no energy to manage at all: dig between Y 0 and −20, find the veined stone that
sheathes a pocket of raw crystal, and stabilise it by hand. Tier 1 is a crafting loop, nothing
more.

Then you find an outpost, wake its console, and it hands you the blueprint for a **Field
Emitter** — and from that point the mod is about volumes, bands, and upkeep. Alloys and relays
in T3. Amplifiers, an automated extraction array, and a network hub in T4. And at T5, the
**Rift**: anchor it, ward it, and extract from it six times. It tells you what it has left.

Twenty-five machines, 58 blocks, 88 items. Five distinct structures to find plus dungeons,
five mobs, an in-game **Codex** that unlocks its chapters as you earn them, and a weather event
that reshapes the surface.

## Built to be modified

- **Every recipe is a datapack recipe.** Nothing about any machine's inputs, outputs, timings,
  or energy cost is hardcoded. Fourteen recipe types, all JSON, all yours to override.
- **~50 config options** across six files — field range, storm frequency, mob stats, augment
  stacking, overheat penalties. Whole systems (harmonics, dissonance discharge, the field HUD)
  can be switched off outright if they aren't the game you want to run.
- **JEI** and **Jade** supported out of the box.
- **English and French**, both complete.

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.172** or newer
- Java 21

## Reporting problems

This is an alpha built and tested by one person. If something is broken, the report that helps
most says what you did, what you expected, and what happened instead — and includes your
`latest.log`.

---

# Français

> **Alpha 0.1.0 — NeoForge 1.21.1.** Jouable de bout en bout, mais c'est une alpha : des
> aspérités, et des changements qui casseront les mondes d'une version à l'autre. Ne lancez pas
> un monde auquel vous tenez.

## De l'énergie sans un seul câble

Quelque chose était là avant vous. Ça tournait à la **Résonance** — une énergie qui n'a jamais
circulé dans un fil. Elle emplissait l'air.

Veskorius est un mod de progression technique bâti sur cette seule idée, suivie honnêtement
jusqu'à ses conséquences. Une machine n'est *branchée* à rien. Elle tourne si elle **se tient
dans un champ actif**, et elle s'arrête sinon. Pas de câble, pas de tuyau, pas de conduit,
aucun réseau à tirer — un **volume**, et des choses qui sont dedans ou qui n'y sont pas.

Les objets, eux, circulent normalement. Entonnoirs, coffres, automatisation : tout se comporte
comme vous l'attendez. Seule l'*énergie* est invisible.

## Lire une base qu'on ne voit pas

Les câbles ont une vertu : on regarde un fil et on sait où va le courant. Enlevez-les, il faut
autre chose — Veskorius vous donne la **couleur**.

Chaque émetteur diffuse sur une **bande harmonique**. Chaque machine est accordée sur une
bande. Faites tourner une machine sur la mauvaise : elle marche quand même, elle coûte
seulement plus cher et elle salit le champ. Cette saleté, c'est la **dissonance** ; elle
s'accumule, et un champ saturé se met à mal se tenir. Laissé assez longtemps, il se décharge.

Rien de tout ça ne se cache dans un menu. Votre base se lit à l'œil :

| Ce que vous voyez | Ce que ça veut dire |
|---|---|
| Une machine **brille** | Elle tourne vraiment ce tick — donc elle est alimentée |
| Une machine **éteinte** avec ses ingrédients | Elle est **hors champ** |
| Une **coupole** de particules colorées | La portée d'un champ, et sa bande |
| Le glow **clignote** entre deux couleurs | La machine est **désaccordée** — elle tourne, elle paie |
| La coupole **grisaille** | La dissonance monte. Il faut entretenir |

## Cinq paliers, vingt-cinq machines

Vous commencez sans la moindre énergie à gérer : creusez entre Y 0 et −20, trouvez la pierre
veinée qui enrobe une poche de cristal brut, stabilisez-le à la main. Le palier 1 est une
boucle d'artisanat, rien d'autre.

Puis vous trouvez un avant-poste, vous réveillez sa console, et elle vous remet le plan du
**Field Emitter** — à partir de là, le mod parle de volumes, de bandes et d'entretien. Alliages
et relais en T3. Amplificateurs, extraction automatisée et hub de réseau en T4. Et au T5, la
**Faille** : l'ancrer, la contenir, en extraire six fois. Elle vous dit ce qu'il lui reste.

Vingt-cinq machines, 58 blocs, 88 objets. Cinq structures à trouver et des donjons, cinq
créatures, un **Codex** en jeu qui débloque ses chapitres à mesure que vous les méritez, et un
événement météo qui remodèle la surface.

## Fait pour être modifié

- **Chaque recette est une recette de datapack.** Rien — entrées, sorties, durées, coût en
  énergie — n'est écrit en dur dans le code. Quatorze types de recette, tous en JSON.
- **~50 options de configuration** réparties en six fichiers : portée des champs, fréquence des
  orages, statistiques des créatures, cumul des augments, pénalités de surchauffe. Des systèmes
  entiers (harmoniques, décharge de dissonance, HUD de champ) se désactivent complètement s'ils
  ne correspondent pas au jeu que vous voulez faire tourner.
- **JEI** et **Jade** pris en charge.
- **Anglais et français**, les deux complets.

## Prérequis

- Minecraft **1.21.1**
- NeoForge **21.1.172** ou plus récent
- Java 21

## Signaler un problème

C'est une alpha écrite et testée par une seule personne. Le rapport le plus utile dit ce que
vous avez fait, ce que vous attendiez, ce qui s'est produit à la place — et joint votre
`latest.log`.
