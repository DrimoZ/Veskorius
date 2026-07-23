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

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Growth and world interactions.").push("world");

        SPORE_GROWTH_CHANCE = b
            .comment("Chance per random tick that Resonance Veined Stone grows a spore, when it has",
                "an exposed face in low light. Higher = faster (about 2 MC days at the default).",
                "Default: 0.05. Set to 0.0 to disable spore growth (Crystal Strider breeding then",
                "depends on creative or another source).")
            .defineInRange("sporeGrowthChance", 0.05, 0.0, 1.0);

        b.pop();

        SPEC = b.build();
    }
}
