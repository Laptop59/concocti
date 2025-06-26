package io.github.laptop59.concocti.client.gui;

import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiFrameSlot;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import static io.github.laptop59.concocti.common.Concocti.MODID;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractConcoctiMachineScreen<M extends AbstractConcoctiMachineMenu<M>> extends AbstractContainerScreen<M> {

    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/concocti_machine.png");

    public static final ResourceLocation SLOT_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/concocti_machine_slot");

    public static final ResourceLocation CONCOCTI_UPGRADE_ICON = ResourceLocation.fromNamespaceAndPath(MODID, "container/slot_icons/concocti_upgrade");

    public AbstractConcoctiMachineScreen(
            M menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
    }

    /** Returns the required background texture of this screen. Can be overridden. */
    protected @NotNull ResourceLocation getBgTexture() {
        return BG_TEXTURE;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }


    /**
     * Renders the common parts of a Concocti Machine, like the background, slots and certain tooltips. <p>
     * Usually, there is no need to override this method, use {@link #renderBgSpecific(GuiGraphics, float, int, int)}
     * to render anything specific.
     */
    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        ConcoctiUpgradeSlot upgradeSlot = menu.getUpgradeSlot();
        ConcoctiFrameSlot frameSlot = menu.getFrameSlot();

        // First, render the background.
        guiGraphics.blit(getBgTexture(), leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);

        // This screen will render all slots beforehand.
        for (Slot slot : menu.getMachineSlots()) {
            int x = leftPos + slot.x - 1;
            int y = topPos + slot.y - 1;
            guiGraphics.blitSprite(SLOT_SPRITE, x, y, 18, 18);
            // Render some icons if needed.
            ResourceLocation extraIcon = switch (slot) {
                case ConcoctiUpgradeSlot concoctiUpgradeSlot -> CONCOCTI_UPGRADE_ICON;
                default -> null;
            };
            if (extraIcon != null) guiGraphics.blitSprite(extraIcon, x+1, y+1, 16, 16);
        }

        if (isHovering(upgradeSlot.x, upgradeSlot.y, 16, 16, mouseX, mouseY) && !upgradeSlot.hasItem()) {
            guiGraphics.renderTooltip(font, Component.translatable("screen.concocti.no_upgrade"), mouseX, mouseY);
        }
        if (isHovering(frameSlot.x, frameSlot.y, 16, 16, mouseX, mouseY) && !frameSlot.hasItem()) {
            guiGraphics.renderTooltip(font, Component.translatable("screen.concocti.no_frame"), mouseX, mouseY);
        }

        renderBgSpecific(guiGraphics, partialTick, mouseX, mouseY);
    }

    /** A method to render anything specific to a Concocti Machine's screen. */
    abstract protected void renderBgSpecific(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY);
}
