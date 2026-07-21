package com.veskorius.item;

import com.mojang.serialization.Codec;
import com.veskorius.Veskorius;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
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
}
