package io.github.laptop59.concocti.client.gui;

import io.github.laptop59.concocti.common.block.frame.FrameAttributes;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiFrameSlot;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractConcoctiMachineScreen<M extends AbstractConcoctiMachineMenu<M>> extends AbstractContainerScreen<M> {

    public AbstractConcoctiMachineScreen(
            M menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        ConcoctiUpgradeSlot upgradeSlot = menu.getUpgradeSlot();
        ConcoctiFrameSlot frameSlot = menu.getFrameSlot();

        if (isHovering(upgradeSlot.x, upgradeSlot.y, 16, 16, mouseX, mouseY) && !upgradeSlot.hasItem()) {
            guiGraphics.renderTooltip(font, Component.translatable("screen.concocti.no_upgrade"), mouseX, mouseY);
        }
        if (isHovering(frameSlot.x, frameSlot.y, 16, 16, mouseX, mouseY) && !frameSlot.hasItem()) {
            guiGraphics.renderTooltip(font, Component.translatable("screen.concocti.no_frame"), mouseX, mouseY);
        }
    }
}
