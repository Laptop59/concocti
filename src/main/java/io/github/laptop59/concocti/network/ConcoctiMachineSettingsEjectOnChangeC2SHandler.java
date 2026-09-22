package io.github.laptop59.concocti.network;

import io.github.laptop59.concocti.common.machine.SettingsHolder;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ConcoctiMachineSettingsEjectOnChangeC2SHandler {
    public static void handleData(final ConcoctiMachineSettingsEjectOnChangeC2S data, final IPayloadContext context) {
        // Try to get the cursor item of the player.
        context.enqueueWork(() -> {
                    Player player = context.player();
                    if (player instanceof ServerPlayer target) {
                        AbstractContainerMenu menu = target.containerMenu;
                        if (menu instanceof AbstractConcoctiMachineMenu<?> machineMenu) {
                            if (machineMenu.containerId != data.containerId()) return; // just in case
                            SettingsHolder settingsHolder = (SettingsHolder) machineMenu.getContainer();
                            settingsHolder.changeEjectOn();
                        }
                    }
                })
                .exceptionally(e -> {
                    // Who cares anyway?
                    return null;
                });
    }
}
