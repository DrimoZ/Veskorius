package com.veskorius.worldgen;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
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
        return true;
    }

    private static boolean isReplaceable(BlockState state) {
        return state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    }
}
