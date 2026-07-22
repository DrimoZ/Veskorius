package com.veskorius.worldgen;

import com.veskorius.block.ModBlocks;
import net.minecraft.core.BlockPos;
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

        // Console d'attunement (Avant-poste) : au centre, sur le sol.
        if (config.console()) {
            level.setBlock(origin.above(), ModBlocks.ATTUNEMENT_CONSOLE.get().defaultBlockState(), 2);
        }

        return true;
    }
}
