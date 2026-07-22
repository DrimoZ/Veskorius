package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.CrystalRoostBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Machine #8 (05-Machines.md). Tout le comportement est dans {@link AbstractMachineBlock}. */
public class CrystalRoostBlock extends AbstractMachineBlock {

    public static final MapCodec<CrystalRoostBlock> CODEC = simpleCodec(CrystalRoostBlock::new);

    public CrystalRoostBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CrystalRoostBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends AbstractMachineBlockEntity> getMachineType() {
        return ModBlockEntities.CRYSTAL_ROOST.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalRoostBlockEntity(pos, state);
    }
}
