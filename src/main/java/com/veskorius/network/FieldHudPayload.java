package com.veskorius.network;

import com.veskorius.Veskorius;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * État du champ de Résonance <b>où se tient le joueur</b>, poussé périodiquement aux
 * seuls joueurs qui portent l'objet de lecture (12-UX-and-Advancements.md, « HUD de
 * champ »). Le serveur n'envoie rien à ceux qui ne le portent pas, et rien du tout
 * hors champ : le HUD s'efface tout seul par péremption côté client.
 *
 * <p>{@code band} vaut {@link #NO_BAND} quand les harmoniques sont désactivées
 * (interrupteur maître) — le HUD se réduit alors à la réserve, ce qui reste vrai et
 * utile ; il n'affiche jamais une bande dans un monde qui n'en a pas.
 */
public record FieldHudPayload(int band, int reserve, int capacity,
                              int dissonance, int dissonanceCapacity,
                              boolean unstable) implements CustomPacketPayload {

    /** Aucune bande à afficher (harmoniques désactivées). */
    public static final int NO_BAND = -1;

    public static final Type<FieldHudPayload> TYPE =
        new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
            Veskorius.MOD_ID, "field_hud"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FieldHudPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FieldHudPayload::band,
            ByteBufCodecs.VAR_INT, FieldHudPayload::reserve,
            ByteBufCodecs.VAR_INT, FieldHudPayload::capacity,
            ByteBufCodecs.VAR_INT, FieldHudPayload::dissonance,
            ByteBufCodecs.VAR_INT, FieldHudPayload::dissonanceCapacity,
            ByteBufCodecs.BOOL, FieldHudPayload::unstable,
            FieldHudPayload::new);

    @Override
    public Type<FieldHudPayload> type() {
        return TYPE;
    }
}
