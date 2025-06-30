package io.github.laptop59.concocti.network;

import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ConcoctiMachineSettingsSlotChangeC2SHandler {
    public static void handleData(final ConcoctiMachineSettingsSlotChangeC2S data, final IPayloadContext context) {
        // Try to get the cursor item of the player.
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer target) {
                AbstractContainerMenu menu = target.containerMenu;
                if (menu instanceof AbstractConcoctiMachineMenu<?> machineMenu) {
                    if (machineMenu.containerId != data.containerId()) return; // just in case
                    AbstractConcoctiMachineBlockEntity<?, ?, ?, ?, ?> blockEntity
                        = (AbstractConcoctiMachineBlockEntity<?, ?, ?, ?, ?>) machineMenu.getContainer();
                    blockEntity.machineSettings.cycleSlot(data.direction(), data.wasRightClicked());
                }
            }
        })
        .exceptionally(e -> {
            // Who cares anyway?
            return null;
        });
    }
}
