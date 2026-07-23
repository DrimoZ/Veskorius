package com.veskorius;

import com.veskorius.block.ModBlocks;
import com.veskorius.block.entity.ModBlockEntities;
import com.veskorius.codex.ModAttachments;
import com.veskorius.config.BasicsConfig;
import com.veskorius.config.GenerationConfig;
import com.veskorius.config.MachinesConfig;
import com.veskorius.config.MobsConfig;
import com.veskorius.entity.ModEntities;
import com.veskorius.item.ModDataComponents;
import com.veskorius.item.ModItems;
import com.veskorius.menu.ModMenuTypes;
import com.veskorius.recipe.ModRecipeSerializers;
import com.veskorius.recipe.ModRecipeTypes;
import com.veskorius.worldgen.ModFeatures;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
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
                output.accept(ModItems.RESONANCE_CRYSTAL_CLUSTER_ITEM.get());
                output.accept(ModItems.RESONANCE_VEINED_STONE_ITEM.get());
                output.accept(ModItems.RAW_FLUX_DEPOSIT.get());
                output.accept(ModItems.STABLE_RESONANCE_CRYSTAL.get());
                output.accept(ModItems.REFINED_RESONANCE_CRYSTAL.get());
                output.accept(ModItems.RESONANCE_COMPONENT.get());
                output.accept(ModItems.RESONANCE_DUST.get());
                output.accept(ModItems.RESONANCE_CATALYST_CORE.get());
                output.accept(ModItems.RESONANCE_TUNER.get());
                output.accept(ModItems.RESONANCE_STORAGE_CELL.get());
                output.accept(ModItems.RESONANCE_LOCATOR.get());
                output.accept(ModItems.RESONANCE_SPORE.get());
                output.accept(ModItems.CUSTODE_ALLOY_FRAGMENT.get());
                output.accept(ModItems.CRYSTAL_STRIDER_SPAWN_EGG.get());
                output.accept(ModItems.CUSTODE_SPAWN_EGG.get());
                output.accept(ModItems.FOSSILIZED_RATION.get());
                output.accept(ModItems.RESONANCE_CODEX.get());
                output.accept(com.veskorius.item.ResonanceBlueprintItem.of(2));
                output.accept(com.veskorius.item.CodexFragmentItem.of(
                    com.veskorius.item.CodexEntries.DAILY_LIFE_LAMPS));
                output.accept(ModItems.RESONANCE_STABILIZER_ITEM.get());
                output.accept(ModItems.COMPONENT_ASSEMBLER_ITEM.get());
                output.accept(ModItems.RESONANCE_WHETSTONE_ITEM.get());
                output.accept(ModItems.FLUX_PURIFIER_ITEM.get());
                output.accept(ModItems.FIELD_EMITTER_ITEM.get());
                output.accept(ModItems.CRYSTAL_CRUSHER_ITEM.get());
                output.accept(ModItems.CRYSTAL_ROOST_ITEM.get());
            })
            .build());

    public Veskorius(IEventBus modEventBus, ModContainer modContainer) {
        // Config d'équilibrage exposée aux modpack makers (type SERVER : synchronisée,
        // par monde, livrable via defaultconfigs/). Découpée PAR THÈME (14-Configuration.md)
        // pour qu'un modpack surcharge un domaine sans toucher aux autres.
        // À venir avec leur système : veskorius-harmonics.toml, veskorius-structures.toml.
        modContainer.registerConfig(ModConfig.Type.SERVER, BasicsConfig.SPEC, "veskorius-basics.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, MachinesConfig.SPEC, "veskorius-machines.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, GenerationConfig.SPEC, "veskorius-generation.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, MobsConfig.SPEC, "veskorius-mobs.toml");

        ModItems.ITEMS.register(modEventBus);
        ModDataComponents.COMPONENTS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
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
