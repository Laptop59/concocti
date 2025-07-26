package io.github.laptop59.concocti.client.gui;

import io.github.laptop59.concocti.client.gui.components.FluidBars;
import io.github.laptop59.concocti.client.gui.components.RenderInfo;
import io.github.laptop59.concocti.client.gui.components.Renderable;
import io.github.laptop59.concocti.common.menu.ConcoctiFluidHatchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ConcoctiFluidHatchScreen extends AbstractConcoctiMachineScreen<ConcoctiFluidHatchMenu> {
    private final FluidBars.Tall<ConcoctiFluidHatchMenu> fluidTank = new FluidBars.Tall<>(80, 26, this, menu, 0);

    public ConcoctiFluidHatchScreen(
            ConcoctiFluidHatchMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
    }

    public List<Renderable> getUniqueChildren() {
        return List.of(
                fluidTank
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
        fluidTank.update(menu.getFluidStack(), menu.getFluidStackSize());
        // Don't forget to first render the abstract screen!
        super.render(guiGraphics, renderInfo);
        renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());
    }
}