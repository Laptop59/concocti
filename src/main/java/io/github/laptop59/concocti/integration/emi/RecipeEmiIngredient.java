package io.github.laptop59.concocti.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.List;

public abstract class RecipeEmiIngredient implements EmiIngredient {
    protected EmiStack getDisplayingEmiStack() {
        return getEmiStacks().get(currentIndex(getEmiStacks().size()));
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
        getDisplayingEmiStack().render(draw, x, y, delta, flags);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        return EmiIngredient.of(getEmiStacks()).getTooltip();
    }

    public int currentIndex(int size) {
        return Math.toIntExact(System.currentTimeMillis() / 1000 % size);
    }
}
