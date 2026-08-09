# Installation

## Requirements

| | |
|---|---|
| Minecraft | **1.21.1** |
| Loader | **NeoForge 21.1.172** or newer |
| Java | **21** |

Forge and Fabric are not supported and there is no plan to support them — the mod is built
against NeoForge APIs throughout.

## Installing

1. Install the NeoForge 1.21.1 profile in your launcher.
2. Drop `veskorius-0.1.0.jar` into the `mods/` folder of that profile.
3. Launch. On your first login you receive a **Resonance Codex** — the in-game guidebook.

Nothing else is bundled. The jar contains only Veskorius.

## Optional mods

Neither is required, and nothing is gated behind them.

- **JEI** — every machine recipe shows up with its own category, including the ones with
  byproducts and the ones that repair rather than craft.
- **Jade** — hovering a machine shows what it's doing.

## Alpha caveats

`0.1.0` is a first alpha. Two things follow from that:

- **World compatibility is not guaranteed between alpha versions.** Registry names, recipe
  shapes, and worldgen may change. Treat a Veskorius world as disposable until 1.0.
- **Back up before updating.** If an update breaks a save, the backup is the only recourse.

## Servers

Install the same jar server-side and client-side. Field state, dissonance, and storm state all
live on the server and are saved with the world; client-side rendering (the field HUD, the
Codex) is properly separated and the automated test suite runs headless on a server.

That said: **a dedicated server has not been play-tested at 0.1.0.** It should work. If it
doesn't, that is a bug worth reporting.
