package com.veskorius.block.entity;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Veskorius.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonanceStabilizerBlockEntity>>
        RESONANCE_STABILIZER = BLOCK_ENTITIES.register("resonance_stabilizer",
            () -> BlockEntityType.Builder
                .of(ResonanceStabilizerBlockEntity::new, ModBlocks.RESONANCE_STABILIZER.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ComponentAssemblerBlockEntity>>
        COMPONENT_ASSEMBLER = BLOCK_ENTITIES.register("component_assembler",
            () -> BlockEntityType.Builder
                .of(ComponentAssemblerBlockEntity::new, ModBlocks.COMPONENT_ASSEMBLER.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonanceWhetstoneBlockEntity>>
        RESONANCE_WHETSTONE = BLOCK_ENTITIES.register("resonance_whetstone",
            () -> BlockEntityType.Builder
                .of(ResonanceWhetstoneBlockEntity::new, ModBlocks.RESONANCE_WHETSTONE.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluxPurifierBlockEntity>>
        FLUX_PURIFIER = BLOCK_ENTITIES.register("flux_purifier",
            () -> BlockEntityType.Builder
                .of(FluxPurifierBlockEntity::new, ModBlocks.FLUX_PURIFIER.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FieldEmitterBlockEntity>>
        FIELD_EMITTER = BLOCK_ENTITIES.register("field_emitter",
            () -> BlockEntityType.Builder
                // L'émetteur ancien des structures partage ce type : c'est le MÊME
                // émetteur, avec les mêmes règles (17-Dungeons.md §5.1). Deux types
                // distincts obligeraient à maintenir deux fois le comportement, et
                // c'est précisément ce qu'on veut éviter — la machine que le joueur
                // trouve en ruine doit être celle qu'il fabriquera, sans divergence.
                .of(FieldEmitterBlockEntity::new,
                    ModBlocks.FIELD_EMITTER.get(), ModBlocks.ANCIENT_EMITTER.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DamagedRelayBlockEntity>>
        DAMAGED_RELAY = BLOCK_ENTITIES.register("damaged_relay",
            () -> BlockEntityType.Builder
                .of(DamagedRelayBlockEntity::new, ModBlocks.DAMAGED_RELAY.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArchivePedestalBlockEntity>>
        ARCHIVE_PEDESTAL = BLOCK_ENTITIES.register("archive_pedestal",
            () -> BlockEntityType.Builder
                .of(ArchivePedestalBlockEntity::new, ModBlocks.ARCHIVE_PEDESTAL.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VeskorianAlloyForgeBlockEntity>>
        VESKORIAN_ALLOY_FORGE = BLOCK_ENTITIES.register("veskorian_alloy_forge",
            () -> BlockEntityType.Builder
                .of(VeskorianAlloyForgeBlockEntity::new, ModBlocks.VESKORIAN_ALLOY_FORGE.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonanceRelayBlockEntity>>
        RESONANCE_RELAY = BLOCK_ENTITIES.register("resonance_relay",
            () -> BlockEntityType.Builder
                .of(ResonanceRelayBlockEntity::new, ModBlocks.RESONANCE_RELAY.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluxCompressorBlockEntity>>
        FLUX_COMPRESSOR = BLOCK_ENTITIES.register("flux_compressor",
            () -> BlockEntityType.Builder
                .of(FluxCompressorBlockEntity::new, ModBlocks.FLUX_COMPRESSOR.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StructuralSynthesizerBlockEntity>>
        STRUCTURAL_SYNTHESIZER = BLOCK_ENTITIES.register("structural_synthesizer",
            () -> BlockEntityType.Builder
                .of(StructuralSynthesizerBlockEntity::new, ModBlocks.STRUCTURAL_SYNTHESIZER.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeepCrystalDrillerBlockEntity>>
        DEEP_CRYSTAL_DRILLER = BLOCK_ENTITIES.register("deep_crystal_driller",
            () -> BlockEntityType.Builder
                .of(DeepCrystalDrillerBlockEntity::new, ModBlocks.DEEP_CRYSTAL_DRILLER.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SlagVentBlockEntity>>
        SLAG_VENT = BLOCK_ENTITIES.register("slag_vent",
            () -> BlockEntityType.Builder
                .of(SlagVentBlockEntity::new, ModBlocks.SLAG_VENT.get())
                .build(null));

    /** Émetteur Accordable : même émetteur, bande harmonique choisie (06-Energy.md). */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TunableFieldEmitterBlockEntity>>
        TUNABLE_FIELD_EMITTER = BLOCK_ENTITIES.register("tunable_field_emitter",
            () -> BlockEntityType.Builder
                .of(TunableFieldEmitterBlockEntity::new, ModBlocks.TUNABLE_FIELD_EMITTER.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DampingArrayBlockEntity>>
        DAMPING_ARRAY = BLOCK_ENTITIES.register("damping_array",
            () -> BlockEntityType.Builder
                .of(DampingArrayBlockEntity::new, ModBlocks.DAMPING_ARRAY.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrystalCrusherBlockEntity>>
        CRYSTAL_CRUSHER = BLOCK_ENTITIES.register("crystal_crusher",
            () -> BlockEntityType.Builder
                .of(CrystalCrusherBlockEntity::new, ModBlocks.CRYSTAL_CRUSHER.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrystalRoostBlockEntity>>
        CRYSTAL_ROOST = BLOCK_ENTITIES.register("crystal_roost",
            () -> BlockEntityType.Builder
                .of(CrystalRoostBlockEntity::new, ModBlocks.CRYSTAL_ROOST.get())
                .build(null));
}
