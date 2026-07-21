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
 * Poche de Raw Resonance Crystal + sa coquille de Resonance Veined Stone
 * (07-World-Generation.md, tâche 14).
 *
 * La feature *ore* vanilla ne sait faire qu'un amas ; il faut une feature custom
 * pour enrober l'amas d'une coquille — c'est cette coquille qui est le « tell »
 * visuel du pilier 2 (connaissance spatiale) : voir la pierre veinée, c'est savoir
 * qu'une poche est proche sans avoir encore rien miné.
 *
 * Algorithme : une marche aléatoire pose les cristaux (amas compact et connexe),
 * puis chaque bloc de pierre à portée de la coquille devient de la pierre veinée.
 */
public class ResonanceCrystalPocketFeature extends Feature<CrystalPocketConfiguration> {

    /** Table de loot du brossage d'un dépôt de flux (voir loot_table/gameplay/). */
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

        // 1. Les cristaux, par marche aléatoire depuis l'origine.
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

        // 2. La coquille : pierre veinée, avec par endroits une croûte de flux
        //    brossable (le « tell » + un chemin T1 alternatif au Quartz).
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
                    // Le brossage tire de cette table (param set CHEST, 1 objet max).
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
