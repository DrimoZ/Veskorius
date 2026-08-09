package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * <b>Verre à textures connectées.</b> Deux plaques côte à côte ne montrent aucune
 * séparation : ni le joint entre elles, ni le cadre de chacune sur le bord commun.
 *
 * <p><b>Deux mécanismes distincts, et il faut les deux.</b>
 *
 * <ol>
 *   <li><b>Le joint.</b> {@code skipRendering} supprime la face partagée entre deux blocs
 *       identiques — c'est ce que fait le verre vanilla, et sans ça on regarde à travers
 *       deux surfaces superposées, ce qui trouble la transparence et dessine une ligne.</li>
 *   <li><b>Le cadre.</b> Il ne suffit pas d'enlever la face intérieure : sur la face qu'on
 *       REGARDE, chaque bloc dessinait encore ses quatre bordures, donc un mur de verre
 *       affichait un quadrillage. Le cadre est donc sorti de la texture et posé en
 *       géométrie, uniquement là où le verre s'arrête.</li>
 * </ol>
 *
 * <p><b>Six booléens, et un blockstate multipart plutôt que soixante-quatre modèles.</b>
 * Chaque direction porte « y a-t-il le même verre de ce côté ». Le blockstate assemble
 * alors un cube sans bordure, plus une <i>bague</i> de cadre par direction ouverte — six
 * pièces combinables au lieu des 2⁶ modèles qu'il aurait fallu écrire un par un. C'est
 * exactement ce pour quoi le multipart existe, et ça garde la génération lisible.
 *
 * <p><b>Les deux verres se connectent séparément</b> : le lumineux ne se lie pas à
 * l'ordinaire. Ils n'ont pas la même luminosité, donc les fondre l'un dans l'autre
 * mentirait sur ce qu'on regarde.
 */
public class ConnectedGlassBlock extends Block {

    public static final MapCodec<ConnectedGlassBlock> CODEC = simpleCodec(ConnectedGlassBlock::new);

    /** « Le même verre est de ce côté. » Réutilise les propriétés directionnelles vanilla (celles des clôtures et des vitres). */
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    public ConnectedGlassBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
            .setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false)
            .setValue(WEST, false).setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /** La propriété qui décrit un côté donné. */
    public static BooleanProperty property(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = context.getClickedPos().relative(direction);
            state = state.setValue(property(direction),
                connectsTo(context.getLevel().getBlockState(neighbour)));
        }
        return state;
    }

    /**
     * Recalcule le seul côté qui a bougé. Minecraft appelle cette méthode sur chaque
     * voisin d'un bloc posé ou cassé : c'est ce qui fait qu'un mur se recoud tout seul
     * quand on y ajoute ou retire une plaque, sans qu'on ait à balayer quoi que ce soit.
     */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
                                     LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return state.setValue(property(direction), connectsTo(neighbour));
    }

    /** Même bloc exactement : le verre lumineux ne se lie pas à l'ordinaire. */
    private boolean connectsTo(BlockState neighbour) {
        return neighbour.is(this);
    }

    /**
     * Supprime la face partagée avec un bloc identique — le comportement du verre vanilla.
     * Sans ça, deux plaques accolées dessinent chacune leur face intérieure, ce qui se voit
     * comme une ligne et double la transparence à traverser.
     */
    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, side);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return false;
    }

    /** Le verre n'occulte rien : sans ça, un mur de verre s'assombrit de l'intérieur. */
    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

}
