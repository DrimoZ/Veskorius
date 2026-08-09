# Machines

Twenty-five machines across five tiers. Every value below comes from a **datapack recipe**, not
from the code — see [Datapacks and Recipes](Datapacks-and-Recipes) if you want different
numbers.

## Tier 1 — no field required

| Machine | What it does |
|---|---|
| **Resonance Stabilizer** | Raw crystal + quartz → stable crystal, 30 s. Autonomous |
| **Crystal Crusher** | 1 raw crystal → 3 resonance dust, 10 s. Autonomous |
| **Resonance Whetstone** | Repairs a tool by 25%, 8 s. Autonomous. Costs a stable crystal to build |
| **Component Assembler** | Stable crystal + 2 iron → 2 components, 5 s. **3 Osc/tick** — the one T1 machine that needs a field |

## Tier 2 — the field

| Machine | What it does |
|---|---|
| **Field Emitter** | Radius 8, reserve 4000 Osc. Fuel is data-driven |
| **Tunable Field Emitter** | Same emitter, with a chosen harmonic band |
| **Flux Purifier** | Stable + redstone → refined crystal, 45 s (22 s overheated) |
| **Resonance Storage Cell** | Portable battery, 8000 Osc |
| **Resonance Locator** | Mode tool (Resources / Structures). Enables the field HUD |
| **Crystal Roost** | 1 raw crystal per 600 s, if a Crystal Strider is nearby |

## Tier 3 — the network

| Machine | What it does |
|---|---|
| **Resonance Relay** | Range 20, chainable, 1 Osc/tick. A buffer, never a wire |
| **Veskorian Alloy Forge** | 2 refined + 2 ingots → alloy **+ slag**, 20 s |
| **Structural Synthesizer** | 4 ingots + 8 stone → 4 blocks **+ residue**, 60 s |
| **Deep Crystal Driller** | Harvests clusters below Y −40, 6 Osc/tick |
| **Slag Vent** | Empties 1 slag / 10 s from each forge within 8 blocks |
| **Flux Compressor** | 4 refined → 1 concentrated flux, 30 s, 6 Osc/tick |
| **Reclaimer** | 4 slag → 1 gravel; 4 sludge → 1 dust. Closes the loop |
| **Advanced Assembler** | 4 components + 2 conductive ingots → 1 **resonance matrix** |
| **Damping Array** | Purges dissonance from a field, range 16 |

Two machines here exist because of what the others *leave behind*. The Forge and the
Synthesizer produce **byproducts**; the Slag Vent moves them and the Reclaimer turns them back
into something. Waste is a real output with a real outlet, not a rounding error.

The **resonance matrix** from the Advanced Assembler is the gate to Tier 4 — all four T4
machines require one.

## Tier 4 — scale

| Machine | What it does |
|---|---|
| **Harmonic Amplifier** | Widens an existing field. Manufactures nothing |
| **Deep Synthesis Chamber** | Consumes a hyper-refined crystal on construction, permanently |
| **Automated Extraction Array** | Extraction without a player present |
| **Resonance Network Hub** | Network coordination |
| **Convergence Core** | Multiblock: a ring of 8 relays at 5 blocks. Range 40, strength 1000 |

## Tier 5 — the Rift

| Machine | What it does |
|---|---|
| **Rift Anchor** | Anchors the Rift |
| **Rift Ward Emitter** | Contains it |
| **Rift Core Extractor** | Extracts from it — **six times, and no more**. The Core tells you what it has left |

## What every machine shares

Every cycle machine inherits the same chassis:

- a progress bar and a persistent inventory, dropped on the ground when the block breaks
- an **augment slot** (Catalyst Core, +15% speed by default)
- a **manual on/off** switch
- **redstone control** in three modes: ignored / requires a signal / requires no signal
- **overheat** where supported: 2× speed for 2× energy, with a 20% chance of losing the input
- a **harmonic band**
- **configurable faces**: each side set to input, output, or disabled
- a glow on the front that is on **only when the machine actually runs that tick**

None of this uses a custom network packet — the GUI buttons ride the vanilla
`clickMenuButton` channel.

## The Resonance Tuner

The mod's wrench. Right-click a machine to apply the current mode; right-click the air to cycle
modes (Rotate / On-Off / Overheat / Redstone); **shift-right-click any block entity to
dismantle it**, block and contents straight to your inventory.
