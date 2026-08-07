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

        // Les objets de machine ne sont plus listés ici. Cette liste était à recopier à
        // chaque machine ajoutée, et rien ne signalait l'oubli : la machine se posait, se
        // texturait et fonctionnait, seul son objet apparaissait en cube violet. Elle est
        // désormais produite par ModBlockStateProvider.oriented(), qui est appelé pour
        // toute machine par construction — voir la note qui y est portée.
    }
}
