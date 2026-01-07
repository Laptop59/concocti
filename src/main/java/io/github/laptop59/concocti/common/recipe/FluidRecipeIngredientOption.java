package io.github.laptop59.concocti.common.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record FluidRecipeIngredientOption(FluidIngredient ingredient, long amount, boolean unconsumed) {

    public FluidRecipeIngredientOption {
        Objects.requireNonNull(ingredient);
    }

    public static final Codec<FluidRecipeIngredientOption> FLAT_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            FluidIngredient.MAP_CODEC_NONEMPTY.fieldOf("ingredient").forGetter(FluidRecipeIngredientOption::ingredient),
            Codec.LONG.optionalFieldOf("amount", 1L).forGetter(FluidRecipeIngredientOption::amount),
            Codec.BOOL.optionalFieldOf("unconsumed", false).forGetter(FluidRecipeIngredientOption::unconsumed)
        ).apply(instance, FluidRecipeIngredientOption::new)
    );

    public static final Codec<FluidRecipeIngredientOption> NESTED_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            FluidIngredient.CODEC_NON_EMPTY.fieldOf("ingredient").forGetter(FluidRecipeIngredientOption::ingredient),
            Codec.LONG.optionalFieldOf("amount", 1L).forGetter(FluidRecipeIngredientOption::amount),
            Codec.BOOL.optionalFieldOf("unconsumed", false).forGetter(FluidRecipeIngredientOption::unconsumed)
        ).apply(instance, FluidRecipeIngredientOption::new)
    );

    public static final Codec<FluidRecipeIngredientOption> CODEC =
        Codec.either(FLAT_CODEC, NESTED_CODEC) // Accept both types (even the legacy one)
            .xmap(
                either -> either.map(a -> a, b -> b),
                Either::left // Always encode flat (it's the modern one to use)
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidRecipeIngredientOption> STREAM_CODEC = StreamCodec.composite(
        FluidIngredient.STREAM_CODEC, FluidRecipeIngredientOption::ingredient,
        ByteBufCodecs.VAR_LONG, FluidRecipeIngredientOption::amount,
        ByteBufCodecs.BOOL, FluidRecipeIngredientOption::unconsumed,
        FluidRecipeIngredientOption::new
    );

    boolean matches(FluidStack stack) {
        return ingredient.test(stack) && stack.getAmount() >= amount;
    }

    long matchesAmount(FluidStack stack) {
        return ingredient.test(stack) ? stack.getAmount() : 0;
    }

    boolean consume(FluidStack stack) {
        if (matches(stack)) {
            stack.shrink((int) amount);
            return true;
        } else return false;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        FluidRecipeIngredientOption that = (FluidRecipeIngredientOption) object;
        return amount == that.amount && unconsumed == that.unconsumed && Objects.equals(ingredient, that.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, amount, unconsumed);
    }

    public List<FluidStack> intoFluidStackUncached() {
        return Arrays.stream(ingredient.getStacks())
            .map(ingredient -> {
                FluidStack stack = ingredient.copy();
                stack.setAmount((int) amount);
                return stack;
            })
            .toList();
    }

    public static FluidRecipeIngredientOption of(@NotNull FluidIngredient ingredient, long amount, boolean unconsumed) {
        return new FluidRecipeIngredientOption(ingredient, amount, unconsumed);
    }

    public static FluidRecipeIngredientOption of(@NotNull FluidIngredient ingredient, long amount) {
        return new FluidRecipeIngredientOption(ingredient, amount, false);
    }
}
