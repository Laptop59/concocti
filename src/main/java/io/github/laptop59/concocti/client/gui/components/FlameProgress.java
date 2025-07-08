package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A class to show the progress of a block's action via an arrow.
 */
public class FlameProgress extends Renderable {
    public static final ResourceLocation BASE_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/flame/base");
    public static final ResourceLocation PROGRESS_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/flame/progress");

    float progress;

    public FlameProgress(int guiLeft, int guiTop) {
        super(guiLeft, guiTop);
    }

    protected void render(GuiGraphics guiGraphics, RenderInfo renderInfo) {
        int flameHeight = progress == 0.0 ? 0 : (int) Math.min(14.0, Math.ceil((1 - progress) * 14));
        guiGraphics.blitSprite(BASE_SPRITE, 14, 14, 0, 0, renderInfo.left(), renderInfo.top() + 1, 14, 14);
        guiGraphics.blitSprite(PROGRESS_SPRITE, 14, 14, 0, 14 - flameHeight,
                renderInfo.left(), renderInfo.top() - flameHeight + 14, 14, flameHeight);
    }

    @Override
    public int getWidth() {
        return 14;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    public void update(float progress) {
        this.progress = progress;
    }
}
