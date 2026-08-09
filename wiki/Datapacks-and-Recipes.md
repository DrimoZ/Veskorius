# Datapacks and Recipes

**No machine recipe in Veskorius is hardcoded.** Not the inputs, not the outputs, not the
durations, not the energy costs, not the byproducts. Every one of them is a JSON recipe in the
mod's own datapack, and a datapack loaded after it wins.

This is a design rule, not an accident: if a value belongs to a machine's behaviour, it lives
in a recipe file where a modpack author can reach it.

## The fourteen recipe types

| Type | Machine |
|---|---|
| `veskorius:stabilizing` | Resonance Stabilizer |
| `veskorius:crushing` | Crystal Crusher |
| `veskorius:assembling` | Component Assembler |
| `veskorius:advanced_assembling` | Advanced Assembler |
| `veskorius:purifying` | Flux Purifier |
| `veskorius:compressing` | Flux Compressor |
| `veskorius:forging` | Veskorian Alloy Forge |
| `veskorius:synthesizing` | Structural Synthesizer |
| `veskorius:synthesis` | Deep Synthesis Chamber |
| `veskorius:reclaiming` | Reclaimer |
| `veskorius:roosting` | Crystal Roost |
| `veskorius:sharpening` | Resonance Whetstone |
| `veskorius:fueling` | What an emitter will burn |
| `veskorius:damping` | What a Damping Array will consume |

Files go in `data/<your_pack>/recipe/<type>/<name>.json`.

## The standard shape

Most of the list above shares one shape — ingredients in, a result out, over a duration, at a
cost:

```json
{
  "type": "veskorius:forging",
  "ingredients": [
    { "count": 2, "item": "veskorius:refined_resonance_crystal" },
    { "count": 2, "tag": "veskorius:iron_substitutes" }
  ],
  "result": { "count": 1, "id": "veskorius:veskorian_alloy_ingot" },
  "byproduct": { "count": 1, "id": "veskorius:flux_slag" },
  "time": 400,
  "osc_per_tick": 4
}
```

| Field | |
|---|---|
| `ingredients` | Items or tags, each with a `count` |
| `result` | What comes out |
| `byproduct` | Optional. Waste, produced alongside the result |
| `time` | Cycle length in ticks |
| `osc_per_tick` | Energy drawn per tick. **Omit it and the machine is autonomous** |
| `stable` | Optional. Exempts the recipe from overheat input loss |

Autonomy is not a property of the machine — it is the absence of `osc_per_tick` in its
recipes. Give the Stabilizer's recipe a cost and the Stabilizer needs a field.

## The four that differ

**`sharpening`** — repairs rather than crafts:
```json
{
  "type": "veskorius:sharpening",
  "catalyst": { "count": 1, "item": "veskorius:stable_resonance_crystal" },
  "repair_percent": 25,
  "time": 160
}
```

**`fueling`** — what an emitter burns, and for how much:
```json
{
  "type": "veskorius:fueling",
  "ingredient": { "item": "veskorius:stable_resonance_crystal" },
  "osc": 4000
}
```

**`damping`** — what clears dissonance, and how much of it:
```json
{
  "type": "veskorius:damping",
  "ingredient": { "item": "veskorius:concentrated_flux" },
  "dissonance": 2500
}
```

**`roosting`** — the standard shape, but with a very long `time` (12000 ticks) and no
`osc_per_tick`. The Roost consumes 2 quartz and yields a raw crystal every ten minutes,
provided a Crystal Strider stays nearby.

## Useful tags

Recipes ship against tags wherever a substitution makes sense, so you can widen inputs without
rewriting recipes:

- `veskorius:iron_substitutes` — anything that counts as iron
- `veskorius:stabilizer_flux` — quartz, raw flux deposit, and whatever you add

## One trap worth knowing

A **shapeless crafting recipe cannot exceed 9 ingredients.** Over that, Minecraft drops it
silently at world load — no error, no log line, just a machine you can never craft. If you
write a crafting recipe for a new machine and it simply doesn't exist in game, count the
ingredients first.

## Changing structure generation

Structure frequency and placement are worldgen JSON (`structure_set`), overridable by datapack
like any vanilla structure. There is deliberately no config option duplicating them.
