package com.veskorius.codex;

import com.veskorius.network.CodexSyncPayload;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Logique de déblocage du Codex (15-Codex-Guidebook.md). L'état vit <b>sur le joueur</b>
 * ({@link ModAttachments#CODEX_UNLOCKS}), pas sur l'objet : la connaissance s'accumule
 * même quand le Codex n'est pas porté, et survit à la mort. Ces méthodes sont statiques
 * et sans dépendance à un événement pour rester testables.
 *
 * Les entrées {@code ALWAYS} ne sont jamais stockées : elles comptent débloquées d'office.
 * Tout changement est immédiatement synchronisé au client ({@link #syncTo(Player)}).
 */
public final class CodexUnlocks {

    private CodexUnlocks() {
    }

    // --- Lecture / écriture de l'état sur le joueur --------------------------

    public static Set<ResourceLocation> unlocked(Player player) {
        return player.getData(ModAttachments.CODEX_UNLOCKS.get());
    }

    public static boolean isUnlocked(Player player, CodexEntry entry) {
        return entry.unlock().type() == CodexUnlock.Type.ALWAYS
            || unlocked(player).contains(entry.id());
    }

    public static boolean isUnlocked(Player player, ResourceLocation entryId) {
        CodexEntry entry = CodexRegistry.get(entryId);
        return entry != null && isUnlocked(player, entry);
    }

    public static int unlockedCount(Player player, CodexCategory category) {
        int count = 0;
        for (CodexEntry entry : CodexRegistry.byCategory(category)) {
            if (isUnlocked(player, entry)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Marque une entrée débloquée pour ce joueur et synchronise. Retourne vrai si
     * c'était nouveau (pour n'émettre le retour « nouvelle entrée » qu'une fois).
     * Idempotent.
     */
    public static boolean unlock(Player player, ResourceLocation entryId) {
        Set<ResourceLocation> current = unlocked(player);
        if (current.contains(entryId)) {
            return false;
        }
        Set<ResourceLocation> next = new LinkedHashSet<>(current);
        next.add(entryId);
        player.setData(ModAttachments.CODEX_UNLOCKS.get(), next);
        syncTo(player);
        return true;
    }

    /** Pousse l'état complet au client propriétaire (no-op hors ServerPlayer connecté). */
    public static void syncTo(Player player) {
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
            PacketDistributor.sendToPlayer(serverPlayer,
                new CodexSyncPayload(List.copyOf(unlocked(player))));
        }
    }

    // --- Déclencheurs ---------------------------------------------------------

    public static boolean playerHas(Player player, ItemLike item) {
        Item target = item.asItem();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Débloque toutes les entrées {@code ITEM} dont le joueur possède l'objet. Ne
     * requiert PAS de porter le Codex : la connaissance s'ajoute de toute façon.
     */
    public static void grantForItem(Player player) {
        for (CodexEntry entry : CodexRegistry.all()) {
            CodexUnlock unlock = entry.unlock();
            if (unlock.type() == CodexUnlock.Type.ITEM && unlock.item() != null
                && playerHas(player, unlock.item()) && unlock(player, entry.id())) {
                notifyNewEntry(player, entry);
            }
        }
    }

    /** Débloque les entrées {@code ADVANCEMENT} liées à l'advancement gagné. */
    public static void grantForAdvancement(Player player, ResourceLocation advancementId) {
        for (CodexEntry entry : CodexRegistry.all()) {
            CodexUnlock unlock = entry.unlock();
            if (unlock.type() == CodexUnlock.Type.ADVANCEMENT
                && advancementId.equals(unlock.advancement())
                && unlock(player, entry.id())) {
                notifyNewEntry(player, entry);
            }
        }
    }

    /**
     * Reconstruit les entrées {@code ADVANCEMENT} à partir des advancements réellement
     * possédés. Rend le système auto-réparant : un monde existant (advancements déjà
     * gagnés avant le Codex) ou un joueur revenu se re-remplit de ses paliers.
     */
    public static void grantForEarnedAdvancements(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        for (CodexEntry entry : CodexRegistry.all()) {
            CodexUnlock unlock = entry.unlock();
            if (unlock.type() != CodexUnlock.Type.ADVANCEMENT || unlock.advancement() == null) {
                continue;
            }
            AdvancementHolder holder = server.getAdvancements().get(unlock.advancement());
            if (holder != null
                && player.getAdvancements().getOrStartProgress(holder).isDone()
                && unlock(player, entry.id())) {
                notifyNewEntry(player, entry);
            }
        }
    }

    /** Débloque l'entrée de lore {@code FRAGMENT} correspondant au fragment lu. */
    public static void grantForFragment(Player player, ResourceLocation entryId) {
        CodexEntry entry = CodexRegistry.get(entryId);
        if (entry != null && entry.unlock().type() == CodexUnlock.Type.FRAGMENT
            && unlock(player, entry.id())) {
            notifyNewEntry(player, entry);
        }
    }

    private static void notifyNewEntry(Player player, CodexEntry entry) {
        player.displayClientMessage(Component.translatable("gui.veskorius.codex.new_entry",
            Component.translatable(entry.titleKey())), true);
        player.level().playSound(null, player.blockPosition(),
            SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.6f, 1.0f);
    }

    /** Le premier {@code resonance_codex} de l'inventaire, ou {@link ItemStack#EMPTY}. */
    public static ItemStack findCodex(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(com.veskorius.item.ModItems.RESONANCE_CODEX.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
