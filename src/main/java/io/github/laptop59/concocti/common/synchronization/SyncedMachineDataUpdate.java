package io.github.laptop59.concocti.common.synchronization;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

/// Data that synchronizes a part or all of the state of a machine between the server and the client.
public record SyncedMachineDataUpdate(
        Optional<SyncedBase> base,
        Optional<SyncedSettings> settings,
        Optional<SyncedFluids> fluids,
        Optional<SyncedExtra> extra
) {
    public static final byte BASE_FLAG = 1 << 0;
    public static final byte SETTINGS_FLAG = 1 << 1;
    public static final byte FLUIDS_FLAG = 1 << 2;
    public static final byte EXTRA_FLAG = 1 << 3;

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncedMachineDataUpdate> STREAM_CODEC = StreamCodec.of(
            (buffer, update) -> {
                int flags = (update.base.isPresent() ? BASE_FLAG : 0) |
                        (update.settings.isPresent() ? SETTINGS_FLAG : 0) |
                        (update.fluids.isPresent() ? FLUIDS_FLAG : 0) |
                        (update.extra.isPresent() ? EXTRA_FLAG : 0);
                buffer.writeByte(flags);

                update.base.ifPresent(base -> SyncedBase.STREAM_CODEC.encode(buffer, base));
                update.settings.ifPresent(settings -> SyncedSettings.STREAM_CODEC.encode(buffer, settings));
                update.fluids.ifPresent(fluids -> SyncedFluids.STREAM_CODEC.encode(buffer, fluids));
                update.extra.ifPresent(extra -> SyncedExtra.STREAM_CODEC.encode(buffer, extra));
            },
            buffer -> {
                byte flags = buffer.readByte();

                Optional<SyncedBase> base = Optional.empty();
                Optional<SyncedSettings> settings = Optional.empty();
                Optional<SyncedFluids> fluids = Optional.empty();
                Optional<SyncedExtra> extra = Optional.empty();

                if ((flags & BASE_FLAG) != 0)
                    base = Optional.of(SyncedBase.STREAM_CODEC.decode(buffer));

                if ((flags & SETTINGS_FLAG) != 0)
                    settings = Optional.of(SyncedSettings.STREAM_CODEC.decode(buffer));

                if ((flags & FLUIDS_FLAG) != 0)
                    fluids = Optional.of(SyncedFluids.STREAM_CODEC.decode(buffer));

                if ((flags & EXTRA_FLAG) != 0)
                    extra = Optional.of(SyncedExtra.STREAM_CODEC.decode(buffer));

                return new SyncedMachineDataUpdate(
                        base,
                        settings,
                        fluids,
                        extra
                );
            }
    );
}
