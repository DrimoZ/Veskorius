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
        basicItem(ModItems.RESONANCE_DUST.get());
        basicItem(ModItems.RAW_FLUX_DEPOSIT.get());
        basicItem(ModItems.RESONANCE_CATALYST_CORE.get());
        basicItem(ModItems.RESONANCE_TUNER.get());
        basicItem(ModItems.RESONANCE_STORAGE_CELL.get());
        basicItem(ModItems.RESONANCE_LOCATOR.get());
        basicItem(ModItems.RESONANCE_SPORE.get());
        basicItem(ModItems.RESONANCE_SLUDGE.get());
        basicItem(ModItems.RESONANCE_BLUEPRINT.get());
        basicItem(ModItems.CODEX_FRAGMENT.get());
        basicItem(ModItems.FOSSILIZED_RATION.get());

        // Codex : texture propre plutôt que le livre vanilla. Le manuel signature du mod
        // ne devrait pas se confondre, dans une barre d'action, avec un livre
        // d'enchantement — c'est l'objet que le joueur cherchera le plus souvent.
        basicItem(ModItems.RESONANCE_CODEX.get());

        basicItem(ModItems.CUSTODE_ALLOY_FRAGMENT.get());

        // Œufs d'apparition : modèle vanilla template_spawn_egg (couleurs = item).
        withExistingParent("crystal_strider_spawn_egg", mcLoc("item/template_spawn_egg"));
        withExistingParent("custode_spawn_egg", mcLoc("item/template_spawn_egg"));

        // L'objet du Stabilizer reprend le modele de bloc. Il n'est pas genere par
        // le BlockStateProvider parce que horizontalBlock, contrairement a
        // simpleBlockWithItem, ne cree pas de modele d'objet.
        withExistingParent("resonance_stabilizer", modLoc("block/resonance_stabilizer"));
        withExistingParent("component_assembler", modLoc("block/component_assembler"));
        withExistingParent("flux_purifier", modLoc("block/flux_purifier"));
        withExistingParent("resonance_whetstone", modLoc("block/resonance_whetstone"));
        withExistingParent("field_emitter", modLoc("block/field_emitter"));
        withExistingParent("tunable_field_emitter", modLoc("block/tunable_field_emitter"));
        withExistingParent("crystal_crusher", modLoc("block/crystal_crusher"));
        withExistingParent("crystal_roost", modLoc("block/crystal_roost"));
        withExistingParent("damping_array", modLoc("block/damping_array"));
    }
}
