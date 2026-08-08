package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import com.veskorius.block.entity.ReclaimerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Machine de palier (05-Machines.md). Tout le comportement est dans {@link AbstractMachineBlock}. */
public class ReclaimerBlock extends AbstractMachineBlock {

    public static final MapCodec<ReclaimerBlock> CODEC = simpleCodec(ReclaimerBlock::new);

    public ReclaimerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ReclaimerBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends AbstractMachineBlockEntity> getMachineType() {
        return ModBlockEntities.RECLAIMER.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReclaimerBlockEntity(pos, state);
    }
}
