package com.veskorius.worldgen;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.entity.CrystalStriderEntity;
import com.veskorius.entity.ModEntities;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * A pocket of Raw Resonance Crystal + its Resonance Veined Stone shell
 * (07-World-Generation.md, task 14).
 *
 * The vanilla ore feature can only make a cluster; a custom feature is needed to
 * wrap the cluster in a shell — that shell is the visual "tell" of pillar 2 (spatial
 * knowledge): seeing veined stone means a pocket is near, before mining anything.
 *
 * Algorithm: a random walk places the crystals (compact, connected cluster), then
 * every stone block within the shell radius becomes veined stone.
 */
public class ResonanceCrystalPocketFeature extends Feature<CrystalPocketConfiguration> {

    /** Combien de Fileurs au plus par poche peuplée : un petit groupe, pas un troupeau. */
    private static final int STRIDERS_PER_POCKET = 2;

    /** Loot table for brushing a flux deposit (see loot_table/gameplay/). */
    private static final ResourceKey<LootTable> BRUSH_FLUX_LOOT = ResourceKey.create(
        Registries.LOOT_TABLE,
        ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "gameplay/brush_flux_deposit"));

    public ResonanceCrystalPocketFeature() {
        super(CrystalPocketConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<CrystalPocketConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        CrystalPocketConfiguration config = context.config();

        BlockState crystal = ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get().defaultBlockState();
        BlockState veined = ModBlocks.RESONANCE_VEINED_STONE.get().defaultBlockState();

        // 1. The crystals, via a random walk from the origin.
        Set<BlockPos> crystals = new HashSet<>();
        BlockPos.MutableBlockPos cursor = context.origin().mutable();
        for (int i = 0; i < config.crystalTries(); i++) {
            if (isReplaceable(level.getBlockState(cursor))) {
                level.setBlock(cursor, crystal, 2);
                crystals.add(cursor.immutable());
            }
            cursor.move(Direction.getRandom(random));
        }
        if (crystals.isEmpty()) {
            return false;
        }

        // 2. The shell: veined stone, with here and there a brushable flux crust
        //    (the "tell" + an alternative T1 path to Quartz).
        BlockState flux = ModBlocks.RAW_FLUX_DEPOSIT.get().defaultBlockState();
        int r = config.shellThickness();
        Set<BlockPos> shell = new HashSet<>();
        BlockPos.MutableBlockPos shellPos = new BlockPos.MutableBlockPos();
        for (BlockPos c : crystals) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        shellPos.set(c.getX() + dx, c.getY() + dy, c.getZ() + dz);
                        if (!crystals.contains(shellPos) && isReplaceable(level.getBlockState(shellPos))) {
                            shell.add(shellPos.immutable());
                        }
                    }
                }
            }
        }
        for (BlockPos s : shell) {
            if (random.nextFloat() < config.fluxChance()) {
                level.setBlock(s, flux, 2);
                if (level.getBlockEntity(s) instanceof BrushableBlockEntity brushable) {
                    // Brushing draws from this table (CHEST param set, 1 item max).
                    brushable.setLootTable(BRUSH_FLUX_LOOT, random.nextLong());
                }
            } else {
                level.setBlock(s, veined, 2);
            }
        }

        // 3. The fauna. See seedStriders for why it lives here.
        if (config.striderChance() > 0.0f && random.nextFloat() < config.striderChance()) {
            seedStriders(level, random, crystals);
        }
        return true;
    }

    /**
     * Places a small group of Crystal Striders in the pocket it belongs to.
     *
     * <p><b>Why the feature seeds them instead of natural spawning.</b> The Strider is a
     * {@code MobCategory.CREATURE} restricted to Y ≤ 0 by its spawn predicate, and that
     * combination is a dead end — verified against the vanilla spawner rather than assumed:
     *
     * <ul>
     *   <li><b>World generation never places it.</b> {@code NaturalSpawner
     *       .spawnMobsForChunkGeneration} is the path that populates a world with passive
     *       animals, and it picks positions with {@code getTopNonCollidingPos} — the
     *       <i>surface</i>. Above Y 0 in essentially every biome, so the predicate rejects
     *       every attempt.</li>
     *   <li><b>Runtime spawning is throttled and capped out.</b> {@code spawnForChunk}
     *       gates persistent categories behind {@code gameTime % 400 == 0}, and
     *       {@code CREATURE} has a cap of <b>10</b> with {@code isPersistent = true} —
     *       so surface animals, which never despawn, saturate that cap permanently.</li>
     * </ul>
     *
     * The Roost (05-Machines.md #8) requires a Strider within 6 blocks to produce
     * anything, so the whole passive-production branch was unreachable content. Seeding
     * them with the pocket also matches the design better than the spawn table ever did:
     * 09-Entities.md calls the Strider « la faune des poches de cristal », so it should
     * be <i>in</i> a pocket, not wherever the spawn algorithm happens to allow.
     */
    /** Exposé aux GameTest : c est le seul chemin qui peuple réellement l espèce. */
    public static void seedStriders(WorldGenLevel level, RandomSource random, Set<BlockPos> crystals) {
        BlockPos origin = crystals.iterator().next();
        int placed = 0;
        // On cherche une poche d'air ADJACENTE aux cristaux : la feature creuse rarement
        // un volume libre, l'espace vient des grottes qui la traversent.
        for (BlockPos crystal : crystals) {
            if (placed >= STRIDERS_PER_POCKET) {
                break;
            }
            for (Direction dir : Direction.values()) {
                BlockPos at = crystal.relative(dir);
                if (!level.getBlockState(at).isAir() || !level.getBlockState(at.above()).isAir()) {
                    continue;
                }
                CrystalStriderEntity strider = ModEntities.CRYSTAL_STRIDER.get().create(level.getLevel());
                if (strider == null) {
                    return;
                }
                strider.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
                strider.finalizeSpawn(level, level.getCurrentDifficultyAt(at),
                    MobSpawnType.CHUNK_GENERATION, null);
                level.addFreshEntity(strider);
                placed++;
                break;
            }
        }
        if (placed == 0) {
            Veskorius.LOGGER.debug("Poche de cristal en {} : aucun espace libre pour un Fileur", origin);
        }
    }

    private static boolean isReplaceable(BlockState state) {
        return state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    }
}
