package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.common.menu.SyncedMachineData;
import net.minecraft.network.RegistryFriendlyByteBuf;

/// Implemented by classes who can create and send (sync) packet data to clients.
public interface SyncedDataCreator {
    /**
     * Creates a snapshot of this machine's data so that the client can be updated about this machine's state.
     * @param complete Whether the snapshot should be full of data or not. If {@code false}, then the client
     *                 may not send everything (example: unchanged fluid tank)
     * @return The snapshot
     */
    SyncedMachineData createSyncedData(boolean complete);

    /** Writes the complete synced snapshot of this entity to the given buffer,
     * ideal for menu opens.
     * @param buffer The buffer to write to.
     */
    default void writeCompleteSyncedDataToBuf(RegistryFriendlyByteBuf buffer) {
        SyncedMachineData data = createSyncedData(true);
        SyncedMachineData.STREAM_CODEC.encode(buffer, data);
    }
}
