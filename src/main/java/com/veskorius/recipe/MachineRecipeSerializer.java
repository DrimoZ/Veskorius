package com.veskorius.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * Serializer partagé des {@link MachineRecipe}. Une instance par machine, liée à
 * son {@link RecipeType} : c'est ce lien qui fait qu'une recette décodée « connaît »
 * sa machine, tout en réutilisant le même code de (dé)sérialisation.
 *
 * Le format JSON :
 * <pre>
 * {
 *   "type": "veskorius:assembling",
 *   "ingredients": [ { "item": "veskorius:stable_resonance_crystal" },
 *                    { "item": "minecraft:iron_ingot", "count": 2 } ],
 *   "result": { "id": "veskorius:resonance_component", "count": 2 },
 *   "time": 100,
 *   "osc_per_tick": 3
 * }
 * </pre>
 */
public class MachineRecipeSerializer implements RecipeSerializer<MachineRecipe> {

    private final MapCodec<MachineRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> streamCodec;

    public MachineRecipeSerializer(Supplier<RecipeType<?>> type) {
        Supplier<RecipeSerializer<?>> self = () -> this;

        this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SizedIngredient.FLAT_CODEC.listOf().fieldOf("ingredients").forGetter(MachineRecipe::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(MachineRecipe::result),
            ExtraCodecs.POSITIVE_INT.fieldOf("time").forGetter(MachineRecipe::time),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("osc_per_tick", 0).forGetter(MachineRecipe::oscPerTick),
            com.mojang.serialization.Codec.BOOL.optionalFieldOf("stable", false).forGetter(MachineRecipe::stable)
        ).apply(instance, (ingredients, result, time, osc, stable) ->
            new MachineRecipe(type, self, ingredients, result, time, osc, stable)));

        this.streamCodec = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), MachineRecipe::ingredients,
            ItemStack.STREAM_CODEC, MachineRecipe::result,
            ByteBufCodecs.VAR_INT, MachineRecipe::time,
            ByteBufCodecs.VAR_INT, MachineRecipe::oscPerTick,
            ByteBufCodecs.BOOL, MachineRecipe::stable,
            (ingredients, result, time, osc, stable) ->
                new MachineRecipe(type, self, ingredients, result, time, osc, stable));
    }

    @Override
    public MapCodec<MachineRecipe> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> streamCodec() {
        return streamCodec;
    }
}
