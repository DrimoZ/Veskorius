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
 * Serializer du {@link DampingAgentRecipe}. Format JSON :
 * <pre>
 * {
 *   "type": "veskorius:damping",
 *   "ingredient": { "item": "veskorius:refined_resonance_crystal" },
 *   "dissonance": 500
 * }
 * </pre>
 */
public class DampingAgentRecipeSerializer implements RecipeSerializer<DampingAgentRecipe> {

    private static final MapCodec<DampingAgentRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(DampingAgentRecipe::agent),
        ExtraCodecs.POSITIVE_INT.fieldOf("dissonance").forGetter(DampingAgentRecipe::dissonance)
    ).apply(instance, DampingAgentRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, DampingAgentRecipe> STREAM_CODEC =
        StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, DampingAgentRecipe::agent,
            ByteBufCodecs.VAR_INT, DampingAgentRecipe::dissonance,
            DampingAgentRecipe::new);

    @Override
    public MapCodec<DampingAgentRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, DampingAgentRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
