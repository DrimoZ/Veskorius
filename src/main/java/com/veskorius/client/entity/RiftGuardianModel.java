package com.veskorius.client.entity;

import com.veskorius.entity.RiftGuardianEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Modèle du Gardien de Faille : même grammaire bipède que le Custode — c'est le même
 * peuple, un âge plus tard — mais <b>une fois et demie plus grand</b>, épaules larges,
 * tête enfoncée dans le torse.
 *
 * <p>La silhouette est ce qui doit dire « ce n'est pas un garde » avant toute animation :
 * un Custode se contourne, celui-ci barre la salle. Il n'a pas de bras baissés mais des
 * <b>épaulières</b>, pour qu'on ne le lise pas comme un Custode agrandi.
 */
public class RiftGuardianModel extends HierarchicalModel<RiftGuardianEntity> {

    private final ModelPart root;
    private final ModelPart head;

    public RiftGuardianModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        parts.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 0).addBox(-7.0f, -20.0f, -4.0f, 14.0f, 20.0f, 8.0f),
            PartPose.offset(0.0f, 12.0f, 0.0f));
        // Tête enfoncée : elle démarre SOUS le haut du torse, d'où l'allure voûtée.
        parts.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 28).addBox(-4.0f, -7.0f, -4.0f, 8.0f, 7.0f, 8.0f),
            PartPose.offset(0.0f, -6.0f, 0.0f));

        CubeListBuilder pauldron =
            CubeListBuilder.create().texOffs(32, 28).addBox(-3.0f, -2.0f, -3.0f, 6.0f, 6.0f, 6.0f);
        parts.addOrReplaceChild("pauldron_l", pauldron, PartPose.offset(-9.0f, -6.0f, 0.0f));
        parts.addOrReplaceChild("pauldron_r", pauldron, PartPose.offset(9.0f, -6.0f, 0.0f));

        CubeListBuilder arm =
            CubeListBuilder.create().texOffs(0, 44).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 18.0f, 5.0f);
        parts.addOrReplaceChild("arm_l", arm, PartPose.offset(-9.0f, -2.0f, 0.0f));
        parts.addOrReplaceChild("arm_r", arm, PartPose.offset(9.0f, -2.0f, 0.0f));

        CubeListBuilder leg =
            CubeListBuilder.create().texOffs(24, 44).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 14.0f, 6.0f);
        parts.addOrReplaceChild("leg_l", leg, PartPose.offset(-3.5f, 12.0f, 0.0f));
        parts.addOrReplaceChild("leg_r", leg, PartPose.offset(3.5f, 12.0f, 0.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(RiftGuardianEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // La tête suit le joueur : c'est la seule animation, et c'est celle qui compte —
        // un boss qui ne regarde pas sa cible ne menace personne.
        head.yRot = netHeadYaw * ((float) Math.PI / 180f);
        head.xRot = headPitch * ((float) Math.PI / 180f);
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
