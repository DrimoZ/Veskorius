package com.veskorius.menu;

import com.veskorius.Veskorius;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(BuiltInRegistries.MENU, Veskorius.MOD_ID);

    /**
     * {@code IMenuTypeExtension.create} (et non {@code new MenuType<>(...)}) :
     * c'est la variante NeoForge qui donne acces aux donnees supplementaires
     * envoyees a l'ouverture — ici la BlockPos de la machine.
     */
    public static final DeferredHolder<MenuType<?>, MenuType<ResonanceStabilizerMenu>>
        RESONANCE_STABILIZER = MENUS.register("resonance_stabilizer",
            () -> IMenuTypeExtension.create(ResonanceStabilizerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ResonanceWhetstoneMenu>>
        RESONANCE_WHETSTONE = MENUS.register("resonance_whetstone",
            () -> IMenuTypeExtension.create(ResonanceWhetstoneMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ComponentAssemblerMenu>>
        COMPONENT_ASSEMBLER = MENUS.register("component_assembler",
            () -> IMenuTypeExtension.create(ComponentAssemblerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FluxPurifierMenu>>
        FLUX_PURIFIER = MENUS.register("flux_purifier",
            () -> IMenuTypeExtension.create(FluxPurifierMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FieldEmitterMenu>>
        FIELD_EMITTER = MENUS.register("field_emitter",
            () -> IMenuTypeExtension.create(FieldEmitterMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<CrystalCrusherMenu>>
        CRYSTAL_CRUSHER = MENUS.register("crystal_crusher",
            () -> IMenuTypeExtension.create(CrystalCrusherMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<CrystalRoostMenu>>
        CRYSTAL_ROOST = MENUS.register("crystal_roost",
            () -> IMenuTypeExtension.create(CrystalRoostMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<DampingArrayMenu>>
        DAMPING_ARRAY = MENUS.register("damping_array",
            () -> IMenuTypeExtension.create(DampingArrayMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<RiftCoreExtractorMenu>>
        RIFT_CORE_EXTRACTOR = MENUS.register("rift_core_extractor",
            () -> IMenuTypeExtension.create(RiftCoreExtractorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<AutomatedExtractionArrayMenu>>
        AUTOMATED_EXTRACTION_ARRAY = MENUS.register("automated_extraction_array",
            () -> IMenuTypeExtension.create(AutomatedExtractionArrayMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<DeepSynthesisChamberMenu>>
        DEEP_SYNTHESIS_CHAMBER = MENUS.register("deep_synthesis_chamber",
            () -> IMenuTypeExtension.create(DeepSynthesisChamberMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FluxCompressorMenu>>
        FLUX_COMPRESSOR = MENUS.register("flux_compressor",
            () -> IMenuTypeExtension.create(FluxCompressorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<StructuralSynthesizerMenu>>
        STRUCTURAL_SYNTHESIZER = MENUS.register("structural_synthesizer",
            () -> IMenuTypeExtension.create(StructuralSynthesizerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<DeepCrystalDrillerMenu>>
        DEEP_CRYSTAL_DRILLER = MENUS.register("deep_crystal_driller",
            () -> IMenuTypeExtension.create(DeepCrystalDrillerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<VeskorianAlloyForgeMenu>>
        VESKORIAN_ALLOY_FORGE = MENUS.register("veskorian_alloy_forge",
            () -> IMenuTypeExtension.create(VeskorianAlloyForgeMenu::new));
}
