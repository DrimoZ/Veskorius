package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.FieldEmitterBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import com.veskorius.block.entity.TunableFieldEmitterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Émetteur Accordable (06-Energy.md) : même bloc que le Field Emitter à tous égards
 * (carburant, réserve, GUI, coupole), à ceci près que sa <b>bande harmonique se
 * choisit</b> au Resonance Tuner. Hérite donc de {@link FieldEmitterBlock} — seuls le
 * codec et le type de block entity changent.
 */
public class TunableFieldEmitterBlock extends FieldEmitterBlock {

    public static final MapCodec<TunableFieldEmitterBlock> CODEC =
        simpleCodec(TunableFieldEmitterBlock::new);

    public TunableFieldEmitterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends FieldEmitterBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends FieldEmitterBlockEntity> emitterType() {
        return ModBlockEntities.TUNABLE_FIELD_EMITTER.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TunableFieldEmitterBlockEntity(pos, state);
    }
}
