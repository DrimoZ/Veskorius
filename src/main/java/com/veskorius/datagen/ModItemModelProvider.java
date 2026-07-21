package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Veskorius.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.RAW_RESONANCE_CRYSTAL.get());
        basicItem(ModItems.STABLE_RESONANCE_CRYSTAL.get());
        basicItem(ModItems.REFINED_RESONANCE_CRYSTAL.get());
        basicItem(ModItems.RESONANCE_COMPONENT.get());

        // L'objet du Stabilizer reprend le modele de bloc. Il n'est pas genere par
        // le BlockStateProvider parce que horizontalBlock, contrairement a
        // simpleBlockWithItem, ne cree pas de modele d'objet.
        withExistingParent("resonance_stabilizer", modLoc("block/resonance_stabilizer"));
        withExistingParent("resonance_whetstone", modLoc("block/resonance_whetstone"));
        withExistingParent("field_emitter", modLoc("block/field_emitter"));
    }
}
