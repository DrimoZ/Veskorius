package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.ArchivePedestalBlockEntity;
import com.veskorius.block.entity.FieldEmitterBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Socle d'archive</b> : on y pose une cote, on la reprend d'un clic à main vide. Quand
 * les quatre socles de la salle portent les quatre cotes <b>dans l'ordre</b>, l'émetteur
 * voisin se rallume — et le sas de la salle de lecture s'ouvre comme n'importe quel sas.
 *
 * <p>Voir {@link ArchivePedestalBlockEntity} pour la règle d'ordre et pour la raison
 * d'être de ce détour par l'émetteur plutôt que par une serrure dédiée.
 */
public class ArchivePedestalBlock extends BaseEntityBlock {

    public static final MapCodec<ArchivePedestalBlock> CODEC = simpleCodec(ArchivePedestalBlock::new);

    /** Rayon de recherche de l'émetteur à rallumer. Voir la note de portée dans la BE. */
    private static final int SCAN = 12;

    private static final VoxelShape SHAPE = Shapes.or(
        box(1.0, 0.0, 1.0, 15.0, 4.0, 15.0),
        box(3.0, 4.0, 3.0, 13.0, 12.0, 13.0),
        box(1.0, 12.0, 1.0, 15.0, 14.0, 15.0));

    public ArchivePedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends ArchivePedestalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArchivePedestalBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ArchivePedestalBlockEntity pedestal)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!pedestal.place(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, pos, SoundEvents.LODESTONE_PLACE, SoundSource.BLOCKS, 0.7f, 1.3f);
        checkSolved(level, pos, player);
        return ItemInteractionResult.CONSUME;
    }

    /** Main vide : on reprend la cote — donc on peut se tromper sans se bloquer. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof ArchivePedestalBlockEntity pedestal
            && !pedestal.isEmpty()) {
            ItemStack taken = pedestal.take();
            if (!player.addItem(taken)) {
                player.drop(taken, false);
            }
        }
        return InteractionResult.CONSUME;
    }

    /**
     * Si les quatre cotes sont en place et dans l'ordre, on <b>rallume l'émetteur</b> de la
     * salle. C'est tout : le sas fait le reste, avec le même code qu'ailleurs.
     */
    private static void checkSolved(Level level, BlockPos pos, Player player) {
        if (!ArchivePedestalBlockEntity.solved(level, pos)) {
            return;
        }
        for (BlockPos at : BlockPos.betweenClosed(pos.offset(-SCAN, -4, -SCAN),
            pos.offset(SCAN, 4, SCAN))) {
            if (level.getBlockEntity(at) instanceof FieldEmitterBlockEntity emitter) {
                emitter.restoreReserve(Integer.MAX_VALUE);
                level.playSound(null, at, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 0.8f);
            }
        }
        player.displayClientMessage(
            Component.translatable("block.veskorius.archive_pedestal.solved"), true);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                            boolean movedByPiston) {
        if (!state.is(newState.getBlock())
            && level.getBlockEntity(pos) instanceof ArchivePedestalBlockEntity pedestal
            && !pedestal.isEmpty()) {
            Block.popResource(level, pos, pedestal.take());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
