package com.veskorius.client;

import com.veskorius.Veskorius;
import com.veskorius.client.screen.ComponentAssemblerScreen;
import com.veskorius.client.screen.ResonanceStabilizerScreen;
import com.veskorius.client.screen.ResonanceWhetstoneScreen;
import com.veskorius.menu.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Veskorius.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.RESONANCE_STABILIZER.get(), ResonanceStabilizerScreen::new);
        event.register(ModMenuTypes.COMPONENT_ASSEMBLER.get(), ComponentAssemblerScreen::new);
        event.register(ModMenuTypes.RESONANCE_WHETSTONE.get(), ResonanceWhetstoneScreen::new);
    }
}
