package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import com.veskorius.block.entity.ResonanceWhetstoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Machine #3 (05-Machines.md). Tout le comportement est dans {@link AbstractMachineBlock}. */
public class ResonanceWhetstoneBlock extends AbstractMachineBlock {

    public static final MapCodec<ResonanceWhetstoneBlock> CODEC =
        simpleCodec(ResonanceWhetstoneBlock::new);

    public ResonanceWhetstoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ResonanceWhetstoneBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends AbstractMachineBlockEntity> getMachineType() {
        return ModBlockEntities.RESONANCE_WHETSTONE.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonanceWhetstoneBlockEntity(pos, state);
    }
}
