package io.github.laptop59.concocti.common.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
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

    public boolean test(List<SizedIngredient> inputItems, List<SizedFluidIngredient> inputFluids) {
        loop1:
        for (SizedIngredient inputItem : inputItems) {
            for (int i = 0; i < this.size(); i++) {
                ItemStack toSearch = this.getItem(i);
                if (inputItem.test(toSearch)) continue loop1;
            }
            return false;
        }
        loop2:
        for (SizedFluidIngredient fluidIngredient : inputFluids) {
            for (int i = 0; i < this.getFluids(); i++) {
                FluidStack toSearch = this.getFluid(i);
                if (fluidIngredient.test(toSearch)) continue loop2;
            }
            return false;
        }
        return true;
    }

    public void consume(List<SizedIngredient> inputItems, List<SizedFluidIngredient> inputFluids) {
        for (SizedIngredient inputItem : inputItems) {
            for (int i = 0; i < this.size(); i++) {
                ItemStack toSearch = this.getItem(i);
                if (inputItem.test(toSearch)) {
                    inv.extractItem(i, inputItem.count(), false);
                    break;
                }
            }
        }
        for (SizedFluidIngredient fluidIngredient : inputFluids) {
            for (int i = 0; i < this.getFluids(); i++) {
                FluidStack toSearch = this.getFluid(i);
                if (fluidIngredient.test(toSearch)) for (FluidStack tester : fluidIngredient.getFluids()) {
                    FluidStack drained = fluids.drain(tester, IFluidHandler.FluidAction.SIMULATE);
                    if (drained.getAmount() == tester.getAmount()) {
                        fluids.drain(tester, IFluidHandler.FluidAction.EXECUTE);
                        break;
                    }
                }
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
