package io.github.laptop59.concocti.integration.jei;

import dev.emi.emi.api.widget.WidgetHolder;
import io.github.laptop59.concocti.client.ConcoctiClient;
import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.machine.RecipeBuilder;
import io.github.laptop59.concocti.common.machine.RecipeSlotFlags;
import io.github.laptop59.concocti.common.recipe.FluidRecipeIngredient;
import io.github.laptop59.concocti.common.recipe.ItemRecipeIngredient;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class JeiRecipeBuilder implements RecipeBuilder {
    WidgetHolder widgetHolder;

    IRecipeLayoutBuilder builder;
    IFocusGroup focuses;

    public JeiRecipeBuilder(@NotNull IRecipeLayoutBuilder builder, @NotNull IFocusGroup focuses) {
        this.builder = builder;
        this.focuses = focuses;
    }

    @Override
    public void addInputSlot(int x, int y, RecipeSlotFlags flags, boolean isFluidSlot) {
        addSlot(RecipeIngredientRole.INPUT, x, y, flags, 0, isFluidSlot);
    }

    @Override
    public void addCatalystSlot(int x, int y, RecipeSlotFlags flags, boolean isFluidSlot) {
        addSlot(RecipeIngredientRole.CATALYST, x, y, flags, 0, isFluidSlot);
    }

    @Override
    public void addOutputSlot(int x, int y, RecipeSlotFlags flags, float chance, boolean isFluidSlot) {
        IRecipeSlotBuilder giz = addSlot(RecipeIngredientRole.OUTPUT, x, y, flags, chance == 1f ? 0 : 18, isFluidSlot);
        if (chance != 1f) {
            giz.addRichTooltipCallback((view, tooltip) -> {
                tooltip.add(ConcoctiClient.getChanceComponent(chance));
            });
        }
    }

    private IRecipeSlotBuilder addSlot(RecipeIngredientRole role, int x, int y, RecipeSlotFlags flags, int slotV, boolean isFluidSlot) {
        int slotU = isFluidSlot ? 18 : 0;
        IRecipeSlotBuilder slot = builder.addSlot(role, x, y);
        slot.setFluidRenderer(1, false, 16, 16);
        IDrawableStatic slotTexture = ConcoctiJeiPlugin.JEI_HELPERS.getGuiHelper().createDrawable(
                ResourceLocation.fromNamespaceAndPath(Concocti.MODID, "textures/gui/recipe_viewer/widgets.png"),
                slotU, slotV, 18, 18
        );
        slot.setBackground(slotTexture, -1, -1);
        switch (flags.getInternalObject()) {
            case ItemRecipeIngredient ingredient -> slot.addItemStacks(ingredient.getStacks());
            case FluidRecipeIngredient ingredient -> ingredient.getStacks().forEach(stack -> slot.addFluidStack(
                    stack.getFluid(),
                    stack.getAmount(),
                    stack.getComponentsPatch()
            ));
            case ItemStack stack -> {
                if (!stack.isEmpty()) slot.addItemStack(stack);
            }
            case FluidStack stack -> {
                if (!stack.isEmpty()) slot.addFluidStack(
                    stack.getFluid(),
                    stack.getAmount(),
                    stack.getComponentsPatch()
                );
            }
            case Ingredient ingredient -> {
                if (!ingredient.isEmpty()) slot.addIngredients(ingredient);
            }
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
