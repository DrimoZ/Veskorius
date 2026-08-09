# Troubleshooting

## My machine won't turn on

In order of how often it turns out to be the answer:

1. **It's outside a field.** A dark machine with a full input slot is almost always an unpowered
   one. Equip the **Resonance Locator** to switch on the field HUD and see whether the dome
   actually reaches it.
2. **The emitter is out of fuel.** The Field Emitter holds 4000 Osc and burns through it.
3. **It's switched off, or waiting on redstone.** Open the GUI: three buttons, and one of them
   may be set to "requires a signal" on a block with no signal.
4. **The emitter is on a different band** and you have decided that matters. Note that a
   detuned machine still *runs* — if yours is fully dark, the band is not your problem.

## My machine glows but produces nothing

The glow means it drew power this tick. If nothing comes out, the recipe isn't matching:
check counts (most recipes need 2 of something), and check the output slot isn't full.

## A machine I crafted doesn't exist

If a crafting recipe seems to be missing entirely from JEI and the crafting table, count the
ingredients. **A shapeless recipe with more than 9 ingredients is dropped silently at world
load.** This applies to your own datapack recipes too.

## My field dome went grey

That is **dissonance** accumulating, and it is a countdown. Either retune the machines that are
running detuned, or build a **Damping Array** (range 16). Left alone, an unstable field
discharges for 6 damage in a radius of 6.

If you never want that to happen: `harmonics.discharge.enabled = false`.

## I broke a machine and lost what was inside

You shouldn't have — machine inventories drop on the ground when the block breaks. If you want
to move a machine *with* its contents, **shift-right-click it with the Resonance Tuner**: block
and inventory go straight into your inventory.

## Storms never happen

By design, until Tier 3. At least one **online** player must have earned the Tier 3 advancement
before the game will even roll for one. After that it is one roll per Minecraft day with a
1-in-6 chance.

## Everything I collected during the storm vanished

Also by design. **Anything still on the ground when a storm ends is removed.** The storm is a
ten-minute window, not a deposit. Pick things up as you go.

## The Rift Core Extractor stopped working

Six extractions, and that's the lot. The Rift Core reports how many remain — check it before
assuming something is broken.

## My config changes did nothing

Config is **server config**: it lives in the world folder (or the server folder), not in the
global `config/` of your instance. Editing the wrong copy is the usual cause. Also note that a
leftover `veskorius-server.toml` from an older version is orphaned — its values are not
migrated into the five themed files.

## Reporting a bug

Say what you did, what you expected, what happened instead, and attach `latest.log`. If it
involves worldgen or a structure, coordinates and a seed help enormously.
