package com.veskorius.client.entity;

import com.veskorius.Veskorius;
import com.veskorius.entity.CustodeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Rendu du Custode Lourd : <b>le rendu du Custode</b>, à l'échelle 1,1 et sur une texture
 * d'acier sombre.
 *
 * <p>Pas de modèle propre, et c'est délibéré. Le dossier en fait une variante du même
 * garde, pas une autre créature ; lui sculpter une silhouette différente dirait au joueur
 * qu'il affronte autre chose. Un cran de taille et une matière plus lourde suffisent à
 * signaler « le même, en plus dur » — ce qu'il faut comprendre avant d'engager un combat
 * à 60 PV avec les réflexes d'un combat à 30.
 *
 * <p>Il hérite de {@link CustodeRenderer} et reste donc typé sur {@link CustodeEntity} :
 * comme le Lourd EST un Custode, l'enregistrement l'accepte tel quel. Generifier
 * {@code CustodeModel} pour distinguer deux rendus qui partagent tout aurait ajouté un
 * paramètre de type à toute la chaîne pour exprimer une différence de texture.
 */
public class CustodeLourdRenderer extends CustodeRenderer {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Veskorius.MOD_ID, "textures/entity/custode_lourd.png");

    public CustodeLourdRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void scale(CustodeEntity entity, com.mojang.blaze3d.vertex.PoseStack poses,
                         float partialTick) {
        poses.scale(1.1f, 1.1f, 1.1f);
    }

    @Override
    public ResourceLocation getTextureLocation(CustodeEntity entity) {
        return TEXTURE;
    }
}
