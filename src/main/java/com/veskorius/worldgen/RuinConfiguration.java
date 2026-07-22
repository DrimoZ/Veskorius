package com.veskorius.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Configuration d'une petite ruine veskorienne bâtie par {@link RuinFeature}.
 *
 * Data-driven : un datapack peut changer la taille, activer/désactiver la console
 * ou pointer une autre table de butin sans recompiler.
 *
 * @param radius     demi-largeur de l'emprise (rayon → footprint 2r+1).
 * @param height     hauteur intérieure des murs.
 * @param console    place une {@code attunement_console} à l'intérieur (Avant-poste).
 * @param lootTable  table de butin du coffre de la ruine.
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
