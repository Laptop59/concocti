package io.github.laptop59.concocti.integration.jei;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.recipe.ConcoctiMelterRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiMelterRecipeCategory extends AbstractConcoctiRecipeCategory<ConcoctiMelterRecipe> {

    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/jei/concocti_melter.png");

    public ConcoctiMelterRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper, new ItemStack(ConcoctiBlocks.CONCOCTI_MELTER.get()));
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
    protected ResourceLocation getTexture() {
        return texture;
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
