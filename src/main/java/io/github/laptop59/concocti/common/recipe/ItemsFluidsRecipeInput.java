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
        /* TODO
        boolean itemsInvolveRemainder = false, fluidsInvolveRemainder = false;
        int[] itemsPreferredSlot = new int[size()];
        int[] fluidsPreferredSlot = new int[getFluids()];

        int k = 0;
        loop1:
        for (FluidRecipeIngredient inputItem : inputItems) {
            if (!inputItem.remainder().isEmpty()) itemsInvolveRemainder = true;
            for (int i = 0; i < this.size(); i++) {
                ItemStack toSearch = this.getItem(i);
                if (inputItem.test(toSearch)) {
                    itemsPreferredSlot[k++] = i;
                    continue loop1;
                }
            }
            return false;
        }

        k = 0;
        loop2:
        for (FluidRecipeIngredient fluidIngredient : inputFluids) {
            if (!fluidIngredient.remainder().isEmpty()) fluidsInvolveRemainder = true;
            for (int i = 0; i < this.getFluids(); i++) {
                FluidStack toSearch = this.getFluid(i);
                if (fluidIngredient.test(toSearch)) {
                    fluidsPreferredSlot[k++] = i;
                    continue loop2;
                }
            }
            return false;
        }
        */
        return true;
    }

    public boolean consume(List<ItemRecipeIngredient> inputItems, List<FluidRecipeIngredient> inputFluids) {
                /* TODO
        loop1:
        for (FluidRecipeIngredient inputItem : inputItems) {
            for (int i = 0; i < this.size(); i++) {
                ItemStack toSearch = this.getItem(i);
                if (inputItem.test(toSearch)) {
                    inv.extractItem(i, inputItem.count(), false);
                    continue loop1;
                }
            }
            return false;
        }

        loop2:
        for (FluidRecipeIngredient fluidIngredient : inputFluids) {
            for (int i = 0; i < this.getFluids(); i++) {
                FluidStack toSearch = this.getFluid(i);
                if (fluidIngredient.test(toSearch)) for (FluidStack tester : fluidIngredient.getFluids()) {
                    FluidStack drained = fluids.drain(tester, IFluidHandler.FluidAction.SIMULATE);
                    if (drained.getAmount() == tester.getAmount()) {
                        fluids.drain(tester, IFluidHandler.FluidAction.EXECUTE);
                        continue loop2;
                    }
                }
            }
            return false;
        }
        */
        return true;
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
