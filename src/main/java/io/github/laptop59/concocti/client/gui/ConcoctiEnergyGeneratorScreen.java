package io.github.laptop59.concocti.client.gui;

import io.github.laptop59.concocti.client.gui.components.*;
import io.github.laptop59.concocti.common.menu.ConcoctiEnergyGeneratorMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiMelterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ConcoctiEnergyGeneratorScreen extends AbstractConcoctiMachineScreen<ConcoctiEnergyGeneratorMenu> {
    private final EnergyBar<ConcoctiEnergyGeneratorMenu> energyBar = new EnergyBar<>(10, 18, this, menu);
    private final FlameProgress flameProgress = new FlameProgress(76, 28 + 18 + 2);

    public ConcoctiEnergyGeneratorScreen(
            ConcoctiEnergyGeneratorMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
    }

    public List<Renderable> getUniqueChildren() {
        return List.of(
                flameProgress,
                energyBar
        );
    }

    @Override
    public List<Renderable> getChildren() {
        ArrayList<Renderable> children = new ArrayList<>(super.getChildren());
        children.addAll(getUniqueChildren());
        return List.copyOf(children);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, RenderInfo renderInfo) {
        // Don't forget to first render the abstract screen!
        super.render(guiGraphics, renderInfo);

        flameProgress.update(menu.getProgress());

        energyBar.update(menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true));

        renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());
    }
}
