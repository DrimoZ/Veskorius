package com.veskorius.config;

/**
 * Façade de configuration du mod (voir {@code veskorius-design/14-Configuration.md}).
 *
 * <p><b>Découpage par thème (2026-07-23).</b> Le fichier unique
 * {@code veskorius-server.toml} a été remplacé par <b>plusieurs fichiers thématiques</b>,
 * pour qu'un modpack maker trouve immédiatement ce qu'il cherche et surcharge un thème
 * sans toucher aux autres :
 *
 * <ul>
 *   <li>{@link BasicsConfig} → {@code veskorius-basics.toml} (champ, objets portables)</li>
 *   <li>{@link MachinesConfig} → {@code veskorius-machines.toml} (augments, surchauffe)</li>
 *   <li>{@link GenerationConfig} → {@code veskorius-generation.toml} (croissance, aléas)</li>
 *   <li>{@link MobsConfig} → {@code veskorius-mobs.toml} (gardiens, faune)</li>
 *   <li>{@link HarmonicsConfig} → {@code veskorius-harmonics.toml} (bandes, dissonance,
 *       décharge, damping, HUD — avec interrupteur maître)</li>
 * </ul>
 *
 * <p>Pas de {@code veskorius-structures.toml} : fréquence et placement des structures vivent
 * dans le JSON datapack ({@code structure_set}), déjà surchargeable — un TOML qui les
 * dupliquerait serait une clé qui ne pilote rien, c'est-à-dire un piège (14-Configuration.md).
 *
 * <p><b>Cette classe ne déclare aucune valeur</b> : elle n'expose que les accesseurs de
 * commodité, ce qui a permis de découper les fichiers <em>sans toucher un seul
 * appelant</em>. Règle inchangée : <b>lire à l'exécution</b>, jamais au chargement de
 * classe (la config SERVER n'est pas encore chargée à ce moment-là).
 *
 * <p><b>Migration :</b> un {@code veskorius-server.toml} d'un monde antérieur devient
 * orphelin — ses réglages personnalisés ne sont pas repris automatiquement. Sans
 * conséquence tant que le mod n'est pas publié ; à mentionner au changelog le jour venu.
 */
public final class VeskoriusConfig {

    private VeskoriusConfig() {
    }

    // --- Champ et objets portables (BasicsConfig) ----------------------------

    public static int fieldEmitterRange() {
        return BasicsConfig.FIELD_EMITTER_RANGE.getAsInt();
    }

    public static int fieldEmitterCapacity() {
        return BasicsConfig.FIELD_EMITTER_CAPACITY.getAsInt();
    }

    public static int storageCellCapacity() {
        return BasicsConfig.STORAGE_CELL_CAPACITY.getAsInt();
    }

    public static int storageCellChargeRate() {
        return BasicsConfig.STORAGE_CELL_CHARGE_RATE.getAsInt();
    }

    public static int locatorCapacity() {
        return BasicsConfig.LOCATOR_CAPACITY.getAsInt();
    }

    public static int locatorCostPerUse() {
        return BasicsConfig.LOCATOR_COST_PER_USE.getAsInt();
    }

    public static int locatorRechargeRate() {
        return BasicsConfig.LOCATOR_RECHARGE_RATE.getAsInt();
    }

    public static int locatorRange() {
        return BasicsConfig.LOCATOR_RANGE.getAsInt();
    }

    // --- Machines (MachinesConfig) -------------------------------------------

    /** Multiplicateur de vitesse d'un augment : 1 + bonus%. */
    public static double augmentSpeedMultiplier() {
        return 1.0 + MachinesConfig.AUGMENT_SPEED_BONUS_PERCENT.getAsInt() / 100.0;
    }

    public static double overheatSpeedMultiplier() {
        return MachinesConfig.OVERHEAT_SPEED_MULTIPLIER.getAsDouble();
    }

    public static double overheatOscMultiplier() {
        return MachinesConfig.OVERHEAT_OSC_MULTIPLIER.getAsDouble();
    }

    public static double overheatInputLossChance() {
        return MachinesConfig.OVERHEAT_INPUT_LOSS_CHANCE.getAsDouble();
    }

    // --- Mobs (MobsConfig) ---------------------------------------------------

    public static double custodeHealth() {
        return MobsConfig.CUSTODE_HEALTH.getAsDouble();
    }

    public static double custodeDamage() {
        return MobsConfig.CUSTODE_DAMAGE.getAsDouble();
    }

    public static double custodeDetectionRange() {
        return MobsConfig.CUSTODE_DETECTION_RANGE.getAsDouble();
    }

    public static double custodeAlertRange() {
        return MobsConfig.CUSTODE_ALERT_RANGE.getAsDouble();
    }

    public static int striderMilkCooldown() {
        return MobsConfig.STRIDER_MILK_COOLDOWN.getAsInt();
    }

    public static double roostStriderRange() {
        return MobsConfig.ROOST_STRIDER_RANGE.getAsDouble();
    }

    // --- Génération (GenerationConfig) ---------------------------------------

    public static double sporeGrowthChance() {
        return GenerationConfig.SPORE_GROWTH_CHANCE.getAsDouble();
    }
}
