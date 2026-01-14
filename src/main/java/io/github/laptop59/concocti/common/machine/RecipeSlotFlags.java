package io.github.laptop59.concocti.common.machine;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.laptop59.concocti.common.recipe.FluidOutput;
import io.github.laptop59.concocti.common.recipe.FluidRecipeIngredient;
import io.github.laptop59.concocti.common.recipe.ItemOutput;
import io.github.laptop59.concocti.common.recipe.ItemRecipeIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/** Currently, internal object can only be of the following:<br>
 * ItemRecipeIngredient,
 * FluidRecipeIngredient,
 * ItemStack,
 * FluidStack,
 * Ingredient
 */
public class RecipeSlotFlags {
    @Nullable Object internalObject = null;
    @Nullable ItemStack remainder = null;

    /** Creates flags with the default settings. */
    public RecipeSlotFlags() {}

    /** Creates flags with the default settings and populated by the ingredient given. */
    public RecipeSlotFlags(@Nullable ItemRecipeIngredient ingredient) {
        internalObject = ingredient;
    }

    /** Creates flags with the default settings and populated by the ingredient given. */
    public RecipeSlotFlags(@Nullable FluidRecipeIngredient ingredient) {
        internalObject = ingredient;
    }

    /** Creates flags with the default settings and populated by the ingredient given. */
    public RecipeSlotFlags(@Nullable ItemStack stack) {
        internalObject = stack;
    }

    /** Creates flags with the default settings and populated by the ingredient given. */
    public RecipeSlotFlags(@Nullable FluidStack stack) {
        internalObject = stack;
    }

    /** Creates flags with the default settings and populated by the ingredient given. */
    public RecipeSlotFlags(@Nullable Ingredient ingredient) {
        internalObject = ingredient;
    }

    public RecipeSlotFlags withRemainder(@Nullable ItemStack remainder) {
        this.remainder = remainder;
        return this;
    }

    public @Nullable ItemStack getRemainder() {
        return remainder;
    }

    public @Nullable Object getInternalObject() {
        return internalObject;
    }

    public boolean isItemLike() {
        return internalObject instanceof ItemRecipeIngredient || internalObject instanceof ItemOutput || internalObject instanceof ItemStack;
    }

    public boolean isFluidLike() {
        return internalObject instanceof FluidRecipeIngredient || internalObject instanceof FluidOutput || internalObject instanceof FluidStack;
    }
}
