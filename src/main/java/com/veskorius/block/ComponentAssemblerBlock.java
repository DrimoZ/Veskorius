package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ComponentAssemblerBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Machine #2 (05-Machines.md). Tout le comportement est dans {@link AbstractMachineBlock}. */
public class ComponentAssemblerBlock extends AbstractMachineBlock {

    public static final MapCodec<ComponentAssemblerBlock> CODEC =
        simpleCodec(ComponentAssemblerBlock::new);

    public ComponentAssemblerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ComponentAssemblerBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends AbstractMachineBlockEntity> getMachineType() {
        return ModBlockEntities.COMPONENT_ASSEMBLER.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ComponentAssemblerBlockEntity(pos, state);
    }
}
