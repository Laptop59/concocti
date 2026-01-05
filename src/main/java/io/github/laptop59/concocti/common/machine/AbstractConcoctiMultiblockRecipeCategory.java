package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.common.recipe.AbstractConcoctiMultiblockRecipe;
import io.github.laptop59.concocti.common.recipe.FluidRecipeIngredient;
import io.github.laptop59.concocti.common.recipe.ItemRecipeIngredient;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConcoctiMultiblockRecipeCategory<R extends AbstractConcoctiMultiblockRecipe<R>> extends AbstractConcoctiRecipeCategory<R> {

    private final int WIDTH = 176;

    public AbstractConcoctiMultiblockRecipeCategory() {
    }

    public record DrawInfo(List<Integer> slots, int arrowPos) {
    }

    public DrawInfo createDrawInfo(R recipe) {
        int drawnSlots = 0;
        drawnSlots += recipe.getInputItems().size() + recipe.getInputFluids().size() + recipe.getOutputFluids().size() + recipe.getOutputItems().size();
        int drawnWidth = drawnSlots * 18 + (11 + 22 + 11);
        int left = (WIDTH - drawnWidth) / 2 - 3;
        ArrayList<Integer> toBeDrawnSlots = new ArrayList<>(drawnSlots);
        // Add the recipe inputs.
        int drawnInputSlots = recipe.getInputItems().size() + recipe.getInputFluids().size();
        for (int i = 0; i < drawnInputSlots; i++) {
            toBeDrawnSlots.add(left);
            left += 18;
        }
        left += 11;
        int arrowPos = left;
        left += 22 + 11;
        int drawnOutputSlots = recipe.getOutputItems().size() + recipe.getOutputFluids().size();
        for (int i = 0; i < drawnOutputSlots; i++) {
            toBeDrawnSlots.add(left);
            left += 18;
        }
        return new DrawInfo(toBeDrawnSlots, arrowPos);
    }

    @Override
    public void set(@NotNull RecipeBuilder builder, @NotNull R recipe) {
        int drawnSlots = 0;
        drawnSlots += recipe.getInputItems().size() + recipe.getInputFluids().size() + recipe.getOutputFluids().size() + recipe.getOutputItems().size();
        int drawnWidth = drawnSlots * 18 + (11 + 22 + 11);
        int left = (WIDTH - drawnWidth) / 2 - 3;
        // Add the recipe inputs.
        for (ItemRecipeIngredient ingredient : recipe.getInputItems()) {
            builder.addInputSlot(left, 6, ingredient);
            left += 18;
        }
        for (FluidRecipeIngredient ingredient : recipe.getInputFluids()) {
            builder.addInputSlot(left, 6, ingredient);
            left += 18;
        }
        left += 11;
        left += 22 + 11;
        for (ItemStack itemStack : recipe.getOutputItems()) {
            builder.addOutputSlot(left, 6, itemStack);
            left += 18;
        }
        for (FluidStack fluidStack : recipe.getOutputFluids()) {
            builder.addOutputSlot(left, 6, fluidStack);
            left += 18;
        }
    }
}