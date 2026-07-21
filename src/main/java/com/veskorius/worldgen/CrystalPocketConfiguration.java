package com.veskorius.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Configuration de la feature de poche de cristal. Data-driven (dans le
 * ConfiguredFeature JSON) pour permettre de régler la taille sans recompiler.
 *
 * @param crystalTries épaisseur du « pas » aléatoire posant les cristaux : plus
 *                     grand = poche plus grosse.
 * @param shellThickness rayon de la coquille de Resonance Veined Stone autour des
 *                       cristaux (1 = une couche).
 */
public record CrystalPocketConfiguration(int crystalTries, int shellThickness) implements FeatureConfiguration {

    public static final Codec<CrystalPocketConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.POSITIVE_INT.fieldOf("crystal_tries").forGetter(CrystalPocketConfiguration::crystalTries),
        ExtraCodecs.POSITIVE_INT.fieldOf("shell_thickness").forGetter(CrystalPocketConfiguration::shellThickness)
    ).apply(instance, CrystalPocketConfiguration::new));
}
