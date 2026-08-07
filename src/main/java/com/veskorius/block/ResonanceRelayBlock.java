package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.ModBlockEntities;
import com.veskorius.block.entity.ResonanceRelayBlockEntity;
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
 * Resonance Relay (machine #9). Bloc <b>sans inventaire et sans GUI</b> : il n'a rien à
 * transformer, donc rien à contenir, et un menu à une seule jauge serait un menu de plus à
 * ouvrir pour apprendre ce que la façade dit déjà.
 *
 * <p>Réutilise {@link FieldEmitterBlock#FACING} et {@link FieldEmitterBlock#LIT} — les mêmes
 * propriétés, pas seulement des propriétés équivalentes. C'est ce qui permet au générateur de
 * blockstates de traiter émetteurs et relais avec la même fabrique, et surtout au Resonance
 * Tuner de pivoter un relais sans une ligne de cas particulier.
 *
 * <p>Le clic droit ne fait qu'<b>annoncer la charge</b> dans la barre d'action. Un relais est
 * le seul bloc du réseau qu'on pose loin de tout : quand une base s'éteint à l'autre bout d'une
 * chaîne, il faut pouvoir remonter le fil relais par relais pour trouver lequel ne reçoit plus.
 * Sans ce retour, la seule façon de diagnostiquer serait de tout casser.
 */
public class ResonanceRelayBlock extends BaseEntityBlock {

    public static final MapCodec<ResonanceRelayBlock> CODEC = simpleCodec(ResonanceRelayBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public ResonanceRelayBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
            .setValue(FACING, Direction.NORTH)
            .setValue(LIT, Boolean.FALSE));
    }

    @Override
    protected MapCodec<ResonanceRelayBlock> codec() {
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

    /**
     * Le mât est mince, et une boîte de collision pleine autour d'un mât mince est un mur
     * invisible : on bute contre du vide qu'on voit. La forme suit donc la semelle et le fût,
     * et ignore les bras — ils ne portent rien et on doit pouvoir passer sous eux.
     */
    private static final net.minecraft.world.phys.shapes.VoxelShape SHAPE =
        net.minecraft.world.phys.shapes.Shapes.or(
            Block.box(3, 0, 3, 13, 2, 13),
            Block.box(5, 2, 5, 11, 16, 11));

    @Override
    protected net.minecraft.world.phys.shapes.VoxelShape getShape(
        BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
        net.minecraft.world.phys.shapes.CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonanceRelayBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.RESONANCE_RELAY.get(),
            ResonanceRelayBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof ResonanceRelayBlockEntity relay) {
            player.displayClientMessage(Component.translatable("message.veskorius.relay_charge",
                relay.getReserve(), ResonanceRelayBlockEntity.CAPACITY), true);
        }
        return InteractionResult.CONSUME;
    }
}
