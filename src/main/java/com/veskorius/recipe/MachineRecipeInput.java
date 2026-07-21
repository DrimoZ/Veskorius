package com.veskorius.recipe;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Entrée de recette générique pour les machines : la simple liste des piles des
 * slots d'entrée, dans l'ordre des slots.
 *
 * En 1.21, {@code Recipe} ne prend plus un {@code Container} mais un
 * {@link RecipeInput} — cette classe est l'adaptateur minimal, réutilisable par
 * toutes les machines quelle que soit leur disposition de slots.
 */
public record MachineRecipeInput(List<ItemStack> items) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }
}
