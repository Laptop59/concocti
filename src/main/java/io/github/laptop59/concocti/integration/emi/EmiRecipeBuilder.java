package io.github.laptop59.concocti.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.machine.RecipeBuilder;
import io.github.laptop59.concocti.common.machine.RecipeSlotFlags;
import io.github.laptop59.concocti.common.recipe.FluidRecipeIngredient;
import io.github.laptop59.concocti.common.recipe.ItemRecipeIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EmiRecipeBuilder implements RecipeBuilder {
    List<EmiIngredient> inputs;
    List<EmiStack> outputs;
    List<EmiIngredient> catalysts;
    WidgetHolder widgetHolder;
    ConcoctiEmiRecipe<?> emiRecipe;

    int ingredientIndex = 0;
    ArrayList<Integer> numberOfIndices = new ArrayList<>();

    public EmiRecipeBuilder(@Nullable List<EmiIngredient> inputs, @Nullable List<EmiStack> outputs, @Nullable List<EmiIngredient> catalysts, @Nullable WidgetHolder widgetHolder, @NotNull ConcoctiEmiRecipe<?> emiRecipe) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.catalysts = catalysts;
        this.widgetHolder = widgetHolder;
        this.emiRecipe = emiRecipe;
    }

    @Override
    public void reset() {
        Collections.fill(numberOfIndices, 0);
    }

    @Override
    public void addInputSlot(int x, int y, RecipeSlotFlags flags, boolean isFluidSlot) {
        createSlot(x, y, flags, 1f, isFluidSlot);
        addEmiIngredient(inputs, flags.getInternalObject(), flags.getRemainder(), ingredientIndex++);
    }

    public boolean isCatalyticInput(EmiIngredient emiIngredient) {
        return emiIngredient instanceof UnconsumedIngredient || emiIngredient instanceof UnconsumedStack;
    }

    @Override
    public void addCatalystSlot(int x, int y, RecipeSlotFlags flags, boolean isFluidSlot) {
        createSlot(x, y, flags, 1f, isFluidSlot);
        addEmiIngredient(catalysts, flags.getInternalObject(), flags.getRemainder(), ingredientIndex++);
    }

    @Override
    public void addOutputSlot(int x, int y, RecipeSlotFlags flags, float chance, boolean isFluidSlot) {
        createSlot(x, y, flags, chance, isFluidSlot);
        addEmiStack(outputs, flags.getInternalObject());
    }

    @Override
    public void addSolarInput(long amount) {
        addEmiIngredient(inputs, new SolarEmiStack(amount), null, ingredientIndex++);
    }

    @Override
    public void addSolarOutput(long amount) {
        addEmiStack(outputs, new SolarEmiStack(amount));
    }

    private void createSlot(int x, int y, RecipeSlotFlags flags, float chance, boolean isFluidSlot) {
        if (widgetHolder != null) {
            x--; //
            y--; // compensate, or else it looks off

            Object internalObject = flags.getInternalObject();

            EmiIngredient ingredient = intoIngredient(internalObject, flags.getRemainder(), emiRecipe, ingredientIndex);

            SlotWidget slot;

            if (ingredient == null || internalObject == null) {
                slot = widgetHolder.addSlot(x, y);
            } else {
                ingredient.setChance(chance);
                slot = widgetHolder.addSlot(ingredient, x, y);
            }

            if (emiRecipe != null)
                slot.recipeContext(emiRecipe);

            boolean isChanceBased = chance != 1.0f;
            int u = isFluidSlot ? 18 : 0;
            int v = isChanceBased ? 18 : 0;
            slot.customBackground(
                    ResourceLocation.fromNamespaceAndPath(Concocti.MODID, "textures/gui/recipe_viewer/widgets.png"),
                    u,
                    v,
                    18,
                    18
            );
        }
    }

    private EmiIngredient addEmiIngredient(List<EmiIngredient> list, Object element, @Nullable ItemStack remainder, int i) {
        EmiIngredient ingredient = intoIngredient(element, remainder, emiRecipe, i);
        if (element == null) return ingredient;

        if (isCatalyticInput(ingredient)) list = catalysts;
        if (list != null) list.add(ingredient);

        return ingredient;
    }

    private EmiStack addEmiStack(List<EmiStack> list, Object element) {
        if (list == null) return null;

        EmiStack stack = switch (element) {
            case ItemStack itemStack -> EmiStack.of(itemStack);
            case FluidStack fluidStack -> EmiStack.of(fluidStack.getFluid(), fluidStack.getComponentsPatch(), fluidStack.getAmount());
            case null, default -> EmiStack.of(Items.BARRIER);
        };

        if (stack == null || element == null) {
            stack = EmiStack.EMPTY;
        }

        if (!stack.equals(EmiStack.EMPTY)) list.add(stack);
        return stack;
    }

    private EmiIngredient intoIngredient(Object element, @Nullable ItemStack remainder, ConcoctiEmiRecipe<?> recipe, int i) {
        EmiIngredient ingredient = switch (element) {
            case ItemStack itemStack -> EmiStack.of(itemStack);
            case FluidStack fluidStack -> EmiStack.of(fluidStack.getFluid(), fluidStack.getComponentsPatch(), fluidStack.getAmount());
            case ItemRecipeIngredient ingredient1 -> {
                List<EmiIngredient> ingredients = ItemRecipeEmiIngredient.wrap(ingredient1);
                while (i >= numberOfIndices.size()) numberOfIndices.add(0);
                numberOfIndices.set(i, ingredients.size());
                yield ingredients.get(recipe.getRecipeIndex(i));
            }
            case FluidRecipeIngredient ingredient1 -> {
                List<EmiIngredient> ingredients = FluidRecipeEmiIngredient.wrap(ingredient1);
                while (i >= numberOfIndices.size()) numberOfIndices.add(0);
                numberOfIndices.set(i, ingredients.size());
                yield ingredients.get(recipe.getRecipeIndex(i));
            }
            case Ingredient ingredient1 -> EmiIngredient.of(ingredient1);
            case SolarEmiStack solar -> solar;
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
