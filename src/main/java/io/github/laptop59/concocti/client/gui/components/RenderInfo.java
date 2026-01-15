package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public record RenderInfo(
        int mouseX,
        int mouseY,
        int left,
        int top,
        Font font,
        ArrayList<Block> blocks
) {
    public RenderInfo(int mouseX, int mouseY, int left, int top, Font font) {
        this(mouseX, mouseY, left, top, font, new ArrayList<>());
    }

    public void push(Block block) {
        blocks.add(block);
    }

    public void pop() {
        blocks.removeLast();
    }

    /**
     * A structure that allows blocking of tooltips at certain regions.
     */
    public record Block(int left, int top, int width, int height) {
    }

    /**
     * Renders a tooltip conditionally based on the current {@code Block}s.
     *
     * @param guiGraphics Graphics object to use for rendering.
     * @param text        The text of the tooltip to render.
     */
    public void renderTooltip(GuiGraphics guiGraphics, Component text) {
        for (Block block : blocks()) {
            if (mouseX > block.left &&
                    mouseX < block.left + block.width &&
                    mouseY > block.top &&
                    mouseY < block.top + block.height) return;
        }
        guiGraphics.renderTooltip(font, List.of(text.getVisualOrderText()), mouseX, mouseY);
    }

    /**
     * Renders a tooltip conditionally based on the current {@code Block}s.
     *
     * @param guiGraphics Graphics object to use for rendering.
     * @param text        The text of the tooltip to render, with each component given in the list being rendered in a different line.
     */
    public void renderTooltip(GuiGraphics guiGraphics, List<Component> text) {
        for (Block block : blocks()) {
            if (mouseX > block.left &&
                    mouseX < block.left + block.width &&
                    mouseY > block.top &&
                    mouseY < block.top + block.height) return;
        }
        guiGraphics.renderTooltip(font, text.stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY);
    }

    /**
     * Checks if a cursor position is within the provided left, top, width, height of a rectangular region.
     *
     * @param x      The left position of the rectangular region.
     * @param y      The up position of the rectangular region.
     * @param width  The width of the region.
     * @param height The height of the region.
     * @param mouseX The x-coordinate of the cursor.
     * @param mouseY The y-coordinate of the cursor.
     * @return Whether the cursor is within the specified rectangular region.
     */
    public static boolean isHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= (double) (x - 1)
                && mouseX < (double) (x + width + 1)
                && mouseY >= (double) (y - 1)
                && mouseY < (double) (y + height + 1);
    }

    /**
     * Checks if a cursor position is within the provided left, top, width, height of a rectangular component.
     *
     * @param renderInfo The provided render info of the component.
     * @param width      The width of the region.
     * @param height     The height of the region.
     * @return Whether the cursor is within the specified rectangular region.
     */
    public static boolean isHovering(RenderInfo renderInfo, int width, int height) {
        return renderInfo.isHovering(width, height);
    }

    /**
     * Checks if a cursor position is within the provided left, top, width, height of this provided rectangular component.
     *
     * @param width  The width of the region.
     * @param height The height of the region.
     * @return Whether the cursor is within the specified rectangular region.
     */
    public boolean isHovering(int width, int height) {
        return isHovering(left(), top(), width, height, mouseX(), mouseY());
    }

    /**
     * Adds an offset to the rendering information for the rendered element to be shifted.
     *
     * @param left Number of pixels to shift from the left.
     * @param top  Number of pixels to shift from the top.
     * @return a copied version of this object with the above changes. This does not mutate the original object.
     */
    public RenderInfo offset(int left, int top) {
        return new RenderInfo(mouseX(), mouseY(), left() + left, top() + top, font(), blocks());
    }

    /**
     * Nullifies the position of a COPY of this object by setting it to origin (0,0), and returning the copy.
     */
    public RenderInfo withNullifiedOffset() {
        return new RenderInfo(mouseX(), mouseY(), 0, 0, font(), blocks());
    }

    /**
     * Creates a new nullified object with a font.
     */
    public static RenderInfo withNullifiedOffset(Font font) {
        return new RenderInfo(0, 0, 0, 0, font);
    }
}