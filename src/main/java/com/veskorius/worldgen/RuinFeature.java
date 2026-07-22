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
 * Petite ruine veskorienne (08-Structures.md) : Habitation Modeste et Avant-poste.
 *
 * Implémentée comme une **feature** (et non le système Structure/jigsaw vanilla) :
 * elle réutilise le pipeline éprouvé des poches de cristal (ConfiguredFeature +
 * PlacedFeature + BiomeModifier), donc peu de risque et validable au datagen. Le
 * compromis assumé : pas de {@code /locate} vanilla — le repérage passera par les
 * blocs (tell de surface, Locator qui scanne). Bâtiments placeholder (Phase 6).
 *
 * Pose une pièce creuse en pierre veinée (murs + sol + plafond), un coffre relié à
 * une table de butin, et — pour l'Avant-poste — la console d'attunement.
 */
public class RuinFeature extends Feature<RuinConfiguration> {

    public RuinFeature() {
        super(RuinConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<RuinConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        RuinConfiguration config = context.config();
        BlockPos origin = context.origin();

        BlockState wall = ModBlocks.RESONANCE_VEINED_STONE.get().defaultBlockState();
        int r = config.radius();
        int h = Math.max(3, config.height());

        // Coquille creuse : murs + sol (dy=0) + plafond (dy=h), intérieur vidé.
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

        // Coffre de butin, posé sur le sol dans un coin intérieur.
        BlockPos chestPos = origin.offset(-r + 1, 1, -r + 1);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity chest) {
            chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, config.lootTable()), random.nextLong());
        }

        // Console d'attunement + un Custode de garde + un « tell » de surface (Avant-poste).
        if (config.console()) {
            level.setBlock(origin.above(), ModBlocks.ATTUNEMENT_CONSOLE.get().defaultBlockState(), 2);
            spawnGuardian(level, origin);
            placeSurfaceMarker(level, origin);
        }

        return true;
    }

    /**
     * Petite amorce de pilier en pierre veinée à la surface, au-dessus de l'Avant-poste
     * (08-Structures.md, tell de surface) : rend la structure repérable une fois le T2
     * acquis (pilier 5). Le sol souterrain reste à trouver, mais le marqueur oriente.
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

    /** Pose un Custode persistant qui garde l'Avant-poste (09-Entities.md). */
    private static void spawnGuardian(WorldGenLevel level, BlockPos origin) {
        CustodeEntity custode = ModEntities.CUSTODE.get().create(level.getLevel());
        if (custode == null) {
            return;
        }
        BlockPos spot = origin.offset(1, 1, 1);
        custode.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, 0.0f, 0.0f);
        // Persistant : il garde le site, il ne doit pas disparaître au despawn.
        custode.setPersistenceRequired();
        custode.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), MobSpawnType.STRUCTURE, null);
        level.addFreshEntity(custode);
    }
}
