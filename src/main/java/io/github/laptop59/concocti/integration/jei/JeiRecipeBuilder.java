package io.github.laptop59.concocti.integration.jei;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.machine.RecipeBuilder;
import io.github.laptop59.concocti.common.machine.RecipeSlotFlags;
import io.github.laptop59.concocti.common.recipe.FluidRecipeIngredient;
import io.github.laptop59.concocti.common.recipe.ItemRecipeIngredient;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class JeiRecipeBuilder implements RecipeBuilder {
    WidgetHolder widgetHolder;

    IRecipeLayoutBuilder builder;
    IFocusGroup focuses;

    public JeiRecipeBuilder(@NotNull IRecipeLayoutBuilder builder, @NotNull IFocusGroup focuses) {
        this.builder = builder;
        this.focuses = focuses;
    }

    @Override
    public void addInputSlot(int x, int y, RecipeSlotFlags flags) {
        addSlot(RecipeIngredientRole.INPUT, x, y, flags);
    }

    @Override
    public void addCatalystSlot(int x, int y, RecipeSlotFlags flags) {
        addSlot(RecipeIngredientRole.CATALYST, x, y, flags);
    }

    @Override
    public void addOutputSlot(int x, int y, RecipeSlotFlags flags, float chance) {
        addSlot(RecipeIngredientRole.OUTPUT, x, y, flags);
    }

    private IRecipeSlotBuilder addSlot(RecipeIngredientRole role, int x, int y, RecipeSlotFlags flags) {
        IRecipeSlotBuilder slot = builder.addSlot(role, x, y);
        switch (flags.getInternalObject()) {
            case ItemRecipeIngredient ingredient -> slot.addItemStacks(ingredient.getStacks());
            case FluidRecipeIngredient ingredient -> ingredient.getStacks().forEach(stack -> slot.addFluidStack(
                stack.getFluid(),
                stack.getAmount(),
                stack.getComponentsPatch()
            ));
            case ItemStack stack -> slot.addItemStack(stack);
            case FluidStack stack -> slot.addFluidStack(
                stack.getFluid(),
                stack.getAmount(),
                stack.getComponentsPatch()
            );
            case Ingredient ingredient -> slot.addIngredients(ingredient);
            case null -> {}
            default -> Concocti.LOGGER.warn("Could not convert {} to an object required for JEI.", flags.getInternalObject());
        }
        return slot;
    }

    private <T> void add(List<T> list, T element) {
        if (list != null)
            list.add(element);
    }
}
