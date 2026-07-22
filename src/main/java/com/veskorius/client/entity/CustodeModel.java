package com.veskorius.client.entity;

import com.veskorius.entity.CustodeEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Modèle placeholder du Custode : silhouette bipède debout (corps + tête + 2 jambes
 * + 2 bras). Volontairement simple — vrai modèle à la passe d'assets (Phase 6).
 */
public class CustodeModel extends HierarchicalModel<CustodeEntity> {

    private final ModelPart root;

    public CustodeModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        parts.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -12.0f, -2.0f, 8.0f, 12.0f, 4.0f),
            PartPose.offset(0.0f, 12.0f, 0.0f));
        parts.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(24, 0).addBox(-3.0f, -6.0f, -3.0f, 6.0f, 6.0f, 6.0f),
            PartPose.offset(0.0f, 0.0f, 0.0f));

        CubeListBuilder arm = CubeListBuilder.create().texOffs(0, 16).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f);
        parts.addOrReplaceChild("arm_l", arm, PartPose.offset(-5.5f, 1.0f, 0.0f));
        parts.addOrReplaceChild("arm_r", arm, PartPose.offset(5.5f, 1.0f, 0.0f));

        CubeListBuilder leg = CubeListBuilder.create().texOffs(16, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f);
        parts.addOrReplaceChild("leg_l", leg, PartPose.offset(-2.0f, 12.0f, 0.0f));
        parts.addOrReplaceChild("leg_r", leg, PartPose.offset(2.0f, 12.0f, 0.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(CustodeEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Placeholder : pas d'animation.
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
