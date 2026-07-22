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
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
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
    public static final ResourceKey<BiomeModifier> ADD_CRYSTAL_STRIDER =
        ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("add_crystal_strider"));

    // Ruines (tâche 10) : Habitation Modeste + Avant-poste, en feature (voir RuinFeature).
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
        // Feature custom : amas de cristaux + coquille de pierre veinée (le « tell »
        // visuel). La feature *ore* vanilla ne ferait que l'amas, sans coquille.
        context.register(RESONANCE_CRYSTAL_POCKET_CF,
            new ConfiguredFeature<>(ModFeatures.CRYSTAL_POCKET.get(),
                new CrystalPocketConfiguration(CRYSTAL_TRIES, SHELL_THICKNESS, FLUX_CHANCE)));

        // Ruines : Habitation Modeste (sans console, butin quotidien) et Avant-poste
        // (avec console → blueprint T2). Pièces 7×7, hauteur 4 (voir RuinFeature).
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
            CountPlacement.of(POCKET_COUNT),
            InSquarePlacement.spread(),
            HeightRangePlacement.uniform(VerticalAnchor.absolute(MIN_Y), VerticalAnchor.absolute(MAX_Y)),
            BiomeFilter.biome())));

        // Ruines : rares, souterraines. Fréquences de départ à VALIDER EN PLAYTEST
        // (le spawn est une feature, pas une structure vanilla : pas de garantie
        // d'espacement, juste une rareté par chunk).
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

        // Faune neutre des poches : le Fileur de Cristal (09-Entities.md). Ajouté aux
        // spawns CREATURE de tout l'Overworld ; la restriction à la strate Y 0/-40 est
        // dans la règle de placement (ModEntityEvents). Poids/effectif à valider en
        // playtest (le spawn souterrain d'une créature est limité par l'algo vanilla).
        context.register(ADD_CRYSTAL_STRIDER, new BiomeModifiers.AddSpawnsBiomeModifier(
            biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
            List.of(new MobSpawnSettings.SpawnerData(ModEntities.CRYSTAL_STRIDER.get(), 8, 1, 3))));

        // Ruines ajoutées à l'Overworld, au pas des structures souterraines.
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
