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

    /**
     * Plafond matériel du nombre de slots d'augment : chaque machine en réserve toujours
     * autant (les slots au-delà du nombre <i>actif</i> configuré rejettent les objets).
     * Fixe pour que la taille d'inventaire ne dépende jamais de la config — sinon un
     * changement de config désaligne les sauvegardes.
     */
    public static final int MAX_AUGMENT_SLOTS = 4;

    /**
     * Règle de cumul d'un même effet d'augment (05-Machines.md, « slot d'augment → slots
     * d'augment »). Ne mord qu'avec plusieurs slots actifs ET plusieurs augments du même
     * effet ; aujourd'hui, seul le Catalyst Core (vitesse) existe.
     */
    public enum AugmentStacking {
        /** Un même effet ne compte qu'une fois, quel que soit le nombre d'exemplaires. */
        FORBID,
        /** Cumule jusqu'à {@code augmentStackingCap} exemplaires. */
        CAPPED,
        /** Cumule sans plafond (borné de fait par le nombre de slots). */
        FREE
    }

    public static final ModConfigSpec SPEC;

    // --- Augments (05-Machines.md) -------------------------------------------

    public static final ModConfigSpec.IntValue AUGMENT_SPEED_BONUS_PERCENT;
    public static final ModConfigSpec.IntValue AUGMENT_SLOTS;
    public static final ModConfigSpec.EnumValue<AugmentStacking> AUGMENT_STACKING;
    public static final ModConfigSpec.IntValue AUGMENT_STACKING_CAP;

    // --- Surchauffe (05-Machines.md #5, 06-Energy.md) ------------------------

    public static final ModConfigSpec.DoubleValue OVERHEAT_SPEED_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue OVERHEAT_OSC_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue OVERHEAT_INPUT_LOSS_CHANCE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Machine augments (Resonance Catalyst Core and future augments).").push("augment");

        AUGMENT_SPEED_BONUS_PERCENT = b
            .comment("Speed bonus (%) of a Resonance Catalyst Core installed in an augment slot.",
                "The cycle time is divided by (1 + this_percent/100) PER stacked core (see",
                "stacking rules below). Design default: 15.")
            .defineInRange("augmentSpeedBonusPercent", 15, 0, 10_000);

        AUGMENT_SLOTS = b
            .comment("How many augment slots each machine offers (1-" + MAX_AUGMENT_SLOTS + ").",
                "Default 1 = exactly the historical behaviour (a single augment slot).",
                "Slots beyond this are reserved but reject items.")
            .defineInRange("augmentSlots", 1, 1, MAX_AUGMENT_SLOTS);

        AUGMENT_STACKING = b
            .comment("How multiple augments of the SAME effect combine across slots:",
                "FORBID = only one counts; CAPPED = up to augmentStackingCap; FREE = all.",
                "Only matters with augmentSlots > 1. Design default: FREE.")
            .defineEnum("augmentStacking", AugmentStacking.FREE);

        AUGMENT_STACKING_CAP = b
            .comment("Maximum stacked copies of one effect when augmentStacking = CAPPED.",
                "Default: 2.")
            .defineInRange("augmentStackingCap", 2, 1, MAX_AUGMENT_SLOTS);

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

    // --- Accesseurs (lecture à l'exécution uniquement) -----------------------

    public static int augmentSlots() {
        return AUGMENT_SLOTS.getAsInt();
    }

    public static AugmentStacking augmentStacking() {
        return AUGMENT_STACKING.get();
    }

    public static int augmentStackingCap() {
        return AUGMENT_STACKING_CAP.getAsInt();
    }

    /**
     * Nombre effectif d'exemplaires d'un même effet, après application de la règle de
     * cumul. Fonction <b>pure</b> (testable sans config ni monde) : c'est le cœur des
     * « règles de cumul » du dossier.
     */
    public static int effectiveStack(int rawCount, AugmentStacking mode, int cap) {
        if (rawCount <= 0) {
            return 0;
        }
        return switch (mode) {
            case FORBID -> 1;
            case CAPPED -> Math.min(rawCount, Math.max(1, cap));
            case FREE -> rawCount;
        };
    }
}
