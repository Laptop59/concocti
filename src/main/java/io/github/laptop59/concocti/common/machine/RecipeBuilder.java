package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.common.recipe.FluidOutput;
import io.github.laptop59.concocti.common.recipe.FluidRecipeIngredient;
import io.github.laptop59.concocti.common.recipe.ItemOutput;
import io.github.laptop59.concocti.common.recipe.ItemRecipeIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

/** Aims to unify code of different recipe mods. */
public interface RecipeBuilder {

    default void addInputSlot(int x, int y, boolean isFluidSlot) {
        addInputSlot(x, y, new RecipeSlotFlags(), isFluidSlot);
    }

    default void addCatalystSlot(int x, int y, boolean isFluidSlot) {
        addCatalystSlot(x, y, new RecipeSlotFlags(), isFluidSlot);
    }

    default void addOutputSlot(int x, int y, boolean isFluidSlot) {
        addOutputSlot(x, y, new RecipeSlotFlags(), 1f, isFluidSlot);
    }

    default void addInputSlot(int x, int y, ItemRecipeIngredient ingredient) {
        addInputSlot(x, y, new RecipeSlotFlags(ingredient), false);
    }

    default void addCatalystSlot(int x, int y, ItemRecipeIngredient ingredient) {
        addCatalystSlot(x, y, new RecipeSlotFlags(ingredient), false);
    }

    default void addInputSlot(int x, int y, FluidRecipeIngredient ingredient) {
        addInputSlot(x, y, new RecipeSlotFlags(ingredient), true);
    }

    default void addCatalystSlot(int x, int y, FluidRecipeIngredient ingredient) {
        addCatalystSlot(x, y, new RecipeSlotFlags(ingredient), true);
    }

    default void addOutputSlot(int x, int y, ItemStack stack) {
        addOutputSlot(x, y, new RecipeSlotFlags(stack), 1f, false);
    }

    default void addOutputSlot(int x, int y, FluidStack stack) {
        addOutputSlot(x, y, new RecipeSlotFlags(stack), 1f, true);
    }

    default void addOutputSlot(int x, int y, ItemStack stack, float chance) {
        addOutputSlot(x, y, new RecipeSlotFlags(stack), chance, false);
    }

    default void addOutputSlot(int x, int y, FluidStack stack, float chance) {
        addOutputSlot(x, y, new RecipeSlotFlags(stack), chance, true);
    }

    default void addOutputSlot(int x, int y, ItemOutput output) {
        addOutputSlot(x, y, new RecipeSlotFlags(output.stack()), output.chance(), false);
    }

    default void addOutputSlot(int x, int y, FluidOutput output) {
        addOutputSlot(x, y, new RecipeSlotFlags(output.stack()), output.chance(), true);
    }

    default void addInputSlot(int x, int y, Ingredient ingredient) {
        addInputSlot(x, y, new RecipeSlotFlags(ingredient), false);
    }

    default void addInputSlot(int x, int y, Ingredient ingredient, ItemStack remainder) {
        addInputSlot(x, y, new RecipeSlotFlags(ingredient).withRemainder(remainder), false);
    }

    default void addCatalystSlot(int x, int y, Ingredient ingredient) {
        addInputSlot(x, y, new RecipeSlotFlags(ingredient), false);
    }

    void addInputSlot(int x, int y, RecipeSlotFlags flags, boolean isFluidSlot);
    void addCatalystSlot(int x, int y, RecipeSlotFlags flags, boolean isFluidSlot);
    void addOutputSlot(int x, int y, RecipeSlotFlags flags, float chance, boolean isFluidSlot);
    default void reset() {}
}
