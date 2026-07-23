package com.veskorius.event;

import com.veskorius.Veskorius;
import com.veskorius.codex.CodexUnlocks;
import com.veskorius.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Câble le Codex de Résonance à la progression du joueur (15-Codex-Guidebook.md).
 * L'état vit sur le joueur ({@code ModAttachments.CODEX_UNLOCKS}) ; ce handler ne fait
 * que déclencher les déblocages et pousser l'état au client. Toute la logique vit dans
 * {@link CodexUnlocks} (statique, testable).
 *
 * <ul>
 *   <li>connexion : Codex donné une fois (drapeau persistant), paliers déjà atteints
 *       reconstruits, puis état synchronisé au client ;</li>
 *   <li>respawn / changement de dimension : re-synchronisation (nouvelle instance
 *       client) ;</li>
 *   <li>advancement gagné : déblocage immédiat des entrées liées ;</li>
 *   <li>scan d'inventaire throttlé (1×/s) : déblocage des entrées {@code ITEM} et
 *       reconstruction des paliers — s'accumule même sans porter le Codex.</li>
 * </ul>
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID)
public final class CodexEventHandler {

    /** Clé du drapeau « Codex déjà donné », dans les données persistantes du joueur. */
    private static final String GRANTED_KEY = "veskorius:codex_granted";

    /** Intervalle du scan d'inventaire (ticks). 1×/s : négligeable. */
    private static final int SCAN_INTERVAL = 20;

    private CodexEventHandler() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        // Le Codex est donné une seule fois dans la vie du joueur.
        CompoundTag data = player.getPersistentData();
        CompoundTag persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.getBoolean(GRANTED_KEY)) {
            persisted.putBoolean(GRANTED_KEY, true);
            data.put(Player.PERSISTED_NBT_TAG, persisted);
            ItemStack codex = new ItemStack(ModItems.RESONANCE_CODEX.get());
            if (!player.getInventory().add(codex)) {
                player.drop(codex, false);
            }
        }
        // Rattrape les paliers déjà atteints (monde existant), puis synchronise.
        CodexUnlocks.grantForEarnedAdvancements(player);
        CodexUnlocks.syncTo(player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CodexUnlocks.syncTo(player);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CodexUnlocks.syncTo(player);
        }
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CodexUnlocks.grantForAdvancement(player, event.getAdvancement().id());
        }
    }

    /**
     * Comble un trou du scan périodique : crafter une machine puis la poser dans la
     * seconde retirerait l'objet de l'inventaire avant le prochain scan. Au craft,
     * l'objet est encore là — on scanne tout de suite.
     */
    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CodexUnlocks.grantForItem(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
            || player.tickCount % SCAN_INTERVAL != 0) {
            return;
        }
        CodexUnlocks.grantForItem(player);
        CodexUnlocks.grantForEarnedAdvancements(player);
    }
}
