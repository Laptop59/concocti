package io.github.laptop59.concocti.integration.jei;

import com.mojang.math.Transformation;
import io.github.laptop59.concocti.client.gui.components.RenderInfo;
import io.github.laptop59.concocti.client.gui.components.Renderable;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.recipe.ConcoctiElectronCollectorRecipe;
import io.github.laptop59.concocti.common.recipe.ConcoctiMelterRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.Ingredient;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiElectronCollectorRecipeCategory extends AbstractConcoctiRecipeCategory<ConcoctiElectronCollectorRecipe> {

    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/jei/concocti_electron_collector.png");

    public ConcoctiElectronCollectorRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper, new ItemStack(ConcoctiBlocks.CONCOCTI_ELECTRON_COLLECTOR.get()));
    }

    @Override
    public @NotNull RecipeType<ConcoctiElectronCollectorRecipe> getRecipeType() {
        return ConcoctiJeiPlugin.CONCOCTI_ELECTRON_COLLECTOR_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.concocti.concocti_electron_collector");
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull ConcoctiElectronCollectorRecipe recipe,
                           @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        // Show the duration, if needed.
        if (isCursorTouchingArrow(mouseX, mouseY, recipe)) {
            String chance = String.format("%.2f", recipe.getChance() * 100);
            tooltip.add(Component.translatable("screen.concocti.duration", (double) getTicks(recipe) / 20));
            tooltip.add(Component.translatable("screen.concocti.chance", chance));
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return texture;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ConcoctiElectronCollectorRecipe recipe, @NotNull IFocusGroup focuses) {
        // Add the fluid output.
        builder.addSlot(RecipeIngredientRole.OUTPUT, 113, 6)
                .addIngredient(NeoForgeTypes.FLUID_STACK, recipe.getOutputFluid())
                .setSlotName("output_fluid");

        ItemStack rod = new ItemStack(ConcoctiItems.CONDUCTIVIUM_LIGHTNING_ROD.get());
        ArrayList<MutableComponent> mutableComponents = new ArrayList<>();

        mutableComponents.add(Component.translatable("screen.concocti.requirements").withColor(0xC7C7C7));
        mutableComponents.add(Component.translatable("screen.concocti.on_top_of_machine").withColor(0xEEEEEE));
        mutableComponents.add(Component.translatable("screen.concocti.struck_by_lightning").withColor(0xEEEEEE));

        mutableComponents.replaceAll(mutableComponent -> mutableComponent.withStyle(
                mutableComponent.getStyle().withItalic(false)
        ));

        ArrayList<Component> components = new ArrayList<>(mutableComponents.size());
        components.addAll(mutableComponents);

        ItemLore itemLore = new ItemLore(components);
        rod.set(DataComponents.LORE, itemLore);

        builder.addSlot(RecipeIngredientRole.CATALYST, 40, 6)
                .setSlotName("lightning_rod")
                .addIngredients(Ingredient.of(rod));
    }
}
