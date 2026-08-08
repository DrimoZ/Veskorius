package com.veskorius.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * {@code veskorius-generation.toml} — ce que le monde fait pousser et ce qu'il inflige
 * (14-Configuration.md, découpage par thème).
 *
 * <p><b>La génération elle-même reste datapack-only</b> (features, placement, biome
 * modifiers) : une valeur TOML n'aurait aucun effet dessus. Ce fichier ne porte que les
 * constantes lues <em>à l'exécution</em> — croissance, aléas de strate.
 *
 * <p>Accueillera l'intensité du gaz de Résonance par strate et la rareté du biome
 * {@code resonant_deeps} quand ils seront codés (voir 07 et 16).
 */
public final class GenerationConfig {

    private GenerationConfig() {
    }

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue SPORE_GROWTH_CHANCE;

    // --- Orage de Résonance ---------------------------------------------
    public static final ModConfigSpec.IntValue STORM_DURATION_TICKS;
    public static final ModConfigSpec.IntValue STORM_ROLL_INTERVAL;
    public static final ModConfigSpec.IntValue STORM_ROLL_CHANCE;
    public static final ModConfigSpec.IntValue STORM_SEED_RADIUS;

    // --- Culture (Buisson de Floraison) ----------------------------------
    public static final ModConfigSpec.IntValue BLOOM_GROWTH_CHANCE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Growth and world interactions.").push("world");

        SPORE_GROWTH_CHANCE = b
            .comment("Chance per random tick that Resonance Veined Stone grows a spore, when it has",
                "an exposed face in low light. Higher = faster (about 2 MC days at the default).",
                "Default: 0.05. Set to 0.0 to disable spore growth (Crystal Strider breeding then",
                "depends on creative or another source).")
            .defineInRange("sporeGrowthChance", 0.05, 0.0, 1.0);

        BLOOM_GROWTH_CHANCE = b
            .comment("One random tick in N advances a Resonance Bloom Bush by one stage.",                "Lower = faster. Default: 5, which is roughly a sweet berry bush.")
            .defineInRange("bloomGrowthChance", 5, 1, 1000);

        b.pop();

        // L'ORAGE EST LE SEUL ÉVÉNEMENT ALÉATOIRE DU MOD, donc le seul dont un serveur
        // puisse légitimement vouloir changer la fréquence sans toucher au reste. Ses
        // valeurs étaient en dur alors que les cinq autres thèmes du mod sont réglables :
        // un modpack ne pouvait ni le calmer ni l'intensifier.
        b.comment("Resonance Storm: the only weather event, active from Tier 3.")
            .push("storm");

        STORM_DURATION_TICKS = b
            .comment("How long a storm lasts, in ticks (20 = 1 s). Default: 12000 (10 min).",
                "Everything still on the ground when it ends is removed — a longer storm is a",
                "longer window, never a bigger stockpile.")
            .defineInRange("durationTicks", 12000, 200, 24000);
        STORM_ROLL_INTERVAL = b
            .comment("Ticks between two chances of a storm starting. Default: 24000 (1 MC day).")
            .defineInRange("rollIntervalTicks", 24000, 200, 1000000);
        STORM_ROLL_CHANCE = b
            .comment("One roll in N starts a storm. Default: 6, so about one storm every",
                "five to seven Minecraft days. Set very high to make storms nearly absent.")
            .defineInRange("rollChance", 6, 1, 10000);
        STORM_SEED_RADIUS = b
            .comment("Radius (blocks) around each player where meteoric craters settle.",
                "Default: 48. Larger spreads the hunt out; smaller concentrates it.")
            .defineInRange("seedRadius", 48, 8, 128);

        b.pop();

        SPEC = b.build();
    }
}
