package io.github.laptop59.concocti.common.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

public class LightningRecipeInput implements RecipeInput {
    protected LightningState lightningState;

    public LightningRecipeInput(LightningState lightningState) {
        this.lightningState = lightningState;
    }

    public boolean test() {
        return lightningState.getLightningCollected();
    }

    public void consume() {
        lightningState.setLightningCollected(false);
    }

    @Override
    public @NotNull ItemStack getItem(int index) {
        throw new UnsupportedOperationException("LightningInput has zero item slots.");
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return !lightningState.getLightningCollected();
    }

}
