package io.github.laptop59.concocti.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.recipe.ItemOption;
import io.github.laptop59.concocti.common.recipe.ItemRecipeIngredient;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ItemRecipeEmiIngredient {
    private ItemRecipeEmiIngredient() {}

    public static List<EmiIngredient> wrap(ItemRecipeIngredient ingredient) {
        ArrayList<EmiIngredient> all = new ArrayList<>(ingredient.options().size());
        for (ItemOption option : ingredient.options()) {
            List<EmiStack> consumed = new ArrayList<>();
            List<EmiStack> unconsumed = new ArrayList<>();
            List<EmiStack> unconsumed_lose_durability = new ArrayList<>();

            ItemStack[] stacks = option.ingredient().getItems();
            for (ItemStack itemStack : stacks) {
                itemStack = itemStack.copy();
                itemStack.setCount((int) option.count());
                EmiStack emiStack = EmiStack.of(itemStack);
                if (option.unconsumed()) {
                    if (itemStack.isDamageableItem()) {
                        ItemStack itemStack2 = itemStack.copy();
                        itemStack2.setDamageValue(itemStack2.getDamageValue() + 1);
                        if (itemStack2.getDamageValue() >= itemStack2.getMaxDamage()) itemStack2.shrink(1);
                        emiStack = emiStack.setRemainder(EmiStack.of(itemStack2));
                        unconsumed_lose_durability.add(emiStack);
                    } else unconsumed.add(emiStack);
                } else consumed.add(emiStack);
            }

            // Separate it
            if (!consumed.isEmpty()) all.add(EmiIngredient.of(consumed));
            if (!unconsumed.isEmpty()) all.add(new UnconsumedIngredient(EmiIngredient.of(unconsumed)));
            if (!unconsumed_lose_durability.isEmpty()) all.add(new UnconsumedIngredientNonCatalyst(EmiIngredient.of(unconsumed_lose_durability)));
        }
        return all;
    }
}