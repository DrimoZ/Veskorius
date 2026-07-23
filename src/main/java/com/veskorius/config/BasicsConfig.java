package com.veskorius.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * {@code veskorius-basics.toml} — constantes transversales : le champ de Résonance
 * lui-même et les objets portables qui en vivent (14-Configuration.md, découpage par
 * thème).
 *
 * <p>Type SERVER : synchronisé, par monde, livrable via {@code defaultconfigs/}.
 * Ne jamais lire ces valeurs au chargement de classe — uniquement à l'exécution.
 */
public final class BasicsConfig {

    private BasicsConfig() {
    }

    public static final ModConfigSpec SPEC;

    // --- Champ (06-Energy.md) ------------------------------------------------

    public static final ModConfigSpec.IntValue FIELD_EMITTER_RANGE;
    public static final ModConfigSpec.IntValue FIELD_EMITTER_CAPACITY;

    // --- Objets portables (05-Machines.md #6/#7, 06-Energy.md) ---------------

    public static final ModConfigSpec.IntValue STORAGE_CELL_CAPACITY;
    public static final ModConfigSpec.IntValue STORAGE_CELL_CHARGE_RATE;
    public static final ModConfigSpec.IntValue LOCATOR_CAPACITY;
    public static final ModConfigSpec.IntValue LOCATOR_COST_PER_USE;
    public static final ModConfigSpec.IntValue LOCATOR_RECHARGE_RATE;
    public static final ModConfigSpec.IntValue LOCATOR_RANGE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("The Resonance field itself (Osc). See 06-Energy.md.").push("field");

        FIELD_EMITTER_RANGE = b
            .comment("Field Emitter range, in blocks (radius). Design default: 8.")
            .defineInRange("fieldEmitterRange", 8, 1, 128);

        FIELD_EMITTER_CAPACITY = b
            .comment("Maximum Osc reserve of a Field Emitter.",
                "A fuel's Osc value is data-driven (veskorius:fueling recipes, see 14-Configuration.md):",
                "setting the capacity to a multiple lets an emitter store several fuels ahead of time.",
                "Design default: 4000 (= one Stable Crystal).")
            .defineInRange("fieldEmitterCapacity", 4000, 1, 1_000_000_000);

        b.pop();

        b.comment("Portable items powered by the field (Storage Cell, Locator).").push("portable");

        STORAGE_CELL_CAPACITY = b
            .comment("Capacity of a Resonance Storage Cell (portable battery). Design default: 8000.")
            .defineInRange("storageCellCapacity", 8000, 1, 1_000_000_000);

        STORAGE_CELL_CHARGE_RATE = b
            .comment("Osc absorbed per tick by a Storage Cell inside a field (20 ticks = 1 s).",
                "Design default: 20 (full charge in ~20 s).")
            .defineInRange("storageCellChargeRate", 20, 1, 1_000_000_000);

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

        SPEC = b.build();
    }
}
