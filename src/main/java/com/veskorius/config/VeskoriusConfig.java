package com.veskorius.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Gameplay configuration exposed to modpack makers (see
 * {@code veskorius-design/14-Configuration.md} for the full philosophy).
 *
 * <p><b>What lives here and what does not.</b> Veskorius is "data-driven first":
 * everything shaped like <em>content</em> (operating and crafting recipes, tags,
 * loot, world generation) is already overridable via <b>datapack</b>, not this
 * file. This {@link ModConfigSpec} only holds the <em>balance constants</em> that
 * were hard-coded in Java and have no natural JSON representation — ranges,
 * capacities, multipliers.
 *
 * <p><b>SERVER type.</b> These values affect game logic, so they are synced to
 * connected clients (no desync), stored per world
 * ({@code saves/<world>/serverconfig/veskorius-server.toml}) and shippable by a
 * modpack via {@code defaultconfigs/veskorius-server.toml}. Technical corollary:
 * never read these values at class-load time (the config is not loaded yet) — only
 * at runtime (a machine tick, an item use). That is the case everywhere they are
 * wired in.
 */
public final class VeskoriusConfig {

    private VeskoriusConfig() {
    }

    public static final ModConfigSpec SPEC;

    // --- Energy (06-Energy.md) -----------------------------------------------

    public static final ModConfigSpec.IntValue FIELD_EMITTER_RANGE;
    public static final ModConfigSpec.IntValue FIELD_EMITTER_CAPACITY;
    public static final ModConfigSpec.IntValue STORAGE_CELL_CAPACITY;
    public static final ModConfigSpec.IntValue STORAGE_CELL_CHARGE_RATE;

    // --- Machines (05-Machines.md, 06-Energy.md) -----------------------------

    public static final ModConfigSpec.IntValue AUGMENT_SPEED_BONUS_PERCENT;
    public static final ModConfigSpec.DoubleValue OVERHEAT_SPEED_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue OVERHEAT_OSC_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue OVERHEAT_INPUT_LOSS_CHANCE;

    // --- Tools (05-Machines.md, 06-Energy.md) --------------------------------

    public static final ModConfigSpec.IntValue LOCATOR_CAPACITY;
    public static final ModConfigSpec.IntValue LOCATOR_COST_PER_USE;
    public static final ModConfigSpec.IntValue LOCATOR_RECHARGE_RATE;
    public static final ModConfigSpec.IntValue LOCATOR_RANGE;

    // --- Entities (09-Entities.md) -------------------------------------------

    public static final ModConfigSpec.DoubleValue CUSTODE_HEALTH;
    public static final ModConfigSpec.DoubleValue CUSTODE_DAMAGE;
    public static final ModConfigSpec.DoubleValue CUSTODE_DETECTION_RANGE;
    public static final ModConfigSpec.DoubleValue CUSTODE_ALERT_RANGE;
    public static final ModConfigSpec.IntValue STRIDER_MILK_COOLDOWN;
    public static final ModConfigSpec.DoubleValue ROOST_STRIDER_RANGE;

    // --- World interactions (04-Materials.md) --------------------------------

    public static final ModConfigSpec.DoubleValue SPORE_GROWTH_CHANCE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Resonance energy (Osc). See 06-Energy.md.").push("energy");

        FIELD_EMITTER_RANGE = b
            .comment("Field Emitter range, in blocks (radius). Design default: 8.")
            .defineInRange("fieldEmitterRange", 8, 1, 128);

        FIELD_EMITTER_CAPACITY = b
            .comment("Maximum Osc reserve of a Field Emitter.",
                "A fuel's Osc value is data-driven (veskorius:fueling recipes, see 14-Configuration.md):",
                "setting the capacity to a multiple lets an emitter store several fuels ahead of time.",
                "Design default: 4000 (= one Stable Crystal).")
            .defineInRange("fieldEmitterCapacity", 4000, 1, 1_000_000_000);

        STORAGE_CELL_CAPACITY = b
            .comment("Capacity of a Resonance Storage Cell (portable battery). Design default: 8000.")
            .defineInRange("storageCellCapacity", 8000, 1, 1_000_000_000);

        STORAGE_CELL_CHARGE_RATE = b
            .comment("Osc absorbed per tick by a Storage Cell inside a field (20 ticks = 1 s).",
                "Design default: 20 (full charge in ~20 s).")
            .defineInRange("storageCellChargeRate", 20, 1, 1_000_000_000);

        b.pop();

        b.comment("Cross-cutting settings for active machines.").push("machines");

        AUGMENT_SPEED_BONUS_PERCENT = b
            .comment("Speed bonus (%) of a Resonance Catalyst Core installed in the augment slot.",
                "The cycle time is divided by (1 + this_percent/100). Design default: 15.")
            .defineInRange("augmentSpeedBonusPercent", 15, 0, 10_000);

        OVERHEAT_SPEED_MULTIPLIER = b
            .comment("Overheat: the cycle duration is DIVIDED by this factor. Design default: 2.0.")
            .defineInRange("overheatSpeedMultiplier", 2.0, 1.0, 1000.0);

        OVERHEAT_OSC_MULTIPLIER = b
            .comment("Overheat: the Osc/tick consumption is MULTIPLIED by this factor.",
                "Design default: 2.0.")
            .defineInRange("overheatOscMultiplier", 2.0, 1.0, 1000.0);

        OVERHEAT_INPUT_LOSS_CHANCE = b
            .comment("Overheat: probability (0.0-1.0) that a cycle loses its input without producing",
                "an output (Flux Purifier and future overheating machines). Design default: 0.2.")
            .defineInRange("overheatInputLossChance", 0.2, 0.0, 1.0);

        b.pop();

        b.comment("Portable tools (Resonance Locator).").push("tools");

        LOCATOR_CAPACITY = b
            .comment("Internal battery of the Resonance Locator, in Osc. Design default: 100.")
            .defineInRange("locatorCapacity", 100, 1, 1_000_000_000);
        LOCATOR_COST_PER_USE = b
            .comment("Osc consumed per Locator ping. Design default: 5 (~20 pings).")
            .defineInRange("locatorCostPerUse", 5, 1, 1_000_000_000);
        LOCATOR_RECHARGE_RATE = b
            .comment("Osc recharged per tick (from a field or a carried Storage Cell). Default: 5.")
            .defineInRange("locatorRechargeRate", 5, 1, 1_000_000_000);
        LOCATOR_RANGE = b
            .comment("Locator detection range, in blocks (07: \"from 40 blocks\"). Default: 40.")
            .defineInRange("locatorRange", 40, 1, 512);

        b.pop();

        b.comment("Entities (09-Entities.md). Custode stats apply to newly spawned individuals.")
            .push("entities");

        CUSTODE_HEALTH = b
            .comment("Custode health points. Design default: 30.")
            .defineInRange("custodeHealth", 30.0, 1.0, 1024.0);
        CUSTODE_DAMAGE = b
            .comment("Custode attack damage. Design default: 6.")
            .defineInRange("custodeDamage", 6.0, 0.0, 1024.0);
        CUSTODE_DETECTION_RANGE = b
            .comment("Radius (blocks) within which a Custode targets a player (reactive). Default: 6.")
            .defineInRange("custodeDetectionRange", 6.0, 1.0, 128.0);
        CUSTODE_ALERT_RANGE = b
            .comment("Radius (blocks) within which breaking a Veskorius machine alerts Custodes",
                "(site defense - wider than passive detection). Default: 16.")
            .defineInRange("custodeAlertRange", 16.0, 1.0, 128.0);
        STRIDER_MILK_COOLDOWN = b
            .comment("Crystal Strider milking cooldown, in ticks (20 = 1 s). Default: 6000 (5 min).")
            .defineInRange("striderMilkCooldown", 6000, 1, 100_000_000);
        ROOST_STRIDER_RANGE = b
            .comment("Radius (blocks) within which a Crystal Strider activates a Crystal Roost. Default: 6.")
            .defineInRange("roostStriderRange", 6.0, 1.0, 128.0);

        b.pop();

        b.comment("World interactions.").push("world");

        SPORE_GROWTH_CHANCE = b
            .comment("Chance per random tick that Resonance Veined Stone grows a spore, when it has",
                "an exposed face in low light. Higher = faster (about 2 MC days at the default).",
                "Default: 0.05.")
            .defineInRange("sporeGrowthChance", 0.05, 0.0, 1.0);

        b.pop();

        SPEC = b.build();
    }

    // --- Convenience accessors (runtime reads only) --------------------------

    public static int fieldEmitterRange() {
        return FIELD_EMITTER_RANGE.getAsInt();
    }

    public static int fieldEmitterCapacity() {
        return FIELD_EMITTER_CAPACITY.getAsInt();
    }

    public static int storageCellCapacity() {
        return STORAGE_CELL_CAPACITY.getAsInt();
    }

    public static int storageCellChargeRate() {
        return STORAGE_CELL_CHARGE_RATE.getAsInt();
    }

    /** Augment speed multiplier: 1 + bonus%. */
    public static double augmentSpeedMultiplier() {
        return 1.0 + AUGMENT_SPEED_BONUS_PERCENT.getAsInt() / 100.0;
    }

    public static double overheatSpeedMultiplier() {
        return OVERHEAT_SPEED_MULTIPLIER.getAsDouble();
    }

    public static double overheatOscMultiplier() {
        return OVERHEAT_OSC_MULTIPLIER.getAsDouble();
    }

    public static double overheatInputLossChance() {
        return OVERHEAT_INPUT_LOSS_CHANCE.getAsDouble();
    }

    public static int locatorCapacity() {
        return LOCATOR_CAPACITY.getAsInt();
    }

    public static int locatorCostPerUse() {
        return LOCATOR_COST_PER_USE.getAsInt();
    }

    public static int locatorRechargeRate() {
        return LOCATOR_RECHARGE_RATE.getAsInt();
    }

    public static int locatorRange() {
        return LOCATOR_RANGE.getAsInt();
    }

    public static double custodeHealth() {
        return CUSTODE_HEALTH.getAsDouble();
    }

    public static double custodeDamage() {
        return CUSTODE_DAMAGE.getAsDouble();
    }

    public static double custodeDetectionRange() {
        return CUSTODE_DETECTION_RANGE.getAsDouble();
    }

    public static double custodeAlertRange() {
        return CUSTODE_ALERT_RANGE.getAsDouble();
    }

    public static int striderMilkCooldown() {
        return STRIDER_MILK_COOLDOWN.getAsInt();
    }

    public static double roostStriderRange() {
        return ROOST_STRIDER_RANGE.getAsDouble();
    }

    public static double sporeGrowthChance() {
        return SPORE_GROWTH_CHANCE.getAsDouble();
    }
}
