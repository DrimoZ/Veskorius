# Resonance Fields

Energy in Veskorius is called **Osc** (oscillations) and it never moves through anything. An
emitter fills a sphere around itself; a machine inside that sphere works, and a machine outside
it doesn't. There is nothing to connect and nothing to route.

Mechanically, a machine does not *hold* energy and does not *pull* from a neighbour. Each tick
it asks a single question — *is there energy at my position?* — of a per-dimension index of
active sources. That is the whole model, and everything below is a consequence of it.

## Emitters

The **Field Emitter** is the first source you get. Default: **radius 8**, reserve **4000 Osc**.

It burns fuel to fill that reserve, and what counts as fuel is a datapack recipe
(`veskorius:fueling`). A stable resonance crystal is worth 4000 Osc out of the box; a modpack
can add anything else.

The **Tunable Field Emitter** is the same block with one addition: you choose its
[harmonic band](Harmonics-and-Dissonance).

## Overlapping fields

Two consequences catch people out:

**Stacking does nothing.** Two emitters covering the same block do not add up. A machine in
both fields is powered exactly once, at the cost of one machine. Building a tower of emitters
over your base buys you nothing but fuel consumption.

**The strongest source wins.** When several sources cover the same position, the one with the
highest *strength* is the one that answers. Strength is a fixed property of the source type:

| Source | Strength |
|---|---|
| Field Emitter | 100 |
| Resonance Relay | 100 |
| Harmonic Amplifier | 100 |
| Convergence Core | 1000 |

For most of the game every source is strength 100 and the rule is invisible. It becomes visible
exactly once — when you build a **Convergence Core**, whose field takes over everything it
overlaps, band included.

## Extending reach

- **Resonance Relay** (T3) — range 20, costs 1 Osc/tick, and **chainable**. A relay is a buffer
  and a re-emitter, never a wire: it does not "carry" power from A to B, it becomes its own
  source in its own sphere.
- **Harmonic Amplifier** (T4) — widens an existing field. It manufactures nothing; its only job
  is reach.
- **Convergence Core** (T4→T5) — a multiblock: a ring of **8 relays at 5 blocks**, giving range
  **40** at strength **1000**.

## Portable and handheld

- **Resonance Storage Cell** (T2) — 8000 Osc, portable, charges at 20 Osc/tick.
- **Resonance Locator** (T2) — a mode tool (Resources / Structures), 100 Osc capacity, 5 Osc
  per use, range 40. It also switches on the **field HUD**, which is how you see field domes at
  all.

## What has a field, and what has a cost

Not every machine draws. Tier 1 is mostly autonomous — the Stabilizer, the Whetstone, and the
Crystal Crusher all run with no field whatsoever. The **Component Assembler** is the exception
at 3 Osc/tick, and that exception is the whole shape of early progression: see
[Getting Started](Getting-Started).

Every per-tick cost lives in the recipe JSON as `osc_per_tick`, not in the code. Changing what
a machine costs is a datapack edit — see [Datapacks and Recipes](Datapacks-and-Recipes).
