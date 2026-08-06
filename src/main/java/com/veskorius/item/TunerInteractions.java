package com.veskorius.item;

import com.veskorius.Veskorius;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.FieldEmitterBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        boolean dismantling = player.isShiftKeyDown();

        // N'intercepter que ce que le Tuner sait réellement traiter. Annuler
        // inconditionnellement rendait l'outil hostile : Tuner en main, on ne pouvait plus
        // ouvrir un coffre, un four ou une porte, ni poser un bloc — l'événement était
        // consommé avant même de regarder la cible.
        if (!isTunerTarget(level, pos, dismantling)) {
            return;
        }

        // Cible valide : on prend la main, ce qui empêche l'ouverture du GUI de la machine.
        // Des deux côtés ; le travail réel n'a lieu que côté serveur.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (level.isClientSide) {
            return;
        }

        if (dismantling) {
            dismantle(level, pos, player);
        } else {
            ResonanceTunerItem.applyMode(ResonanceTunerItem.modeOf(stack), level, pos, player);
        }
    }

    /**
     * Vrai si le Tuner doit prendre la main sur ce bloc.
     *
     * <p>En <b>démontage</b> (shift), la cible est n'importe quel bloc-entité, y compris
     * d'un autre mod — c'est la portée voulue par 12-UX. En <b>application de mode</b>, la
     * cible est une machine de Veskorius : tous les modes (Pivoter, On/Off, Surchauffe,
     * Redstone, Accorder) agissent sur nos block entities, et rien d'autre. Tout le reste
     * garde son interaction normale.
     */
    private static boolean isTunerTarget(Level level, BlockPos pos, boolean dismantling) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return false;
        }
        return dismantling
            || be instanceof AbstractMachineBlockEntity
            || be instanceof FieldEmitterBlockEntity;
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

        // Puis le bloc lui-même — via sa TABLE DE BUTIN, pas un `new ItemStack(block)`.
        // La différence n'est pas cosmétique : fabriquer l'objet à partir du bloc ignore
        // toute règle de butin et transforme le Tuner en clé universelle, y compris sur
        // les blocs-entités que le jeu ne rend jamais en survie (spawner, trial_spawner,
        // vault…). Passer par la table respecte les règles de chaque bloc, vanilla comme
        // moddé, et rend nos machines telles quelles. À faire AVANT le retrait : la table
        // a besoin de l'état et de la block entity.
        loot.addAll(dismantleDrops(level, pos, state, be, player));

        // Retire le bloc sans effet secondaire de drop (les inventaires sont vides).
        level.removeBlock(pos, false);

        for (ItemStack stack : loot) {
            ItemHandlerHelper.giveItemToPlayer(player, stack);
        }
        actionBar(player, Component.translatable("item.veskorius.resonance_tuner.dismantled"));
    }

    /**
     * Ce que le bloc lâche quand le Tuner le démonte, d'après sa <b>table de butin</b>.
     *
     * <p>L'outil passé au contexte est une pioche en netherite : le démontage est censé
     * être un dévissage propre, pas un coup de poing — sans quoi toutes nos machines
     * ({@code requiresCorrectToolForDrops}) ne rendraient rien, puisque le joueur tient un
     * Tuner. Aucun Silk Touch : on ne veut pas non plus transformer l'outil en pioche
     * enchantée universelle.
     */
    private static List<ItemStack> dismantleDrops(Level level, BlockPos pos, BlockState state,
                                                  BlockEntity be, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return List.of();
        }
        return Block.getDrops(state, serverLevel, pos, be, player,
            new ItemStack(Items.NETHERITE_PICKAXE));
    }

    /**
     * Récupère et vide le contenu d'un bloc-entité. Stratégies, dans l'ordre :
     * <ol>
     *   <li>nos machines : leur <b>inventaire interne complet</b> (tous les slots,
     *       augment compris) — surtout PAS leur capability sidée, qui est volontairement
     *       insert-only sur les entrées pour l'automatisation et ne rendrait donc rien ;</li>
     *   <li>autres blocs (autres mods, Field Emitter) : la capability ItemHandler ;</li>
     *   <li>à défaut, l'interface {@link Container} vanilla.</li>
     * </ol>
     * Statique et sans joueur pour être testable.
     */
    public static List<ItemStack> collectContents(Level level, BlockPos pos, BlockState state, BlockEntity be) {
        List<ItemStack> contents = new ArrayList<>();

        IItemHandler handler = be instanceof AbstractMachineBlockEntity machine
            ? machine.getInventory()
            : level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, be, null);

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
