package com.veskorius.client.entity;

import com.veskorius.entity.CustodeArchivisteEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;

/** Le modèle du Custode, appliqué à l'Archiviste. Voir la note du renderer. */
public class CustodeArchivisteModel extends HierarchicalModel<CustodeArchivisteEntity> {

    private final ModelPart root;

    public CustodeArchivisteModel(ModelPart root) {
        this.root = root;
    }

    @Override
    public void setupAnim(CustodeArchivisteEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Placeholder, comme le Custode : le vrai jeu d'animations viendra d'un coup.
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
