package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;

public abstract class Renderable {
    int guiLeft, guiTop;

    private Renderable() {
    }

    public Renderable(int guiLeft, int guiTop) {
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
    }

    public void setGuiLeft(int guiLeft) {
        this.guiLeft = guiLeft;
    }

    public void setGuiTop(int guiTop) {
        this.guiTop = guiTop;
    }

    public int getGuiLeft() {
        return this.guiLeft;
    }

    public int getGuiTop() {
        return this.guiTop;
    }

    /**
     * Renders the element onto the screen.
     */
    protected abstract void render(GuiGraphics guiGraphics, RenderInfo renderInfo);

    /**
     * Gets the actual render info from the parent's render info.
     */
    public RenderInfo getActualRenderInfo(RenderInfo parentRenderInfo) {
        return parentRenderInfo.offset(guiLeft, guiTop);
    }

    /**
     * Renders a child element.
     *
     * @param guiGraphics The object that allows graphics drawing.
     * @param renderInfo  The relative render info of the parent. (This is not absolute)
     * @param child       The child to render.
     */
    public static void renderChild(GuiGraphics guiGraphics, RenderInfo renderInfo, Renderable child) {
        child.render(guiGraphics, renderInfo.offset(child.guiLeft, child.guiTop));
    }

    /**
     * Renders a child element without considering the parent's position.
     *
     * @param guiGraphics The object that allows graphics drawing.
     * @param renderInfo  The absolute render info of the parent. (This is not relative)
     * @param child       The child to render.
     */
    public static void renderChildAbsolute(GuiGraphics guiGraphics, RenderInfo renderInfo, Renderable child) {
        child.render(guiGraphics, renderInfo.offset(child.guiLeft, child.guiTop));
    }

    /**
     * Renders a child element from origin ({@code renderInfo} has both its coordinates zero).
     *
     * @param guiGraphics The object that allows graphics drawing.
     * @param renderInfo  The absolute render info of the parent. The position of this object is ignored and is only used for rendering the element itself.
     * @param child       The child to render.
     */
    public static void renderChildFromOrigin(GuiGraphics guiGraphics, RenderInfo renderInfo, Renderable child) {
        child.render(guiGraphics, renderInfo.withNullifiedOffset());
    }

    /**
     * Gets the width of this component.
     */
    public abstract int getWidth();

    /**
     * Gets the width of this component.
     */
    public abstract int getHeight();
}
