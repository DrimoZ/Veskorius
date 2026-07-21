package com.veskorius.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * Serializer de la {@link WhetstoneRecipe}. Format JSON :
 * <pre>
 * {
 *   "type": "veskorius:sharpening",
 *   "catalyst": { "item": "veskorius:stable_resonance_crystal" },
 *   "repair_percent": 25,
 *   "time": 160
 * }
 * </pre>
 */
public class WhetstoneRecipeSerializer implements RecipeSerializer<WhetstoneRecipe> {

    private static final MapCodec<WhetstoneRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        SizedIngredient.FLAT_CODEC.fieldOf("catalyst").forGetter(WhetstoneRecipe::catalyst),
        ExtraCodecs.intRange(1, 100).fieldOf("repair_percent").forGetter(WhetstoneRecipe::repairPercent),
        ExtraCodecs.POSITIVE_INT.fieldOf("time").forGetter(WhetstoneRecipe::time)
    ).apply(instance, WhetstoneRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, WhetstoneRecipe> STREAM_CODEC = StreamCodec.composite(
        SizedIngredient.STREAM_CODEC, WhetstoneRecipe::catalyst,
        ByteBufCodecs.VAR_INT, WhetstoneRecipe::repairPercent,
        ByteBufCodecs.VAR_INT, WhetstoneRecipe::time,
        WhetstoneRecipe::new);

    @Override
    public MapCodec<WhetstoneRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, WhetstoneRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
