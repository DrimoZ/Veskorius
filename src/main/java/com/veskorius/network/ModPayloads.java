package com.veskorius.network;

import com.veskorius.Veskorius;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Enregistre les paquets réseau du mod (bus MOD). Pour l'instant, uniquement la
 * synchronisation du Codex (serveur → client). Le handler délègue à
 * {@code ClientCodexData}, classe client uniquement : la lambda ne la charge qu'à
 * l'exécution (côté client), jamais sur un serveur dédié.
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModPayloads {

    private ModPayloads() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(CodexSyncPayload.TYPE, CodexSyncPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() ->
                com.veskorius.client.ClientCodexData.apply(payload.unlocked())));
    }
}
