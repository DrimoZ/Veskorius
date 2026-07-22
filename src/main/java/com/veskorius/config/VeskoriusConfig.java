package com.veskorius.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration de gameplay exposée aux modpack makers (voir
 * {@code veskorius-design/14-Configuration.md} pour la philosophie complète).
 *
 * <p><b>Ce qui vit ici et ce qui n'y vit pas.</b> Veskorius est « data-driven
 * d'abord » : tout ce qui a une forme de <em>contenu</em> (recettes de
 * fonctionnement et de craft, tags, loot, génération de monde) est déjà surchargé
 * par <b>datapack</b>, pas par ce fichier. Ce {@link ModConfigSpec} ne porte que
 * les <em>constantes d'équilibrage</em> qui étaient codées en dur en Java et n'ont
 * pas de représentation JSON naturelle — portées, capacités, multiplicateurs.
 *
 * <p><b>Type SERVER.</b> Ces valeurs affectent la logique de jeu : elles sont donc
 * synchronisées vers les clients connectés (pas de désync), stockées par monde
 * ({@code saves/<monde>/serverconfig/veskorius-server.toml}) et livrables par un
 * modpack via {@code defaultconfigs/veskorius-server.toml}. Corollaire technique :
 * ne jamais lire ces valeurs au chargement des classes (config pas encore chargée)
 * — uniquement à l'exécution (tick d'une machine, usage d'un item). C'est le cas
 * partout où elles sont câblées.
 */
public final class VeskoriusConfig {

    private VeskoriusConfig() {
    }

    public static final ModConfigSpec SPEC;

    // --- Énergie (06-Energy.md) ----------------------------------------------

    public static final ModConfigSpec.IntValue FIELD_EMITTER_RANGE;
    public static final ModConfigSpec.IntValue FIELD_EMITTER_CAPACITY;
    public static final ModConfigSpec.IntValue STORAGE_CELL_CAPACITY;
    public static final ModConfigSpec.IntValue STORAGE_CELL_CHARGE_RATE;

    // --- Machines (05-Machines.md, 06-Energy.md) -----------------------------

    public static final ModConfigSpec.IntValue AUGMENT_SPEED_BONUS_PERCENT;
    public static final ModConfigSpec.DoubleValue OVERHEAT_SPEED_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue OVERHEAT_OSC_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue OVERHEAT_INPUT_LOSS_CHANCE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Énergie de Résonance (Osc). Voir 06-Energy.md.").push("energy");

        FIELD_EMITTER_RANGE = b
            .comment("Portée d'un Field Emitter, en blocs (rayon). Défaut design : 8.")
            .defineInRange("fieldEmitterRange", 8, 1, 128);

        FIELD_EMITTER_CAPACITY = b
            .comment("Réserve maximale d'Osc d'un Field Emitter.",
                "La valeur d'un carburant est data-driven (recettes veskorius:fueling, voir",
                "14-Configuration.md) : mettre la capacité à un multiple permet à un émetteur de",
                "stocker plusieurs carburants d'avance. Défaut design : 4000 (= un Stable Crystal).")
            .defineInRange("fieldEmitterCapacity", 4000, 1, 1_000_000_000);

        STORAGE_CELL_CAPACITY = b
            .comment("Capacité d'une Resonance Storage Cell (batterie portable). Défaut design : 8000.")
            .defineInRange("storageCellCapacity", 8000, 1, 1_000_000_000);

        STORAGE_CELL_CHARGE_RATE = b
            .comment("Osc absorbés par tick par une Storage Cell dans un champ (20 ticks = 1 s).",
                "Défaut design : 20 (recharge complète en ~20 s).")
            .defineInRange("storageCellChargeRate", 20, 1, 1_000_000_000);

        b.pop();

        b.comment("Réglages transversaux des machines actives.").push("machines");

        AUGMENT_SPEED_BONUS_PERCENT = b
            .comment("Bonus de vitesse (%) d'un Resonance Catalyst Core installé dans le slot",
                "d'augment. Le cycle est divisé par (1 + ce_pourcentage/100). Défaut design : 15.")
            .defineInRange("augmentSpeedBonusPercent", 15, 0, 10_000);

        OVERHEAT_SPEED_MULTIPLIER = b
            .comment("Surchauffe : la durée de cycle est DIVISÉE par ce facteur. Défaut design : 2.0.")
            .defineInRange("overheatSpeedMultiplier", 2.0, 1.0, 1000.0);

        OVERHEAT_OSC_MULTIPLIER = b
            .comment("Surchauffe : la consommation d'Osc/tick est MULTIPLIÉE par ce facteur.",
                "Défaut design : 2.0.")
            .defineInRange("overheatOscMultiplier", 2.0, 1.0, 1000.0);

        OVERHEAT_INPUT_LOSS_CHANCE = b
            .comment("Surchauffe : probabilité (0.0–1.0) qu'un cycle perde son entrée sans produire",
                "de sortie (Flux Purifier et machines à surchauffe futures). Défaut design : 0.2.")
            .defineInRange("overheatInputLossChance", 0.2, 0.0, 1.0);

        b.pop();

        SPEC = b.build();
    }

    // --- Accès de commodité (lecture runtime uniquement) ---------------------

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

    /** Multiplicateur de vitesse d'un augment : 1 + bonus%. */
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
}
