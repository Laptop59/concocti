package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class Cogwheel extends Renderable implements ClickableComponent {
    public static final ResourceLocation COGWHEEL = ResourceLocation.fromNamespaceAndPath(MODID, "container/blockfaces/cogwheel");
    public static final ResourceLocation COGWHEEL_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(MODID, "container/blockfaces/cogwheel_highlighted");

    Runnable whenClicked;
    boolean machineSettingsComponentVisible = false;

    public Cogwheel(int guiLeft, int guiTop, Runnable whenClicked) {
        super(guiLeft, guiTop);
        this.whenClicked = whenClicked;
    }

    public void update(boolean machineSettingsComponentVisible) {
        this.machineSettingsComponentVisible = machineSettingsComponentVisible;
    }

    protected void render(GuiGraphics guiGraphics, RenderInfo renderInfo) {
        boolean cogwheelHovered = renderInfo.isHovering(16, 16);
        guiGraphics.blitSprite(cogwheelHovered ? COGWHEEL_HIGHLIGHTED : COGWHEEL, renderInfo.left(), renderInfo.top(), 16, 16);
        if (cogwheelHovered) {
            renderInfo.renderTooltip(guiGraphics, Component.translatable(
                    machineSettingsComponentVisible ?
                            "screen.concocti.close_machine_settings" :
                            "screen.concocti.open_machine_settings"
            ));
        }
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, @Nullable AbstractContainerScreen<?> screen, @Nullable AbstractContainerMenu menu) {
        whenClicked.run();
        playClickSound();
        return true;
    }
}
