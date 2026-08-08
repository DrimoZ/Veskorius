package com.veskorius.client;

import com.veskorius.Veskorius;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * <b>Vide les caches client à la déconnexion.</b>
 *
 * <p>Deux ensembles statiques vivent côté client : l'état de déblocage du Codex et la
 * dernière mesure de champ du HUD. Tous deux sont propres au monde qu'on vient de quitter,
 * et tous deux survivaient au retour au menu principal.
 *
 * <p><b>Le Codex était le cas nuisible</b>, et sa propre javadoc le disait déjà : « sans ça,
 * rejoindre un second monde comparerait les nouvelles entrées à celles du monde précédent —
 * les pages communes passeraient pour déjà connues et ne s'annonceraient jamais ». La
 * méthode {@code reset()} existait, documentée, correcte… et appelée par personne. Une
 * panne parfaitement silencieuse : sur un second monde, le manuel cesse simplement de
 * signaler ses nouvelles pages, et rien n'indique pourquoi.
 *
 * <p>Le HUD est moins grave — la première mesure du nouveau monde l'écrase — mais il
 * afficherait la valeur de l'ancien monde pendant une fraction de seconde au retour en jeu,
 * et il n'y a aucune raison de garder ça.
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID, value = Dist.CLIENT)
public final class ClientDisconnectHandler {

    private ClientDisconnectHandler() {
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientCodexData.reset();
        ClientFieldData.clear();
    }
}
