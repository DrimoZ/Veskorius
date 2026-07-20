package com.veskorius;

import com.veskorius.block.ModBlocks;
import com.veskorius.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Point d'entree du mod Veskorius.
 *
 * Ce fichier ne fait qu'enregistrer les items/blocs et declarer un onglet
 * createur dedie. Toute logique de gameplay (machines, energie de Resonance)
 * viendra dans des classes separees au fur et a mesure — voir TECH-SPEC.md
 * pour ce qui reste a coder.
 */
@Mod(Veskorius.MOD_ID)
public class Veskorius {

    public static final String MOD_ID = "veskorius";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VESKORIUS_TAB =
        CREATIVE_TABS.register("veskorius_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.veskorius.main_tab"))
            .icon(() -> ModItems.STABLE_RESONANCE_CRYSTAL.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.RAW_RESONANCE_CRYSTAL.get());
                output.accept(ModItems.STABLE_RESONANCE_CRYSTAL.get());
                output.accept(ModItems.REFINED_RESONANCE_CRYSTAL.get());
                output.accept(ModItems.RESONANCE_COMPONENT.get());
                output.accept(ModItems.RESONANCE_STABILIZER_ITEM.get());
            })
            .build());

    public Veskorius(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        LOGGER.info("Veskorius: enregistrement termine.");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Reserve pour l'initialisation qui doit tourner apres l'enregistrement
        // de tous les mods (ex: capacites d'energie de Resonance, plus tard).
    }
}
