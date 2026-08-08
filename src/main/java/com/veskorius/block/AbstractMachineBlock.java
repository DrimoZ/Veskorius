package com.veskorius.block;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
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
 * Socle commun aux blocs des machines actives.
 *
 * Regroupe tout ce qui serait identique sur les 23 machines : orientation
 * horizontale, ticker serveur, ouverture du GUI au clic droit, et vidage de
 * l'inventaire au sol quand le bloc est casse.
 *
 * FACING existe sur toutes les machines des maintenant : le Resonance Tuner
 * (Phase 1, tache 9) doit pouvoir faire pivoter n'importe quelle machine
 * orientee (12-UX-and-Advancements.md), et ajouter la propriete plus tard
 * casserait les blockstates deja poses en monde.
 *
 * Une sous-classe fournit trois choses : son {@code codec()}, son type de block
 * entity, et {@code newBlockEntity}.
 */
public abstract class AbstractMachineBlock extends AbstractOrientedBlock {

    // L'ORIENTATION ET L'ÉTAT ALLUMÉ VIENNENT DU SOCLE, désormais partagé avec les neuf
    // appareils qui ne sont pas des machines à cycle (Relais, Ancre, Évent, Amplificateur…).
    // Ces quarante lignes étaient recopiées dix fois : voir AbstractOrientedBlock pour ce
    // que ça coûtait vraiment — une correction d'orientation à appliquer dix fois, dont
    // l'oubli ne se voyait que sur le bloc oublié, posé de travers dans une seule ruine.
    //
    // FACING et LIT restent accessibles sous AbstractMachineBlock.FACING / .LIT : un champ
    // statique hérité se lit par la sous-classe, et les blockstates n'y voient rien.

    protected AbstractMachineBlock(Properties properties) {
        super(properties);
    }

    /** Type de block entity de cette machine, utilise pour brancher le ticker. */
    protected abstract BlockEntityType<? extends AbstractMachineBlockEntity> getMachineType();

    // --- Ticker --------------------------------------------------------------

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, getMachineType(), AbstractMachineBlockEntity::serverTick);
    }

    // --- Interaction ---------------------------------------------------------

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof MenuProvider provider) {
            // La surcharge NeoForge (IPlayerExtension) ecrit la BlockPos dans le
            // paquet d'ouverture ; le menu la relit cote client pour retrouver sa
            // block entity.
            player.openMenu(provider, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
            && level.getBlockEntity(pos) instanceof AbstractMachineBlockEntity machine) {
            machine.dropContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
