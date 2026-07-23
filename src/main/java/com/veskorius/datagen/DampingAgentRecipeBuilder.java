package com.veskorius.datagen;

import com.veskorius.recipe.DampingAgentRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/** Builder de datagen pour les {@link DampingAgentRecipe} (agents du Damping Array). */
public class DampingAgentRecipeBuilder {

    private final Ingredient agent;
    private final int dissonance;

    private DampingAgentRecipeBuilder(Ingredient agent, int dissonance) {
        this.agent = agent;
        this.dissonance = dissonance;
    }

    public static DampingAgentRecipeBuilder agent(ItemLike item, int dissonance) {
        return new DampingAgentRecipeBuilder(Ingredient.of(item), dissonance);
    }

    public static DampingAgentRecipeBuilder agent(TagKey<Item> tag, int dissonance) {
        return new DampingAgentRecipeBuilder(Ingredient.of(tag), dissonance);
    }

    public void save(RecipeOutput output, ResourceLocation id) {
        output.accept(id, new DampingAgentRecipe(agent, dissonance), null);
    }
}
