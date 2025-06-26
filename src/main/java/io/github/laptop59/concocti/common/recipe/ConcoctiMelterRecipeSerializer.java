package io.github.laptop59.concocti.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class ConcoctiMelterRecipeSerializer implements RecipeSerializer<ConcoctiMelterRecipe> {
    public static final MapCodec<ConcoctiMelterRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(ConcoctiMelterRecipe::getInputItem),
            FluidStack.CODEC.fieldOf("pure_result").forGetter(ConcoctiMelterRecipe::getOutputPureFluid),
            FluidStack.OPTIONAL_CODEC.fieldOf("byproduct_result").forGetter(ConcoctiMelterRecipe::getOutputByproductFluid),
            Codec.INT.fieldOf("ticks").forGetter(ConcoctiMelterRecipe::getTicks)
    ).apply(inst, ConcoctiMelterRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConcoctiMelterRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, ConcoctiMelterRecipe::getInputItem,
                    FluidStack.STREAM_CODEC, ConcoctiMelterRecipe::getOutputPureFluid,
                    FluidStack.OPTIONAL_STREAM_CODEC, ConcoctiMelterRecipe::getOutputByproductFluid,
                    ByteBufCodecs.INT, ConcoctiMelterRecipe::getTicks,
                    ConcoctiMelterRecipe::new
            );

    // Return our map codec.
    @Override
    public @NotNull MapCodec<ConcoctiMelterRecipe> codec() {
        return CODEC;
    }

    // Return our stream codec.
    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, ConcoctiMelterRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}