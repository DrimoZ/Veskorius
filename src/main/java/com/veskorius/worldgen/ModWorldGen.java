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
     * Une poche sur trois abrite des Fileurs de Cristal.
     *
     * <p>C'est ce qui rend l'espèce réellement présente : elle est déclarée en
     * {@code MobCategory.CREATURE} avec un spawn borné à Y ≤ 0, or la génération de monde
     * ne place les CREATURE qu'en SURFACE et le spawn à l'exécution est plafonné à 10
     * individus persistants — saturés en permanence par la faune de surface. Le Fileur
     * n'apparaissait donc jamais, et le Crystal Roost, qui en exige un à moins de
     * 6 blocs, était du contenu inatteignable. Détail dans
     * {@link ResonanceCrystalPocketFeature#seedStriders}.
     *
     * <p>Une poche sur trois plutôt que toutes : les rencontrer doit rester une trouvaille,
     * et un joueur qui veut un cheptel passe par la reproduction (au {@code resonance_spore}),
     * pas par le ratissage de poches.
     */
    private static final float STRIDER_CHANCE = 0.34f;
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

    // Ruins (Modest Dwelling + Outpost) are now REAL jigsaw structures (see ModStructures,
    // A7 / 16 §2), no longer features. Only their loot-table ids live here, shared by the
    // chest loot provider and the baked structure pieces.
    public static final ResourceLocation MODEST_DWELLING_LOOT = id("chests/modest_dwelling");
    public static final ResourceLocation OUTPOST_LOOT = id("chests/outpost");

    private ModWorldGen() {
    }

    public static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        // Custom feature: crystal cluster + veined-stone shell (the visual "tell").
        // The vanilla ore feature would only make the cluster, without the shell.
        context.register(RESONANCE_CRYSTAL_POCKET_CF,
            new ConfiguredFeature<>(ModFeatures.CRYSTAL_POCKET.get(),
                new CrystalPocketConfiguration(CRYSTAL_TRIES, SHELL_THICKNESS, FLUX_CHANCE, STRIDER_CHANCE)));
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> pocket = configured.getOrThrow(RESONANCE_CRYSTAL_POCKET_CF);

        context.register(RESONANCE_CRYSTAL_POCKET_PF, new PlacedFeature(pocket, List.of(
            RarityFilter.onAverageOnceEvery(POCKET_RARITY),
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

        // Fauna of the pockets: the Crystal Strider (09-Entities.md).
        //
        // This spawn entry is kept, but it is NOT what actually populates the world —
        // measured against the vanilla spawner, it can't be: world generation places
        // CREATURE mobs at the SURFACE (getTopNonCollidingPos), which the Y ≤ 0 rule
        // rejects; and runtime CREATURE spawning is gated on `gameTime % 400 == 0` and
        // capped at 10 PERSISTENT individuals — a cap surface animals hold permanently.
        // The Striders are therefore seeded with the crystal pockets themselves (see
        // ResonanceCrystalPocketFeature#seedStriders). This entry only remains as the
        // slow trickle it always was, and so that a datapack can retune it.
        context.register(ADD_CRYSTAL_STRIDER, new BiomeModifiers.AddSpawnsBiomeModifier(
            biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
            List.of(new MobSpawnSettings.SpawnerData(ModEntities.CRYSTAL_STRIDER.get(), 8, 1, 3))));

        // Ruins are no longer features: as real Structures they reference their biomes
        // directly (see ModStructures), so there is no biome modifier to add for them.
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }
}
