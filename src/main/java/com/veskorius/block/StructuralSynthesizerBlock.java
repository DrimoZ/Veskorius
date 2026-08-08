package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import com.veskorius.block.entity.StructuralSynthesizerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Machine de palier (05-Machines.md). Tout le comportement est dans {@link AbstractMachineBlock}. */
public class StructuralSynthesizerBlock extends AbstractMachineBlock {

    public static final MapCodec<StructuralSynthesizerBlock> CODEC = simpleCodec(StructuralSynthesizerBlock::new);

    public StructuralSynthesizerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<StructuralSynthesizerBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends AbstractMachineBlockEntity> getMachineType() {
        return ModBlockEntities.STRUCTURAL_SYNTHESIZER.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StructuralSynthesizerBlockEntity(pos, state);
    }
}
