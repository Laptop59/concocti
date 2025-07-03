package io.github.laptop59.concocti.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.laptop59.concocti.network.FluidBarInteractionPayloadC2S;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A class used to render a fluid bar.
 * @param <T> The type of menu whose screen the bar should render for.
 */
public class FluidBar<T extends AbstractContainerMenu> extends Renderable implements MenuAccess<T>, ClickableComponent {
    T menu;
    AbstractContainerScreen<T> screen;
    ResourceLocation fluid;

    FluidStack stack;
    int max;
    int id;

    public static final ResourceLocation FLUID_BASE_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/fluids/base");
    public static final ResourceLocation FLUID_BLACK_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/fluids/black");

    public FluidBar(int guiLeft, int guiTop, AbstractContainerScreen<T> screen, T menu, ResourceLocation fluid, int id) {
        super(guiLeft, guiTop);
        this.screen = screen;
        this.menu = menu;
        this.fluid = fluid;
        this.id = id;
    }

    public FluidBar(int guiLeft, int guiTop, AbstractContainerScreen<T> screen, T menu, int id) {
        this(guiLeft, guiTop, screen, menu, ResourceLocation.withDefaultNamespace("empty"), id);
    }

    public void update(FluidStack stack, int max) {
        this.stack = stack;
        this.max = max;
    }

    @Override
    protected void render(GuiGraphics guiGraphics, RenderInfo renderInfo) {
        setToSolidifierFluid(BuiltInRegistries.FLUID.getId(stack.getFluid()));
        int height = Mth.ceil(((float) stack.getAmount() / max) * 40.0F);
        guiGraphics.blitSprite(FLUID_BASE_SPRITE, 17, 42, 0, 0, renderInfo.left() - 1, renderInfo.top() - 1, 17, 42);
        // Draw the full fluid.
        // Get the required texture atlas sprite and attributes.
        int incremented;
        if (!fluidIsEmpty()) {
            var attributes = IClientFluidTypeExtensions.of(BuiltInRegistries.FLUID.get(fluid));
            TextureAtlasSprite sprite = screen.getMinecraft().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(attributes.getStillTexture());
            setFluidColor(attributes.getTintColor());
            renderTiledTextureAtlas(guiGraphics, screen, sprite, renderInfo.left(),
                    renderInfo.top() + (40 - height), 15, height, 100, true);
            // Set the shader color back.
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        // Draw the blackened part of the fluid.
        for (int blackenedLeft = 40 - height; blackenedLeft > 0; blackenedLeft -= incremented) {
            incremented = Math.min(blackenedLeft, 15);
            guiGraphics.blitSprite(FLUID_BLACK_SPRITE, 15, 15, 0, 0,
                    renderInfo.left(), renderInfo.top() + 40 - blackenedLeft - height, 15, incremented);
        }

        // Draw the fluid bar highlight if needed.
        if (renderInfo.isHovering(getWidth(), getHeight()))
            guiGraphics.fillGradient(RenderType.guiOverlay(), renderInfo.left(), renderInfo.top(),
                renderInfo.left() + 16, renderInfo.top() + 40, -2130706433, -2130706433, 0);

        if (renderInfo.isHovering(15, 40)) {
            renderInfo.renderTooltip(guiGraphics, Component.translatable("screen.concocti.fluid_bar",
                    Component.translatable(getFluidTranslation()).getString(), stack.getAmount(), max));
        }
    }

    @Override
    public int getWidth() {
        return 15;
    }

    @Override
    public int getHeight() {
        return 40;
    }

    /** Sets the fluid color from a packed int color. */
    private static void setFluidColor(int color) {
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        float a = (color >> 24 & 255) / 255.0F;
        RenderSystem.setShaderColor(r, g, b, a);
    }

    /** Renders a tiled texture atlas. */
    public static void renderTiledTextureAtlas(GuiGraphics matrices, AbstractContainerScreen<?> screen,
               TextureAtlasSprite sprite, int x, int y, int width, int height, int depth, boolean upsideDown) {
        // start drawing sprites
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());

        int spriteHeight = sprite.contents().height();
        int spriteWidth = sprite.contents().width();
        // tile vertically

        Matrix4f matrix = matrices.pose().last().pose();

        final int xTileCount = width / spriteWidth;
        final int xRemainder = width - (xTileCount * spriteWidth);
        final long yTileCount = height / spriteHeight;
        final long yRemainder = height - (yTileCount * spriteHeight);

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int widthLeft = (xTile == xTileCount) ? xRemainder : spriteWidth;
                long heightLeft = (yTile == yTileCount) ? yRemainder : spriteHeight;
                int x2 = x + (xTile * spriteWidth);
                int y2 = y + height - ((yTile + 1) * spriteHeight);
                if (widthLeft > 0 && heightLeft > 0) {
                    long maskTop = spriteHeight - heightLeft;
                    int maskRight = spriteWidth - widthLeft;

                    drawTextureWithMasking(matrix, x2, y2, sprite, maskTop, maskRight, 100);
                }
            }
        }
    }

    private static void drawTextureWithMasking(Matrix4f matrix, float xCoord, float yCoord, TextureAtlasSprite textureSprite, long maskTop, long maskRight, float zLevel) {
        float uMin = textureSprite.getU0();
        float uMax = textureSprite.getU1();
        float vMin = textureSprite.getV0();
        float vMax = textureSprite.getV1();
        uMax = uMax - (maskRight / 16F * (uMax - uMin));
        vMax = vMax - (maskTop / 16F * (vMax - vMin));

        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix, xCoord, yCoord + 16, zLevel).setUv(uMin, vMax);
        bufferBuilder.addVertex(matrix, xCoord + 16 - maskRight, yCoord + 16, zLevel).setUv(uMax, vMax);
        bufferBuilder.addVertex(matrix, xCoord + 16 - maskRight, yCoord + maskTop, zLevel).setUv(uMax, vMin);
        bufferBuilder.addVertex(matrix, xCoord, yCoord + maskTop, zLevel).setUv(uMin, vMin);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    private boolean fluidIsEmpty() {
        return fluid.equals(ResourceLocation.withDefaultNamespace("empty"));
    }

    private String getFluidTranslation() {
        if (fluidIsEmpty()) return "mco.configure.world.slot.empty"; // Found an empty translation???
        String trimmed = fluid.getPath();
        if (trimmed.startsWith("flowing_")) trimmed = trimmed.substring(8);
        return "block." + fluid.getNamespace() + "." + trimmed;
    }

    public void setToSolidifierFluid(int id) {
        fluid = BuiltInRegistries.FLUID.getKey(BuiltInRegistries.FLUID.byId(id));
    }

    @Override
    public @NotNull T getMenu() {
        return menu;
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, AbstractContainerScreen<?> screen, AbstractContainerMenu menu) {
        if (screen != null) {
            // Send a payload.
            PacketDistributor.sendToServer(new FluidBarInteractionPayloadC2S(menu.containerId, id, button));
        }
        return false;
    }
}
