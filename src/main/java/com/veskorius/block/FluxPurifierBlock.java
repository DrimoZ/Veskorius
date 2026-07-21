package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.FluxPurifierBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Machine #5 (05-Machines.md). Tout le comportement est dans {@link AbstractMachineBlock}. */
public class FluxPurifierBlock extends AbstractMachineBlock {

    public static final MapCodec<FluxPurifierBlock> CODEC = simpleCodec(FluxPurifierBlock::new);

    public FluxPurifierBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<FluxPurifierBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends AbstractMachineBlockEntity> getMachineType() {
        return ModBlockEntities.FLUX_PURIFIER.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluxPurifierBlockEntity(pos, state);
    }
}
