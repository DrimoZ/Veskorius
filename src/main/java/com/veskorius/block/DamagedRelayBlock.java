package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.block.entity.DamagedRelayBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import com.veskorius.energy.IResonanceField;
import com.veskorius.energy.ResonanceFieldManager;
import com.veskorius.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Relais endommagé</b> du Sigma Laboratory. Clic droit avec un Resonance Component :
 * il repart pour 90 secondes et rediffuse la Résonance (voir
 * {@link DamagedRelayBlockEntity} pour la mécanique et la raison de sa forme).
 *
 * <p><b>La condition de réparation est ce qui fait le puzzle</b> : un relais mort ne se
 * ranime que s'il est <b>déjà couvert par un champ actif</b>. Rien à lire, rien à
 * deviner — on essaie, ça ne prend pas, on cherche d'où vient le courant. C'est la
 * « connaissance spatiale » du pilier 2 réduite à un geste.
 *
 * <p>Non minable et sans loot : il est du mobilier de structure. Récupérable, il donnerait
 * un relais T3 gratuit au milieu d'un donjon dont c'est précisément la récompense.
 */
public class DamagedRelayBlock extends AbstractOrientedBlock {

    public static final MapCodec<DamagedRelayBlock> CODEC = simpleCodec(DamagedRelayBlock::new);

    public DamagedRelayBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DamagedRelayBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DamagedRelayBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        return level.isClientSide ? null
            : createTickerHelper(type, ModBlockEntities.DAMAGED_RELAY.get(),
                DamagedRelayBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (!stack.is(ModItems.RESONANCE_COMPONENT.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof DamagedRelayBlockEntity relay)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (relay.isRunning()) {
            message(player, "block.veskorius.damaged_relay.already");
            return ItemInteractionResult.CONSUME;
        }
        // LA condition du puzzle : un relais rediffuse, il ne produit pas. Sans champ à
        // sa position, il n'a rien à rediffuser.
        IResonanceField source = ResonanceFieldManager.findSource((ServerLevel) level, pos);
        if (source == null) {
            message(player, "block.veskorius.damaged_relay.no_field");
            return ItemInteractionResult.CONSUME;
        }

        relay.repair();
        level.setBlock(pos, state.setValue(LIT, Boolean.TRUE), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.7f, 1.4f);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        message(player, "block.veskorius.damaged_relay.restored");
        return ItemInteractionResult.CONSUME;
    }

    private static void message(Player player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }
}
