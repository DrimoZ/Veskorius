package com.veskorius.client.entity;

import com.veskorius.Veskorius;
import com.veskorius.entity.RiftGuardianEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RiftGuardianRenderer extends MobRenderer<RiftGuardianEntity, RiftGuardianModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Veskorius.MOD_ID, "textures/entity/rift_guardian.png");

    public RiftGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new RiftGuardianModel(context.bakeLayer(ModModelLayers.RIFT_GUARDIAN)), 1.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(RiftGuardianEntity entity) {
        return TEXTURE;
    }
}
