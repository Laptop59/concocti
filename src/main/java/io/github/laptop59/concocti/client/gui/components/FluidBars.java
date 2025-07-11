package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public final class FluidBars {
    public static class Tall<T extends AbstractContainerMenu> extends AbstractFluidBar<T> {
        public Tall(int guiLeft, int guiTop, AbstractContainerScreen<T> screen, T menu, ResourceLocation fluid, int id) {
            super(guiLeft, guiTop, screen, menu, fluid, id);
        }

        public Tall(int guiLeft, int guiTop, AbstractContainerScreen<T> screen, T menu, int id) {
            super(guiLeft, guiTop, screen, menu, id);
        }

        @Override
        protected int getBarHeight() {
            return 42;
        }

        @Override
        protected ResourceLocation getFrameTexture() {
            return ResourceLocation.fromNamespaceAndPath(MODID, "container/fluids/base_tall");
        }
    }

    public static class Square<T extends AbstractContainerMenu>extends AbstractFluidBar<T> {
        public Square(int guiLeft, int guiTop, AbstractContainerScreen<T> screen, T menu, ResourceLocation fluid, int id) {
            super(guiLeft, guiTop, screen, menu, fluid, id);
        }

        public Square(int guiLeft, int guiTop, AbstractContainerScreen<T> screen, T menu, int id) {
            super(guiLeft, guiTop, screen, menu, id);
        }

        @Override
        protected int getBarHeight() {
            return 18;
        }

        @Override
        protected ResourceLocation getFrameTexture() {
            return ResourceLocation.fromNamespaceAndPath(MODID, "container/fluids/base_square");
        }
    }
}