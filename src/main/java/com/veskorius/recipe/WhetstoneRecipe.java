package com.veskorius.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * Recette du Resonance Whetstone (machine #3) — une forme à part : elle ne produit
 * pas un objet fixe, elle **répare** l'outil placé en entrée.
 *
 * D'où un type de recette dédié ({@code veskorius:sharpening}) plutôt que le
 * {@link MachineRecipe} générique. Ce qui est data-driven : le **catalyseur**
 * consommé, le **pourcentage de durabilité** rendu, et le **temps**. Ce qui reste
 * en code : « tout outil endommageable » (un prédicat sur l'endommagement, pas
 * exprimable comme un simple item/tag).
 *
 * Slots attendus dans l'entrée : 0 = l'outil, 1 = le catalyseur.
 */
public class WhetstoneRecipe implements Recipe<MachineRecipeInput> {

    public static final int SLOT_TOOL = 0;
    public static final int SLOT_CATALYST = 1;

    private final SizedIngredient catalyst;
    private final int repairPercent;
    private final int time;

    public WhetstoneRecipe(SizedIngredient catalyst, int repairPercent, int time) {
        this.catalyst = catalyst;
        this.repairPercent = repairPercent;
        this.time = time;
    }

    public SizedIngredient catalyst() {
        return catalyst;
    }

    public int repairPercent() {
        return repairPercent;
    }

    public int time() {
        return time;
    }

    /** Copie de l'outil réparé de {@code repairPercent}% de sa durabilité max. */
    public ItemStack repair(ItemStack tool) {
        ItemStack repaired = tool.copy();
        int amount = repaired.getMaxDamage() * repairPercent / 100;
        repaired.setDamageValue(Math.max(0, repaired.getDamageValue() - amount));
        return repaired;
    }

    @Override
    public boolean matches(MachineRecipeInput input, Level level) {
        if (input.size() <= SLOT_CATALYST) {
            return false;
        }
        ItemStack tool = input.getItem(SLOT_TOOL);
        if (!tool.isDamageableItem() || !tool.isDamaged()) {
            return false;
        }
        ItemStack catalystStack = input.getItem(SLOT_CATALYST);
        return catalyst.test(catalystStack) && catalystStack.getCount() >= catalyst.count();
    }

    @Override
    public ItemStack assemble(MachineRecipeInput input, HolderLookup.Provider registries) {
        return repair(input.getItem(SLOT_TOOL));
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        // Résultat dynamique (dépend de l'outil) : pas de représentation fixe.
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(catalyst.ingredient());
        return list;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SHARPENING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.SHARPENING.get();
    }
}
