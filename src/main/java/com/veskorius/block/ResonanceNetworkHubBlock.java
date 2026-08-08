package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.ModBlockEntities;
import com.veskorius.block.entity.ResonanceNetworkHubBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Slag Vent (machine #13). <b>Aucun inventaire, aucun GUI</b> : il ne stocke pas la scorie,
 * il la fait disparaître. Lui donner un slot en ferait une poubelle qu'on viderait, c'est-à-dire
 * exactement le geste manuel qu'il existe pour supprimer.
 *
 * <p>Le clic droit dit combien de forges il a servies au dernier passage — c'est le seul moyen
 * de savoir qu'un Vent est sous-dimensionné avant de retrouver ses forges à l'arrêt.
 *
 * <p>Réutilise {@link FieldEmitterBlock#FACING} et {@link FieldEmitterBlock#LIT} : mêmes
 * propriétés, donc même fabrique de modèle et même prise en main par le Resonance Tuner.
 */
public class ResonanceNetworkHubBlock extends BaseEntityBlock {

    public static final MapCodec<ResonanceNetworkHubBlock> CODEC = simpleCodec(ResonanceNetworkHubBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public ResonanceNetworkHubBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
            .setValue(FACING, Direction.NORTH)
            .setValue(LIT, Boolean.FALSE));
    }

    @Override
    protected MapCodec<ResonanceNetworkHubBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
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

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonanceNetworkHubBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.RESONANCE_NETWORK_HUB.get(), ResonanceNetworkHubBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof ResonanceNetworkHubBlockEntity hub) {
            // Le plancher courant et la calibration : les deux seuls chiffres qui expliquent
            // pourquoi telle machine vient de s'arrêter alors que la réserve paraît correcte.
            player.displayClientMessage(Component.translatable("message.veskorius.hub_status",
                hub.getFloor().label(), Math.round(hub.getEfficiency() * 100)), true);
        }
        return InteractionResult.CONSUME;
    }
}
