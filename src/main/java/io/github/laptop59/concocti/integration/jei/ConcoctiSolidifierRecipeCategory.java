package io.github.laptop59.concocti.integration.jei;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.recipe.ConcoctiSolidifierRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiSolidifierRecipeCategory extends AbstractConcoctiRecipeCategory<ConcoctiSolidifierRecipe> {

    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/jei/concocti_solidifier.png");

    public ConcoctiSolidifierRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper, new ItemStack(ConcoctiBlocks.CONCOCTI_SOLIDIFIER.get()));
    }

    @Override
    public @NotNull RecipeType<ConcoctiSolidifierRecipe> getRecipeType() {
        return ConcoctiJeiPlugin.CONCOCTI_SOLIDIFIER_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.concocti.concocti_solidifier");
    }

    @Override
    protected ResourceLocation getTexture() {
        return texture;
    }

    @Override
    protected int getHorizontalArrowOffset() { return 16; }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, ConcoctiSolidifierRecipe recipe, @NotNull IFocusGroup focuses) {
        // Add the recipe inputs (fluid + slot + base item).
        addSizedFluidIngredientSlot(builder, RecipeIngredientRole.INPUT, 12, 6, "input_fluid", recipe.getInputFluid());
        builder.addSlot(RecipeIngredientRole.CATALYST, 30, 6)
                .addItemStacks(Arrays.asList(recipe.getMold().getItems()))
                .setSlotName("mold");
        builder.addSlot(RecipeIngredientRole.INPUT, 48, 6)
                .addItemStacks(Arrays.asList(recipe.getBaseItem().getItems()))
                .setSlotName("base_item");
        // Add the item output.
        builder.addSlot(RecipeIngredientRole.OUTPUT, 137 - 9, 6)
                .addIngredient(VanillaTypes.ITEM_STACK, recipe.getOutputItem())
                .setSlotName("output");
    }
}
