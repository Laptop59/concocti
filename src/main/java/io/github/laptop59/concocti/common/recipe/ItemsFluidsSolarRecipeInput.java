package io.github.laptop59.concocti.common.recipe;

import io.github.laptop59.concocti.common.machine.SolarStorage;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.List;

public class ItemsFluidsSolarRecipeInput extends ItemsFluidsRecipeInput {
    SolarStorage solarStorage;

    public ItemsFluidsSolarRecipeInput(IItemHandler items, IFluidHandler fluids, SolarStorage solarStorage) {
        super(items, fluids);
        this.solarStorage = solarStorage;
    }

    public boolean testSolar(long inputSolar) {
        return solarStorage.extractSolar(inputSolar, true) >= inputSolar;
    }

    public boolean consumeSolar(long inputSolar) {
        return solarStorage.extractSolar(inputSolar, false) >= inputSolar;
    }

    public boolean test(List<ItemRecipeIngredient> inputItems, List<FluidRecipeIngredient> inputFluids, long inputSolar) {
        return testSolar(inputSolar) && testOrConsume(inputItems, inputFluids, false);
    }

    public boolean consume(List<ItemRecipeIngredient> inputItems, List<FluidRecipeIngredient> inputFluids, long inputSolar) {
        return consumeSolar(inputSolar) && testOrConsume(inputItems, inputFluids, true);
    }
}
