package io.github.laptop59.concocti.integration.jei;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.laptop59.concocti.client.gui.components.ArrowProgress;
import io.github.laptop59.concocti.client.gui.components.RenderInfo;
import io.github.laptop59.concocti.client.gui.components.Renderable;
import io.github.laptop59.concocti.common.machine.ConcoctiMachine;
import io.github.laptop59.concocti.common.machine.RecipeBuilder;
import io.github.laptop59.concocti.integration.emi.EmiRecipeBuilder;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.common.util.Lazy;
import io.github.laptop59.concocti.integration.emi.ConcoctiEmiRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.github.laptop59.concocti.client.gui.components.ArrowProgress.BASE_SPRITE;
import static io.github.laptop59.concocti.client.gui.components.ArrowProgress.PROGRESS_SPRITE;
import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A base class to implement common parts of all Concocti recipes.
 *
 * @param <R> The type of recipe represented by this category.
 */
public abstract class AbstractConcoctiRecipeCategory<R extends ProcessingRecipe<R, ? extends RecipeInput>> implements IRecipeCategory<R> {
    private final IDrawable icon;
    protected final ResourceLocation slot = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/jei/slot.png");

    private final ArrowProgress arrowProgress;
    private final Lazy<ResourceLocation> texture =
            new Lazy<>(this::getTexturePath);

    public AbstractConcoctiRecipeCategory(@Nullable IGuiHelper guiHelper, ItemStack icon) {
        this.icon = guiHelper == null ? null : guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, icon);
        arrowProgress = new ArrowProgress(-1_000_000_000, 10 - 4);
    }

    public AbstractConcoctiRecipeCategory(@Nullable IGuiHelper guiHelper, DeferredItem<Item> item) {
        this(guiHelper, item.toStack());
    }

    public ResourceLocation getTexturePath() {
        return ResourceLocation.fromNamespaceAndPath(
                MODID,
                "textures/gui/jei/" + getMachineInstance().ID + ".png"
        );
    }

    /**
     * Gets this category's recipe type.
     */
    @Override
    public abstract @NotNull RecipeType<R> getRecipeType();

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

    protected int getHorizontalArrowOffset(@NotNull R recipe) {
        return 0;
    }

    protected int getTotalArrowLeft(@NotNull R recipe) {
        return 75 - 4 + getHorizontalArrowOffset(recipe);
    }

    protected final ResourceLocation getTexture() {
        return texture.get();
    }

    @Override
    public void draw(@NotNull R recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int left = -4;
        int top = -4;
        // Draw the background texture.
        guiGraphics.blit(this.getTexture(), left, top, 0, 0, 176, 36, 176, 36);
        // Draw the arrow progress.
        long absoluteTicks = System.currentTimeMillis() / 50;
        int tickDuration = getTicks(recipe);
        long passedTicks = absoluteTicks % tickDuration;
        double progress = (double) passedTicks / tickDuration;
        arrowProgress.setGuiLeft(getTotalArrowLeft(recipe));
        arrowProgress.setGuiTop(6);
        arrowProgress.update((float) (progress * 23) / 22);
        Renderable.renderChildAbsolute(guiGraphics, RenderInfo.withNullifiedOffset(null), arrowProgress);
    }

    /**
     * Gets the duration of a recipe.
     */
    protected int getTicks(R recipe) {
        int tickDuration = 40;
        if (recipe instanceof ProcessingRecipe<?, ?> processingRecipe) {
            tickDuration = processingRecipe.getTicks();
        }
        return tickDuration;
    }

    /**
     * Returns whether the cursor is touching the animating arrow.
     */
    protected boolean isCursorTouchingArrow(double mouseX, double mouseY, @NotNull R recipe) {
        double dx = mouseX - getTotalArrowLeft(recipe);
        double dy = mouseY - (10 - 4);
        return dx >= 0 && dx <= 22 && dy >= 0 && dy <= 16;
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull R recipe,
                           @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        // Show the duration, if needed.
        if (isCursorTouchingArrow(mouseX, mouseY, recipe))
            tooltip.add(Component.translatable("screen.concocti.duration", (double) getTicks(recipe) / 20));
    }

    /**
     * Tells JEI how to display this type of recipe.
     */
    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull R recipe, @NotNull IFocusGroup focuses) {
        // Use the builder to add slots
        JeiRecipeBuilder jeiRecipeBuilder = new JeiRecipeBuilder(builder, focuses);
        set(jeiRecipeBuilder, recipe); // Adds slots in this function
    }

    /**
     * Sets the required recipe.
     */
    public abstract void set(@NotNull RecipeBuilder builder, @NotNull R recipe);

    /**
     * Gets the machine instance of the machine using this category
     */
    protected abstract ConcoctiMachine<?, ?, ?, ?, R, ?, ?, ?, ?> getMachineInstance();

    public void addEmiWidgets(ConcoctiEmiRecipe<R> recipe, WidgetHolder widgets) {
        ResourceLocation background = getTexture();
        widgets.add(new Widget() {
            @Override
            public Bounds getBounds() {
                return new Bounds(0, 0, getWidth(), getHeight());
            }

            @Override
            public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
                int left = -4;
                int top = -4;
                // Draw the background texture.
                guiGraphics.blit(background, left, top, 0, 0, 176, 36, 176, 36);
                // Draw the arrow progress.
                long absoluteTicks = System.currentTimeMillis() / 50;
                int tickDuration = getTicks(recipe.getRecipe());
                long passedTicks = absoluteTicks % tickDuration;
                double progress = (double) passedTicks / tickDuration;
                arrowProgress.setGuiLeft(getTotalArrowLeft(recipe.getRecipe()));
                arrowProgress.setGuiTop(6);
                arrowProgress.update((float) (progress * 23) / 22);
                Renderable.renderChildAbsolute(guiGraphics, RenderInfo.withNullifiedOffset(null), arrowProgress);
            }
        });

        EmiRecipeBuilder emiRecipeBuilder = new EmiRecipeBuilder(null, null, null, widgets, recipe);
        set(emiRecipeBuilder, recipe.getRecipe()); // Adds items last
    }

    public List<EmiIngredient> getEmiInputs(ConcoctiEmiRecipe<R> recipe) {
        ArrayList<EmiIngredient> list = new ArrayList<>();
        EmiRecipeBuilder emiRecipeBuilder = new EmiRecipeBuilder(list, null, null, null, recipe);
        set(emiRecipeBuilder, recipe.getRecipe()); // Adds inputs in this function
        return list;
    }

    public List<EmiIngredient> getEmiCatalyst(ConcoctiEmiRecipe<R> recipe) {
        ArrayList<EmiIngredient> list = new ArrayList<>();
        EmiRecipeBuilder emiRecipeBuilder = new EmiRecipeBuilder(null, null, list, null, recipe);
        set(emiRecipeBuilder, recipe.getRecipe()); // Adds catalysts in this function
        return list;
    }

    public List<EmiStack> getEmiOutputs(ConcoctiEmiRecipe<R> recipe) {
        ArrayList<EmiStack> list = new ArrayList<>();
        EmiRecipeBuilder emiRecipeBuilder = new EmiRecipeBuilder(null, list, null, null, recipe);
        set(emiRecipeBuilder, recipe.getRecipe()); // Adds outputs in this function
        return list;
    }
}
