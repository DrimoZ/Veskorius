package com.veskorius.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * {@code veskorius-mobs.toml} — gardiens et faune (09-Entities.md ;
 * 14-Configuration.md, découpage par thème).
 *
 * <p>Les stats de Custode s'appliquent aux <b>individus nouvellement apparus</b>
 * (relues à {@code finalizeSpawn}) : changer la config n'altère pas ceux déjà posés.
 */
public final class MobsConfig {

    private MobsConfig() {
    }

    public static final ModConfigSpec SPEC;

    // --- Custode (garde réactif) ---------------------------------------------

    public static final ModConfigSpec.DoubleValue CUSTODE_HEALTH;
    public static final ModConfigSpec.DoubleValue CUSTODE_DAMAGE;
    public static final ModConfigSpec.DoubleValue CUSTODE_DETECTION_RANGE;
    public static final ModConfigSpec.DoubleValue CUSTODE_ALERT_RANGE;
    public static final ModConfigSpec.DoubleValue CUSTODE_LOURD_HEALTH;
    public static final ModConfigSpec.DoubleValue CUSTODE_LOURD_DAMAGE;
    public static final ModConfigSpec.DoubleValue CUSTODE_LOURD_DETECTION_RANGE;

    // --- Faune (Fileur de Cristal) -------------------------------------------

    public static final ModConfigSpec.IntValue STRIDER_MILK_COOLDOWN;
    public static final ModConfigSpec.DoubleValue ROOST_STRIDER_RANGE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Custode: reactive site guard. Stats apply to newly spawned individuals.")
            .push("custode");

        CUSTODE_HEALTH = b
            .comment("Custode health points. Design default: 30.")
            .defineInRange("custodeHealth", 30.0, 1.0, 1024.0);
        CUSTODE_DAMAGE = b
            .comment("Custode attack damage. Design default: 6.",
                "Set to 0.0 for a harmless guard (you lose the site-defense tension).")
            .defineInRange("custodeDamage", 6.0, 0.0, 1024.0);
        CUSTODE_DETECTION_RANGE = b
            .comment("Radius (blocks) within which a Custode targets a player (reactive). Default: 6.")
            .defineInRange("custodeDetectionRange", 6.0, 1.0, 128.0);
        CUSTODE_ALERT_RANGE = b
            .comment("Radius (blocks) within which breaking a Veskorius machine alerts Custodes",
                "(site defense - wider than passive detection). Default: 16.")
            .defineInRange("custodeAlertRange", 16.0, 1.0, 128.0);

        // Le Lourd partage le rayon d'ALERTE du Custode (16) : c'est la meme portee,
        // pour le meme geste — un garde qui en previent un autre. Un second reglage
        // n'aurait fait qu'offrir deux facons de decrire une seule regle.
        CUSTODE_LOURD_HEALTH = b
            .comment("Custode Lourd health points. Design default: 60.")
            .defineInRange("custodeLourdHealth", 60.0, 1.0, 1024.0);
        CUSTODE_LOURD_DAMAGE = b
            .comment("Custode Lourd attack damage. Design default: 9.")
            .defineInRange("custodeLourdDamage", 9.0, 0.0, 1024.0);
        CUSTODE_LOURD_DETECTION_RANGE = b
            .comment("Radius (blocks) within which a Custode Lourd targets a player. Default: 8.")
            .defineInRange("custodeLourdDetectionRange", 8.0, 1.0, 128.0);

        b.pop();

        b.comment("Crystal Strider: neutral fauna, milking and Crystal Roost.").push("fauna");

        STRIDER_MILK_COOLDOWN = b
            .comment("Crystal Strider milking cooldown, in ticks (20 = 1 s). Default: 6000 (5 min).")
            .defineInRange("striderMilkCooldown", 6000, 1, 100_000_000);
        ROOST_STRIDER_RANGE = b
            .comment("Radius (blocks) within which a Crystal Strider activates a Crystal Roost. Default: 6.")
            .defineInRange("roostStriderRange", 6.0, 1.0, 128.0);

        b.pop();

        SPEC = b.build();
    }
}
