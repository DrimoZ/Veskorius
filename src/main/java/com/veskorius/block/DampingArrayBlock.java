package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.DampingArrayBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Damping Array (06-Energy.md). Tout le comportement est dans {@link AbstractMachineBlock}. */
public class DampingArrayBlock extends AbstractMachineBlock {

    public static final MapCodec<DampingArrayBlock> CODEC = simpleCodec(DampingArrayBlock::new);

    public DampingArrayBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<DampingArrayBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends AbstractMachineBlockEntity> getMachineType() {
        return ModBlockEntities.DAMPING_ARRAY.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DampingArrayBlockEntity(pos, state);
    }
}
