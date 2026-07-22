package com.veskorius.client.entity;

import com.veskorius.entity.CrystalStriderEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Modèle placeholder du Fileur de Cristal : petit quadrupède (corps + tête + 4
 * pattes). Volontairement simple — le vrai modèle viendra à la passe d'assets
 * (Phase 6). La texture unie masque l'absence d'UV détaillé.
 */
public class CrystalStriderModel extends HierarchicalModel<CrystalStriderEntity> {

    private final ModelPart root;

    public CrystalStriderModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        parts.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -4.0f, -5.0f, 6.0f, 5.0f, 10.0f),
            PartPose.offset(0.0f, 19.0f, 0.0f));
        parts.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 16).addBox(-2.5f, -2.5f, -4.0f, 5.0f, 5.0f, 4.0f),
            PartPose.offset(0.0f, 16.0f, -5.0f));

        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 26).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f);
        parts.addOrReplaceChild("leg_fl", leg, PartPose.offset(-2.0f, 19.0f, -3.0f));
        parts.addOrReplaceChild("leg_fr", leg, PartPose.offset(2.0f, 19.0f, -3.0f));
        parts.addOrReplaceChild("leg_bl", leg, PartPose.offset(-2.0f, 19.0f, 3.0f));
        parts.addOrReplaceChild("leg_br", leg, PartPose.offset(2.0f, 19.0f, 3.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(CrystalStriderEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Placeholder : pas d'animation pour l'instant.
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
