package com.veskorius.item;

import com.veskorius.Veskorius;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Interactions du Resonance Tuner sur un bloc, via
 * {@code PlayerInteractEvent.RightClickBlock}.
 *
 * Pourquoi un événement plutôt que {@code Item.useOn} : sur un clic droit SANS
 * shift, Minecraft essaie d'abord l'interaction du BLOC (qui, pour une machine,
 * ouvre le GUI). Si le bloc consomme le clic, l'item n'est jamais consulté. En
 * s'abonnant à cet événement — qui se déclenche AVANT la résolution bloc/item — le
 * Tuner intercepte et annule l'ouverture du GUI.
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class TunerInteractions {

    private TunerInteractions() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ResonanceTunerItem)) {
            return;
        }

        // Empêche l'ouverture du GUI de la machine (et toute autre interaction du
        // bloc), des deux côtés. Le travail réel n'a lieu que côté serveur.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        Level level = event.getLevel();
        if (level.isClientSide) {
            return;
        }

        Player player = event.getEntity();
        BlockPos pos = event.getPos();

        if (player.isShiftKeyDown()) {
            dismantle(level, pos, player);
        } else {
            ResonanceTunerItem.applyMode(ResonanceTunerItem.modeOf(stack), level, pos, player);
        }
    }

    // --- Démontage -----------------------------------------------------------

    /**
     * Démonte le bloc-entité en {@code pos} : rend le bloc et tout son contenu au
     * joueur (priorité à l'inventaire, sol en dernier recours). Valable sur
     * n'importe quel bloc doté d'une block entity, y compris d'autres mods.
     */
    public static void dismantle(Level level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);

        // Seulement les blocs-entités, et jamais un bloc incassable (bedrock, etc.).
        if (be == null || state.getDestroySpeed(level, pos) < 0) {
            return;
        }

        List<ItemStack> loot = new ArrayList<>();

        // Le contenu D'ABORD : on vide les inventaires avant de retirer le bloc,
        // pour que le onRemove de la machine ne les redépose pas au sol.
        loot.addAll(collectContents(level, pos, state, be));

        // Puis le bloc lui-même, sous sa forme d'objet.
        ItemStack blockItem = new ItemStack(state.getBlock());
        if (!blockItem.isEmpty()) {
            loot.add(blockItem);
        }

        // Retire le bloc sans effet secondaire de drop (les inventaires sont vides).
        level.removeBlock(pos, false);

        for (ItemStack stack : loot) {
            ItemHandlerHelper.giveItemToPlayer(player, stack);
        }
        actionBar(player, Component.translatable("item.veskorius.resonance_tuner.dismantled"));
    }

    /**
     * Récupère et vide le contenu d'un bloc-entité. Trois stratégies, dans l'ordre :
     * la capability ItemHandler (autres mods + Field Emitter), l'inventaire direct
     * des machines du mod (qui n'exposent pas la capability), enfin l'interface
     * {@link Container} vanilla. Statique et sans joueur pour être testable.
     */
    public static List<ItemStack> collectContents(Level level, BlockPos pos, BlockState state, BlockEntity be) {
        List<ItemStack> contents = new ArrayList<>();

        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, be, null);
        if (handler == null && be instanceof AbstractMachineBlockEntity machine) {
            handler = machine.getInventory();
        }

        if (handler != null) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack extracted = handler.extractItem(slot, Integer.MAX_VALUE, false);
                if (!extracted.isEmpty()) {
                    contents.add(extracted);
                }
            }
        } else if (be instanceof Container container) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack removed = container.removeItemNoUpdate(slot);
                if (!removed.isEmpty()) {
                    contents.add(removed);
                }
            }
        }
        return contents;
    }

    private static void actionBar(Player player, Component message) {
        player.displayClientMessage(message, true);
    }
}
