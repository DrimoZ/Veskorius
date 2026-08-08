package com.veskorius.client.entity;

import com.veskorius.Veskorius;
import com.veskorius.entity.CustodeArchivisteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * L'Archiviste reprend le <b>modèle du Custode</b> et n'en change que la texture et
 * l'échelle. C'est la lecture juste : un garde d'élite est le même peuple mieux équipé,
 * pas une autre créature. Lui donner un squelette propre aurait rompu ce lien de parenté
 * pour un gain nul — on le reconnaît de loin comme un Custode, et de près comme autre
 * chose.
 */
public class CustodeArchivisteRenderer extends MobRenderer<CustodeArchivisteEntity, CustodeArchivisteModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Veskorius.MOD_ID, "textures/entity/custode_archiviste.png");

    public CustodeArchivisteRenderer(EntityRendererProvider.Context context) {
        super(context, new CustodeArchivisteModel(context.bakeLayer(ModModelLayers.CUSTODE)), 0.6f);
    }

    @Override
    public ResourceLocation getTextureLocation(CustodeArchivisteEntity entity) {
        return TEXTURE;
    }
}
