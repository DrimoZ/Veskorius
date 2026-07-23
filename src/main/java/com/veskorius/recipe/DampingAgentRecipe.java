package com.veskorius.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Agent de damping du Damping Array, data-driven (type {@code veskorius:damping}).
 *
 * <p>Même motif que les carburants du Field Emitter ({@link EmitterFuelRecipe}) : une
 * entrée JSON {@code ingredient → dissonance absorbée par unité}. Ce n'est pas une
 * recette de fabrication (pas de résultat), c'est une table de correspondance
 * interrogée par le Damping Array et affichable dans JEI.
 *
 * <p>Le choix d'un {@code RecipeType} plutôt qu'un simple tag est délibéré : il permet
 * une valeur <b>par agent</b>. Le Refined Crystal ouvre la voie dès le T2 ; le
 * Concentrated Flux (T3) prendra le relais avec une valeur bien supérieure, sans une
 * ligne de code (voir `04-Materials.md`).
 */
public class DampingAgentRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient agent;
    private final int dissonance;

    public DampingAgentRecipe(Ingredient agent, int dissonance) {
        this.agent = agent;
        this.dissonance = dissonance;
    }

    public Ingredient agent() {
        return agent;
    }

    /** Dissonance absorbée quand une unité de cet agent est consommée. */
    public int dissonance() {
        return dissonance;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return agent.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(agent);
        return list;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DAMPING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.DAMPING.get();
    }
}
