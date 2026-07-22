package com.veskorius.client.entity;

import com.veskorius.Veskorius;
import com.veskorius.entity.CustodeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CustodeRenderer extends MobRenderer<CustodeEntity, CustodeModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Veskorius.MOD_ID, "textures/entity/custode.png");

    public CustodeRenderer(EntityRendererProvider.Context context) {
        super(context, new CustodeModel(context.bakeLayer(ModModelLayers.CUSTODE)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(CustodeEntity entity) {
        return TEXTURE;
    }
}
