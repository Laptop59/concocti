package io.github.laptop59.concocti.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.laptop59.concocti.client.gui.AbstractConcoctiMachineScreen;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A class that allows a machine to have settings.
 * @param <T>
 */
public class MachineSettingsComponent<T extends AbstractContainerMenu> extends Renderable {
    public static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/concocti_machine_settings.png");
    public static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/blockface_slot.png");

    public static final int WIDTH = 176, HEIGHT = 78;

    T menu;
    AbstractContainerScreen<T> screen;

    static int guiLeft = 0;
    static int guiTop = 82;

    public MachineSettingsComponent(AbstractContainerScreen<T> screen, T menu) {
        super(guiLeft, guiTop);
        this.screen = screen;
        this.menu = menu;
    }

    public void update() {
        this.setGuiLeft(guiLeft);
        this.setGuiTop(guiTop);
    }

    @Override
    protected void render(GuiGraphics guiGraphics, RenderInfo renderInfo) {
        int titleX = renderInfo.left() + 8;
        int titleY = renderInfo.top() + 8;

        guiGraphics.drawString(renderInfo.font(), Component.translatable("screen.concocti.machine_settings"), titleX, titleY, 0xFFFFFFFF);
        guiGraphics.blit(BASE_TEXTURE, renderInfo.left(), renderInfo.top() + 1, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);

        AbstractConcoctiMachineMenu<?> menu1 = (AbstractConcoctiMachineMenu<?>) menu;
        MachineSettings settings = menu1.getMachineSettings();
        Direction currentDirection = menu1.getFacingDirection();

        for (Map.Entry<Direction, MachineSettings.SlotType> entry : settings.slots.entrySet()) {
            Direction direction = entry.getKey();
            MachineSettings.SlotType slotType = entry.getValue();
            //    #   -1
            //  # # #  0         v
            //    # #  1         j
            // -1 0 1          > i
            int i, j;
            String relativeDirection = getRelativeDirection(direction, currentDirection);
            switch (relativeDirection) {
                case "front" -> { i = 0; j = 0; }
                case "back" -> { i = 1; j = 1; }
                case "up" -> { i = 0; j = -1; }
                case "down" -> { i = 0; j = 1; }
                case "right" -> { i = 1; j = 0; }
                case "left" -> { i = -1; j = 0; }
                default -> throw new IllegalStateException("Could not get position of relative direction " + relativeDirection + ".");
            }
            int x = i * 20 + WIDTH / 2 - 10;
            int y = j * 20 + HEIGHT / 2 - 10;
            int color = slotType.color;
            RenderSystem.setShaderColor(
                    ((color >> 16) & 0xFF) / 255f,
                    ((color >> 8) & 0xFF) / 255f,
                    (color & 0xFF) / 255f,
                    ((color >> 24) & 0xFF) / 255f
            );
            RenderInfo slotInfo = renderInfo.offset(x, y);
            guiGraphics.blit(SLOT_TEXTURE, slotInfo.left(), slotInfo.top(), 0, 0, 16, 16, 16, 16);
            String slotTypeKey = "screen.concocti.slot_type." + slotType.name().toLowerCase(Locale.ROOT);
            String relativeDirectionKey = "screen.concocti.slot." + relativeDirection.toLowerCase(Locale.ROOT);
            if (renderInfo.isHovering(16, 16))
                slotInfo.renderTooltip(guiGraphics, Component.translatable(
                    "screen.concocti.slot_compound",
                        Component.translatable(slotTypeKey),
                        Component.translatable(relativeDirectionKey)
                ).withColor(slotType.color));
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static @NotNull String getRelativeDirection(Direction direction, Direction currentDirection) {
        String relativeDirection;
        if (direction == currentDirection) {
            relativeDirection = "front";
        } else if (direction == currentDirection.getOpposite()) {
            relativeDirection = "back";
        } else if (direction == Direction.UP) {
            relativeDirection = "up";
        } else if (direction == Direction.DOWN) {
            relativeDirection = "down";
        } else if (direction == currentDirection.getClockWise()) { // NORTH -> EAST
            relativeDirection = "right";
        } else if (direction == currentDirection.getCounterClockWise()) { // NORTH -> WEST
            relativeDirection = "left";
        } else {
            throw new IllegalStateException("Could not get the position of the direction " + direction + " when the block entity faced " + currentDirection + ".");
        }
        return relativeDirection;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }
}
