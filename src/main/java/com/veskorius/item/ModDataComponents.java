package com.veskorius.item;

import com.mojang.serialization.Codec;
import com.veskorius.Veskorius;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {

    public static final DeferredRegister.DataComponents COMPONENTS =
        DeferredRegister.createDataComponents(Veskorius.MOD_ID);

    /**
     * Mode courant d'un Resonance Tuner, stocké sur l'item (ordinal de
     * {@link TunerMode}). Persistant (survit à la sauvegarde) et synchronisé
     * réseau (le tooltip affiche le mode côté client).
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TUNER_MODE =
        COMPONENTS.registerComponentType("tuner_mode",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));

    /**
     * Charge en Osc d'une Resonance Storage Cell, stockée sur l'item (06-Energy.md,
     * « Osc portable »). Persistant (survit à la sauvegarde) et synchronisé réseau
     * pour que le tooltip et la barre de charge s'affichent côté client.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STORAGE_CELL_CHARGE =
        COMPONENTS.registerComponentType("storage_cell_charge",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));

    /**
     * Tier d'un {@code resonance_blueprint} (2, 3, 4). Détermine quelles recettes de
     * machine l'acceptent comme clé (voir 03-Progression.md, gatekeeping physique).
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BLUEPRINT_TIER =
        COMPONENTS.registerComponentType("blueprint_tier",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));

    /**
     * Entrée de Codex portée par un {@code codex_fragment} : l'id de l'entrée de lore
     * à afficher à la lecture (08-Structures.md). Pur lore, ne débloque rien.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> CODEX_ENTRY =
        COMPONENTS.registerComponentType("codex_entry",
            builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC));
}
