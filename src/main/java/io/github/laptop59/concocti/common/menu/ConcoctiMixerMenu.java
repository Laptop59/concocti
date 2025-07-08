package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.block.entity.ConcoctiSolidifierBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConcoctiMixerMenu extends AbstractConcoctiMachineMenu<ConcoctiMixerMenu> {
    @Contract(pure = true)
    @Override
    public List<Property<?>> getMachineSpecificProperties() {
        return List.of(
                Properties.FLUID_INPUT_1,
                Properties.FLUID_INPUT_2,
                Properties.FLUID_INPUT_3,
                Properties.FLUID_INPUT_4,
                Properties.FLUID_OUTPUT
        );
    }

    // Client
    public ConcoctiMixerMenu(
            int containerId, Inventory playerInventory
    ) {
        super(containerId, playerInventory, 7, ConcoctiMenus.CONCOCTI_MIXER_MENU);
    }

    // Server
    public ConcoctiMixerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(containerId, playerInventory, container, data, ConcoctiMenus.CONCOCTI_MIXER_MENU);
    }

    @Override
    protected void addOtherSlots() {
        // Input slots
        for (int i = 0; i < 4; i++) {
            int slot = i + 2;
            this.addSlot(new Slot(container, slot, 30 + i * 18, 37 + 15));
        }
        // Output slot
        this.addSlot(new ResultSlot(null, container, 6, 30 + 104, 37 + 15));
    }

    @Override
    public @Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
        if (this.moveItemStackTo(movedStack, 2, 6, false)) {
            return ItemStack.EMPTY;
        }
        return null;
    }

    public List<FluidStack> getInputFluidStacks() {
        return List.of(
                viewer.get(Properties.FLUID_INPUT_1),
                viewer.get(Properties.FLUID_INPUT_2),
                viewer.get(Properties.FLUID_INPUT_3),
                viewer.get(Properties.FLUID_INPUT_4)
        );
    }

    public FluidStack getOutputFluidStack() {
        return viewer.get(Properties.FLUID_OUTPUT);
    }

    public int getInputFluidStackSize() {
        return AbstractConcoctiMachineBlockEntity.TANK_CAPACITY;
    }

    public int getOutputFluidStackSize() {
        return AbstractConcoctiMachineBlockEntity.TANK_CAPACITY * 2;
    }
}
