package com.veskorius.recipe;

import com.veskorius.Veskorius;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Un serializer par machine, chacun lié à son {@link RecipeType}. Le nom du
 * serializer est ce qui apparaît dans le champ {@code "type"} du JSON de recette.
 */
public class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, Veskorius.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> STABILIZING =
        RECIPE_SERIALIZERS.register("stabilizing",
            () -> new MachineRecipeSerializer(ModRecipeTypes.STABILIZING::get));

    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> ASSEMBLING =
        RECIPE_SERIALIZERS.register("assembling",
            () -> new MachineRecipeSerializer(ModRecipeTypes.ASSEMBLING::get));

    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> PURIFYING =
        RECIPE_SERIALIZERS.register("purifying",
            () -> new MachineRecipeSerializer(ModRecipeTypes.PURIFYING::get));
}
