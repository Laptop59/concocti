package io.github.laptop59.concocti.integration.emi;

import com.google.common.collect.Lists;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static io.github.laptop59.concocti.client.ConcoctiClient.UNCONSUMED;

public class UnconsumedStack extends EmiStack {
    EmiStack stack;

    public UnconsumedStack(EmiStack stack) {
        this.stack = stack;
    }

    @Override
    public List<EmiStack> getEmiStacks() {
        return stack.getEmiStacks();
    }

    public EmiStack getRemainder() {
        return stack.getRemainder();
    }

    public EmiStack setRemainder(EmiStack stack) {
        if (stack instanceof UnconsumedStack unconsumedStack && unconsumedStack.stack == this.stack)
            return stack.setRemainder(this.stack.copy());
        this.stack.setRemainder(stack);
        return this;
    }

    public EmiStack comparison(Function<Comparison, Comparison> comparison) {
        return stack.comparison(comparison);
    }

    public EmiStack comparison(Comparison comparison) {
        return stack.comparison(comparison);
    }

    public long getAmount() {
        return stack.getAmount();
    }

    public EmiStack setAmount(long amount) {
        stack.setAmount(amount);
        return this;
    }

    public float getChance() {
        return stack.getChance();
    }

    public EmiStack setChance(float chance) {
        stack.setChance(chance);
        return this;
    }

    public <T> @Nullable T get(DataComponentType<? extends T> type) {
        return stack.get(type);
    }

    public <T> T getOrDefault(DataComponentType<? extends T> type, T fallback) {
        return stack.getOrDefault(type, fallback);
    }

    public <T> @Nullable T getKeyOfType(Class<T> clazz) {
        return stack.getKeyOfType(clazz);
    }

    public ItemStack getItemStack() {
        return stack.getItemStack();
    }

    public boolean isEqual(EmiStack stack) {
        if (stack instanceof UnconsumedStack unconsumedStack) {
            return unconsumedStack.stack.isEqual(stack);
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UnconsumedStack unconsumedStack) {
            return unconsumedStack.stack.equals(stack);
        }
        return false;
    }

    @Override
    public EmiStack copy() {
        return new UnconsumedStack(stack.copy());
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
        stack.render(draw, x, y, delta, flags);
        EmiRender.renderCatalystIcon(this, draw, x, y);
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public DataComponentPatch getComponentChanges() {
        return stack.getComponentChanges();
    }

    @Override
    public Object getKey() {
        return stack.getKey();
    }

    @Override
    public ResourceLocation getId() {
        return stack.getId();
    }

    @Override
    public List<Component> getTooltipText() {
        ArrayList<Component> components = new ArrayList<>(stack.getTooltipText());
        components.add(UNCONSUMED);
        return components;
    }

    public List<ClientTooltipComponent> getTooltip() {
        ArrayList<ClientTooltipComponent> components = new ArrayList<>(stack.getTooltip());
        components.add(ClientTooltipComponent.create(UNCONSUMED.getVisualOrderText()));
        return components;
    }

    @Override
    public Component getName() {
        return stack.getName();
    }
}
