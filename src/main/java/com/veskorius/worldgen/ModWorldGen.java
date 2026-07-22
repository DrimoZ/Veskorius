package com.veskorius.worldgen;

import com.veskorius.Veskorius;
import com.veskorius.entity.ModEntities;
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
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * World generation of the mod, built by datagen (07-World-Generation.md).
 *
 * Everything is data-driven: these bootstraps produce, via datagen, the
 * {@code ConfiguredFeature} (the pocket/ruin: which blocks, which size), the
 * {@code PlacedFeature} (where: rarity, Y band) and the NeoForge
 * {@code BiomeModifier} that adds it all to the Overworld biomes. Going through
 * datagen validates the format against the codecs — more reliable than hand-written
 * JSON.
 *
 * Starting densities are marked "validate in playtest" (07-World-Generation.md).
 */
public final class ModWorldGen {

    /** Step count of the random walk placing crystals (cluster size). Validate in playtest. */
    private static final int CRYSTAL_TRIES = 8;
    /** Thickness of the veined-stone shell (1 layer). */
    private static final int SHELL_THICKNESS = 1;
    /** ~15% of shell blocks are a brushable flux crust (04-Materials.md). */
    private static final float FLUX_CHANCE = 0.15f;
    /**
     * Pocket rarity: one attempt on average every {@code POCKET_RARITY} chunks (not
     * several per chunk). Tuned so pockets are a bit rarer than diamond — a pocket
     * is still a large node (several crystals + shell), so rare is not stingy.
     * Validate in playtest; overridable by datapack (PlacedFeature JSON).
     */
    private static final int POCKET_RARITY = 10;
    private static final int MIN_Y = -20;
    private static final int MAX_Y = 0;

    public static final ResourceKey<ConfiguredFeature<?, ?>> RESONANCE_CRYSTAL_POCKET_CF =
        ResourceKey.create(Registries.CONFIGURED_FEATURE, id("resonance_crystal_pocket"));
    public static final ResourceKey<PlacedFeature> RESONANCE_CRYSTAL_POCKET_PF =
        ResourceKey.create(Registries.PLACED_FEATURE, id("resonance_crystal_pocket"));
    public static final ResourceKey<BiomeModifier> ADD_RESONANCE_CRYSTAL =
        ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("add_resonance_crystal"));
    public static final ResourceKey<BiomeModifier> ADD_CRYSTAL_STRIDER =
        ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("add_crystal_strider"));

    // Ruins (task 10): Modest Dwelling + Outpost, as a feature (see RuinFeature).
    public static final ResourceLocation MODEST_DWELLING_LOOT = id("chests/modest_dwelling");
    public static final ResourceLocation OUTPOST_LOOT = id("chests/outpost");

    public static final ResourceKey<ConfiguredFeature<?, ?>> MODEST_DWELLING_CF =
        ResourceKey.create(Registries.CONFIGURED_FEATURE, id("modest_dwelling"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> OUTPOST_CF =
        ResourceKey.create(Registries.CONFIGURED_FEATURE, id("outpost"));
    public static final ResourceKey<PlacedFeature> MODEST_DWELLING_PF =
        ResourceKey.create(Registries.PLACED_FEATURE, id("modest_dwelling"));
    public static final ResourceKey<PlacedFeature> OUTPOST_PF =
        ResourceKey.create(Registries.PLACED_FEATURE, id("outpost"));
    public static final ResourceKey<BiomeModifier> ADD_MODEST_DWELLING =
        ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("add_modest_dwelling"));
    public static final ResourceKey<BiomeModifier> ADD_OUTPOST =
        ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("add_outpost"));

    private ModWorldGen() {
    }

    public static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        // Custom feature: crystal cluster + veined-stone shell (the visual "tell").
        // The vanilla ore feature would only make the cluster, without the shell.
        context.register(RESONANCE_CRYSTAL_POCKET_CF,
            new ConfiguredFeature<>(ModFeatures.CRYSTAL_POCKET.get(),
                new CrystalPocketConfiguration(CRYSTAL_TRIES, SHELL_THICKNESS, FLUX_CHANCE)));

        // Ruins: Modest Dwelling (no console, daily loot) and Outpost (console ->
        // T2 blueprint). 7x7 rooms, height 4 (see RuinFeature).
        context.register(MODEST_DWELLING_CF,
            new ConfiguredFeature<>(ModFeatures.RUIN.get(),
                new RuinConfiguration(3, 4, false, MODEST_DWELLING_LOOT)));
        context.register(OUTPOST_CF,
            new ConfiguredFeature<>(ModFeatures.RUIN.get(),
                new RuinConfiguration(3, 4, true, OUTPOST_LOOT)));
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> pocket = configured.getOrThrow(RESONANCE_CRYSTAL_POCKET_CF);

        context.register(RESONANCE_CRYSTAL_POCKET_PF, new PlacedFeature(pocket, List.of(
            RarityFilter.onAverageOnceEvery(POCKET_RARITY),
            InSquarePlacement.spread(),
            HeightRangePlacement.uniform(VerticalAnchor.absolute(MIN_Y), VerticalAnchor.absolute(MAX_Y)),
            BiomeFilter.biome())));

        // Ruins: rare, underground. Starting frequencies to VALIDATE IN PLAYTEST
        // (this is a feature, not a vanilla structure: no spacing guarantee, just a
        // per-chunk rarity).
        Holder<ConfiguredFeature<?, ?>> dwelling = configured.getOrThrow(MODEST_DWELLING_CF);
        context.register(MODEST_DWELLING_PF, new PlacedFeature(dwelling, List.of(
            RarityFilter.onAverageOnceEvery(40),
            InSquarePlacement.spread(),
            HeightRangePlacement.uniform(VerticalAnchor.absolute(-8), VerticalAnchor.absolute(0)),
            BiomeFilter.biome())));

        Holder<ConfiguredFeature<?, ?>> outpost = configured.getOrThrow(OUTPOST_CF);
        context.register(OUTPOST_PF, new PlacedFeature(outpost, List.of(
            RarityFilter.onAverageOnceEvery(100),
            InSquarePlacement.spread(),
            HeightRangePlacement.uniform(VerticalAnchor.absolute(-30), VerticalAnchor.absolute(-5)),
            BiomeFilter.biome())));
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);

        context.register(ADD_RESONANCE_CRYSTAL, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
            HolderSet.direct(placed.getOrThrow(RESONANCE_CRYSTAL_POCKET_PF)),
            GenerationStep.Decoration.UNDERGROUND_ORES));

        // Neutral fauna of the pockets: the Crystal Strider (09-Entities.md). Added to
        // the CREATURE spawns of the whole Overworld; the Y 0/-40 band restriction is
        // in the spawn placement rule (ModEntityEvents). Weight/count to validate in
        // playtest (underground creature spawning is limited by the vanilla algorithm).
        context.register(ADD_CRYSTAL_STRIDER, new BiomeModifiers.AddSpawnsBiomeModifier(
            biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
            List.of(new MobSpawnSettings.SpawnerData(ModEntities.CRYSTAL_STRIDER.get(), 8, 1, 3))));

        // Ruins added to the Overworld, at the underground-structures decoration step.
        context.register(ADD_MODEST_DWELLING, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
            HolderSet.direct(placed.getOrThrow(MODEST_DWELLING_PF)),
            GenerationStep.Decoration.UNDERGROUND_STRUCTURES));
        context.register(ADD_OUTPOST, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
            HolderSet.direct(placed.getOrThrow(OUTPOST_PF)),
            GenerationStep.Decoration.UNDERGROUND_STRUCTURES));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }
}
