package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.ModBlockEntities;
import com.veskorius.block.entity.RiftCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Le noyau d'une Faille. <b>Indestructible et sans objet</b>, comme le sas et l'émetteur
 * ancien : on ne prend pas une Faille dans sa poche.
 *
 * <p>C'est la seule chose du mod qu'on ne peut ni fabriquer, ni miner, ni déplacer — on ne
 * peut que la stabiliser et l'exploiter là où le monde l'a mise. La ressource finale du jeu
 * en dépend, et c'est ce qui la rend finie (05-Machines.md : « Rejeté : Rift Essence
 * renouvelable — romprait la seule vraie ressource finie du mod »).
 */
public class RiftCoreBlock extends BaseEntityBlock {

    public static final MapCodec<RiftCoreBlock> CODEC = simpleCodec(RiftCoreBlock::new);

    public RiftCoreBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<RiftCoreBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RiftCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.RIFT_CORE.get(), RiftCoreBlockEntity::serverTick);
    }
}
