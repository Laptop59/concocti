package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A class to show the progress of a block's action via an arrow.
 */
public class ArrowProgress extends Renderable {
    public static final ResourceLocation BASE_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/arrow_progress/base");
    public static final ResourceLocation PROGRESS_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/arrow_progress/progress");

    float progress;

    public ArrowProgress(int guiLeft, int guiTop) {
        super(guiLeft, guiTop);
    }

    protected void render(GuiGraphics guiGraphics, RenderInfo renderInfo) {
        guiGraphics.blitSprite(BASE_SPRITE, 22, 15, 0, 0, renderInfo.left(), renderInfo.top() + 1, 22, 15);
        guiGraphics.blitSprite(PROGRESS_SPRITE, 22, 16, 0, 0,
                renderInfo.left(), renderInfo.top(), (int) Math.min(22.0, Math.ceil(progress * 22)), 16);
    }

    @Override
    public int getWidth() {
        return 22;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    public void update(float progress) {
        this.progress = progress;
    }
}
