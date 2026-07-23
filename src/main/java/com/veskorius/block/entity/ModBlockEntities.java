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
                .of(FieldEmitterBlockEntity::new, ModBlocks.FIELD_EMITTER.get())
                .build(null));

    /** Émetteur Accordable : même émetteur, bande harmonique choisie (06-Energy.md). */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TunableFieldEmitterBlockEntity>>
        TUNABLE_FIELD_EMITTER = BLOCK_ENTITIES.register("tunable_field_emitter",
            () -> BlockEntityType.Builder
                .of(TunableFieldEmitterBlockEntity::new, ModBlocks.TUNABLE_FIELD_EMITTER.get())
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
