package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.CrystalCrusherBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Machine #22 (05-Machines.md). Tout le comportement est dans {@link AbstractMachineBlock}. */
public class CrystalCrusherBlock extends AbstractMachineBlock {

    public static final MapCodec<CrystalCrusherBlock> CODEC =
        simpleCodec(CrystalCrusherBlock::new);

    public CrystalCrusherBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CrystalCrusherBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends AbstractMachineBlockEntity> getMachineType() {
        return ModBlockEntities.CRYSTAL_CRUSHER.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalCrusherBlockEntity(pos, state);
    }
}
