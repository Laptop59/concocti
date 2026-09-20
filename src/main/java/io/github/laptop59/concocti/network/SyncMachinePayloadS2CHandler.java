package io.github.laptop59.concocti.network;

import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncMachinePayloadS2CHandler {
    public static void handleData(final SyncMachinePayloadS2C data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof AbstractConcoctiMachineMenu<?> menu && data.containerId() == menu.containerId) {
                menu.updateWithSyncedData(data.data());
            }
        });
    }
}
