package com.veskorius.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * {@code veskorius-machines.toml} — réglages transversaux des machines actives :
 * augments et surchauffe (14-Configuration.md, découpage par thème).
 *
 * <p>Les <b>durées de cycle et coûts en Osc</b> ne sont volontairement PAS ici :
 * ils vivent dans les recettes JSON (data-driven), jamais en double.
 */
public final class MachinesConfig {

    private MachinesConfig() {
    }

    public static final ModConfigSpec SPEC;

    // --- Augments (05-Machines.md) -------------------------------------------

    public static final ModConfigSpec.IntValue AUGMENT_SPEED_BONUS_PERCENT;

    // --- Surchauffe (05-Machines.md #5, 06-Energy.md) ------------------------

    public static final ModConfigSpec.DoubleValue OVERHEAT_SPEED_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue OVERHEAT_OSC_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue OVERHEAT_INPUT_LOSS_CHANCE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Machine augments (Resonance Catalyst Core and future augments).").push("augment");

        AUGMENT_SPEED_BONUS_PERCENT = b
            .comment("Speed bonus (%) of a Resonance Catalyst Core installed in an augment slot.",
                "The cycle time is divided by (1 + this_percent/100). Design default: 15.")
            .defineInRange("augmentSpeedBonusPercent", 15, 0, 10_000);

        b.pop();

        b.comment("Overheat mode: faster, hungrier, and it can eat the input.").push("overheat");

        OVERHEAT_SPEED_MULTIPLIER = b
            .comment("Overheat: the cycle duration is DIVIDED by this factor. Design default: 2.0.")
            .defineInRange("overheatSpeedMultiplier", 2.0, 1.0, 1000.0);

        OVERHEAT_OSC_MULTIPLIER = b
            .comment("Overheat: the Osc/tick consumption is MULTIPLIED by this factor.",
                "Design default: 2.0.")
            .defineInRange("overheatOscMultiplier", 2.0, 1.0, 1000.0);

        OVERHEAT_INPUT_LOSS_CHANCE = b
            .comment("Overheat: probability (0.0-1.0) that a cycle loses its input without producing",
                "an output (Flux Purifier and future overheating machines). Design default: 0.2.",
                "Set to 0.0 to remove the risk entirely (you lose the speed/safety trade-off).")
            .defineInRange("overheatInputLossChance", 0.2, 0.0, 1.0);

        b.pop();

        SPEC = b.build();
    }
}
