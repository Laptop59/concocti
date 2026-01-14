package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.common.recipe.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractConcoctiMultiblockRecipeCategory<R extends AbstractConcoctiMultiblockRecipe<R>> extends AbstractConcoctiRecipeCategory<R> {

    private final int WIDTH = 176;
    private final int SLOTS_PER_ROW = 4;

    public AbstractConcoctiMultiblockRecipeCategory() {
    }

    @Override
    protected int getSlotsHeight(@NotNull R recipe) {
        return Math.max(
                Math.ceilDiv(recipe.getInputItems().size() + recipe.getInputFluids().size(), SLOTS_PER_ROW),
                Math.ceilDiv(recipe.getOutputItems().size() + recipe.getOutputFluids().size(), SLOTS_PER_ROW)
        );
    }

    @Override
    public void set(@NotNull RecipeBuilder builder, @NotNull R recipe) {
        int left = WIDTH / 2 - 4 * 18;
        // Add the recipe inputs.
        List<ItemRecipeIngredient> itemRecipeIngredients = recipe.getInputItems();
        List<FluidRecipeIngredient> fluidRecipeIngredients = recipe.getInputFluids();
        List<ItemOutput> itemOutputs = recipe.getOutputItems();
        List<FluidOutput> fluidOutputs = recipe.getOutputFluids();

        int slotTop = 16;
        int slotLeft = left;

        int items = itemRecipeIngredients.size(), fluids = fluidRecipeIngredients.size();

        for (int i = 0; i < Math.ceilDiv(items + fluids, SLOTS_PER_ROW) * SLOTS_PER_ROW; i++) {
            if (i < items) {
                builder.addInputSlot(slotLeft, slotTop, itemRecipeIngredients.get(i));
            } else if (i < items + fluids) {
                builder.addInputSlot(slotLeft, slotTop, fluidRecipeIngredients.get(i - items));
            } else {
                builder.addInputSlot(slotLeft, slotTop, false);
            }
            slotLeft += 18;
            if ((i + 1) % SLOTS_PER_ROW == 0) {
                slotLeft = left;
                slotTop += 18;
            }
        }

        items = itemOutputs.size();
        fluids = fluidOutputs.size();

        slotTop = 16;
        slotLeft = WIDTH / 2 + 42;

        for (int i = 0; i < Math.ceilDiv(items + fluids, SLOTS_PER_ROW) * SLOTS_PER_ROW; i++) {
            if (i < items) {
                builder.addOutputSlot(slotLeft, slotTop, itemOutputs.get(i));
            } else if (i < items + fluids) {
                builder.addOutputSlot(slotLeft, slotTop, fluidOutputs.get(i - items));
            } else {
                builder.addOutputSlot(slotLeft, slotTop, false);
            }
            slotLeft += 18;
            if ((i + 1) % SLOTS_PER_ROW == 0) {
                slotLeft = left;
                slotTop += 18;
            }
        }
    }

    @Override
    public int getWidth(@NotNull R recipe) {
        return 216;
    }
}