package io.github.laptop59.concocti.common.machine;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.laptop59.concocti.common.recipe.FluidRecipeIngredient;
import io.github.laptop59.concocti.common.recipe.ItemRecipeIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

/** Aims to unify code of different recipe mods. */
public interface RecipeBuilder {

    default void addInputSlot(int x, int y) {
        addInputSlot(x, y, new RecipeSlotFlags());
    }

    default void addCatalystSlot(int x, int y) {
        addCatalystSlot(x, y, new RecipeSlotFlags());
    }

    default void addOutputSlot(int x, int y) {
        addOutputSlot(x, y, new RecipeSlotFlags(), 1f);
    }

    default void addInputSlot(int x, int y, ItemRecipeIngredient ingredient) {
        addInputSlot(x, y, new RecipeSlotFlags(ingredient));
    }

    default void addCatalystSlot(int x, int y, ItemRecipeIngredient ingredient) {
        addCatalystSlot(x, y, new RecipeSlotFlags(ingredient));
    }

    default void addInputSlot(int x, int y, FluidRecipeIngredient ingredient) {
        addInputSlot(x, y, new RecipeSlotFlags(ingredient));
    }

    default void addInputSlot(int x, int y, FluidRecipeIngredient ingredient, ItemStack remainder) {
        addInputSlot(x, y, new RecipeSlotFlags(ingredient).withRemainder(remainder));
    }

    default void addCatalystSlot(int x, int y, FluidRecipeIngredient ingredient) {
        addCatalystSlot(x, y, new RecipeSlotFlags(ingredient));
    }

    default void addOutputSlot(int x, int y, ItemStack stack) {
        addOutputSlot(x, y, new RecipeSlotFlags(stack), 1f);
    }

    default void addOutputSlot(int x, int y, FluidStack stack) {
        addOutputSlot(x, y, new RecipeSlotFlags(stack), 1f);
    }

    default void addOutputSlot(int x, int y, ItemStack stack, float chance) {
        addOutputSlot(x, y, new RecipeSlotFlags(stack), chance);
    }

    default void addOutputSlot(int x, int y, FluidStack stack, float chance) {
        addOutputSlot(x, y, new RecipeSlotFlags(stack), chance);
    }

    default void addInputSlot(int x, int y, Ingredient ingredient) {
        addInputSlot(x, y, new RecipeSlotFlags(ingredient));
    }

    default void addInputSlot(int x, int y, Ingredient ingredient, ItemStack remainder) {
        addInputSlot(x, y, new RecipeSlotFlags(ingredient).withRemainder(remainder));
    }

    default void addCatalystSlot(int x, int y, Ingredient ingredient) {
        addInputSlot(x, y, new RecipeSlotFlags(ingredient));
    }

    void addInputSlot(int x, int y, RecipeSlotFlags flags);
    void addCatalystSlot(int x, int y, RecipeSlotFlags flags);
    void addOutputSlot(int x, int y, RecipeSlotFlags flags, float chance);
}
