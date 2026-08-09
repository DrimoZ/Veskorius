# Getting Started

Tier 1 has no energy in it at all. You will not manage a single unit of power until you have a
Field Emitter, and you cannot build one until a ruin gives you the blueprint. So the first hour
is exactly what it looks like: dig, craft, explore.

## 1. Find raw crystal

Dig between **Y 0 and −20** and look for **resonance veined stone** — a bluish, veined shell.
It is not the ore; it is the crust *around* a pocket. Break through it and you find **raw
resonance crystal**.

Raw crystal is unstable. Carrying it hurts. That is the entire justification for the next step.

While you are down there, brush the crusts of the pockets: some give **raw flux deposit**,
which substitutes for quartz below.

## 2. Build a Stabilizer

The **Resonance Stabilizer** is crafted from a **fractured chassis** and a raw crystal. It
needs no power — Tier 1 machines all run on their own.

Feed it **raw crystal + quartz** (or raw flux deposit) and 30 seconds later you get a **stable
resonance crystal**. That is the currency of everything that follows.

If you want dust instead of crystal, the **Crystal Crusher** turns 1 raw crystal into 3
**resonance dust** in 10 seconds.

## 3. The rest of Tier 1

- **Component Assembler** — stable crystal + 2 iron → 2 **resonance components** in 5 seconds.
  Everything above T1 eats components. **This is the one T1 machine that needs power** (3
  Osc/tick), which means you can build it now but you cannot run it until step 5. That is
  deliberate, and step 4 is how you get out of it.
- **Resonance Whetstone** — repairs a tool by 25% in 8 seconds. It costs a stable crystal to
  build and needs no field.
- **Resonance Tuner** — the mod's wrench. Right-click a machine to apply the current mode,
  right-click the air to change mode (Rotate / On-Off / Overheat / Redstone), and
  **shift-right-click any block entity to dismantle it** — block and contents go straight into
  your inventory.

## 4. Find an outpost

You cannot craft a Field Emitter yet. You have to be given the blueprint.

Explore, or dig, until you hit a ruin. A **modest dwelling** gives you lore and a hint. An
**outpost** contains a **console**: click it, and it hands you the **T2 blueprint**.

Outposts are also where the progression un-knots itself. The Field Emitter wants **resonance
components**; the Component Assembler that makes them wants a **field**; the field wants an
emitter. The outpost chests are guaranteed to contain components, and that is what breaks the
circle. Take them.

## 5. Your first field

Craft the **Field Emitter** with the blueprint, load it with a stable crystal, and place it. It
now emits a field of **radius 8** with a reserve of **4000 Osc**.

Put a machine inside that radius. When it lights up, it has power. That is the whole
interface — there is no cable to connect and no meter to read.

From here, go to [Resonance Fields](Resonance-Fields) for how the volume actually behaves, and
[Harmonics and Dissonance](Harmonics-and-Dissonance) once you have more than one emitter and
start caring which machine draws from which.

## Reading your base

| What you see | What it means |
|---|---|
| A machine **glows** | It is running this tick — so it has power |
| A machine sits **dark** with ingredients in it | It is **outside** any field |
| A **dome** of coloured particles | A field's reach, and its band |
| The glow **flickers** between two colours | That machine is **detuned** |
| The dome turns **grey** | Dissonance is accumulating |
