package io.github.laptop59.concocti.common.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record FluidOption(FluidIngredient ingredient, long amount, boolean unconsumed) {

    public FluidOption {
        Objects.requireNonNull(ingredient);
    }

    public static final Codec<FluidOption> FLAT_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            FluidIngredient.MAP_CODEC_NONEMPTY.fieldOf("ingredient").forGetter(FluidOption::ingredient),
            Codec.LONG.optionalFieldOf("amount", 1L).forGetter(FluidOption::amount),
            Codec.BOOL.optionalFieldOf("unconsumed", false).forGetter(FluidOption::unconsumed)
        ).apply(instance, FluidOption::new)
    );

    public static final Codec<FluidOption> NESTED_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            FluidIngredient.CODEC_NON_EMPTY.fieldOf("ingredient").forGetter(FluidOption::ingredient),
            Codec.LONG.optionalFieldOf("amount", 1L).forGetter(FluidOption::amount),
            Codec.BOOL.optionalFieldOf("unconsumed", false).forGetter(FluidOption::unconsumed)
        ).apply(instance, FluidOption::new)
    );

    public static final Codec<FluidOption> CODEC =
        Codec.either(FLAT_CODEC, NESTED_CODEC) // Accept both types (even the legacy one)
            .xmap(
                either -> either.map(a -> a, b -> b),
                Either::left // Always encode flat (it's the modern one to use)
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidOption> STREAM_CODEC = StreamCodec.composite(
        FluidIngredient.STREAM_CODEC, FluidOption::ingredient,
        ByteBufCodecs.VAR_LONG, FluidOption::amount,
        ByteBufCodecs.BOOL, FluidOption::unconsumed,
        FluidOption::new
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
        FluidOption that = (FluidOption) object;
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

    public static FluidOption of(@NotNull FluidIngredient ingredient, long amount, boolean unconsumed) {
        return new FluidOption(ingredient, amount, unconsumed);
    }

    public static FluidOption of(@NotNull FluidIngredient ingredient, long amount) {
        return new FluidOption(ingredient, amount, false);
    }
}
