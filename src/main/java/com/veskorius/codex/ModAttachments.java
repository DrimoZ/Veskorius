package com.veskorius.codex;

import com.mojang.serialization.Codec;
import com.veskorius.Veskorius;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Attachments du mod. Le déblocage du Codex (15-Codex-Guidebook.md) est stocké
 * <b>sur le joueur</b>, pas sur l'objet : la connaissance s'accumule même quand le
 * Codex n'est pas en main (dans un coffre, par exemple), et <b>survit à la mort</b>
 * ({@code copyOnDeath}). L'objet Codex n'est qu'une clé d'ouverture. L'état est
 * synchronisé au client par paquet (voir {@code CodexSyncPayload}) pour que le GUI le
 * lise.
 */
public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Veskorius.MOD_ID);

    /** Codec liste ⇄ ensemble ordonné (l'ordre d'insertion = ordre de découverte). */
    private static final Codec<Set<ResourceLocation>> SET_CODEC =
        ResourceLocation.CODEC.listOf().xmap(
            list -> new LinkedHashSet<>(list),
            List::copyOf);

    /** Ids des entrées de Codex débloquées par ce joueur. */
    public static final Supplier<AttachmentType<Set<ResourceLocation>>> CODEX_UNLOCKS =
        ATTACHMENT_TYPES.register("codex_unlocks", () ->
            AttachmentType.<Set<ResourceLocation>>builder(() -> new LinkedHashSet<>())
                .serialize(SET_CODEC)
                .copyOnDeath()
                .build());

    private ModAttachments() {
    }
}
