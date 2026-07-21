package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.FieldEmitterBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Field Emitter (machine #4). Bloc passif : pas de GUI à cycle, mais un slot de
 * carburant qu'on remplit au clic droit (un Stable Crystal à la fois) ou par
 * hopper (capability ItemHandler, voir {@code ModCapabilities}).
 *
 * Ne partage pas {@link AbstractMachineBlock} : son ticker cible une block entity
 * qui n'est pas une machine à cycle, et son clic droit insère du carburant au lieu
 * d'ouvrir un menu. FACING est néanmoins présent, pour l'uniformité du Resonance
 * Tuner (12-UX-and-Advancements.md) et parce que l'ajouter plus tard casserait les
 * blocs déjà posés.
 */
public class FieldEmitterBlock extends BaseEntityBlock {

    public static final MapCodec<FieldEmitterBlock> CODEC = simpleCodec(FieldEmitterBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public FieldEmitterBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<FieldEmitterBlock> codec() {
        return CODEC;
    }

    // --- Orientation ---------------------------------------------------------

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // --- Rendu et ticker -----------------------------------------------------

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FieldEmitterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.FIELD_EMITTER.get(),
            FieldEmitterBlockEntity::serverTick);
    }

    // --- Interaction : GUI (main vide) et insertion du carburant (cristal) ---

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof MenuProvider provider) {
            player.openMenu(provider, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FieldEmitterBlockEntity emitter)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        // Ne réagit qu'au carburant accepté ; tout le reste retombe sur le
        // comportement par défaut (poser le bloc tenu, etc.).
        if (!emitter.getFuelHandler().isItemValid(FieldEmitterBlockEntity.SLOT_FUEL, stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        ItemStack single = stack.copyWithCount(1);
        ItemStack leftover = emitter.getFuelHandler().insertItem(FieldEmitterBlockEntity.SLOT_FUEL, single, false);
        if (!leftover.isEmpty()) {
            // Slot déjà plein : ne rien consommer.
            return ItemInteractionResult.CONSUME;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
            && level.getBlockEntity(pos) instanceof FieldEmitterBlockEntity emitter) {
            emitter.dropContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
