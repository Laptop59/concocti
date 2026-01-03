package io.github.laptop59.concocti.common.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emi.emi.api.stack.EmiStack;
import io.github.laptop59.concocti.network.ConcoctiMachineSettingsSlotChangeC2S;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record ItemOption(@NotNull Ingredient ingredient, long count, @NotNull ItemStack remainder) {
    public static final Codec<ItemOption> FLAT_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Ingredient.MAP_CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ItemOption::ingredient),
            Codec.LONG.optionalFieldOf("count", 1L).forGetter(ItemOption::count),
            ItemStack.CODEC.optionalFieldOf("remainder", ItemStack.EMPTY).forGetter(ItemOption::remainder)
        ).apply(instance, ItemOption::new)
    );

    public static final Codec<ItemOption> NESTED_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ItemOption::ingredient),
            Codec.LONG.optionalFieldOf("count", 1L).forGetter(ItemOption::count),
            ItemStack.CODEC.optionalFieldOf("remainder", ItemStack.EMPTY).forGetter(ItemOption::remainder)
        ).apply(instance, ItemOption::new)
    );

    public static final Codec<ItemOption> CODEC =
        Codec.either(FLAT_CODEC, NESTED_CODEC) // Accept both types (even the legacy one)
            .xmap(
                either -> either.map(a -> a, b -> b),
                Either::left // Always encode flat (it's the modern one to use)
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemOption> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, ItemOption::ingredient,
        ByteBufCodecs.VAR_LONG, ItemOption::count,
        ItemStack.OPTIONAL_STREAM_CODEC, ItemOption::remainder,
        ItemOption::new
    );

    boolean matches(ItemStack stack) {
        return ingredient.test(stack) && stack.getCount() >= count;
    }

    long matchesCount(ItemStack stack) {
        return ingredient.test(stack) ? stack.getCount() : 0;
    }

    boolean consume(ItemStack stack) {
        if (matches(stack)) {
            stack.shrink((int) count);
            return true;
        } else return false;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ItemOption that = (ItemOption) object;
        return count == that.count && Objects.equals(remainder, that.remainder) && Objects.equals(ingredient, that.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, count, remainder);
    }

    public List<ItemStack> intoItemStackUncached() {
        return Arrays.stream(ingredient.getItems())
            .map(ingredient -> {
                ItemStack stack = ingredient.copy();
                stack.setCount((int) count);
                return stack;
            })
            .toList();
    }

    // Helper methods

    public static ItemOption of(@NotNull Ingredient ingredient, long count, @NotNull ItemStack remainder) {
        return new ItemOption(ingredient, count, remainder);
    }

    public static ItemOption of(@NotNull Ingredient ingredient, long count) {
        return new ItemOption(ingredient, count, ItemStack.EMPTY);
    }

    public static ItemOption of(@NotNull Ingredient ingredient, @NotNull ItemStack remainder) {
        return new ItemOption(ingredient, 1, remainder);
    }

    public static ItemOption of(@NotNull Ingredient ingredient) {
        return new ItemOption(ingredient, 1, ItemStack.EMPTY);
    }
}
