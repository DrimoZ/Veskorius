package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import com.veskorius.block.entity.VeskorianAlloyForgeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Machine #10 (05-Machines.md). Tout le comportement est dans {@link AbstractMachineBlock}. */
public class VeskorianAlloyForgeBlock extends AbstractMachineBlock {

    public static final MapCodec<VeskorianAlloyForgeBlock> CODEC =
        simpleCodec(VeskorianAlloyForgeBlock::new);

    public VeskorianAlloyForgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<VeskorianAlloyForgeBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends AbstractMachineBlockEntity> getMachineType() {
        return ModBlockEntities.VESKORIAN_ALLOY_FORGE.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VeskorianAlloyForgeBlockEntity(pos, state);
    }
}
