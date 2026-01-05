package io.github.laptop59.concocti.common.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.laptop59.concocti.integration.emi.UnconsumedIngredient;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static io.github.laptop59.concocti.client.ConcoctiClient.UNCONSUMED;

public record ItemOption(@NotNull Ingredient ingredient, long count, boolean unconsumed, boolean loseDurability) {

    public ItemOption {
        Objects.requireNonNull(ingredient);
    }

    public static final Codec<ItemOption> FLAT_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Ingredient.MAP_CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ItemOption::ingredient),
            Codec.LONG.optionalFieldOf("count", 1L).forGetter(ItemOption::count),
            Codec.BOOL.optionalFieldOf("unconsumed", false).forGetter(ItemOption::unconsumed),
            Codec.BOOL.optionalFieldOf("lose_durability", false).forGetter(ItemOption::loseDurability)
        ).apply(instance, ItemOption::new)
    );

    public static final Codec<ItemOption> NESTED_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ItemOption::ingredient),
            Codec.LONG.optionalFieldOf("count", 1L).forGetter(ItemOption::count),
            Codec.BOOL.optionalFieldOf("unconsumed", false).forGetter(ItemOption::unconsumed),
            Codec.BOOL.optionalFieldOf("lose_durability", false).forGetter(ItemOption::loseDurability)
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
        ByteBufCodecs.BOOL, ItemOption::unconsumed,
        ByteBufCodecs.BOOL, ItemOption::loseDurability,
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
            if (!unconsumed)
                stack.shrink((int) count);
            else if (loseDurability)
                if (stack.isDamageableItem()) {
                    stack.setDamageValue(stack.getDamageValue() + 1);
                    if (stack.getDamageValue() >= stack.getMaxDamage()) stack.shrink(1);
                }
            return true;
        } else return false;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ItemOption that = (ItemOption) object;
        return count == that.count && unconsumed == that.unconsumed && Objects.equals(ingredient, that.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, count, unconsumed);
    }

    public List<ItemStack> intoItemStackUncached() {
        return Arrays.stream(ingredient.getItems())
            .map(ingredient -> {
                ItemStack stack = ingredient.copy();
                stack.setCount((int) count);
                if (unconsumed) {
                    // Add fake lore
                    ItemLore lore = stack.get(DataComponents.LORE);
                    if (lore == null)
                        lore = new ItemLore(List.of(UNCONSUMED));
                    else
                        lore = lore.withLineAdded(UNCONSUMED);
                    stack.set(DataComponents.LORE, lore);
                }
                return stack;
            })
            .toList();
    }

    // Helper methods

    public static ItemOption of(@NotNull Ingredient ingredient, long count, boolean unconsumed) {
        return new ItemOption(ingredient, count, unconsumed, false);
    }

    public static ItemOption of(@NotNull Ingredient ingredient, long count) {
        return new ItemOption(ingredient, count, false, false);
    }

    public static ItemOption of(@NotNull Ingredient ingredient, boolean unconsumed) {
        return new ItemOption(ingredient, 1, unconsumed, false);
    }

    public static ItemOption of(@NotNull Ingredient ingredient) {
        return new ItemOption(ingredient, 1, false, false);
    }

    public static ItemOption of(@NotNull Ingredient ingredient, long count, boolean unconsumed, boolean loseDurability) {
        return new ItemOption(ingredient, count, unconsumed, loseDurability);
    }

    public static ItemOption of(@NotNull Ingredient ingredient, boolean unconsumed, boolean loseDurability) {
        return new ItemOption(ingredient, 1, unconsumed, loseDurability);
    }
}
