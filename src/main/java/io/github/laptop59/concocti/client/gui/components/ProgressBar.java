package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A class to show the progress bar of a multiblock machine in a GUI.
 */
public class ProgressBar<T extends AbstractContainerMenu> extends Renderable {
    public static final ResourceLocation PROGRESS_BAR_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/progress_bar/bar");
    public static final ResourceLocation PROGRESS_BAR_SPEEDY_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/progress_bar/speedy");
    public static final ResourceLocation PROGRESS_BAR_BASE_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/progress_bar/base");

    T menu;
    AbstractContainerScreen<T> screen;

    float progress;
    boolean speedy;

    public ProgressBar(int guiLeft, int guiTop, AbstractContainerScreen<T> screen, T menu) {
        super(guiLeft, guiTop);
        this.screen = screen;
        this.menu = menu;
    }

    public void update(float progress, boolean speedy) {
        this.progress = progress;
        this.speedy = speedy;
    }


    @Override
    protected void render(GuiGraphics guiGraphics, RenderInfo renderInfo) {
        int x = screen.getGuiLeft() + guiLeft - 1;
        int y = screen.getGuiTop() + guiTop - 1;

        int progressWidth = (int) (progress * 144);

        guiGraphics.blitSprite(
                PROGRESS_BAR_BASE_SPRITE,
                144, 6,
                0, 0,
                x, y,
                144, 6
        );
        guiGraphics.blitSprite(
                speedy ? PROGRESS_BAR_SPEEDY_SPRITE : PROGRESS_BAR_SPRITE,
                144, 6,
                0, 0,
                x, y,
                progressWidth, 6
        );
    }

    @Override
    public int getWidth() {
        return 6;
    }

    @Override
    public int getHeight() {
        return 144;
    }
}
