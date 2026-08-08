package com.veskorius.client;

import com.veskorius.Veskorius;
import com.veskorius.client.screen.ComponentAssemblerScreen;
import com.veskorius.client.screen.CrystalCrusherScreen;
import com.veskorius.client.screen.CrystalRoostScreen;
import com.veskorius.client.screen.FieldEmitterScreen;
import com.veskorius.client.screen.FluxPurifierScreen;
import com.veskorius.client.entity.CrystalStriderModel;
import com.veskorius.client.entity.CrystalStriderRenderer;
import com.veskorius.client.entity.CustodeModel;
import com.veskorius.client.entity.CustodeRenderer;
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
        event.register(ModMenuTypes.CRYSTAL_ROOST.get(), CrystalRoostScreen::new);
        event.register(ModMenuTypes.DAMPING_ARRAY.get(), com.veskorius.client.screen.DampingArrayScreen::new);
        event.register(ModMenuTypes.VESKORIAN_ALLOY_FORGE.get(),
            com.veskorius.client.screen.VeskorianAlloyForgeScreen::new);
        event.register(ModMenuTypes.RIFT_CORE_EXTRACTOR.get(),
            com.veskorius.client.screen.RiftCoreExtractorScreen::new);
        event.register(ModMenuTypes.AUTOMATED_EXTRACTION_ARRAY.get(),
            com.veskorius.client.screen.AutomatedExtractionArrayScreen::new);
        event.register(ModMenuTypes.DEEP_SYNTHESIS_CHAMBER.get(),
            com.veskorius.client.screen.DeepSynthesisChamberScreen::new);
        event.register(ModMenuTypes.FLUX_COMPRESSOR.get(),
            com.veskorius.client.screen.FluxCompressorScreen::new);
        event.register(ModMenuTypes.STRUCTURAL_SYNTHESIZER.get(),
            com.veskorius.client.screen.StructuralSynthesizerScreen::new);
        event.register(ModMenuTypes.DEEP_CRYSTAL_DRILLER.get(),
            com.veskorius.client.screen.DeepCrystalDrillerScreen::new);
    }

    /**
     * HUD de champ (12-UX) : au-dessus de la couche du viseur, donc sous le chat et les
     * bulles de dialogue — un instrument, jamais un obstacle.
     */
    @SubscribeEvent
    public static void registerGuiLayers(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) {
        event.registerAbove(net.neoforged.neoforge.client.gui.VanillaGuiLayers.CROSSHAIR,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "field_hud"),
            new FieldHudOverlay());
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.CRYSTAL_STRIDER, CrystalStriderModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.CUSTODE, CustodeModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.RIFT_GUARDIAN,
            com.veskorius.client.entity.RiftGuardianModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CRYSTAL_STRIDER.get(), CrystalStriderRenderer::new);
        event.registerEntityRenderer(ModEntities.CUSTODE.get(), CustodeRenderer::new);
        event.registerEntityRenderer(ModEntities.CUSTODE_ARCHIVISTE.get(),
            com.veskorius.client.entity.CustodeArchivisteRenderer::new);
        event.registerEntityRenderer(ModEntities.RIFT_GUARDIAN.get(),
            com.veskorius.client.entity.RiftGuardianRenderer::new);
    }
}
