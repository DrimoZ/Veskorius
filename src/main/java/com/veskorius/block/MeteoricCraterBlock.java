package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * <b>Cratère météorique</b> — le dépôt qu'un Orage de Résonance laisse sur les blocs de
 * surface exposés (07-World-Generation.md).
 *
 * <p><b>Il se ramasse à la main, et il est éphémère.</b> Le dossier est explicite : « tout
 * fragment non récupéré avant la fin de l'orage disparaît — aucun stock à faire
 * indéfiniment, juste une fenêtre à saisir ». C'est ce qui distingue l'orage d'une
 * ressource : on ne l'exploite pas, on y court.
 *
 * <p><b>Un bloc et pas un objet au sol.</b> Un objet dérive, se ramasse tout seul quand on
 * passe à côté, et se perd dans l'inventaire sans qu'on ait rien décidé. Un cratère se voit
 * de loin, se choisit, et demande d'aller jusqu'à lui — ce qui fait de l'orage une chasse.
 */
public class MeteoricCraterBlock extends Block {

    public static final MapCodec<MeteoricCraterBlock> CODEC = simpleCodec(MeteoricCraterBlock::new);

    /** Deux pixels de haut : une croûte, pas un caillou. */
    private static final VoxelShape SHAPE = box(3.0, 0.0, 3.0, 13.0, 2.0, 13.0);

    public MeteoricCraterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return SHAPE;
    }

    /** Il lui faut un sol : sans support il tombe, comme toute croûte posée. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(),
            net.minecraft.core.Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, net.minecraft.core.Direction direction,
                                     BlockState neighbour, net.minecraft.world.level.LevelAccessor level,
                                     BlockPos pos, BlockPos neighbourPos) {
        return canSurvive(state, level, pos)
            ? state
            : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    }

    /**
     * Ramassage à la main : le fragment part dans l'inventaire et le cratère disparaît.
     * Le casser à l'outil marche aussi (loot table) — mais sous un orage de dix minutes,
     * un clic droit vaut mieux qu'un coup de pioche.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            popResource(level, pos, new ItemStack(ModItems.METEORIC_RESONANCE_SHARD.get()));
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_BREAK,
                SoundSource.BLOCKS, 1.0f, 1.4f);
        }
        return InteractionResult.SUCCESS;
    }
}
