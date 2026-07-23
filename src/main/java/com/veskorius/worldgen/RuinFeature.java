package com.veskorius.worldgen;

import com.veskorius.block.ModBlocks;
import com.veskorius.entity.CustodeEntity;
import com.veskorius.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * A small Veskorian ruin (08-Structures.md): the Modest Dwelling and the Outpost.
 *
 * Implemented as a <b>feature</b> (not the vanilla Structure/jigsaw system): it
 * reuses the proven crystal-pocket pipeline (ConfiguredFeature + PlacedFeature +
 * BiomeModifier), so it carries little risk and validates at datagen. The accepted
 * trade-off: no vanilla {@code /locate} — spotting relies on blocks (surface tell,
 * Locator scan). Placeholder buildings (Phase 6).
 *
 * Places a hollow room of veined stone (walls + floor + ceiling), a chest linked to
 * a loot table, and — for the Outpost — the attunement console, a guardian Custode
 * and a surface marker.
 */
public class RuinFeature extends Feature<RuinConfiguration> {

    /** Minimum fraction (%) of the footprint that must be solid ground to build here. */
    private static final int MIN_GROUND_PERCENT = 60;

    public RuinFeature() {
        super(RuinConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<RuinConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        RuinConfiguration config = context.config();
        BlockPos origin = context.origin();

        int r = config.radius();
        int h = Math.max(3, config.height());

        // Skip open air (caves, overhangs): a ruin should sit in the ground, not float.
        if (!hasEnoughGround(level, origin, r)) {
            return false;
        }

        BlockState wall = ModBlocks.RESONANCE_VEINED_STONE.get().defaultBlockState();

        // Hollow shell: walls + floor (dy=0) + ceiling (dy=h), interior cleared.
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy <= h; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    boolean edge = dx == -r || dx == r || dz == -r || dz == r;
                    boolean floorOrCeil = dy == 0 || dy == h;
                    pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (edge || floorOrCeil) {
                        level.setBlock(pos, wall, 2);
                    } else {
                        level.setBlock(pos, Blocks.CAVE_AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Loot chest, on the floor in an inner corner.
        BlockPos chestPos = origin.offset(-r + 1, 1, -r + 1);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity chest) {
            chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, config.lootTable()), random.nextLong());
        }

        // Attunement console + a guardian Custode + a surface marker (Outpost).
        if (config.console()) {
            level.setBlock(origin.above(), ModBlocks.ATTUNEMENT_CONSOLE.get().defaultBlockState(), 2);
            spawnGuardian(level, origin);
            placeSurfaceMarker(level, origin);
        }

        // Interior dressing so a ruin reads as a lived-in place, not an empty box.
        // Full buildings are still Phase 6; this is a cheap interim pass.
        decorateInterior(level, origin, r, h, random, config.console());

        return true;
    }

    /**
     * Scatters a little themed furniture, rubble and cobwebs inside the room so it
     * feels like a dwelling (or a workshop, for the Outpost) rather than a hollow
     * shell. Skips reserved cells (chest corner, console centre) and only writes into
     * air, so it never overwrites the loot chest or the console.
     */
    private static void decorateInterior(WorldGenLevel level, BlockPos origin, int r, int h,
                                         RandomSource random, boolean outpost) {
        BlockState[] furniture = outpost
            ? new BlockState[] {
                Blocks.CRAFTING_TABLE.defaultBlockState(),
                Blocks.FURNACE.defaultBlockState(),
                Blocks.BARREL.defaultBlockState(),
                Blocks.CHIPPED_ANVIL.defaultBlockState(),
                Blocks.SMITHING_TABLE.defaultBlockState()}
            : new BlockState[] {
                Blocks.CRAFTING_TABLE.defaultBlockState(),
                Blocks.BARREL.defaultBlockState(),
                Blocks.CAULDRON.defaultBlockState(),
                Blocks.COMPOSTER.defaultBlockState(),
                Blocks.BOOKSHELF.defaultBlockState()};
        BlockState[] rubble = {
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
            Blocks.GRAVEL.defaultBlockState()};
        int span = r - 1;

        int pieces = 3 + random.nextInt(2);
        int placed = 0;
        for (int attempt = 0; attempt < 30 && placed < pieces; attempt++) {
            int dx = random.nextInt(2 * span + 1) - span;
            int dz = random.nextInt(2 * span + 1) - span;
            BlockPos p = origin.offset(dx, 1, dz);
            if (!isReserved(dx, dz, r, outpost) && level.getBlockState(p).isAir()) {
                level.setBlock(p, furniture[random.nextInt(furniture.length)], 2);
                placed++;
            }
        }

        int rubblePiles = 2 + random.nextInt(3);
        for (int i = 0; i < rubblePiles; i++) {
            int dx = random.nextInt(2 * span + 1) - span;
            int dz = random.nextInt(2 * span + 1) - span;
            BlockPos p = origin.offset(dx, 1, dz);
            if (!isReserved(dx, dz, r, outpost) && level.getBlockState(p).isAir()) {
                level.setBlock(p, rubble[random.nextInt(rubble.length)], 2);
            }
        }

        int webs = 1 + random.nextInt(2);
        for (int i = 0; i < webs; i++) {
            int sx = random.nextBoolean() ? span : -span;
            int sz = random.nextBoolean() ? span : -span;
            BlockPos p = origin.offset(sx, h - 1, sz);
            if (level.getBlockState(p).isAir()) {
                level.setBlock(p, Blocks.COBWEB.defaultBlockState(), 2);
            }
        }
    }

    /** Cells kept clear of decoration: the loot chest corner and the console centre. */
    private static boolean isReserved(int dx, int dz, int r, boolean outpost) {
        boolean chestCorner = dx == -r + 1 && dz == -r + 1;
        boolean consoleCentre = outpost && dx == 0 && dz == 0;
        return chestCorner || consoleCentre;
    }

    /** True if the footprint at floor level is mostly solid ground (avoids floating ruins). */
    private static boolean hasEnoughGround(WorldGenLevel level, BlockPos origin, int r) {
        int solid = 0;
        int total = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                pos.set(origin.getX() + dx, origin.getY(), origin.getZ() + dz);
                total++;
                if (!level.getBlockState(pos).isAir()) {
                    solid++;
                }
            }
        }
        return solid * 100 >= total * MIN_GROUND_PERCENT;
    }

    /**
     * A short pillar stub of veined stone at the surface, above the Outpost
     * (08-Structures.md, surface tell): makes the structure spottable once T2 is
     * unlocked (pillar 5). The underground room still has to be dug to, but the
     * marker points the way.
     */
    private static void placeSurfaceMarker(WorldGenLevel level, BlockPos origin) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        BlockState veined = ModBlocks.RESONANCE_VEINED_STONE.get().defaultBlockState();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(origin.getX(), surfaceY, origin.getZ());
        for (int i = 0; i < 3; i++) {
            level.setBlock(cursor, veined, 2);
            cursor.move(Direction.UP);
        }
    }

    /** Places a persistent Custode that guards the Outpost (09-Entities.md). */
    private static void spawnGuardian(WorldGenLevel level, BlockPos origin) {
        CustodeEntity custode = ModEntities.CUSTODE.get().create(level.getLevel());
        if (custode == null) {
            return;
        }
        BlockPos spot = origin.offset(1, 1, 1);
        custode.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, 0.0f, 0.0f);
        // Persistent: it guards the site and must not despawn.
        custode.setPersistenceRequired();
        custode.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), MobSpawnType.STRUCTURE, null);
        level.addFreshEntity(custode);
    }
}
