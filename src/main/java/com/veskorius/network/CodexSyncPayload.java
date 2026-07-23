package com.veskorius.network;

import com.veskorius.Veskorius;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Envoie au client la liste des entrées de Codex débloquées par le joueur
 * (15-Codex-Guidebook.md). L'état vit sur le joueur ({@code ModAttachments.CODEX_UNLOCKS}) ;
 * ce paquet le pousse au client à la connexion et à chaque nouveau déblocage, pour que le
 * {@code CodexScreen} le lise sans stocker quoi que ce soit sur l'objet.
 */
public record CodexSyncPayload(List<ResourceLocation> unlocked) implements CustomPacketPayload {

    public static final Type<CodexSyncPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "codex_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CodexSyncPayload> STREAM_CODEC =
        StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), CodexSyncPayload::unlocked,
            CodexSyncPayload::new);

    @Override
    public Type<CodexSyncPayload> type() {
        return TYPE;
    }
}
