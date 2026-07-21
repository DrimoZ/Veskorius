package com.veskorius.datagen;

import com.veskorius.block.ModBlocks;
import com.veskorius.item.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // Recette de construction du Resonance Stabilizer : 4 Cobblestone +
        // 2 Copper Ingot + 1 Raw Resonance Crystal (05-Machines.md, tableau
        // "Recettes de construction"). Le dossier de conception fixe les
        // quantites mais pas la disposition — la forme ci-dessous place le
        // cristal au centre, encadre verticalement par le cuivre, la pierre
        // formant la structure. Si une disposition canonique est decidee plus
        // tard, c'est 05-Machines.md qu'il faut modifier d'abord.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.RESONANCE_STABILIZER.get())
            .pattern(" U ")
            .pattern("CRC")
            .pattern("CUC")
            .define('U', Items.COPPER_INGOT)
            .define('C', Items.COBBLESTONE)
            .define('R', ModItems.RAW_RESONANCE_CRYSTAL.get())
            .unlockedBy(getHasName(ModItems.RAW_RESONANCE_CRYSTAL.get()),
                has(ModItems.RAW_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        // Resonance Whetstone : 2 Cobblestone + 1 Iron Ingot + 1 Stable
        // Resonance Crystal (05-Machines.md). Le cristal est pose sur le socle
        // de pierre, le fer le maintient — meme remarque que ci-dessus sur la
        // disposition, seules les quantites sont imposees par la conception.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.RESONANCE_WHETSTONE.get())
            .pattern(" S ")
            .pattern("CIC")
            .define('S', ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .define('C', Items.COBBLESTONE)
            .define('I', Items.IRON_INGOT)
            .unlockedBy(getHasName(ModItems.STABLE_RESONANCE_CRYSTAL.get()),
                has(ModItems.STABLE_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        // Field Emitter : 4 Resonance Component + 1 Stable Resonance Crystal +
        // 2 Gold Ingot (05-Machines.md #4). Le cristal au centre (le cœur qui
        // émet), les composants aux coins, l'or au-dessus et en dessous. La forme
        // ci-dessous consomme exactement 4 C, 2 G, 1 S — vérifié contre les
        // quantités du design.
        // NB : cette recette est débloquée en jeu par le fragment de l'Avant-poste
        // (advancement veskorius:tier2_field, 12-UX-and-Advancements.md) — le
        // JSON produit ici reste inerte tant que l'advancement n'est pas obtenu,
        // ce câblage viendra avec la tâche 10 (structures + fragments).
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FIELD_EMITTER.get())
            .pattern("CGC")
            .pattern("CSC")
            .pattern(" G ")
            .define('C', ModItems.RESONANCE_COMPONENT.get())
            .define('G', Items.GOLD_INGOT)
            .define('S', ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .unlockedBy(getHasName(ModItems.RESONANCE_COMPONENT.get()),
                has(ModItems.RESONANCE_COMPONENT.get()))
            .save(recipeOutput);
    }
}
