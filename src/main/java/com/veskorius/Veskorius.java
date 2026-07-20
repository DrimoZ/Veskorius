package com.veskorius;

import com.veskorius.block.ModBlocks;
import com.veskorius.block.entity.ModBlockEntities;
import com.veskorius.item.ModItems;
import com.veskorius.menu.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
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
 * Ce fichier ne fait qu'enregistrer les objets du mod et declarer un onglet
 * createur dedie. Toute logique de gameplay vit dans des classes separees.
 *
 * Source de verite pour le gameplay et l'ordre des taches :
 * veskorius-design/ (en particulier 11-Development-Plan.md).
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
                output.accept(ModItems.RESONANCE_WHETSTONE_ITEM.get());
            })
            .build());

    public Veskorius(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        // Pas de NeoForge.EVENT_BUS.register(this) ici : depuis NeoForge 21.1,
        // enregistrer un objet sans aucune methode @SubscribeEvent leve une
        // IllegalArgumentException et fait echouer le chargement du mod. Les
        // abonnements au bus de jeu passent par des classes dediees annotees
        // @EventBusSubscriber (voir client/ClientModEvents et datagen/).

        LOGGER.info("Veskorius: enregistrement termine.");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Reserve pour l'initialisation qui doit tourner apres l'enregistrement
        // de tous les mods (ex: capacites d'energie de Resonance, plus tard).
    }
}
