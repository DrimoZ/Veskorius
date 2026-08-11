package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

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
 * <p>Où poser ce cadre est décidé par {@link ConnectedFrame}, la même règle que pour les
 * châssis, et assemblé au rendu par {@code ConnectedFrameModel}. Le verre a d'abord eu sa
 * propre implémentation à six booléens de blockstate : c'était le même raisonnement écrit
 * deux fois, dont une seule moitié corrigée à chaque défaut trouvé.
 */
public class ConnectedGlassBlock extends Block {

    public static final MapCodec<ConnectedGlassBlock> CODEC = simpleCodec(ConnectedGlassBlock::new);

    public ConnectedGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
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
