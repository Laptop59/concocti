package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.SequencedCollection;

public interface RootComponent {
    /** Renders the element's first layer onto the screen. */
    void render(GuiGraphics guiGraphics, RenderInfo renderInfo);

    /** Renders the element's second layer (above first layer) onto the screen. */
    default void render2(GuiGraphics guiGraphics, RenderInfo renderInfo) {}

    /**
     * Renders a child element without considering the info's position and only that of the child.
     * @param guiGraphics The object that allows graphics drawing.
     * @param child The child to render.
     */
    default void renderChildAbsolute(GuiGraphics guiGraphics, RenderInfo renderInfo, Renderable child) {
        Renderable.renderChildAbsolute(guiGraphics, renderInfo, child);
    }

    /**
     * Renders a child element considering the info's position and only that of the child.
     * @param guiGraphics The object that allows graphics drawing.
     * @param child The child to render.
     */
    default void renderChild(GuiGraphics guiGraphics, RenderInfo renderInfo, Renderable child) {
        Renderable.renderChild(guiGraphics, renderInfo, child);
    }

    /**
     * Renders multiple children without considering the info's position and only that of the child.
     * @param guiGraphics The object that allows graphics drawing.
     * @param children The children to render.
     */
    default void renderChildrenAbsolute(GuiGraphics guiGraphics, RenderInfo renderInfo, Renderable... children) {
        for (Renderable child : children)
            Renderable.renderChildAbsolute(guiGraphics, renderInfo, child);
    }

    /**
     * Renders multiple children from a list without considering the info's position and only that of the child.
     * @param guiGraphics The object that allows graphics drawing.
     * @param children The children to render.
     */
    default void renderChildrenAbsolute(GuiGraphics guiGraphics, RenderInfo renderInfo, SequencedCollection<Renderable> children) {
        for (Renderable child : children)
            Renderable.renderChildAbsolute(guiGraphics, renderInfo, child);
    }

    /**
     * Renders multiple children without the info's position and only that of the child.
     * @param guiGraphics The object that allows graphics drawing.
     * @param children The children to render.
     */
    default void renderChildren(GuiGraphics guiGraphics, RenderInfo renderInfo, Renderable... children) {
        for (Renderable child : children)
            Renderable.renderChild(guiGraphics, renderInfo, child);
    }

    /**
     * Renders multiple children from a list considering the info's position and only that of the child.
     * @param guiGraphics The object that allows graphics drawing.
     * @param children The children to render.
     */
    default void renderChildren(GuiGraphics guiGraphics, RenderInfo renderInfo, SequencedCollection<Renderable> children) {
        for (Renderable child : children)
            Renderable.renderChild(guiGraphics, renderInfo, child);
    }

    /**
     * Gets all children components of this root component. Override this for any implementation, but make sure to include the superclass' components!
     */
    default List<Renderable> getChildren() {
        return List.of();
    }
}
