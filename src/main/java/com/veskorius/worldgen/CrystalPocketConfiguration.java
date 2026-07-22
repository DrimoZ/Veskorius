package com.veskorius.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Configuration of the crystal-pocket feature. Data-driven (in the ConfiguredFeature
 * JSON) so the size can be tuned without recompiling.
 *
 * @param crystalTries   number of random-walk steps placing crystals: higher = bigger pocket.
 * @param shellThickness radius of the Resonance Veined Stone shell around the crystals (1 = one layer).
 * @param fluxChance     probability that a shell block is a Raw Flux Deposit crust
 *                       (brushable) rather than plain veined stone.
 */
public record CrystalPocketConfiguration(int crystalTries, int shellThickness, float fluxChance)
        implements FeatureConfiguration {

    public static final Codec<CrystalPocketConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.POSITIVE_INT.fieldOf("crystal_tries").forGetter(CrystalPocketConfiguration::crystalTries),
        ExtraCodecs.POSITIVE_INT.fieldOf("shell_thickness").forGetter(CrystalPocketConfiguration::shellThickness),
        Codec.floatRange(0.0f, 1.0f).fieldOf("flux_chance").forGetter(CrystalPocketConfiguration::fluxChance)
    ).apply(instance, CrystalPocketConfiguration::new));
}
