package io.github.laptop59.concocti.common.synchronization;

import io.github.laptop59.concocti.client.gui.components.MachineSettingsSlots;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

/// Settings of a machine that are synchronized.
public record SyncedSettings(
        Optional<Direction> facingDirection,
        MachineSettingsSlots machineSettingsSlots,

        boolean ejectOn,
        boolean pullOn
) {
    public static final StreamCodec<ByteBuf, SyncedSettings> STREAM_CODEC = StreamCodec.composite(
            Direction.STREAM_CODEC.apply(ByteBufCodecs::optional), SyncedSettings::facingDirection,
            MachineSettingsSlots.STREAM_CODEC, SyncedSettings::machineSettingsSlots,

            ByteBufCodecs.BOOL, SyncedSettings::ejectOn,
            ByteBufCodecs.BOOL, SyncedSettings::pullOn,

            SyncedSettings::new
    );
}
