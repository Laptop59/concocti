package io.github.laptop59.concocti.integration.emi;

import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.ArrayList;
import java.util.List;

import static io.github.laptop59.concocti.client.ConcoctiClient.UNCONSUMED;

public record UnconsumedIngredient(EmiIngredient ingredient) implements EmiIngredient {
    @Override
    public List<EmiStack> getEmiStacks() {
        return ingredient.getEmiStacks();
    }

    @Override
    public boolean isEmpty() {
        return ingredient.isEmpty();
    }

    @Override
    public EmiIngredient copy() {
        return new UnconsumedIngredient(ingredient.copy());
    }

    @Override
    public long getAmount() {
        return ingredient.getAmount();
    }

    @Override
    public EmiIngredient setAmount(long amount) {
        return ingredient.setAmount(amount);
    }

    @Override
    public float getChance() {
        return ingredient.getChance();
    }

    @Override
    public EmiIngredient setChance(float chance) {
        return ingredient.setChance(chance);
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
        ingredient.render(draw, x, y, delta, flags);
        EmiRender.renderCatalystIcon(this, draw, x, y);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        ArrayList<ClientTooltipComponent> components = new ArrayList<>();
        components.addAll(ingredient.getTooltip());
        components.add(ClientTooltipComponent.create(UNCONSUMED.getVisualOrderText()));
        return components;
    }
}
