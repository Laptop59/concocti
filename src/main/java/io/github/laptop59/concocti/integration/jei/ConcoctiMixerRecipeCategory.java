package io.github.laptop59.concocti.integration.jei;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.recipe.ConcoctiMixerRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiMixerRecipeCategory extends AbstractConcoctiRecipeCategory<ConcoctiMixerRecipe> {
    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/jei/concocti_mixer.png");
    private final int WIDTH = 176;

    public ConcoctiMixerRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper, new ItemStack(ConcoctiBlocks.CONCOCTI_MIXER.get()));
    }

    @Override
    public @NotNull RecipeType<ConcoctiMixerRecipe> getRecipeType() {
        return ConcoctiJeiPlugin.CONCOCTI_MIXER_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.concocti.concocti_mixer");
    }

    @Override
    protected ResourceLocation getTexture() {
        return texture;
    }

    public record DrawInfo(List<Integer> slots, int arrowPos) {}

    public DrawInfo createDrawInfo(ConcoctiMixerRecipe recipe) {
        int drawnSlots = 0;
        if (recipe.getOutputItem() != null && !recipe.getOutputItem().isEmpty()) drawnSlots++;
        if (recipe.getOutputFluid() != null && !recipe.getOutputFluid().isEmpty()) drawnSlots++;
        drawnSlots += recipe.getInputItems().size() + recipe.getInputFluids().size();
        int drawnWidth = drawnSlots * 18 + (11 + 22 + 11);
        int left = (WIDTH - drawnWidth) / 2;
        ArrayList<Integer> toBeDrawnSlots = new ArrayList<>(drawnSlots);
        // Add the recipe inputs.
        for (SizedIngredient ingredient : recipe.getInputItems()) {
            toBeDrawnSlots.add(left);
            left += 18;
        }
        for (SizedFluidIngredient ingredient : recipe.getInputFluids()) {
            toBeDrawnSlots.add(left);
            left += 18;
        }
        left += 11;
        int arrowPos = left;
        left += 22 + 11;
        if (recipe.getOutputItem() != null && !recipe.getOutputItem().isEmpty()) {
            toBeDrawnSlots.add(left);
            left += 18;
        }
        if (recipe.getOutputFluid() != null && !recipe.getOutputFluid().isEmpty()) {
            toBeDrawnSlots.add(left);
        }
        return new DrawInfo(toBeDrawnSlots, arrowPos);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, ConcoctiMixerRecipe recipe, @NotNull IFocusGroup focuses) {
        int drawnSlots = 0;
        if (recipe.getOutputItem() != null && !recipe.getOutputItem().isEmpty()) drawnSlots++;
        if (recipe.getOutputFluid() != null && !recipe.getOutputFluid().isEmpty()) drawnSlots++;
        drawnSlots += recipe.getInputItems().size() + recipe.getInputFluids().size();
        int drawnWidth = drawnSlots * 18 + (11 + 22 + 11);
        int left = (WIDTH - drawnWidth) / 2;
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
        if (recipe.getOutputItem() != null && !recipe.getOutputItem().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, left, 6)
                    .addItemStack(recipe.getOutputItem())
                    .setSlotName("output_item");
            left += 18;
        }
        if (recipe.getOutputFluid() != null && !recipe.getOutputFluid().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, left, 6)
                    .addFluidStack(recipe.getOutputFluid().getFluid(), recipe.getOutputFluid().getAmount())
                    .setSlotName("output_fluid");
            // left += 18;
        }
    }

    @Override
    public void draw(@NotNull ConcoctiMixerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        DrawInfo drawInfo = createDrawInfo(recipe);
        for (int x : drawInfo.slots) {
            guiGraphics.blit(slot, x - 1, 6 - 1, 0, 0, 18, 18, 18, 18);
        }
    }

    @Override
    protected int getHorizontalArrowOffset(@NotNull ConcoctiMixerRecipe recipe) { return createDrawInfo(recipe).arrowPos - 72; }
}
