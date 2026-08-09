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
 * Carburant du Field Emitter, data-driven (type {@code veskorius:fueling}).
 *
 * Remplace l'ancien couple « item de carburant codé en dur + valeur d'Osc dans la
 * config » : chaque carburant est désormais une entrée JSON {@code ingredient → osc}.
 * Un modpack ajoute un carburant en déposant un JSON, en retire un en le supprimant,
 * et fixe la valeur qu'il rend — par exemple un Refined Crystal à 9000 Osc
 * (06-Energy.md), à condition d'augmenter la capacité de l'émetteur en conséquence
 * (voir {@code 14-Configuration.md}).
 *
 * Ce n'est pas une recette de fabrication : elle n'a pas de résultat. Elle sert de
 * table de correspondance interrogée par le Field Emitter (« cet objet est-il un
 * carburant, et combien vaut-il ? ») et affichée dans JEI.
 */
public class EmitterFuelRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient fuel;
    private final int osc;

    public EmitterFuelRecipe(Ingredient fuel, int osc) {
        this.fuel = fuel;
        this.osc = osc;
    }

    public Ingredient fuel() {
        return fuel;
    }

    /** Osc rendus quand une unité de ce carburant est brûlée. */
    public int osc() {
        return osc;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return fuel.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        // Un carburant ne produit pas d'objet : il alimente la réserve de l'émetteur.
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(fuel);
        return list;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    /**
     * <b>Hors du livre de recettes vanilla.</b>
     *
     * <p>Le livre classe toutes les recettes chargées et se plaignait des nôtres à chaque
     * entrée dans un monde — vingt lignes de « Unknown recipe category » par chargement,
     * avec un identifiant malformé où le namespace apparaissait deux fois. Il essayait de
     * ranger dans un onglet d'établi des recettes qui ne se font pas à l'établi.
     *
     * <p>{@code isSpecial()} est le mécanisme prévu pour ça : une recette spéciale ne
     * s'affiche pas au livre et ne se débloque pas. C'est exact ici — on ne fabrique pas
     * ces recettes, une machine les exécute — et ça vide le journal d'un bruit que
     * personne n'aurait fini par lire.
     *
     * <p>JEI, lui, les montre : il a ses propres catégories, déclarées par le plugin du
     * mod. Le livre vanilla et JEI ne servent pas la même chose.
     */
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.FUELING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.FUELING.get();
    }
}
