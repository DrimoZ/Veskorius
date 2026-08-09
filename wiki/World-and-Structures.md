# World and Structures

## Where to dig

| Depth | What's there |
|---|---|
| **Y 0 to −20** | **Resonance veined stone** — a bluish veined shell around a pocket of **raw resonance crystal**. Brushing the crusts yields **raw flux deposit** |
| **Below Y −40** | Deeper clusters, harvestable by the **Deep Crystal Driller** |

Veined stone is the crust, not the ore. Break through it.

## Structures

Eight structures generate, and they are not all the same kind of thing.

| Structure | Role |
|---|---|
| **Modest dwelling** | Lore and a hint. The gentlest introduction |
| **Outpost** | The T2 unlock. Its **attunement console** hands you the blueprint; its chests are guaranteed to hold resonance components |
| **Guard post** | Small, defended |
| **Ruin marker** | A surface tell that something is below |
| **Drill shaft** | A vertical way down |
| **Sunken chamber** | Deeper, wetter, less friendly |
| **Regional archive** | A gated interior with its own console and pedestal. Two loot tiers, shallow and deep |
| **Sigma laboratory** | The end of the archaeological thread, and the reason the storms start |

Ruins are built from the mod's own architectural vocabulary — veined stone bricks, columns,
bulkheads, conduit lines, resonance lamps, and **connected resonance glass** that shows no seam
between neighbouring blocks. Everything you find in a ruin, you can eventually build.

## Creatures

| Mob | Behaviour |
|---|---|
| **Crystal Strider** | Passive. Milkable on a cooldown; a **Crystal Roost** produces raw crystal if one stays within 6 blocks |
| **Custode** | 30 HP, 6 damage, notices you at 6 blocks and calls others at 16 |
| **Custode Lourd** | 60 HP, 9 damage, notices you at 8 blocks |
| **Custode Archiviste** | Guards the archive |
| **Rift Guardian** | Tier 5 |

The Custodes were built to **protect**, not to hunt. They defend a place; they do not come
looking for you.

## Plant life

**Resonance spores** grow on veined stone in low light, and **dissonance blooms** and
**resonance bloom bushes** spread on their own — slowly, at rates you can tune
(`world.sporeGrowthChance`, `world.bloomGrowthChance`).

## The Resonance Storm

The only weather event in the mod, and the only random one.

Roughly **every five to seven Minecraft days** the game rolls; if it passes, a storm runs for
**ten minutes** and seeds **meteoric craters** on exposed surface blocks around players.

**Anything you don't collect before it ends is gone.** That is the point — it is a hunt with a
window, not a deposit you can farm. Storm state is saved with the world, so a server restart
mid-storm doesn't leave permanent craters behind.

**Storms only begin after Tier 3.** Literally: at least one online player must have earned the
T3 advancement. A storm on the first night would be an unexplained weather effect dropping an
item you couldn't use for hours.

Duration, frequency, and radius are all config (`storm.*`) — see
[Configuration](Configuration).

## The Rift

Tier 5 ends at the **Rift**. You anchor it, you ward it, and you extract from it **six times**.
Not seven. The Rift Core will tell you how many you have left.
