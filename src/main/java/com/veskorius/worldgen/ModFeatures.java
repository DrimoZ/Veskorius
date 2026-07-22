package com.veskorius.worldgen;

import com.veskorius.Veskorius;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
        DeferredRegister.create(Registries.FEATURE, Veskorius.MOD_ID);

    public static final DeferredHolder<Feature<?>, ResonanceCrystalPocketFeature> CRYSTAL_POCKET =
        FEATURES.register("crystal_pocket", ResonanceCrystalPocketFeature::new);

    /** Petites ruines (Habitation Modeste, Avant-poste) — voir {@link RuinFeature}. */
    public static final DeferredHolder<Feature<?>, RuinFeature> RUIN =
        FEATURES.register("ruin", RuinFeature::new);
}
