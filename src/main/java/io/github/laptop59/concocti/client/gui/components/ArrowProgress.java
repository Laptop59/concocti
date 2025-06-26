package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A class to show the progress of a block's action via an arrow.
 * @param <T>
 */
public class ArrowProgress<T extends AbstractContainerMenu> implements MenuAccess<T> {
    public static final ResourceLocation BASE_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/arrow_progress/base");
    public static final ResourceLocation PROGRESS_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/arrow_progress/progress");

    T menu;
    AbstractContainerScreen<T> screen;
    int guiLeft;
    int guiTop;

    public ArrowProgress(AbstractContainerScreen<T> screen, T menu, int guiLeft, int guiTop) {
        this.screen = screen;
        this.menu = menu;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
    }

    public void render(GuiGraphics guiGraphics, float progress) {
        ArrowProgress.render(guiGraphics, progress, screen.getGuiLeft() + guiLeft, screen.getGuiTop() + guiTop - 1);
    }

    public static void render(GuiGraphics guiGraphics, float progress, int left, int top) {
        guiGraphics.blitSprite(BASE_SPRITE, 22, 15, 0, 0, left, top + 1, 22, 15);
        guiGraphics.blitSprite(PROGRESS_SPRITE, 22, 16, 0, 0,
                left, top, (int) Math.min(22.0, Math.ceil(progress * 22)), 16);
    }

    @Override
    public @NotNull T getMenu() {
        return menu;
    }
}
