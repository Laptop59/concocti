package io.github.laptop59.concocti.common.recipe;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.List;

public class ItemsFluidsRecipeInput extends RecipeWrapper {
    protected IFluidHandler fluids;

    public ItemsFluidsRecipeInput(IItemHandler items, IFluidHandler fluids) {
        super(items);
        this.fluids = fluids;
    }

    public int getFluids() {
        return fluids.getTanks();
    }

    public FluidStack getFluid(int i) {
        return fluids.getFluidInTank(i);
    }

    public boolean test(List<ItemRecipeIngredient> inputItems, List<FluidRecipeIngredient> inputFluids) {
        return testOrConsume(inputItems, inputFluids, false);
    }

    public boolean consume(List<ItemRecipeIngredient> inputItems, List<FluidRecipeIngredient> inputFluids) {
        return testOrConsume(inputItems, inputFluids, true);
    }

    public boolean testOrConsume(List<ItemRecipeIngredient> inputItems, List<FluidRecipeIngredient> inputFluids, boolean shouldConsume) {
        // Reserve (simulated consumed) items in the inventory to really test
        {
            long[] currentCountsLeft = new long[size()];
            for (int i = 0; i < this.size(); i++) {
                currentCountsLeft[i] = this.getItem(i).getCount();
            }
            loop:
            for (ItemRecipeIngredient inputItem : inputItems) {
                for (ItemRecipeIngredientOption option : inputItem.options()) {
                    long[] countsLeft = currentCountsLeft.clone();
                    long countLeft = option.count();
                    for (int i = 0; i < this.size(); i++) {
                        ItemStack toSearch = this.getItem(i);
                        if (option.ingredient().test(toSearch)) {
                            long left = countsLeft[i];
                            int consumed = (int) Math.min(countLeft, left);
                            countsLeft[i] -= consumed;
                            countLeft -= consumed;
                            if (countLeft <= 0) {
                                currentCountsLeft = countsLeft;
                                // EXECUTE
                                if (shouldConsume) consume(option);
                                continue loop;
                            }
                        }
                    }
                }
                return false;
            }
        }
        {
            long[] currentAmountsLeft = new long[getFluids()];
            for (int i = 0; i < this.getFluids(); i++) {
                currentAmountsLeft[i] = this.getFluid(i).getAmount();
            }
            loop:
            for (FluidRecipeIngredient inputFluid : inputFluids) {
                for (FluidRecipeIngredientOption option : inputFluid.options()) {
                    long[] amountsLeft = currentAmountsLeft.clone();
                    long amountLeft = option.amount();
                    for (int i = 0; i < this.getFluids(); i++) {
                        FluidStack toSearch = this.getFluid(i).copy();
                        long left = amountsLeft[i];
                        int consumable = (int) Math.min(amountLeft, left);
                        if (consumable == 0) continue; // skip empty
                        toSearch.setAmount(consumable);
                        if (option.ingredient().test(toSearch)) {
                            amountsLeft[i] -= consumable;
                            amountLeft -= consumable;
                            if (amountLeft <= 0) {
                                currentAmountsLeft = amountsLeft;
                                // EXECUTE
                                if (shouldConsume) consume(option);
                                continue loop;
                            }
                        }
                    }
                }
                return false;
            }
        }
        return true;
    }

    public void consume(ItemRecipeIngredientOption option) {
        if (option.unconsumed()) {
            if (option.loseDurability()) {
                for (int i = 0; i < size(); i++) {
                    ItemStack stack = getItem(i);
                    if (option.ingredient().test(stack)) {
                        if (stack.isDamageableItem()) {
                            stack.setDamageValue(stack.getDamageValue() + 1);
                            if (stack.getDamageValue() >= stack.getMaxDamage())
                                stack.shrink(1);
                        }
                    }
                }
            }
            return;
        }
        long leftToConsume = option.count();
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getItem(i);
            int maxConsume = (int) Math.min(leftToConsume, Integer.MAX_VALUE);
            if (option.ingredient().test(stack)) {
                leftToConsume -= inv.extractItem(i, maxConsume, false).getCount();
                if (leftToConsume <= 0) return;
            }
        }
    }

    public void consume(FluidRecipeIngredientOption option) {
        if (option.unconsumed()) return;
        long leftToConsume = option.amount();
        for (int i = 0; i < getFluids(); i++) {
            FluidStack stack = getFluid(i).copy();
            int maxConsume = (int) Math.min(leftToConsume, Integer.MAX_VALUE);
            stack.setAmount(maxConsume);
            if (option.ingredient().test(stack)) {
                leftToConsume -= fluids.drain(stack, IFluidHandler.FluidAction.EXECUTE).getAmount();
                if (leftToConsume <= 0) return;
            }
        }
    }

    @Override
    public boolean isEmpty() {
        if (!super.isEmpty()) return false;
        for (int i = 0; i < this.getFluids(); i++) {
            FluidStack fluid = this.getFluid(i);
            if (!fluid.isEmpty()) return false;
        }
        return true;
    }
}
