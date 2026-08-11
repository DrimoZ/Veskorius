package com.veskorius.client.model;

import com.veskorius.Veskorius;
import com.veskorius.block.ChassisFrame;
import com.veskorius.block.ModBlocks;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

/**
 * Branche le modèle dynamique des châssis sur le pipeline de rendu.
 *
 * <p>Deux temps, et il faut les deux. {@link #registerPieces} fait <b>cuire</b> la plaque, les
 * douze baguettes et les vingt-quatre quarts de cadre : sans ça, personne ne les réclame — le
 * blockstate ne pointe plus que sur la plaque — et ils n'existeraient jamais sous forme de
 * quads. {@link #swapInDynamicModel} remplace ensuite le modèle de chaque état du bloc par le
 * modèle dynamique, qui a désormais tous ses morceaux sous la main.
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ChassisModelHandler {

    private ChassisModelHandler() {
    }

    /** Les trois châssis, avec le préfixe de leurs modèles. */
    private static Map<Block, String> chassis() {
        Map<Block, String> map = new HashMap<>();
        map.put(ModBlocks.FRACTURED_CHASSIS.get(), "fractured_chassis");
        map.put(ModBlocks.ATTUNED_CHASSIS.get(), "attuned_chassis");
        map.put(ModBlocks.VESKORIAN_CHASSIS.get(), "veskorian_chassis");
        return map;
    }

    private static ModelResourceLocation piece(String name) {
        return ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "block/" + name));
    }

    @SubscribeEvent
    public static void registerPieces(ModelEvent.RegisterAdditional event) {
        for (String name : chassis().values()) {
            event.register(piece(name + "_plate"));
            ChassisFrame.forEachEdge((a, b) ->
                event.register(piece(ChassisFrame.barName(name, a, b))));
            ChassisFrame.forEachFaceCorner((f, p, q) ->
                event.register(piece(ChassisFrame.cornerName(name, f, p, q))));
        }
    }

    @SubscribeEvent
    public static void swapInDynamicModel(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        for (Map.Entry<Block, String> entry : chassis().entrySet()) {
            Block block = entry.getKey();
            String name = entry.getValue();

            BakedModel plate = models.get(piece(name + "_plate"));
            if (plate == null) {
                // Un morceau absent donnerait un bloc invisible, sans la moindre erreur.
                // Mieux vaut laisser le modèle d'origine que rendre le bloc introuvable.
                Veskorius.LOGGER.error("Chassis {} : plaque non cuite, cadre connecté désactivé", name);
                continue;
            }
            Map<ConnectedChassisModel.EdgeKey, BakedModel> bars = new HashMap<>();
            ChassisFrame.forEachEdge((a, b) -> {
                BakedModel m = models.get(piece(ChassisFrame.barName(name, a, b)));
                if (m != null) {
                    bars.put(new ConnectedChassisModel.EdgeKey(a, b), m);
                }
            });
            Map<ConnectedChassisModel.CornerKey, BakedModel> corners = new HashMap<>();
            ChassisFrame.forEachFaceCorner((f, p, q) -> {
                BakedModel m = models.get(piece(ChassisFrame.cornerName(name, f, p, q)));
                if (m != null) {
                    corners.put(new ConnectedChassisModel.CornerKey(f, p, q), m);
                }
            });

            ConnectedChassisModel dynamic =
                new ConnectedChassisModel(plate, block, bars, corners);
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                models.put(BlockModelShaper.stateToModelLocation(state), dynamic);
            }
        }
    }

}
