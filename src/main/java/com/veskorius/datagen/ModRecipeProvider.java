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
    }
}
