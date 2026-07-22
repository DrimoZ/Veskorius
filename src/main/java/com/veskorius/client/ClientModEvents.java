package com.veskorius.client;

import com.veskorius.Veskorius;
import com.veskorius.client.screen.ComponentAssemblerScreen;
import com.veskorius.client.screen.CrystalCrusherScreen;
import com.veskorius.client.screen.FieldEmitterScreen;
import com.veskorius.client.screen.FluxPurifierScreen;
import com.veskorius.client.entity.CrystalStriderModel;
import com.veskorius.client.entity.CrystalStriderRenderer;
import com.veskorius.client.entity.ModModelLayers;
import com.veskorius.client.screen.ResonanceStabilizerScreen;
import com.veskorius.client.screen.ResonanceWhetstoneScreen;
import com.veskorius.entity.ModEntities;
import com.veskorius.menu.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Veskorius.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.RESONANCE_STABILIZER.get(), ResonanceStabilizerScreen::new);
        event.register(ModMenuTypes.COMPONENT_ASSEMBLER.get(), ComponentAssemblerScreen::new);
        event.register(ModMenuTypes.RESONANCE_WHETSTONE.get(), ResonanceWhetstoneScreen::new);
        event.register(ModMenuTypes.FLUX_PURIFIER.get(), FluxPurifierScreen::new);
        event.register(ModMenuTypes.FIELD_EMITTER.get(), FieldEmitterScreen::new);
        event.register(ModMenuTypes.CRYSTAL_CRUSHER.get(), CrystalCrusherScreen::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.CRYSTAL_STRIDER, CrystalStriderModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CRYSTAL_STRIDER.get(), CrystalStriderRenderer::new);
    }
}
