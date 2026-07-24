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
    public static final ModConfigSpec.BooleanValue DISCHARGE_ENABLED;
    public static final ModConfigSpec.IntValue DISCHARGE_RADIUS;
    public static final ModConfigSpec.DoubleValue DISCHARGE_DAMAGE;
    public static final ModConfigSpec.DoubleValue DISCHARGE_RELEASE_FRACTION;
    public static final ModConfigSpec.IntValue DISCHARGE_COOLDOWN_TICKS;

    public static final ModConfigSpec.IntValue DAMPING_RANGE;
    public static final ModConfigSpec.IntValue DAMPING_CYCLE_TICKS;

    public static final ModConfigSpec.BooleanValue HUD_ENABLED;
    public static final ModConfigSpec.IntValue HUD_UPDATE_INTERVAL;

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

        b.comment("Resonance discharge: when a field's dissonance reaches its cap, the emitter",
            "releases a brief AoE pulse — the local echo of the Collapse. This is the third",
            "and last stage of neglected dissonance (after: dome desaturates, then field",
            "stutters). It is a consequence you SEE coming, never a silent one.")
            .push("discharge");

        DISCHARGE_ENABLED = b
            .comment("Whether a saturated field discharges at all. false = dissonance simply",
                "stays capped and the field stays unstable, with no pulse.")
            .define("enabled", true);

        DISCHARGE_RADIUS = b
            .comment("Radius (blocks) of the discharge pulse. Default: 6.")
            .defineInRange("radius", 6, 1, 64);

        DISCHARGE_DAMAGE = b
            .comment("Damage dealt to each living entity caught in the pulse (2.0 = 1 heart).",
                "0 disables the damage while keeping the sound/particles. Default: 6.0.")
            .defineInRange("damage", 6.0, 0.0, 1000.0);

        DISCHARGE_RELEASE_FRACTION = b
            .comment("Fraction of the dissonance cap bled off by one discharge — the relief",
                "valve. Low = the field keeps discharging until the cause is fixed; high = one",
                "pulse clears most of it. Default: 0.5.")
            .defineInRange("releaseFraction", 0.5, 0.0, 1.0);

        DISCHARGE_COOLDOWN_TICKS = b
            .comment("Minimum ticks between two discharges from the same emitter, so a field",
                "held at the cap pulses at a readable pace instead of every tick. Default: 100.")
            .defineInRange("cooldownTicks", 100, 1, 100_000);

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

        b.comment("Field HUD: a small overlay showing the field the player stands in.",
            "Only sent to players carrying the reader item (Resonance Locator), in their",
            "inventory or in a Curios slot if that mod is present.")
            .push("hud");

        HUD_ENABLED = b
            .comment("Whether the server pushes field readings to carriers. false = no HUD",
                "and no packets at all.")
            .define("enabled", true);

        HUD_UPDATE_INTERVAL = b
            .comment("Ticks between two readings. Higher = fewer packets, laggier gauge.",
                "Default: 10 (twice a second).")
            .defineInRange("updateIntervalTicks", 10, 1, 200);

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

    public static boolean dischargeEnabled() {
        return DISCHARGE_ENABLED.get();
    }

    public static int dischargeRadius() {
        return DISCHARGE_RADIUS.getAsInt();
    }

    public static double dischargeDamage() {
        return DISCHARGE_DAMAGE.getAsDouble();
    }

    public static double dischargeReleaseFraction() {
        return DISCHARGE_RELEASE_FRACTION.getAsDouble();
    }

    public static int dischargeCooldownTicks() {
        return DISCHARGE_COOLDOWN_TICKS.getAsInt();
    }

    public static int dampingRange() {
        return DAMPING_RANGE.getAsInt();
    }

    public static int dampingCycleTicks() {
        return DAMPING_CYCLE_TICKS.getAsInt();
    }

    public static boolean hudEnabled() {
        return HUD_ENABLED.get();
    }

    public static int hudUpdateInterval() {
        return HUD_UPDATE_INTERVAL.getAsInt();
    }
}
