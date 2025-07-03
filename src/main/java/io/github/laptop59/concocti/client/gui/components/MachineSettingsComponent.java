package io.github.laptop59.concocti.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.network.ConcoctiMachineSettingsSlotChangeC2S;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A class that allows a machine to have settings.
 * @param <T>
 */
public class MachineSettingsComponent<T extends AbstractContainerMenu> extends Renderable implements ClickableComponent {
    public static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/concocti_machine_settings.png");
    public static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/blockface_slot.png");
    public static final ResourceLocation SLOT_OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/blockface_slot_overlay.png");

    public static final int WIDTH = 176, HEIGHT = 78;

    T menu;
    AbstractContainerScreen<T> screen;

    static int guiLeft = 0;
    static int guiTop = 82;

    protected record Pos(int x, int y) {}

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
        MachineSettingsSlots slots = menu1.getMachineSettingsSlots();
        Direction currentDirection = menu1.getFacingDirection();

        for (Map.Entry<Direction, SlotType> entry : slots.entrySet()) {
            Direction direction = entry.getKey();
            SlotType slotType = entry.getValue();
            //    #   -1
            //  # # #  0         v
            //    # #  1         j
            // -1 0 1          > i
            Pos slotPos = getPos(direction, currentDirection);
            String relativeDirection = getRelativeDirection(direction, currentDirection);
            int color = slotType.color;
            RenderSystem.setShaderColor(
                    ((color >> 16) & 0xFF) / 255f,
                    ((color >> 8) & 0xFF) / 255f,
                    (color & 0xFF) / 255f,
                    ((color >> 24) & 0xFF) / 255f
            );
            RenderInfo slotInfo = renderInfo.offset(slotPos.x(), slotPos.y());
            guiGraphics.blit(SLOT_TEXTURE, slotInfo.left(), slotInfo.top(), 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            guiGraphics.blit(SLOT_OVERLAY_TEXTURE, slotInfo.left(), slotInfo.top(), 0, 0, 16, 16, 16, 16);
            String slotTypeKey = "screen.concocti.slot_type." + slotType.name().toLowerCase(Locale.ROOT);
            String relativeDirectionKey = "screen.concocti.slot_" + relativeDirection.toLowerCase(Locale.ROOT);
            if (slotInfo.isHovering(16, 16))
                slotInfo.renderTooltip(guiGraphics, Component.translatable(
                    "screen.concocti.slot_compound",
                        Component.translatable(slotTypeKey),
                        Component.translatable(relativeDirectionKey)
                ).withColor(slotType.color));
        }
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, @Nullable AbstractContainerScreen<?> screen, @Nullable AbstractContainerMenu menu) {
        AbstractConcoctiMachineMenu<?> menu1 = (AbstractConcoctiMachineMenu<?>) menu;
        Direction currentDirection = menu1.getFacingDirection();

        for (Map.Entry<Direction, SlotType> entry : menu1.getMachineSettingsSlots().entrySet()) {
            Direction direction = entry.getKey();
            //    #   -1
            //  # # #  0         v
            //    # #  1         j
            // -1 0 1          > i
            Pos slotPos = getPos(direction, currentDirection);
            RenderInfo renderInfo = this.getActualRenderInfo(new RenderInfo((int) mouseX, (int) mouseY, slotPos.x, slotPos.y, null));
            if (renderInfo.isHovering(16, 16)) {
                Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value());
                PacketDistributor.sendToServer(new ConcoctiMachineSettingsSlotChangeC2S(
                        direction,
                        button == 1,
                        menu.containerId
                ));
                return true;
            }
        }
        return false;
    }

    private static @NotNull Pos getPos(Direction direction, Direction currentDirection) {
        String relativeDirection = getRelativeDirection(direction, currentDirection);
        Pos pos = switch (relativeDirection) {
            case "front" -> new Pos(0, 0);
            case "back" -> new Pos(1, 1);
            case "up" -> new Pos(0, -1);
            case "down" -> new Pos(0, 1);
            case "right" -> new Pos(1, 0);
            case "left" -> new Pos(-1, 0);
            default -> throw new IllegalStateException("Could not get position of relative direction " + relativeDirection + ".");
        };
        pos = new Pos(
                pos.x * 20 + WIDTH / 2 - 10,
                pos.y * 20 + HEIGHT / 2 - 10
        );
        return pos;
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
        } else if (direction == currentDirection.getCounterClockWise()) { // NORTH -> WEST
            relativeDirection = "right";
        } else if (direction == currentDirection.getClockWise()) { // NORTH -> EAST
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
