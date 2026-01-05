package io.github.laptop59.concocti.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** An item ingredient with remainder information as well. */
public final class ItemRecipeIngredient {
    public static final Codec<ItemRecipeIngredient> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ItemOption.CODEC.listOf().fieldOf("options").forGetter(ItemRecipeIngredient::options)
        ).apply(instance, ItemRecipeIngredient::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemRecipeIngredient> STREAM_CODEC = StreamCodec.composite(
        ItemOption.STREAM_CODEC.apply(ByteBufCodecs.list()), ItemRecipeIngredient::options,
        ItemRecipeIngredient::new
    );

    public static ItemRecipeIngredient of(@NotNull ItemOption... options) {
        return new ItemRecipeIngredient(options);
    }

    public static ItemRecipeIngredient of(@NotNull Ingredient ingredient, long count, boolean unconsumed) {
        Objects.requireNonNull(ingredient);
        return ItemRecipeIngredient.of(ItemOption.of(ingredient, count, unconsumed));
    }

    public static ItemRecipeIngredient of(@NotNull Ingredient ingredient, long count) {
        return ItemRecipeIngredient.of(ItemOption.of(ingredient, count, false));
    }

    public static ItemRecipeIngredient of(@NotNull ItemLike itemLike, long count) {
        return ItemRecipeIngredient.of(ItemOption.of(Ingredient.of(itemLike), count, false));
    }

    public static ItemRecipeIngredient of(@NotNull TagKey<Item> tag, long count) {
        return ItemRecipeIngredient.of(ItemOption.of(Ingredient.of(tag), count, false));
    }

    public static ItemRecipeIngredient of(@NotNull Ingredient ingredient, boolean unconsumed) {
        return ItemRecipeIngredient.of(ItemOption.of(ingredient, 1, unconsumed));
    }

    public static ItemRecipeIngredient of(@NotNull Ingredient ingredient) {
        return ItemRecipeIngredient.of(ItemOption.of(ingredient, 1, false));
    }

    @NotNull
    private final List<ItemOption> options;

    private List<ItemStack> cachedStacks;

    public ItemRecipeIngredient(@NotNull List<ItemOption> options) {
        this.options = List.copyOf(options);
    }

    public ItemRecipeIngredient(@NotNull ItemOption... options) {
        this.options = List.of(options);
    }

    public List<ItemOption> options() {
        return options;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof ItemRecipeIngredient ingredient) {
            return options.equals(ingredient.options);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return options.hashCode();
    }

    @Override
    public String toString() {
        return options.toString();
    }

    public List<ItemStack> getStacks() {
        if (cachedStacks == null)
            cachedStacks = getItemStacksUncached();
        return cachedStacks;
    }

    private List<ItemStack> getItemStacksUncached() {
        return options
            .stream()
            .map(ItemOption::intoItemStackUncached)
            .flatMap(List::stream)
            .toList();
    }

    public Optional<ItemOption> testOpt(ItemStack stack) {
        for (ItemOption option : options) {
            if (option.matches(stack)) return Optional.of(option);
        }
        return Optional.empty();
    }

    public Optional<ItemOption> consumeOpt(ItemStack stack) {
        for (ItemOption option : options) {
            if (option.consume(stack)) return Optional.of(option);
        }
        return Optional.empty();
    }

    public boolean test(ItemStack stack) {
        return testOpt(stack).isPresent();
    }

    public boolean consume(ItemStack stack) {
        return consumeOpt(stack).isPresent();
    }

    public Optional<ItemOption> testOpt(IItemHandler handler) {
        for (ItemOption option : options) {
            long countLeft = option.count();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack itemStack = handler.getStackInSlot(i);
                if (option.ingredient().test(itemStack))
                    countLeft -= handler.extractItem(i, (int) Math.min(Integer.MAX_VALUE, countLeft), true).getCount();
                if (countLeft <= 0) return Optional.of(option);
            }
        }
        return Optional.empty();
    }

    public Optional<ItemOption> consumeOpt(IItemHandler handler) {
        for (ItemOption option : options) {
            long countLeft = option.count();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack itemStack = handler.getStackInSlot(i);
                if (option.ingredient().test(itemStack))
                    countLeft -= handler.extractItem(i, (int) Math.min(Integer.MAX_VALUE, countLeft), false).getCount();
                if (countLeft <= 0) return Optional.of(option);
            }
        }
        return Optional.empty();
    }

    public boolean test(IItemHandler handler) {
        return testOpt(handler).isPresent();
    }

    public boolean consume(IItemHandler handler) {
        return consumeOpt(handler).isPresent();
    }
}
