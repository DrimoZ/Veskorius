package com.veskorius.client.entity;

import com.veskorius.Veskorius;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/** Emplacements de layer de modèle des entités du mod. */
public final class ModModelLayers {

    private ModModelLayers() {
    }

    public static final ModelLayerLocation CRYSTAL_STRIDER = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "crystal_strider"), "main");

    public static final ModelLayerLocation RIFT_GUARDIAN = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "rift_guardian"), "main");

    public static final ModelLayerLocation CUSTODE = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "custode"), "main");
}
