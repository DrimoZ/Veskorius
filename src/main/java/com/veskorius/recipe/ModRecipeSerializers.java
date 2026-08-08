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

    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> CRUSHING =
        RECIPE_SERIALIZERS.register("crushing",
            () -> new MachineRecipeSerializer(ModRecipeTypes.CRUSHING::get));

    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> ROOSTING =
        RECIPE_SERIALIZERS.register("roosting",
            () -> new MachineRecipeSerializer(ModRecipeTypes.ROOSTING::get));

    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> SYNTHESIS =
        RECIPE_SERIALIZERS.register("synthesis",
            () -> new MachineRecipeSerializer(ModRecipeTypes.SYNTHESIS::get));

    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> COMPRESSING =
        RECIPE_SERIALIZERS.register("compressing",
            () -> new MachineRecipeSerializer(ModRecipeTypes.COMPRESSING::get));
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> RECLAIMING =
        RECIPE_SERIALIZERS.register("reclaiming",
            () -> new MachineRecipeSerializer(ModRecipeTypes.RECLAIMING::get));

    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> SYNTHESIZING =
        RECIPE_SERIALIZERS.register("synthesizing",
            () -> new MachineRecipeSerializer(ModRecipeTypes.SYNTHESIZING::get));

    public static final DeferredHolder<RecipeSerializer<?>, WhetstoneRecipeSerializer> SHARPENING =
        RECIPE_SERIALIZERS.register("sharpening", WhetstoneRecipeSerializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> FORGING =
        RECIPE_SERIALIZERS.register("forging",
            () -> new MachineRecipeSerializer(ModRecipeTypes.FORGING::get));

    public static final DeferredHolder<RecipeSerializer<?>, EmitterFuelRecipeSerializer> FUELING =
        RECIPE_SERIALIZERS.register("fueling", EmitterFuelRecipeSerializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, DampingAgentRecipeSerializer> DAMPING =
        RECIPE_SERIALIZERS.register("damping", DampingAgentRecipeSerializer::new);
}
