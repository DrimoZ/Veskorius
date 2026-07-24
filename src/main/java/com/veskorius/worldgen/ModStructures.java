package com.veskorius.worldgen;

import com.mojang.datafixers.util.Pair;
import com.veskorius.Veskorius;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

/**
 * Vraies {@code Structure} vanilla en <b>jigsaw</b> (08-Structures.md, 16 §2), remplaçant
 * les anciennes ruines-{@code feature}. Bénéfice immédiat : <b>/locate fonctionne</b>, les
 * structures sont indexables et le <b>mode Structures du Locator</b> s'allume (tag
 * {@code #veskorius:locatable}).
 *
 * <p>Trois registres datapack, construits par datagen (donc validés par les codecs) :
 * <ol>
 *   <li><b>template_pool</b> — un pool à une seule pièce par structure (la pièce NBT de
 *       {@code ModStructurePieceProvider}). Une seule pièce suffit aujourd'hui ; le
 *       jigsaw permettra d'agrandir en ajoutant des pièces au pool, sans réécrire.</li>
 *   <li><b>structure</b> — la {@code JigsawStructure} : pool de départ, biomes, strate Y,
 *       étape de génération.</li>
 *   <li><b>structure_set</b> — le placement ({@code RandomSpreadStructurePlacement} :
 *       espacement, séparation, sel), <b>data-driven</b> et surchargeable par datapack
 *       (c'est la surface de configuration de fréquence, cohérente avec la doctrine
 *       « data-driven d'abord » de 14 — pas de clé TOML redondante).</li>
 * </ol>
 * Fréquences de départ à VALIDER EN PLAYTEST (07-World-Generation.md).
 */
public final class ModStructures {

    public static final ResourceKey<StructureTemplatePool> MODEST_DWELLING_POOL = poolKey("modest_dwelling");
    public static final ResourceKey<StructureTemplatePool> OUTPOST_POOL = poolKey("outpost");

    public static final ResourceKey<Structure> MODEST_DWELLING = structureKey("modest_dwelling");
    public static final ResourceKey<Structure> OUTPOST = structureKey("outpost");

    public static final ResourceKey<StructureSet> MODEST_DWELLING_SET = setKey("modest_dwelling");
    public static final ResourceKey<StructureSet> OUTPOST_SET = setKey("outpost");

    private ModStructures() {
    }

    public static void bootstrapTemplatePools(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = pools.getOrThrow(Pools.EMPTY);

        context.register(MODEST_DWELLING_POOL, new StructureTemplatePool(empty,
            List.of(Pair.of(StructurePoolElement.single(id("modest_dwelling").toString()), 1)),
            StructureTemplatePool.Projection.RIGID));
        context.register(OUTPOST_POOL, new StructureTemplatePool(empty,
            List.of(Pair.of(StructurePoolElement.single(id("outpost").toString()), 1)),
            StructureTemplatePool.Projection.RIGID));
    }

    public static void bootstrapStructures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        HolderSet<Biome> overworld = biomes.getOrThrow(BiomeTags.IS_OVERWORLD);

        // Habitation Modeste : la plus commune, Y -20 à 0.
        context.register(MODEST_DWELLING, jigsaw(overworld,
            pools.getOrThrow(MODEST_DWELLING_POOL), -20, 0));
        // Avant-poste : plus rare, Y -40 à 0 (porte du T2).
        context.register(OUTPOST, jigsaw(overworld,
            pools.getOrThrow(OUTPOST_POOL), -40, 0));
    }

    public static void bootstrapStructureSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);

        // Espacement/séparation en CHUNKS ; sels distincts pour que les deux structures
        // ne se superposent pas systématiquement. À valider en playtest.
        context.register(MODEST_DWELLING_SET, new StructureSet(
            structures.getOrThrow(MODEST_DWELLING),
            new RandomSpreadStructurePlacement(20, 7, RandomSpreadType.LINEAR, 84_215_631)));
        context.register(OUTPOST_SET, new StructureSet(
            structures.getOrThrow(OUTPOST),
            new RandomSpreadStructurePlacement(32, 9, RandomSpreadType.LINEAR, 74_328_509)));
    }

    /** {@code JigsawStructure} à pièce unique, posée à une profondeur fixe (pas de projection surface). */
    private static JigsawStructure jigsaw(HolderSet<Biome> biomes,
                                          Holder<StructureTemplatePool> startPool, int minY, int maxY) {
        HeightProvider height = UniformHeight.of(
            VerticalAnchor.absolute(minY), VerticalAnchor.absolute(maxY));
        Structure.StructureSettings settings = new Structure.StructureSettings(
            biomes,
            Map.<net.minecraft.world.entity.MobCategory, StructureSpawnOverride>of(),
            GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
            TerrainAdjustment.NONE);
        return new JigsawStructure(
            settings,
            startPool,
            java.util.Optional.empty(), // pas de nom de jigsaw de départ imposé
            1,                          // profondeur : une seule pièce (pas d'expansion)
            height,
            false,                      // pas d'expansion hack
            java.util.Optional.empty(), // profondeur fixe : pas de projection sur la heightmap
            80,                         // distance max depuis le centre (blocs)
            List.of(),                  // pas d'alias de pool
            DimensionPadding.ZERO,
            LiquidSettings.APPLY_WATERLOGGING);
    }

    private static ResourceKey<StructureTemplatePool> poolKey(String path) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, id(path));
    }

    private static ResourceKey<Structure> structureKey(String path) {
        return ResourceKey.create(Registries.STRUCTURE, id(path));
    }

    private static ResourceKey<StructureSet> setKey(String path) {
        return ResourceKey.create(Registries.STRUCTURE_SET, id(path));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }
}
