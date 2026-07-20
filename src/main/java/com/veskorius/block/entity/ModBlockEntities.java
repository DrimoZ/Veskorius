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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonanceWhetstoneBlockEntity>>
        RESONANCE_WHETSTONE = BLOCK_ENTITIES.register("resonance_whetstone",
            () -> BlockEntityType.Builder
                .of(ResonanceWhetstoneBlockEntity::new, ModBlocks.RESONANCE_WHETSTONE.get())
                .build(null));
}
