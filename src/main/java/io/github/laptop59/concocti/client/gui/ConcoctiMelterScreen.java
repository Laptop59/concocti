package io.github.laptop59.concocti.client.gui;

import io.github.laptop59.concocti.client.gui.components.ArrowProgress;
import io.github.laptop59.concocti.client.gui.components.EnergyBar;
import io.github.laptop59.concocti.client.gui.components.FluidBar;
import io.github.laptop59.concocti.common.menu.ConcoctiMelterMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import static io.github.laptop59.concocti.common.Concocti.MODID;

@OnlyIn(Dist.CLIENT)
public class ConcoctiMelterScreen extends AbstractContainerScreen<ConcoctiMelterMenu> {

    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/concocti_melter.png");

    private final FluidBar<ConcoctiMelterMenu> moltenConcoctiFluid = new FluidBar<>(this, menu, "molten_concocti", 105, 28);
    private final FluidBar<ConcoctiMelterMenu> moltenConcoctizedDirtFluid = new FluidBar<>(this, menu, "molten_concoctized_dirt", 129, 28);

    private final EnergyBar<ConcoctiMelterMenu> energyBar = new EnergyBar<>(this, menu, 10, 18);
    private final ArrowProgress<ConcoctiMelterMenu> arrowProgress = new ArrowProgress<>(this, menu, 79 - 7, 34 + 10);

    public ConcoctiMelterScreen(
            ConcoctiMelterMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        guiGraphics.blit(this.texture, left, top, 0, 0, this.imageWidth, this.imageHeight);

        // Render the melt progress and energy bar.
        arrowProgress.render(guiGraphics, menu.getBurnProgress());
        energyBar.render(guiGraphics, mouseX, mouseY, menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true), font);

        // Render the fluids.
        moltenConcoctiFluid.render(guiGraphics, mouseX, mouseY, menu.getNumberFluidLeft(false), menu.getMaxFluidLeft(), font);
        moltenConcoctizedDirtFluid.render(guiGraphics, mouseX, mouseY, menu.getNumberFluidLeft(true), menu.getMaxFluidLeft(), font);

        ConcoctiUpgradeSlot upgradeSlot = menu.getUpgradeSlot();
        Slot inputSlot = menu.getSlot(0);
        if (isHovering(inputSlot.x, inputSlot.y, 16, 16, mouseX, mouseY) && inputSlot.getItem().isEmpty()) {
            guiGraphics.renderTooltip(font,
                        upgradeSlot.getItem().isEmpty() ? Component.translatable("screen.concocti.no_upgrade") : Component.translatable("screen.concocti.upgrade_info", upgradeSlot.getUpgradeUnits())
                    , mouseX, mouseY);
        }
    }
}
