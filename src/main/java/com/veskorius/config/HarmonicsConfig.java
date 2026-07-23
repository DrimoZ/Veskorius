package com.veskorius.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * {@code veskorius-harmonics.toml} — le système Harmoniques & Dissonance
 * (06-Energy.md). Livré <b>avec</b> le code qu'il pilote, conformément à la doctrine
 * de 14-Configuration.md.
 *
 * <p><b>Interrupteur maître</b> : {@link #ENABLED} à {@code false} rend le mod
 * strictement identique à avant — aucune bande, aucun désaccord, aucune dissonance.
 * C'est la garantie promise au modpack maker : on peut retirer toute la couche.
 */
public final class HarmonicsConfig {

    private HarmonicsConfig() {
    }

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.IntValue BAND_COUNT;
    public static final ModConfigSpec.DoubleValue DETUNE_OSC_MULTIPLIER;
    public static final ModConfigSpec.IntValue DISSONANCE_PER_DETUNED_TICK;
    public static final ModConfigSpec.IntValue DISSONANCE_CAPACITY;
    public static final ModConfigSpec.DoubleValue DISSONANCE_UNSTABLE_THRESHOLD;
    public static final ModConfigSpec.IntValue DISSONANCE_DECAY_PER_SECOND;
    public static final ModConfigSpec.IntValue DAMPING_RANGE;
    public static final ModConfigSpec.IntValue DAMPING_CYCLE_TICKS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Harmonics & Dissonance (06-Energy.md).",
            "Fields and machines carry a harmonic band, shown as a colour.",
            "Matched = clean. Mismatched = the machine STILL RUNS, but costs more Osc and",
            "feeds dissonance into the emitter. Dissonance never hard-blocks a machine.")
            .push("harmonics");

        ENABLED = b
            .comment("Master switch. false = no bands, no detune, no dissonance at all —",
                "the mod behaves exactly as it did before this system existed.",
                "Turning it off costs you the network-planning layer (routing power by band).")
            .define("enabled", true);

        BAND_COUNT = b
            .comment("How many harmonic bands are selectable (1-3). 1 effectively disables",
                "the choice while keeping the machinery in place. Design default: 3.")
            .defineInRange("bandCount", 3, 1, 3);

        DETUNE_OSC_MULTIPLIER = b
            .comment("Osc cost multiplier for a machine running detuned (band != field band).",
                "1.0 removes the penalty entirely (you keep the visuals, lose the trade-off).",
                "Design default: 1.5.")
            .defineInRange("detuneOscMultiplier", 1.5, 1.0, 100.0);

        DISSONANCE_PER_DETUNED_TICK = b
            .comment("Dissonance added to the serving emitter per tick of detuned operation.",
                "Design default: 1.")
            .defineInRange("dissonancePerDetunedTick", 1, 0, 1_000_000);

        DISSONANCE_CAPACITY = b
            .comment("Maximum dissonance an emitter can hold. Design default: 2000.")
            .defineInRange("dissonanceCapacity", 2000, 1, 1_000_000_000);

        DISSONANCE_UNSTABLE_THRESHOLD = b
            .comment("Fraction of capacity (0.0-1.0) above which the field becomes UNSTABLE:",
                "it starts skipping supply ticks, so machines stutter visibly instead of",
                "silently degrading. Design default: 0.75.")
            .defineInRange("dissonanceUnstableThreshold", 0.75, 0.0, 1.0);

        DISSONANCE_DECAY_PER_SECOND = b
            .comment("Dissonance that bleeds off on its own each second, so a base that stops",
                "misbehaving recovers without infrastructure. Set to 0 to require a Damping",
                "Array. Design default: 1.")
            .defineInRange("dissonanceDecayPerSecond", 1, 0, 1_000_000);

        b.pop();

        b.comment("Damping Array: the infrastructure that absorbs dissonance from a field.")
            .push("damping");

        DAMPING_RANGE = b
            .comment("Radius (blocks) within which a Damping Array cleans emitters. Default: 16.")
            .defineInRange("dampingRange", 16, 1, 128);

        DAMPING_CYCLE_TICKS = b
            .comment("Ticks per damping cycle (one agent consumed, one sludge produced).",
                "Default: 100 (5 s).")
            .defineInRange("dampingCycleTicks", 100, 1, 100_000);

        b.pop();

        SPEC = b.build();
    }

    // --- Accesseurs (lecture à l'exécution uniquement) -----------------------

    public static boolean enabled() {
        return ENABLED.get();
    }

    public static int bandCount() {
        return BAND_COUNT.getAsInt();
    }

    public static double detuneOscMultiplier() {
        return DETUNE_OSC_MULTIPLIER.getAsDouble();
    }

    public static int dissonancePerDetunedTick() {
        return DISSONANCE_PER_DETUNED_TICK.getAsInt();
    }

    public static int dissonanceCapacity() {
        return DISSONANCE_CAPACITY.getAsInt();
    }

    public static double dissonanceUnstableThreshold() {
        return DISSONANCE_UNSTABLE_THRESHOLD.getAsDouble();
    }

    public static int dissonanceDecayPerSecond() {
        return DISSONANCE_DECAY_PER_SECOND.getAsInt();
    }

    public static int dampingRange() {
        return DAMPING_RANGE.getAsInt();
    }

    public static int dampingCycleTicks() {
        return DAMPING_CYCLE_TICKS.getAsInt();
    }
}
