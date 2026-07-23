package com.veskorius.event;

import com.veskorius.Veskorius;
import com.veskorius.compat.curios.CuriosCompat;
import com.veskorius.config.HarmonicsConfig;
import com.veskorius.energy.IResonanceField;
import com.veskorius.energy.ResonanceFieldManager;
import com.veskorius.item.ModItems;
import com.veskorius.network.FieldHudPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * HUD de champ (12-UX-and-Advancements.md) : pousse périodiquement au joueur l'état du
 * champ <b>où il se tient</b> — bande, réserve, dissonance. Rend lisible ce que le
 * pilier 3 rend invisible (un réseau sans câbles ne se lit pas sur les blocs posés).
 *
 * <p>Trois économies délibérées :
 * <ul>
 *   <li>on n'envoie qu'aux <b>porteurs</b> de l'objet de lecture — un serveur dont
 *       personne ne le porte n'émet pas un paquet ;</li>
 *   <li>on n'envoie <b>rien hors champ</b> : le HUD s'efface par péremption côté client,
 *       ce qui évite un paquet « rien à signaler » deux fois par seconde et par joueur ;</li>
 *   <li>la lecture se fait sur l'index O(n) des émetteurs, jamais par un scan de blocs.</li>
 * </ul>
 *
 * <p>L'objet de lecture est le <b>Resonance Locator</b> : le dossier laissait le choix
 * entre lui et un « Attunement Lens » dédié (12) ; c'est lui qui est retenu — l'outil de
 * détection de résonance existe déjà, se recharge déjà dans le champ, et n'ajouter aucun
 * item garde la progression inchangée.
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID)
public final class FieldHudHandler {

    private FieldHudHandler() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!HarmonicsConfig.hudEnabled()) {
            return;
        }
        long time = event.getServer().getTickCount();
        if (time % HarmonicsConfig.hudUpdateInterval() != 0) {
            return;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!carriesReader(player)) {
                continue;
            }
            FieldHudPayload payload = read(player);
            if (payload != null) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    /**
     * Lecture du champ à la position du joueur, ou {@code null} s'il n'en est couvert par
     * aucun. On prend le champ <b>couvrant</b> (et non le champ actif) : un émetteur à sec
     * ou instable doit rester affiché — c'est précisément l'instant où le joueur a besoin
     * de le voir.
     */
    @Nullable
    private static FieldHudPayload read(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return null;
        }
        IResonanceField field = ResonanceFieldManager.coveringSource(level, player.blockPosition());
        return field == null ? null : of(field);
    }

    /** Lecture d'un champ donné, sous forme de paquet. Exposé aux GameTest. */
    public static FieldHudPayload of(IResonanceField field) {
        boolean harmonics = HarmonicsConfig.enabled();
        return new FieldHudPayload(
            harmonics ? field.getBand().ordinal() : FieldHudPayload.NO_BAND,
            field.getReserve(),
            field.getCapacity(),
            harmonics ? field.getDissonance() : 0,
            harmonics ? HarmonicsConfig.dissonanceCapacity() : 0,
            field.isUnstable());
    }

    /**
     * Vrai si le joueur porte l'objet de lecture : dans son inventaire, ou dans un slot
     * Curios si ce mod est présent (dépendance douce, 10-Mod-Integrations.md — le
     * comportement sans Curios est complet). Exposé aux GameTest.
     */
    public static boolean carriesReader(Player player) {
        Item reader = ModItems.RESONANCE_LOCATOR.get();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.is(reader)) {
                return true;
            }
        }
        return CuriosCompat.isEquipped(player, reader);
    }
}
