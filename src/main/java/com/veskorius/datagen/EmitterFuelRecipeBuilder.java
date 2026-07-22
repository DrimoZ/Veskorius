package com.veskorius.datagen;

import com.veskorius.recipe.EmitterFuelRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/** Builder de datagen pour les {@link EmitterFuelRecipe} (carburants du Field Emitter). */
public class EmitterFuelRecipeBuilder {

    private final Ingredient fuel;
    private final int osc;

    private EmitterFuelRecipeBuilder(Ingredient fuel, int osc) {
        this.fuel = fuel;
        this.osc = osc;
    }

    public static EmitterFuelRecipeBuilder fuel(ItemLike item, int osc) {
        return new EmitterFuelRecipeBuilder(Ingredient.of(item), osc);
    }

    public static EmitterFuelRecipeBuilder fuel(TagKey<Item> tag, int osc) {
        return new EmitterFuelRecipeBuilder(Ingredient.of(tag), osc);
    }

    public void save(RecipeOutput output, ResourceLocation id) {
        output.accept(id, new EmitterFuelRecipe(fuel, osc), null);
    }
}
