package io.github.laptop59.concocti.integration.jei;

import io.github.laptop59.concocti.common.machine.impl.ConcoctiMixer;
import io.github.laptop59.concocti.common.recipe.AbstractConcoctiMultiblockRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractConcoctiMultiblockRecipeCategory<R extends AbstractConcoctiMultiblockRecipe<R>> extends AbstractConcoctiRecipeCategory<R> {

    private final int WIDTH = 176;

    public AbstractConcoctiMultiblockRecipeCategory(IGuiHelper guiHelper, ItemStack itemStack) {
        super(guiHelper, itemStack);
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
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, R recipe, @NotNull IFocusGroup focuses) {
        int drawnSlots = 0;
        drawnSlots += recipe.getInputItems().size() + recipe.getInputFluids().size() + recipe.getOutputFluids().size() + recipe.getOutputItems().size();
        int drawnWidth = drawnSlots * 18 + (11 + 22 + 11);
        int left = (WIDTH - drawnWidth) / 2 - 3;
        // Add the recipe inputs.
        int i = 1;
        for (SizedIngredient ingredient : recipe.getInputItems()) {
            builder.addSlot(RecipeIngredientRole.INPUT, left, 6)
                    .addItemStacks(Arrays.asList(ingredient.getItems()))
                    .setSlotName("input_item_" + i);
            i++;
            left += 18;
        }
        i = 1;
        for (SizedFluidIngredient ingredient : recipe.getInputFluids()) {
            IRecipeSlotBuilder slotBuilder =
                    builder.addSlot(RecipeIngredientRole.INPUT, left, 6)
                            .setSlotName("input_fluid_" + i);
            for (FluidStack fluidStack : ingredient.getFluids())
                slotBuilder.addFluidStack(fluidStack.getFluid(), fluidStack.getAmount());
            i++;
            left += 18;
        }
        left += 11;
        left += 22 + 11;
        i = 1;
        for (ItemStack itemStack : recipe.getOutputItems()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, left, 6)
                    .addItemStacks(List.of(itemStack))
                    .setSlotName("output_item_" + i);
            i++;
            left += 18;
        }
        i = 1;
        for (FluidStack fluidStack : recipe.getOutputFluids()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, left, 6)
                    .addFluidStack(fluidStack.getFluid(), fluidStack.getAmount())
                    .setSlotName("output_fluid_" + i);
            left += 18;
        }
    }
}