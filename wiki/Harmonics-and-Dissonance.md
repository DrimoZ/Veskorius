# Harmonics and Dissonance

Cables have one real virtue: you can look at a wire and know where the power goes. Veskorius
removed the wire, so it owes you a replacement. That replacement is **colour**.

## Bands

Every source broadcasts on a **harmonic band**, and every machine is tuned to one. By default
there are **3 bands** (`harmonics.bandCount`).

A machine running on a band that matches its source is **attuned**. A machine running inside a
field of a different band is **detuned** — and here is the design decision that matters:

> **A detuned machine still works.**

It does not stop, it does not error, it does not need to be fixed before you can play. It costs
**1.5×** the Osc (`detuneOscMultiplier`) and it dirties the field. You will notice because its
glow **flickers between two colours**.

This is deliberate. Bands are an optimisation layer, not a lock. A player who never engages
with them plays a slightly more expensive game; a player who does gets to decide which half of
their base runs during a shortage.

## Dissonance

Every tick a detuned machine runs, it adds **1 dissonance** to the field
(`dissonancePerDetunedTick`). Fields hold **2000** (`dissonanceCapacity`) and shed **1 per
second** on their own (`dissonanceDecayPerSecond`).

So a little detuning cleans itself up. Sustained detuning does not.

Past **75%** capacity (`dissonanceUnstableThreshold`), the field becomes unstable — and you can
see it coming, because **the dome greys out** as dissonance rises. That grey is your only
warning and it is meant to be enough.

## Discharge

An unstable field eventually discharges: **6 damage** in a **radius of 6**, releasing **half**
its accumulated dissonance, with a **5-second cooldown** before it can happen again.

It is survivable. It is also entirely avoidable, and it never happens without the dome having
gone grey first.

If discharge is not the game you want, `harmonics.discharge.enabled = false` turns it off and
leaves the rest of the system intact.

## Maintenance: the Damping Array

The **Damping Array** (T3) purges dissonance from a field: **range 16**, one cycle every **5
seconds**. What it consumes to do so is a datapack recipe (`veskorius:damping`) — the damping
agents are not hardcoded.

Between decay, damping, and simply tuning your machines correctly, a field is never a
maintenance treadmill. It is a thing that misbehaves if you ignore it for a long time.

## Turning the whole system off

`harmonics.enabled = false` removes bands, detuning, dissonance, and discharge in one switch.
Machines then run on any field at base cost. Everything else in the mod — fields, tiers, the
Rift — is unaffected.

See [Configuration](Configuration) for the full list with defaults.
