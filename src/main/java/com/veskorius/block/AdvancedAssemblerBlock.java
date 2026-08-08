package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import com.veskorius.block.entity.AdvancedAssemblerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Machine de palier (05-Machines.md). Tout le comportement est dans {@link AbstractMachineBlock}. */
public class AdvancedAssemblerBlock extends AbstractMachineBlock {

    public static final MapCodec<AdvancedAssemblerBlock> CODEC = simpleCodec(AdvancedAssemblerBlock::new);

    public AdvancedAssemblerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<AdvancedAssemblerBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends AbstractMachineBlockEntity> getMachineType() {
        return ModBlockEntities.ADVANCED_ASSEMBLER.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedAssemblerBlockEntity(pos, state);
    }
}
