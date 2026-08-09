# Configuration

Config lives in five themed files rather than one wall of options. All of them are **server
config**, generated in `config/` per world (or per server).

| File | Covers |
|---|---|
| `veskorius-basics.toml` | Field range and capacity, portable items |
| `veskorius-machines.toml` | Augments, overheat |
| `veskorius-generation.toml` | Plant growth, the Resonance Storm |
| `veskorius-mobs.toml` | Guardians, fauna |
| `veskorius-harmonics.toml` | Bands, dissonance, discharge, damping, the field HUD |

There is no `veskorius-structures.toml` — structure frequency and placement live in worldgen
JSON, where a datapack can reach them.

Every option carries its own comment in the generated file. The tables below are for browsing.

## veskorius-basics.toml

### `[field]`
| Option | Default | |
|---|---|---|
| `fieldEmitterRange` | 8 | Radius of a Field Emitter's sphere |
| `fieldEmitterCapacity` | 4000 | Osc held in its reserve |

### `[portable]`
| Option | Default | |
|---|---|---|
| `storageCellCapacity` | 8000 | Osc in a Storage Cell |
| `storageCellChargeRate` | 20 | Osc/tick while charging |
| `locatorCapacity` | 100 | Osc in a Locator |
| `locatorCostPerUse` | 5 | Osc per scan |
| `locatorRechargeRate` | 5 | Osc/tick in a field |
| `locatorRange` | 40 | Scan radius |

## veskorius-machines.toml

### `[augment]`
| Option | Default | |
|---|---|---|
| `augmentSpeedBonusPercent` | 15 | Speed gained per Catalyst Core |
| `augmentSlots` | 1 | Augment slots per machine |
| `augmentStacking` | `FREE` | How multiple augments combine |
| `augmentStackingCap` | 2 | Ceiling when stacking is capped |

### `[overheat]`
| Option | Default | |
|---|---|---|
| `overheatSpeedMultiplier` | 2.0 | Speed while overheated |
| `overheatOscMultiplier` | 2.0 | Energy cost while overheated |
| `overheatInputLossChance` | 0.2 | Chance of destroying the input |
| `overheatIgnoresStable` | true | Stable-crystal recipes are exempt from that loss |

## veskorius-generation.toml

### `[world]`
| Option | Default | |
|---|---|---|
| `sporeGrowthChance` | 0.05 | Resonance spore spread |
| `bloomGrowthChance` | 5 | Bloom spread |

### `[storm]`
| Option | Default | |
|---|---|---|
| `durationTicks` | 12000 | 10 minutes. A longer storm is a longer window, never a bigger stockpile |
| `rollIntervalTicks` | 24000 | One MC day between rolls |
| `rollChance` | 6 | **One roll in N** starts a storm — so roughly one every five to seven days. Set very high to make storms nearly absent |
| `seedRadius` | 48 | Blocks around each player where craters settle |

## veskorius-mobs.toml

### `[custode]`
| Option | Default |
|---|---|
| `custodeHealth` | 30.0 |
| `custodeDamage` | 6.0 |
| `custodeDetectionRange` | 6.0 |
| `custodeAlertRange` | 16.0 |
| `custodeLourdHealth` | 60.0 |
| `custodeLourdDamage` | 9.0 |
| `custodeLourdDetectionRange` | 8.0 |

### `[fauna]`
| Option | Default | |
|---|---|---|
| `striderMilkCooldown` | 6000 | Ticks between milkings |
| `roostStriderRange` | 6.0 | How close a Strider must stay to a Crystal Roost |

## veskorius-harmonics.toml

### `[harmonics]`
| Option | Default | |
|---|---|---|
| `enabled` | true | **false removes bands, detuning, dissonance and discharge entirely** |
| `bandCount` | 3 | Number of harmonic bands |
| `detuneOscMultiplier` | 1.5 | Energy cost of running detuned |
| `dissonancePerDetunedTick` | 1 | Dirt added per detuned machine per tick |
| `dissonanceCapacity` | 2000 | Field ceiling |
| `dissonanceUnstableThreshold` | 0.75 | Fraction at which a field becomes unstable |
| `dissonanceDecayPerSecond` | 1 | Self-cleaning rate |

### `[discharge]`
| Option | Default | |
|---|---|---|
| `enabled` | true | false keeps dissonance but removes the blast |
| `radius` | 6 | Blocks |
| `damage` | 6.0 | Half-hearts × 2 |
| `releaseFraction` | 0.5 | Dissonance shed per discharge |
| `cooldownTicks` | 100 | 5 seconds between discharges |

### `[damping]`
| Option | Default | |
|---|---|---|
| `dampingRange` | 16 | Damping Array reach |
| `dampingCycleTicks` | 100 | One purge every 5 seconds |

### `[hud]`
| Option | Default | |
|---|---|---|
| `enabled` | true | The field HUD |
| `updateIntervalTicks` | 10 | Refresh rate |

## Migrating from an older world

A `veskorius-server.toml` left over from before the split is not read. Copy your values into
the themed files.
