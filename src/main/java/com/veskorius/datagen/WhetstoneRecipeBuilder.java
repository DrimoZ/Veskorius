package com.veskorius.datagen;

import com.veskorius.recipe.WhetstoneRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/** Builder de datagen pour les {@link WhetstoneRecipe} (réparation). */
public class WhetstoneRecipeBuilder {

    private SizedIngredient catalyst;
    private int repairPercent = 25;
    private int time = 1;

    public static WhetstoneRecipeBuilder sharpening() {
        return new WhetstoneRecipeBuilder();
    }

    public WhetstoneRecipeBuilder catalyst(ItemLike item, int count) {
        this.catalyst = SizedIngredient.of(item, count);
        return this;
    }

    public WhetstoneRecipeBuilder repairPercent(int percent) {
        this.repairPercent = percent;
        return this;
    }

    public WhetstoneRecipeBuilder time(int ticks) {
        this.time = ticks;
        return this;
    }

    public void save(RecipeOutput output, ResourceLocation id) {
        output.accept(id, new WhetstoneRecipe(catalyst, repairPercent, time), null);
    }
}
