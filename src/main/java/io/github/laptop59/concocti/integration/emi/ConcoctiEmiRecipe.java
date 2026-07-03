package io.github.laptop59.concocti.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.common.machine.AbstractConcoctiRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConcoctiEmiRecipe<R extends ProcessingRecipe<R, ? extends RecipeInput>> implements EmiRecipe {
    protected EmiRecipeCategory category;
    protected RecipeHolder<R> recipeHolder;
    protected AbstractConcoctiRecipeCategory<R> internalCategory;
    protected int[] indices;

    /** Indices must be specified if: <br>
     * - a {@link net.neoforged.neoforge.fluids.crafting.FluidIngredient} is used <br>
     * - a {@link io.github.laptop59.concocti.common.recipe.ItemRecipeIngredient} or {@link io.github.laptop59.concocti.common.recipe.FluidRecipeIngredient} is used.
     */
    ConcoctiEmiRecipe(EmiRecipeCategory category, RecipeHolder<R> recipeHolder, AbstractConcoctiRecipeCategory<R> internalCategory, int[] indices) {
        this.category = category;
        this.recipeHolder = recipeHolder;
        this.internalCategory = internalCategory;
        this.indices = indices;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    public R getRecipe() {
        return recipeHolder.value();
    }

    @Override
    public @Nullable ResourceLocation getId() {
        if (indices.length != 0) {
            StringBuilder path = new StringBuilder("/" + recipeHolder.id().getPath());
            for (int index : indices) {
                path.append('/');
                path.append(index);
            }
            return recipeHolder.id().withPath(path.toString());
        }
        return recipeHolder.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        ArrayList<EmiIngredient> list = new ArrayList<>();
        EmiRecipeBuilder emiRecipeBuilder = new EmiRecipeBuilder(list, null, null, null, this);
        emiRecipeBuilder.reset(); // VERY IMPORTANT!
        internalCategory.set(emiRecipeBuilder, recipeHolder.value()); // Adds inputs in this function
        return list;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        ArrayList<EmiIngredient> list = new ArrayList<>();
        EmiRecipeBuilder emiRecipeBuilder = new EmiRecipeBuilder(null, null, list, null, this);
        emiRecipeBuilder.reset(); // VERY IMPORTANT!
        internalCategory.set(emiRecipeBuilder, recipeHolder.value()); // Adds catalysts in this function
        return list;
    }

    @Override
    public List<EmiStack> getOutputs() {
        ArrayList<EmiStack> list = new ArrayList<>();
        EmiRecipeBuilder emiRecipeBuilder = new EmiRecipeBuilder(null, list, null, null, this);
        emiRecipeBuilder.reset(); // VERY IMPORTANT!
        internalCategory.set(emiRecipeBuilder, recipeHolder.value()); // Adds outputs in this function
        return list;
    }

    @Override
    public int getDisplayWidth() {
        return internalCategory.getWidth(recipeHolder.value());
    }

    @Override
    public int getDisplayHeight() {
        return internalCategory.getHeight(recipeHolder.value());
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.add(new Widget() {
            @Override
            public Bounds getBounds() {
                return new Bounds(0, 0, internalCategory.getWidth(recipeHolder.value()), internalCategory.getHeight(recipeHolder.value()));
            }

            @Override
            public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
                internalCategory.render(recipeHolder.value(), guiGraphics, mouseX, mouseY);
            }
        });
        EmiRecipeBuilder emiRecipeBuilder = new EmiRecipeBuilder(null, null, null, widgets, this);
        emiRecipeBuilder.reset(); // VERY IMPORTANT!
        internalCategory.set(emiRecipeBuilder, recipeHolder.value()); // Adds items last

        // Add tooltips
        widgets.add(new Widget() {
            @Override
            public Bounds getBounds() {
                return Bounds.EMPTY;
            }

            @Override
            public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
                ArrayList<Component> tooltips = new ArrayList<>();
                internalCategory.tooltip(tooltips, recipeHolder.value(), mouseX, mouseY);
                if (tooltips.isEmpty()) return;
                draw.renderComponentTooltip(
                    Minecraft.getInstance().font,
                    tooltips,
                    mouseX,
                    mouseY
                );
            }
        });
    }

    public int getRecipeIndex(int i) {
        if (indices.length == 0) return 0; // guaranteed to be valid (just for testing the actual indices max values)
        return indices[i];
    }

    @Nullable
    public RecipeHolder<R> getBackingRecipe() {
        return new RecipeHolder<>(recipeHolder.id(), recipeHolder.value());
    }
}
