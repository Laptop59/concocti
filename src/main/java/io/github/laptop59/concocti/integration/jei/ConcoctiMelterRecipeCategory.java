package io.github.laptop59.concocti.integration.jei;

import io.github.laptop59.concocti.client.gui.components.ArrowProgress;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.recipe.ConcoctiMelterRecipe;
import io.github.laptop59.concocti.common.recipe.ConcoctiMelterRecipeBuilder;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiMelterRecipeCategory implements IRecipeCategory<ConcoctiMelterRecipe> {

    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/jei/concocti_melter.png");
    private final IDrawable icon;

    public ConcoctiMelterRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ConcoctiBlocks.CONCOCTI_MELTER.get()));
    }

    @Override
    public @NotNull RecipeType<ConcoctiMelterRecipe> getRecipeType() {
        return ConcoctiJeiPlugin.CONCOCTI_MELTER_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.concocti.concocti_melter");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 176 - 8;
    }

    @Override
    public int getHeight() {
        return 36 - 8;
    }

    @Override
    public void draw(@NotNull ConcoctiMelterRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int left = -4;
        int top = -4;
        // Draw the background texture.
        guiGraphics.blit(this.texture, left, top, 0, 0, 176, 36, 176, 36);
        // Draw the arrow progress.
        long absoluteTicks = System.currentTimeMillis() / 50;
        long passedTicks = absoluteTicks % recipe.getTicks();
        double progress = (double) passedTicks / recipe.getTicks();
        ArrowProgress.render(guiGraphics, (float) (progress * 23) / 22, left + 75, top + 10);
    }

    /** Returns whether the cursor is touching the animating arrow. */
    private boolean isCursorTouchingArrow(double mouseX, double mouseY) {
        double dx = mouseX - (75 - 4);
        double dy = mouseY - (10 - 4);
        return dx >= 0 && dx <= 22 && dy >= 0 && dy <= 16;
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull ConcoctiMelterRecipe recipe,
                           @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        // Show the duration, if needed.
        if (isCursorTouchingArrow(mouseX, mouseY))
            tooltip.add(Component.translatable("screen.concocti.duration", (double) recipe.getTicks() / 20));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ConcoctiMelterRecipe recipe, @NotNull IFocusGroup focuses) {
        // Add the recipe input.
        builder.addSlot(RecipeIngredientRole.INPUT, 22, 6)
                .addItemStacks(List.of(recipe.getInputItem().getItems()))
                .setSlotName("input");
        // Add the fluid outputs.
        FluidStack[] outputs = new FluidStack[]{recipe.getOutputPureFluid().copy(), recipe.getOutputByproductFluid().copy()};
        int i = 0;
        for (FluidStack fluid : outputs) {
            if (!fluid.isEmpty()) builder.addSlot(RecipeIngredientRole.OUTPUT, 113 + i * 18, 6)
                    .addIngredient(NeoForgeTypes.FLUID_STACK, fluid)
                    .setSlotName("fluid" + i);
            i++;
        }
    }
}
