package com.veskorius.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * <b>Appareil veskorien orienté</b> : une façade qu'on tourne, et un état allumé.
 *
 * <p>C'est tout ce que partagent les blocs actifs du mod, et c'était recopié <b>dix fois</b>.
 * Les mêmes quarante lignes — deux propriétés, l'état par défaut, la définition d'état, la
 * pose face au joueur, la rotation, le miroir, la forme de rendu — dans le Slag Vent, le
 * Relais, l'Ancre, l'Amplificateur, le Cœur de Convergence, le Concentrateur, l'Émetteur de
 * Garde, l'Émetteur de Champ, le Relais Endommagé et le socle des machines. Deux de ces
 * fichiers ne différaient que par <b>cinq lignes</b> sur cent douze.
 *
 * <p><b>Ce que la duplication coûtait vraiment</b>, ce n'est pas le volume : c'est qu'une
 * correction d'orientation devait être appliquée dix fois, et qu'un oubli ne se verrait que
 * sur le bloc oublié, posé de travers dans une seule ruine.
 *
 * <p><b>Les propriétés sont les singletons vanilla</b> ({@code HORIZONTAL_FACING},
 * {@code LIT}) : les redéclarer dans chaque classe ne créait pas d'objets distincts, donc
 * les regrouper ici ne change strictement rien à ce que voient les blockstates, le Resonance
 * Tuner ou les modèles. Les anciennes références du type {@code FieldEmitterBlock.LIT}
 * continuent de résoudre — un champ statique hérité reste accessible par la sous-classe.
 *
 * <p>Reste à chaque bloc ce qui le distingue : sa block entity, son ticker, son clic droit.
 */
public abstract class AbstractOrientedBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    protected AbstractOrientedBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
            .setValue(FACING, Direction.NORTH)
            .setValue(LIT, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    /** Posé face au joueur : on regarde toujours l'appareil qu'on vient d'installer. */
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
     * MODEL et non {@code INVISIBLE} : un {@link BaseEntityBlock} rend son modèle
     * uniquement si on le lui demande, et le défaut hérité l'aurait rendu invisible.
     */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
