package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.synchronization.SyncedMachineData;
import io.github.laptop59.concocti.common.synchronization.SyncedMachineDataUpdate;
import io.github.laptop59.concocti.network.SyncMachinePayloadS2C;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/// Implemented by classes who can create and send (sync) packet data to clients.
public interface SyncedDataCreator {
    /**
     * Creates a complete snapshot of this machine's data so that the client can be updated about this machine's state.
     * @return The snapshot
     */
    SyncedMachineData createSyncedData();

    /** Writes the complete synced snapshot of this entity to the given buffer,
     * ideal for menu opens.
     * @param buffer The buffer to write to.
     */
    default void writeCompleteSyncedDataToBuf(RegistryFriendlyByteBuf buffer) {
        SyncedMachineData data = createSyncedData();
        SyncedMachineData.STREAM_CODEC.encode(buffer, data);
    }

    default void updateToClients(Level level, SyncedMachineDataUpdate update) {
        if (level instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                if (player.containerMenu instanceof AbstractConcoctiMachineMenu<?> menu && menu.getContainer() == this) {
                    PacketDistributor.sendToPlayer(player, new SyncMachinePayloadS2C(
                            menu.containerId,
                            update
                    ));
                }
            }
        }
    }
}
