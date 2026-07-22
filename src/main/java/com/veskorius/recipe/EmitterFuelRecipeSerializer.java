package com.veskorius.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Serializer de l'{@link EmitterFuelRecipe}. Format JSON :
 * <pre>
 * {
 *   "type": "veskorius:fueling",
 *   "ingredient": { "item": "veskorius:stable_resonance_crystal" },
 *   "osc": 4000
 * }
 * </pre>
 */
public class EmitterFuelRecipeSerializer implements RecipeSerializer<EmitterFuelRecipe> {

    private static final MapCodec<EmitterFuelRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(EmitterFuelRecipe::fuel),
        ExtraCodecs.POSITIVE_INT.fieldOf("osc").forGetter(EmitterFuelRecipe::osc)
    ).apply(instance, EmitterFuelRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, EmitterFuelRecipe> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, EmitterFuelRecipe::fuel,
        ByteBufCodecs.VAR_INT, EmitterFuelRecipe::osc,
        EmitterFuelRecipe::new);

    @Override
    public MapCodec<EmitterFuelRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, EmitterFuelRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
