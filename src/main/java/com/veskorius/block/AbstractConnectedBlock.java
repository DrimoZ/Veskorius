package com.veskorius.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * <b>Socle des blocs qui savent qui les touche.</b> Six booléens — un par direction —
 * portant « le même bloc est de ce côté », tenus à jour à la pose et à chaque changement
 * de voisinage.
 *
 * <p>Le blockstate qui s'appuie dessus est un <b>multipart</b> : un cube nu, plus une pièce
 * par arête ouverte. Six booléens décrivent 2⁶ = 64 configurations ; les écrire une par une
 * serait 64 modèles par bloc. Le multipart les compose, et la génération reste lisible.
 *
 * <p><b>La règle porte sur les ARÊTES, jamais sur les faces</b>, et c'est l'erreur qu'on a
 * faite une première fois sur le verre : la bordure du HAUT de la face nord ne dépend pas
 * du nord, elle dépend du HAUT. Conditionner sur la face donnait un mur en quadrillage,
 * chaque bloc gardant ses quatre bordures. Une baguette sur l'arête entre les faces A et B
 * n'existe que si NI A NI B n'a de voisin.
 *
 * <p><b>Ce socle ne convient qu'aux blocs qui sont des cubes pleins.</b> Les machines ont
 * des silhouettes creusées, à étages, parfois traversantes : une baguette posée sur l'arête
 * d'un cube y flotterait dans le vide. C'est pour ça que les machines gardent un cadre
 * <i>peint</i> dans leur texture, et que seuls les châssis et les verres se connectent.
 */
public abstract class AbstractConnectedBlock extends Block {

    /** « Le même bloc est de ce côté. » Propriétés directionnelles vanilla (clôtures, vitres). */
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    protected AbstractConnectedBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
            .setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false)
            .setValue(WEST, false).setValue(UP, false).setValue(DOWN, false));
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
     * Recalcule le seul côté qui a bougé. Minecraft appelle cette méthode sur chaque voisin
     * d'un bloc posé ou cassé : c'est ce qui fait qu'un mur se recoud tout seul quand on y
     * ajoute ou retire une pièce, sans balayage.
     *
     * <p><b>Attention aux structures :</b> {@code StructureTemplate.placeInWorld} n'appelle
     * pas {@code updateShape} sur les blocs intérieurs. Un bloc connecté posé par une ruine
     * doit donc avoir son état déjà cuit dans le NBT à la datagen.
     */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
                                     LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return state.setValue(property(direction), connectsTo(neighbour));
    }

    /**
     * Par défaut : le même bloc, exactement. Deux paliers de châssis ne se fondent pas l'un
     * dans l'autre, pas plus que le verre lumineux ne se fond dans l'ordinaire — la jointure
     * est justement l'information qu'on veut voir.
     */
    protected boolean connectsTo(BlockState neighbour) {
        return neighbour.is(this);
    }
}
