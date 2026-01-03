package io.github.laptop59.concocti.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.laptop59.concocti.common.recipe.ItemOption;
import io.github.laptop59.concocti.common.recipe.ItemRecipeIngredient;

import java.util.Arrays;
import java.util.List;

public final class ItemRecipeEmiIngredient extends RecipeEmiIngredient implements EmiIngredient {
    private final ItemRecipeIngredient ingredient;
    private List<EmiStack> emiCachedStacks;
    private long amount = 1;

    private ItemRecipeEmiIngredient(ItemRecipeIngredient ingredient) {
        this.ingredient = ingredient;
    }

    public static ItemRecipeEmiIngredient wrap(ItemRecipeIngredient ingredient) {
        return new ItemRecipeEmiIngredient(ingredient);
    }

    @Override
    public List<EmiStack> getEmiStacks() {
        if (emiCachedStacks == null)
            emiCachedStacks = getEmiStacksUncached();
        return emiCachedStacks;
    }

    @Override
    public EmiIngredient copy() {
        ItemRecipeEmiIngredient ingredient1 = wrap(ingredient);
        ingredient1.setAmount(amount);
        ingredient1.emiCachedStacks = emiCachedStacks;
        return ingredient1;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public EmiIngredient setAmount(long amount) {
        this.amount = amount;
        emiCachedStacks = null; // Make it such that the stacks are generated correctly with the updated amount.
        return this;
    }

    @Override
    public float getChance() {
        return 1;
    }

    @Override
    public EmiIngredient setChance(float chance) {
        return this;
    }

    private List<EmiStack> getEmiStacksUncached() {
        return ingredient.options()
            .stream()
            .map(option -> intoEmiStackUncached(option, amount))
            .flatMap(List::stream)
            .toList();
    }

    private List<EmiStack> intoEmiStackUncached(ItemOption option, long amount) {
        return Arrays.stream(option.ingredient().getItems())
            .map(ingredient -> {
                EmiStack stack = EmiStack.of(ingredient, option.count() * amount);
                if (!option.remainder().isEmpty()) stack.setRemainder(EmiStack.of(option.remainder()));
                return stack;
            })
            .toList();
    }
}
