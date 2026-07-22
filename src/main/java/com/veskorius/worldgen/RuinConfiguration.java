package com.veskorius.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Configuration of a small Veskorian ruin built by {@link RuinFeature}.
 *
 * Data-driven: a datapack can change the size, enable/disable the console or point
 * to another loot table without recompiling.
 *
 * @param radius     half-width of the footprint (radius -> footprint 2r+1).
 * @param height     interior wall height.
 * @param console    places an {@code attunement_console} inside (Outpost).
 * @param lootTable  loot table of the ruin's chest.
 */
public record RuinConfiguration(int radius, int height, boolean console, ResourceLocation lootTable)
        implements FeatureConfiguration {

    public static final Codec<RuinConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.POSITIVE_INT.fieldOf("radius").forGetter(RuinConfiguration::radius),
        ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(RuinConfiguration::height),
        Codec.BOOL.fieldOf("console").forGetter(RuinConfiguration::console),
        ResourceLocation.CODEC.fieldOf("loot_table").forGetter(RuinConfiguration::lootTable)
    ).apply(instance, RuinConfiguration::new));
}
