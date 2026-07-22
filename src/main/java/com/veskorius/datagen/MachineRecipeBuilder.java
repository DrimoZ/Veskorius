package com.veskorius.datagen;

import com.veskorius.recipe.MachineRecipe;
import com.veskorius.recipe.ModRecipeSerializers;
import com.veskorius.recipe.ModRecipeTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * Builder de datagen pour les {@link MachineRecipe} — l'équivalent de
 * {@code ShapedRecipeBuilder} pour nos recettes de machine. Sert à générer les
 * JSON de départ ; un datapack peut ensuite les modifier ou en ajouter.
 */
public class MachineRecipeBuilder {

    private final Supplier<? extends RecipeType<?>> type;
    private final Supplier<? extends RecipeSerializer<?>> serializer;
    private final List<SizedIngredient> ingredients = new ArrayList<>();
    private final ItemStack result;
    private int time = 1;
    private int oscPerTick = 0;

    private MachineRecipeBuilder(Supplier<? extends RecipeType<?>> type,
                                 Supplier<? extends RecipeSerializer<?>> serializer, ItemStack result) {
        this.type = type;
        this.serializer = serializer;
        this.result = result;
    }

    public static MachineRecipeBuilder stabilizing(ItemLike result, int count) {
        return new MachineRecipeBuilder(ModRecipeTypes.STABILIZING::get,
            ModRecipeSerializers.STABILIZING::get, new ItemStack(result, count));
    }

    public static MachineRecipeBuilder assembling(ItemLike result, int count) {
        return new MachineRecipeBuilder(ModRecipeTypes.ASSEMBLING::get,
            ModRecipeSerializers.ASSEMBLING::get, new ItemStack(result, count));
    }

    public static MachineRecipeBuilder purifying(ItemLike result, int count) {
        return new MachineRecipeBuilder(ModRecipeTypes.PURIFYING::get,
            ModRecipeSerializers.PURIFYING::get, new ItemStack(result, count));
    }

    public static MachineRecipeBuilder crushing(ItemLike result, int count) {
        return new MachineRecipeBuilder(ModRecipeTypes.CRUSHING::get,
            ModRecipeSerializers.CRUSHING::get, new ItemStack(result, count));
    }

    public static MachineRecipeBuilder roosting(ItemLike result, int count) {
        return new MachineRecipeBuilder(ModRecipeTypes.ROOSTING::get,
            ModRecipeSerializers.ROOSTING::get, new ItemStack(result, count));
    }

    public MachineRecipeBuilder input(ItemLike item, int count) {
        ingredients.add(SizedIngredient.of(item, count));
        return this;
    }

    public MachineRecipeBuilder input(TagKey<Item> tag, int count) {
        ingredients.add(SizedIngredient.of(tag, count));
        return this;
    }

    public MachineRecipeBuilder time(int ticks) {
        this.time = ticks;
        return this;
    }

    public MachineRecipeBuilder osc(int oscPerTick) {
        this.oscPerTick = oscPerTick;
        return this;
    }

    public void save(RecipeOutput output, ResourceLocation id) {
        MachineRecipe recipe = new MachineRecipe(
            type, serializer, List.copyOf(ingredients), result, time, oscPerTick);
        // Pas d'advancement de déblocage pour une recette de machine (elle n'est
        // pas au recipe book vanilla) : null en 3e argument.
        output.accept(id, recipe, null);
    }
}
