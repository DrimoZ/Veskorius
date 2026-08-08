package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import com.veskorius.block.entity.DeepCrystalDrillerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Machine de palier (05-Machines.md). Tout le comportement est dans {@link AbstractMachineBlock}. */
public class DeepCrystalDrillerBlock extends AbstractMachineBlock {

    public static final MapCodec<DeepCrystalDrillerBlock> CODEC = simpleCodec(DeepCrystalDrillerBlock::new);

    public DeepCrystalDrillerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<DeepCrystalDrillerBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends AbstractMachineBlockEntity> getMachineType() {
        return ModBlockEntities.DEEP_CRYSTAL_DRILLER.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeepCrystalDrillerBlockEntity(pos, state);
    }
}
