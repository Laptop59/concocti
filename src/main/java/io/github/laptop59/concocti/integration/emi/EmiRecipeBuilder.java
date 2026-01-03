package io.github.laptop59.concocti.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.machine.RecipeBuilder;
import io.github.laptop59.concocti.common.machine.RecipeSlotFlags;
import io.github.laptop59.concocti.common.recipe.FluidRecipeIngredient;
import io.github.laptop59.concocti.common.recipe.ItemRecipeIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class EmiRecipeBuilder implements RecipeBuilder {
    List<EmiIngredient> inputs;
    List<EmiStack> outputs;
    List<EmiIngredient> catalysts;
    WidgetHolder widgetHolder;
    EmiRecipe emiRecipe;

    public EmiRecipeBuilder(@Nullable List<EmiIngredient> inputs, @Nullable List<EmiStack> outputs, @Nullable List<EmiIngredient> catalysts, @Nullable WidgetHolder widgetHolder, @Nullable EmiRecipe emiRecipe) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.catalysts = catalysts;
        this.widgetHolder = widgetHolder;
        this.emiRecipe = emiRecipe;
    }

    @Override
    public void addInputSlot(int x, int y, RecipeSlotFlags flags) {
        createSlot(x, y, flags, 1f);
        addEmiIngredient(inputs, flags.getInternalObject(), flags.getRemainder());
    }

    @Override
    public void addCatalystSlot(int x, int y, RecipeSlotFlags flags) {
        createSlot(x, y, flags, 1f);
        addEmiIngredient(catalysts, flags.getInternalObject(), flags.getRemainder());
    }

    @Override
    public void addOutputSlot(int x, int y, RecipeSlotFlags flags, float chance) {
        createSlot(x, y, flags, chance);
        addEmiStack(outputs, flags.getInternalObject());
    }

    private void createSlot(int x, int y, RecipeSlotFlags flags, float chance) {
        if (widgetHolder != null) {
            x--;
            y--; // compensate
            EmiIngredient ingredient = intoIngredient(flags.getInternalObject(), flags.getRemainder());

            SlotWidget slot;

            if (ingredient == null) {
                slot = widgetHolder.addSlot(x, y);
            } else {
                ingredient.setChance(chance);
                slot = widgetHolder.addSlot(ingredient, x, y);
            }

            if (emiRecipe != null)
                slot.recipeContext(emiRecipe);
        }
    }

    private void addEmiIngredient(List<EmiIngredient> list, Object element, @Nullable ItemStack remainder) {
        if (list == null) return;

        EmiIngredient ingredient = intoIngredient(element, remainder);

        list.add(ingredient);
    }

    private void addEmiStack(List<EmiStack> list, Object element) {
        if (list == null) return;

        EmiStack stack = switch (element) {
            case ItemStack itemStack -> EmiStack.of(itemStack);
            case FluidStack fluidStack -> EmiStack.of(fluidStack.getFluid(), fluidStack.getComponentsPatch(), fluidStack.getAmount());
            case null, default -> EmiStack.of(Items.BARRIER);
        };

        if (stack == null) {
            stack = EmiStack.EMPTY;
        }

        list.add(stack);
    }

    private EmiIngredient intoIngredient(Object element, @Nullable ItemStack remainder) {
        EmiIngredient ingredient = switch (element) {
            case ItemStack itemStack -> EmiStack.of(itemStack);
            case FluidStack fluidStack -> EmiStack.of(fluidStack.getFluid(), fluidStack.getComponentsPatch(), fluidStack.getAmount());
            case ItemRecipeIngredient ingredient1 -> ItemRecipeEmiIngredient.wrap(ingredient1);
            case FluidRecipeIngredient ingredient1 -> FluidRecipeEmiIngredient.wrap(ingredient1);
            case Ingredient ingredient1 -> EmiIngredient.of(ingredient1);
            case null -> EmiIngredient.of(Ingredient.EMPTY);
            default -> {
                Concocti.LOGGER.error("Could not convert {} into an EmiIngredient", element);
                yield EmiIngredient.of(Ingredient.of(Items.BARRIER));
            }
        };

        if (remainder != null && ingredient instanceof EmiStack emiStack) {
            emiStack.setRemainder(EmiStack.of(remainder));
        }

        return ingredient;
    }
}
