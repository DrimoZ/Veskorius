package com.veskorius.worldgen;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Génération des poches de Raw Resonance Crystal (07-World-Generation.md, tâche 4).
 *
 * Tout est data-driven : ces bootstraps produisent, par datagen, le
 * {@code ConfiguredFeature} (la poche : quel bloc, quelle taille), le
 * {@code PlacedFeature} (où : nombre par chunk, tranche Y -20 à 0) et le
 * {@code BiomeModifier} NeoForge qui ajoute le tout aux biomes de l'Overworld.
 * Passer par le datagen valide le format contre les codecs — plus fiable que du
 * JSON écrit à la main.
 *
 * Densités de départ (« à valider en playtest », 07-World-Generation.md) : poche
 * de taille 5, 6 tentatives par chunk.
 */
public final class ModWorldGen {

    /** Taille d'une veine de poche (nombre de blocs). À valider en playtest. */
    private static final int POCKET_SIZE = 5;
    /** Tentatives de placement par chunk. À valider en playtest. */
    private static final int POCKET_COUNT = 6;
    private static final int MIN_Y = -20;
    private static final int MAX_Y = 0;

    public static final ResourceKey<ConfiguredFeature<?, ?>> RESONANCE_CRYSTAL_POCKET_CF =
        ResourceKey.create(Registries.CONFIGURED_FEATURE, id("resonance_crystal_pocket"));
    public static final ResourceKey<PlacedFeature> RESONANCE_CRYSTAL_POCKET_PF =
        ResourceKey.create(Registries.PLACED_FEATURE, id("resonance_crystal_pocket"));
    public static final ResourceKey<BiomeModifier> ADD_RESONANCE_CRYSTAL =
        ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("add_resonance_crystal"));

    private ModWorldGen() {
    }

    public static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        // Le même bloc remplace la pierre ET la deepslate : la tranche Y -20 à 0
        // chevauche la transition, il faut couvrir les deux.
        List<OreConfiguration.TargetBlockState> targets = List.of(
            OreConfiguration.target(stone, ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get().defaultBlockState()),
            OreConfiguration.target(deepslate, ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get().defaultBlockState()));

        context.register(RESONANCE_CRYSTAL_POCKET_CF,
            new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(targets, POCKET_SIZE)));
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> pocket = configured.getOrThrow(RESONANCE_CRYSTAL_POCKET_CF);

        context.register(RESONANCE_CRYSTAL_POCKET_PF, new PlacedFeature(pocket, List.of(
            CountPlacement.of(POCKET_COUNT),
            InSquarePlacement.spread(),
            HeightRangePlacement.uniform(VerticalAnchor.absolute(MIN_Y), VerticalAnchor.absolute(MAX_Y)),
            BiomeFilter.biome())));
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);

        context.register(ADD_RESONANCE_CRYSTAL, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
            HolderSet.direct(placed.getOrThrow(RESONANCE_CRYSTAL_POCKET_PF)),
            GenerationStep.Decoration.UNDERGROUND_ORES));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }
}
