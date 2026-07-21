package com.veskorius.worldgen;

import com.veskorius.Veskorius;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
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

    /** Pas de la marche aléatoire posant les cristaux (taille de l'amas). À valider en playtest. */
    private static final int CRYSTAL_TRIES = 8;
    /** Épaisseur de la coquille de pierre veinée (1 couche). */
    private static final int SHELL_THICKNESS = 1;
    /** ~15 % des blocs de coquille sont une croûte de flux brossable (04-Materials.md). */
    private static final float FLUX_CHANCE = 0.15f;
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
        // Feature custom : amas de cristaux + coquille de pierre veinée (le « tell »
        // visuel). La feature *ore* vanilla ne ferait que l'amas, sans coquille.
        context.register(RESONANCE_CRYSTAL_POCKET_CF,
            new ConfiguredFeature<>(ModFeatures.CRYSTAL_POCKET.get(),
                new CrystalPocketConfiguration(CRYSTAL_TRIES, SHELL_THICKNESS, FLUX_CHANCE)));
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
