package com.veskorius.client.model;

import com.veskorius.Veskorius;
import com.veskorius.block.ConnectedFrame;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Branche le modèle dynamique des blocs à cadre connecté sur le pipeline de rendu.
 *
 * <p>Deux temps, et il faut les deux. {@link #registerPieces} fait <b>cuire</b> le fond, les
 * douze baguettes et les vingt-quatre quarts de cadre : le blockstate ne pointe plus que sur
 * le fond, donc personne d'autre ne les réclame et ils n'existeraient jamais sous forme de
 * quads. {@link #swapInDynamicModel} remplace ensuite le modèle de chaque état du bloc.
 *
 * <p>La liste des blocs concernés vit dans {@link ConnectedFrame#FRAMES}, avec la datagen —
 * c'est le seul endroit où les noms de modèles sont écrits.
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ConnectedFrameModelHandler {

    private ConnectedFrameModelHandler() {
    }

    private static ModelResourceLocation piece(String name) {
        return ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "block/" + name));
    }

    @SubscribeEvent
    public static void registerPieces(ModelEvent.RegisterAdditional event) {
        for (ConnectedFrame.Frame frame : ConnectedFrame.FRAMES) {
            String name = frame.name();
            event.register(piece(frame.baseModel()));
            ConnectedFrame.forEachEdge((a, b) ->
                event.register(piece(ConnectedFrame.barName(name, a, b))));
            ConnectedFrame.forEachFaceCorner((f, p, q) ->
                event.register(piece(ConnectedFrame.cornerName(name, f, p, q))));
        }
    }

    @SubscribeEvent
    public static void swapInDynamicModel(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        for (ConnectedFrame.Frame frame : ConnectedFrame.FRAMES) {
            Block block = frame.block().get();
            String name = frame.name();

            BakedModel base = models.get(piece(frame.baseModel()));
            if (base == null) {
                // Un fond absent donnerait un bloc invisible, sans la moindre erreur. Mieux
                // vaut garder le modèle d'origine que rendre le bloc introuvable.
                Veskorius.LOGGER.error("{} : fond non cuit, cadre connecté désactivé", name);
                continue;
            }

            Map<ConnectedFrameModel.EdgeKey, ConnectedFrameModel.Piece> bars = new HashMap<>();
            ConnectedFrame.forEachEdge((a, b) -> {
                ConnectedFrameModel.Piece p = bake(models, ConnectedFrame.barName(name, a, b), block);
                if (p != null) {
                    bars.put(new ConnectedFrameModel.EdgeKey(a, b), p);
                }
            });
            Map<ConnectedFrameModel.CornerKey, ConnectedFrameModel.Piece> corners = new HashMap<>();
            ConnectedFrame.forEachFaceCorner((f, p, q) -> {
                ConnectedFrameModel.Piece piece =
                    bake(models, ConnectedFrame.cornerName(name, f, p, q), block);
                if (piece != null) {
                    corners.put(new ConnectedFrameModel.CornerKey(f, p, q), piece);
                }
            });

            ConnectedFrameModel dynamic = new ConnectedFrameModel(base, block, frame.underlinesCreases(), bars, corners);
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                models.put(BlockModelShaper.stateToModelLocation(state), dynamic);
            }
        }
    }

    /** Un morceau cuit, avec les couches de rendu qu'il déclare (cutout pour un cadre de verre). */
    private static ConnectedFrameModel.Piece bake(Map<ModelResourceLocation, BakedModel> models,
                                                  String name, Block block) {
        BakedModel model = models.get(piece(name));
        if (model == null) {
            return null;
        }
        return new ConnectedFrameModel.Piece(model, model.getRenderTypes(
            block.defaultBlockState(), RandomSource.create(0), ModelData.EMPTY));
    }
}
