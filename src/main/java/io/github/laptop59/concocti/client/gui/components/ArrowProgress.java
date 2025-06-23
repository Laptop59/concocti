package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A class to show the progress of a block's action via an arrow.
 * @param <T>
 */
public class ArrowProgress<T extends AbstractContainerMenu> implements MenuAccess<T> {
    private final ResourceLocation baseSprite = ResourceLocation.fromNamespaceAndPath(MODID, "container/arrow_progress/base");
    private final ResourceLocation progressSprite = ResourceLocation.fromNamespaceAndPath(MODID, "container/arrow_progress/progress");

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
        guiGraphics.blitSprite(this.baseSprite, 22, 15, 0, 0, screen.getGuiLeft() + guiLeft, screen.getGuiTop() + guiTop, 22, 15);
        guiGraphics.blitSprite(this.progressSprite, 22, 16, 0, 0,
                screen.getGuiLeft() + guiLeft, screen.getGuiTop() + guiTop - 1, (int) Math.ceil(progress * 22), 16);
    }

    @Override
    public @NotNull T getMenu() {
        return menu;
    }
}
