package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * <b>Conduit de Résonance</b> — la ligne qui court dans la maçonnerie et raconte l'état du
 * réseau (17-Dungeons.md, règle R2). Hérite de {@link FieldSensitiveBlock} : il s'allume
 * quand un champ actif le couvre.
 *
 * <p><b>Pourquoi il porte un axe.</b> Un conduit est un <i>tracé</i> : son sens de lecture
 * est sa raison d'être. Sans axe, une descente verticale affichait une gouttière
 * horizontale à chaque bloc — le tuyau avait l'air haché en travers tous les mètres, et
 * l'œil ne suivait plus rien. C'est le seul bloc du vocabulaire où l'orientation porte du
 * sens plutôt que du décor, d'où l'exception.
 */
public class ConduitLineBlock extends FieldSensitiveBlock {

    public static final MapCodec<ConduitLineBlock> CODEC = simpleCodec(ConduitLineBlock::new);

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public ConduitLineBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected MapCodec<? extends FieldSensitiveBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        // Sans ça, un conduit tourné avec sa structure garde son axe d'origine et le tracé
        // part en travers dès que le jigsaw fait pivoter la pièce.
        return switch (rotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (state.getValue(AXIS)) {
                case X -> state.setValue(AXIS, Direction.Axis.Z);
                case Z -> state.setValue(AXIS, Direction.Axis.X);
                default -> state;
            };
            default -> state;
        };
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state;
    }
}
