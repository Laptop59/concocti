package io.github.laptop59.concocti.common.synchronization;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/// Data that synchronizes the entire state of a machine between the server and the client.
/// In this record, everything must be sent.
public record SyncedMachineData(
        SyncedBase base,
        SyncedSettings settings,
        SyncedFluids fluids,
        SyncedExtra extra
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncedMachineData> STREAM_CODEC =
            StreamCodec.composite(
                    SyncedBase.STREAM_CODEC, SyncedMachineData::base,
                    SyncedSettings.STREAM_CODEC, SyncedMachineData::settings,
                    SyncedFluids.STREAM_CODEC, SyncedMachineData::fluids,
                    SyncedExtra.STREAM_CODEC, SyncedMachineData::extra,

                    SyncedMachineData::new
            );
}
