package io.github.laptop59.concocti.integration.emi;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.laptop59.concocti.common.recipe.FluidOption;
import io.github.laptop59.concocti.common.recipe.FluidRecipeIngredient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.Arrays;
import java.util.List;

public final class FluidRecipeEmiIngredient extends RecipeEmiIngredient implements EmiIngredient {
    private final FluidRecipeIngredient ingredient;
    private List<EmiStack> emiCachedStacks;
    private long amount = 1;

    private FluidRecipeEmiIngredient(FluidRecipeIngredient ingredient) {
        this.ingredient = ingredient;
    }

    public static FluidRecipeEmiIngredient wrap(FluidRecipeIngredient ingredient) {
        return new FluidRecipeEmiIngredient(ingredient);
    }

    @Override
    public List<EmiStack> getEmiStacks() {
        if (emiCachedStacks == null)
            emiCachedStacks = getEmiStacksUncached();
        return emiCachedStacks;
    }

    @Override
    public EmiIngredient copy() {
        FluidRecipeEmiIngredient ingredient1 = wrap(ingredient);
        ingredient1.setAmount(amount);
        ingredient1.emiCachedStacks = emiCachedStacks;
        return ingredient1;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public EmiIngredient setAmount(long amount) {
        this.amount = amount;
        emiCachedStacks = null; // Make it such that the stacks are generated correctly with the updated amount.
        return this;
    }

    @Override
    public float getChance() {
        return 1;
    }

    @Override
    public EmiIngredient setChance(float chance) {
        return this;
    }

    private List<EmiStack> getEmiStacksUncached() {
        return ingredient.options()
            .stream()
            .map(this::intoEmiStackUncached)
            .flatMap(List::stream)
            .toList();
    }

    private List<EmiStack> intoEmiStackUncached(FluidOption option) {
        return Arrays.stream(option.ingredient().getStacks())
            .map(ingredient -> EmiStack.of(ingredient.getFluid(), ingredient.getComponentsPatch(), this.amount * option.amount()))
            .toList();
    }
}
