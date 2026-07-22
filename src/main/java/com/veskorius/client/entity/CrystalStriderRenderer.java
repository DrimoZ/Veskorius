package com.veskorius.client.entity;

import com.veskorius.Veskorius;
import com.veskorius.entity.CrystalStriderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CrystalStriderRenderer extends MobRenderer<CrystalStriderEntity, CrystalStriderModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Veskorius.MOD_ID, "textures/entity/crystal_strider.png");

    public CrystalStriderRenderer(EntityRendererProvider.Context context) {
        super(context, new CrystalStriderModel(context.bakeLayer(ModModelLayers.CRYSTAL_STRIDER)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(CrystalStriderEntity entity) {
        return TEXTURE;
    }
}
