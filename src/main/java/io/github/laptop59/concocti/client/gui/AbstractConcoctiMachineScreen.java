package io.github.laptop59.concocti.client.gui;

import io.github.laptop59.concocti.client.gui.components.*;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiFrameSlot;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import io.github.laptop59.concocti.common.menu.IconSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.github.laptop59.concocti.common.Concocti.MODID;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractConcoctiMachineScreen<M extends AbstractConcoctiMachineMenu<M>> extends AbstractContainerScreen<M> implements RootComponent {

    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/concocti_machine.png");

    public static final ResourceLocation SLOT_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/concocti_machine_slot");

    Cogwheel cogwheel = new Cogwheel(imageWidth - 21, imageHeight - 29 - 18 * 4, this::onCogwheelClick);
    MachineSettingsComponent<M> machineSettingsComponent = new MachineSettingsComponent<>(this, menu);
    boolean machineSettingsVisibility = false;

    public AbstractConcoctiMachineScreen(
            M menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
    }

    /**
     * Returns the required background texture of this screen. Can be overridden.
     */
    protected @NotNull ResourceLocation getBgTexture() {
        return BG_TEXTURE;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        RenderInfo renderInfo = new RenderInfo(mouseX, mouseY, leftPos, topPos, font);
        renderTooltip(guiGraphics, mouseX, mouseY);
        machineSettingsComponent.update();
        render2(guiGraphics, renderInfo);
    }

    @Override
    public void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        RenderInfo renderInfo = new RenderInfo(mouseX, mouseY, leftPos, topPos, font);
        if (isMinecraftAbstractContainerUsableHere(renderInfo))
            super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderSlotHighlight(@NotNull GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick) {
        RenderInfo renderInfo = new RenderInfo(mouseX, mouseY, leftPos, topPos, font);
        if (slot.isHighlightable() && isMinecraftAbstractContainerUsableHere(renderInfo)) {
            renderSlotHighlight(guiGraphics, slot.x, slot.y, 0, getSlotColor(slot.index));
        }
    }

    @Override
    public List<Renderable> getChildren() {
        ArrayList<Renderable> children = new ArrayList<>();
        children.add(cogwheel);

        if (machineSettingsVisibility) children.add(machineSettingsComponent);
        return List.copyOf(children);
    }

    public boolean isMinecraftAbstractContainerUsableHere(RenderInfo renderInfo) {
        // Check for machine settings component overlap.
        if (machineSettingsVisibility && renderInfo.offset(
                machineSettingsComponent.getGuiLeft(),
                machineSettingsComponent.getGuiTop()
        ).isHovering(
                machineSettingsComponent.getWidth(),
                machineSettingsComponent.getHeight()
        )) return false;

        // Otherwise return true.
        return true;
    }

    /**
     * Renders the common parts of a Concocti Machine, like the background, slots and certain tooltips. <p>
     * If this method is overridden, make sure to call this class' {@code render} method first, using {@code super.render(guiGraphics, renderInfo)}.
     */
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, RenderInfo renderInfo) {
        // First, render the background.
        guiGraphics.blit(getBgTexture(), leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);

        // This screen will render all slots beforehand.
        for (Slot slot : menu.getMachineSlots()) {
            int x = leftPos + slot.x - 1;
            int y = topPos + slot.y - 1;
            guiGraphics.blitSprite(SLOT_SPRITE, x, y, 18, 18);
            // Render some icons if needed.
            if (!slot.getItem().isEmpty()) continue;
            ResourceLocation extraIcon = null;
            if (slot instanceof IconSlot iconSlot) {
                extraIcon = ResourceLocation.fromNamespaceAndPath(MODID, "container/slot_icons/" + iconSlot.getIcon().path);
            }
            if (extraIcon != null) guiGraphics.blitSprite(extraIcon, x + 1, y + 1, 16, 16);
        }

        cogwheel.update(machineSettingsVisibility);
        renderChild(guiGraphics, renderInfo, cogwheel);
    }

    /**
     * Renders elements above the first layer.
     */
    @Override
    public void render2(@NotNull GuiGraphics guiGraphics, RenderInfo renderInfo) {
        if (machineSettingsVisibility) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 350.0F);
            renderChild(guiGraphics, renderInfo, machineSettingsComponent);
            guiGraphics.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        RenderInfo renderInfo = new RenderInfo((int) mouseX, (int) mouseY, leftPos, topPos, font);
        for (Renderable renderable : getChildren()) {
            // Only check for clickable components.
            if (renderable instanceof ClickableComponent clickable) {
                if (renderable.getActualRenderInfo(renderInfo).isHovering(renderable.getWidth(), renderable.getHeight()) &&
                        clickable.onMouseClick(mouseX - leftPos, mouseY - topPos, button, this, menu)) return true;
            }
        }
        if (!isMinecraftAbstractContainerUsableHere(renderInfo)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected void onCogwheelClick() {
        machineSettingsVisibility = !machineSettingsVisibility;
    }

    @Override
    public void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Push this block if machine settings component is open.
        boolean addedMachineSettingsBlock = machineSettingsVisibility;
        RenderInfo renderInfo = new RenderInfo(mouseX, mouseY, leftPos, topPos, font);
        RenderInfo machineSettingsRenderInfo = machineSettingsComponent.getActualRenderInfo(renderInfo);
        if (addedMachineSettingsBlock) renderInfo.push(new RenderInfo.Block(
                machineSettingsRenderInfo.left(),
                machineSettingsRenderInfo.top(),
                machineSettingsComponent.getWidth(),
                machineSettingsComponent.getHeight()
        ));
        render(guiGraphics, renderInfo);
        if (addedMachineSettingsBlock) renderInfo.pop();
    }

    public static int getHeight() {
        return 176;
    }

    public static int getWidth() {
        return 166;
    }
}
