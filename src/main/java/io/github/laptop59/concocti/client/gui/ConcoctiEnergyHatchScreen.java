package io.github.laptop59.concocti.client.gui;

import io.github.laptop59.concocti.client.gui.components.EnergyBar;
import io.github.laptop59.concocti.client.gui.components.RenderInfo;
import io.github.laptop59.concocti.client.gui.components.Renderable;
import io.github.laptop59.concocti.common.machine.impl.ConcoctiCompressor;
import io.github.laptop59.concocti.common.menu.ConcoctiEnergyHatchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ConcoctiEnergyHatchScreen extends AbstractConcoctiMachineScreen<ConcoctiEnergyHatchMenu> {
    private final EnergyBar<ConcoctiEnergyHatchMenu> energyBar = new EnergyBar<>(80, 18, this, menu);

    public ConcoctiEnergyHatchScreen(
            ConcoctiEnergyHatchMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
    }

    public List<Renderable> getUniqueChildren() {
        return List.of(
                energyBar
        );
    }

    @Override
    public List<Renderable> getChildren() {
        ArrayList<Renderable> children = new ArrayList<>(super.getChildren());
        children.addAll(getUniqueChildren());
        return List.copyOf(children);
    }

    public void render(@NotNull GuiGraphics guiGraphics, RenderInfo renderInfo, float partialTick) {
        energyBar.update(menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true));
        // Don't forget to first render the abstract screen!
        super.render(guiGraphics, renderInfo, partialTick);
        renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());
    }
}