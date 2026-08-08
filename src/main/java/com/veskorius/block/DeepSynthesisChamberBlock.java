package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import com.veskorius.block.entity.DeepSynthesisChamberBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Machine de palier (05-Machines.md). Tout le comportement est dans {@link AbstractMachineBlock}. */
public class DeepSynthesisChamberBlock extends AbstractMachineBlock {

    public static final MapCodec<DeepSynthesisChamberBlock> CODEC = simpleCodec(DeepSynthesisChamberBlock::new);

    public DeepSynthesisChamberBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<DeepSynthesisChamberBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends AbstractMachineBlockEntity> getMachineType() {
        return ModBlockEntities.DEEP_SYNTHESIS_CHAMBER.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeepSynthesisChamberBlockEntity(pos, state);
    }
}
