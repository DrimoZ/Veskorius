package com.veskorius.worldgen;

import com.mojang.datafixers.util.Pair;
import com.veskorius.Veskorius;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

/**
 * Structures jigsaw du mod (08-Structures.md pour le contenu, 17-Dungeons.md pour la
 * forme et l'assemblage).
 *
 * <p>Trois registres datapack, construits par datagen (donc validés par les codecs) :
 * <ol>
 *   <li><b>template_pool</b> — les pools de pièces, en <b>quatre couches</b>
 *       (17-Dungeons.md §2.2) : la pièce de départ, les pièces facultatives, et le pool
 *       de <i>bouchons</i> déclaré en {@code fallback} de tous les autres ;</li>
 *   <li><b>structure</b> — la {@code JigsawStructure} : pool de départ, biomes, strate Y,
 *       profondeur d'assemblage ;</li>
 *   <li><b>structure_set</b> — le placement, <b>data-driven</b> et surchargeable par
 *       datapack (c'est la surface de configuration de fréquence, cohérente avec la
 *       doctrine « data-driven d'abord » de 14 — pas de clé TOML redondante).</li>
 * </ol>
 *
 * <p><b>Ce que la révision du 2026-08-07 a corrigé ici</b> (17-Dungeons.md §0, §2.5) :
 * <ul>
 *   <li><b>la profondeur valait 1</b> — le jigsaw n'assemblait donc rien, et ajouter une
 *       pièce à un pool <i>remplaçait</i> le bâtiment au lieu de l'agrandir. La promesse
 *       « agrandir = ajouter une pièce » était fausse ; elle est vraie maintenant ;</li>
 *   <li><b>aucun processor</b> — toutes les ruines du monde étaient identiques au bloc
 *       près ;</li>
 *   <li><b>aucun {@code StructureSpawnOverride}</b> — les zombies vanilla peuplaient nos
 *       ruines, ce qui contredit frontalement « les gardiens sont réactifs, jamais
 *       agressifs » (09-Entities.md) ;</li>
 *   <li><b>{@code APPLY_WATERLOGGING}</b> — un intérieur scellé se remplissait d'eau dès
 *       qu'un aquifère passait à côté.</li>
 * </ul>
 */
public final class ModStructures {

    // --- Pools ---------------------------------------------------------------
    public static final ResourceKey<StructureTemplatePool> HAMLET_POOL = poolKey("hamlet");
    public static final ResourceKey<StructureTemplatePool> HAMLET_HOUSE_POOL = poolKey("hamlet/house");
    public static final ResourceKey<StructureTemplatePool> HAMLET_CAP_POOL = poolKey("hamlet/cap");

    public static final ResourceKey<StructureTemplatePool> OUTPOST_POOL = poolKey("outpost");
    public static final ResourceKey<StructureTemplatePool> OUTPOST_WING_POOL = poolKey("outpost/wing");
    public static final ResourceKey<StructureTemplatePool> OUTPOST_CAP_POOL = poolKey("outpost/cap");

    /**
     * Clé historique du Hameau. Le fichier de conception l'appelle « Habitation Modeste »
     * et le monde l'appelle {@code veskorius:modest_dwelling} : le nom de registre est
     * <b>conservé tel quel</b> alors que la structure est devenue un hameau, parce que le
     * renommer casserait les mondes existants, les datapacks qui la surchargent, le tag
     * {@code #veskorius:locatable} et les advancements de découverte — pour un gain
     * purement cosmétique.
     */
    public static final ResourceKey<Structure> MODEST_DWELLING = structureKey("modest_dwelling");
    public static final ResourceKey<Structure> OUTPOST = structureKey("outpost");

    public static final ResourceKey<StructureSet> MODEST_DWELLING_SET = setKey("modest_dwelling");
    public static final ResourceKey<StructureSet> OUTPOST_SET = setKey("outpost");

    /**
     * Profondeur d'assemblage. 5 laisse au Hameau ses quatre branches de trois logis, et
     * à l'Avant-poste ses deux ailes chaînables — au-delà, on n'agrandit plus, on éparpille.
     */
    private static final int JIGSAW_DEPTH = 5;

    /**
     * Rayon maximal depuis le centre, en blocs. 80 (l'ancienne valeur) tronquait déjà un
     * Avant-poste à trois paliers ; 116 est la valeur des grandes structures vanilla.
     */
    private static final int MAX_DISTANCE = 116;

    private ModStructures() {
    }

    public static void bootstrapTemplatePools(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        HolderGetter<StructureProcessorList> processors = context.lookup(Registries.PROCESSOR_LIST);
        Holder<StructureTemplatePool> empty = pools.getOrThrow(Pools.EMPTY);
        Holder<StructureProcessorList> worn = processors.getOrThrow(ModProcessorLists.WORN);
        Holder<StructureProcessorList> ruined = processors.getOrThrow(ModProcessorLists.RUINED);

        // Bouchons : le fallback de TOUS les pools ci-dessous. Sans eux, une branche que
        // le jigsaw ne peut plus prolonger reste ouverte sur la roche — ce qui arrive
        // systématiquement au dernier niveau de profondeur.
        context.register(OUTPOST_CAP_POOL, rigid(empty, List.of(
            Pair.of(StructurePoolElement.single(id("outpost_cap").toString(), worn), 1))));
        context.register(HAMLET_CAP_POOL, rigid(empty, List.of(
            Pair.of(StructurePoolElement.single(id("hamlet_cap").toString(), worn), 1))));

        // --- Avant-poste ---------------------------------------------------
        // Pièce de départ UNIQUE et non usée : elle porte le chemin critique (console,
        // sas, émetteur ancien, coffres garantis). Ni le tirage ni l'usure ne doivent
        // pouvoir s'en approcher (17-Dungeons.md §3).
        context.register(OUTPOST_POOL, rigid(empty, List.of(
            Pair.of(StructurePoolElement.single(id("outpost").toString()), 1))));
        // Ailes : facultatives, donc ruinées sans risque. La galerie effondrée est plus
        // fréquente que les deux autres — un avant-poste majoritairement intact ne
        // ressemble pas à une ruine.
        context.register(OUTPOST_WING_POOL, rigid(pools.getOrThrow(OUTPOST_CAP_POOL), List.of(
            Pair.of(StructurePoolElement.single(id("outpost_wing_store").toString(), worn), 2),
            Pair.of(StructurePoolElement.single(id("outpost_wing_quarters").toString(), worn), 2),
            Pair.of(StructurePoolElement.single(id("outpost_wing_collapsed").toString(), ruined), 3))));

        // --- Hameau ---------------------------------------------------------
        context.register(HAMLET_POOL, rigid(empty, List.of(
            Pair.of(StructurePoolElement.single(id("hamlet").toString(), worn), 1))));
        context.register(HAMLET_HOUSE_POOL, rigid(pools.getOrThrow(HAMLET_CAP_POOL), List.of(
            Pair.of(StructurePoolElement.single(id("hamlet_dwelling").toString(), worn), 3),
            Pair.of(StructurePoolElement.single(id("hamlet_workshop").toString(), worn), 2),
            Pair.of(StructurePoolElement.single(id("hamlet_collapsed").toString(), ruined), 2))));
    }

    public static void bootstrapStructures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        HolderSet<Biome> overworld = biomes.getOrThrow(BiomeTags.IS_OVERWORLD);

        // Hameau : la plus commune. Sa pièce de départ fait 6 de haut, donc la fourchette
        // Y porte sur son PLANCHER : -20 à -6 le laisse entièrement entre -20 et 0, la
        // strate que 07-World-Generation.md lui assigne.
        context.register(MODEST_DWELLING, jigsaw(overworld,
            pools.getOrThrow(HAMLET_POOL), -20, -6));
        // Avant-poste : la pièce de départ fait 20 de haut. -40 à -22 la maintient donc
        // exactement dans la strate « 0 à -40 » de 08-Structures.md, toit compris — une
        // fourchette portant sur le plancher sans tenir compte de la hauteur ferait
        // dépasser le bâtiment à l'air libre.
        context.register(OUTPOST, jigsaw(overworld,
            pools.getOrThrow(OUTPOST_POOL), -40, -22));
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

    private static StructureTemplatePool rigid(Holder<StructureTemplatePool> fallback,
                                               List<Pair<java.util.function.Function<
                                                   StructureTemplatePool.Projection,
                                                   ? extends StructurePoolElement>, Integer>> elements) {
        return new StructureTemplatePool(fallback, elements, StructureTemplatePool.Projection.RIGID);
    }

    /** {@code JigsawStructure} posée à une profondeur fixe (pas de projection surface). */
    private static JigsawStructure jigsaw(HolderSet<Biome> biomes,
                                          Holder<StructureTemplatePool> startPool, int minY, int maxY) {
        HeightProvider height = UniformHeight.of(
            VerticalAnchor.absolute(minY), VerticalAnchor.absolute(maxY));
        Structure.StructureSettings settings = new Structure.StructureSettings(
            biomes,
            // Aucun spawn de monstre à l'intérieur : une liste VIDE portant sur
            // l'emprise des PIÈCES. Sans elle, les zombies et squelettes vanilla
            // apparaissent dans le noir de nos ruines et contredisent la seule règle
            // transversale de 09-Entities.md — un gardien veskorien est réactif, il ne
            // surgit pas d'un coin sombre. Les Custodes, eux, sont posés dans les pièces
            // et persistants : ils ne dépendent pas de cette table.
            Map.of(MobCategory.MONSTER,
                new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE,
                    WeightedRandomList.<MobSpawnSettings.SpawnerData>create())),
            GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
            TerrainAdjustment.NONE);
        return new JigsawStructure(
            settings,
            startPool,
            Optional.empty(),           // pas de nom de jigsaw de départ imposé
            JIGSAW_DEPTH,
            height,
            false,                      // pas d'expansion hack (réservé au surface-level)
            Optional.empty(),           // profondeur fixe : pas de projection sur la heightmap
            MAX_DISTANCE,
            List.of(),                  // pas d'alias de pool
            DimensionPadding.ZERO,
            // Un intérieur scellé ne se remplit pas parce qu'un aquifère passe à côté :
            // les blocs waterloggables de la pièce restent secs.
            LiquidSettings.IGNORE_WATERLOGGING);
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
