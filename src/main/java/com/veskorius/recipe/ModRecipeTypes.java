package com.veskorius.recipe;

import com.veskorius.Veskorius;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Un {@link RecipeType} par machine : c'est la clé de recherche qui garantit
 * qu'une machine ne voit que SES recettes ({@code getRecipeFor(type, ...)}).
 */
public class ModRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, Veskorius.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> STABILIZING = register("stabilizing");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> ASSEMBLING = register("assembling");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> PURIFYING = register("purifying");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> CRUSHING = register("crushing");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> ROOSTING = register("roosting");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> FORGING = register("forging");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> COMPRESSING = register("compressing");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> SYNTHESIS = register("synthesis");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> SYNTHESIZING = register("synthesizing");
    public static final DeferredHolder<RecipeType<?>, RecipeType<WhetstoneRecipe>> SHARPENING = register("sharpening");
    public static final DeferredHolder<RecipeType<?>, RecipeType<EmitterFuelRecipe>> FUELING = register("fueling");
    public static final DeferredHolder<RecipeType<?>, RecipeType<DampingAgentRecipe>> DAMPING = register("damping");

    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> register(String name) {
        return RECIPE_TYPES.register(name,
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, name)));
    }
}
