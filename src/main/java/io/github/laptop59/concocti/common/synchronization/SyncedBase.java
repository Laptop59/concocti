package io.github.laptop59.concocti.common.synchronization;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/// Base properties of a machine that are synchronized.
public record SyncedBase(
        int ticksLeft,
        int totalTicks,
        int tickMultiplier,
        int energyStored,
        int maxEnergyStored
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncedBase> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncedBase::ticksLeft,
            ByteBufCodecs.VAR_INT, SyncedBase::totalTicks,
            ByteBufCodecs.VAR_INT, SyncedBase::tickMultiplier,

            ByteBufCodecs.INT, SyncedBase::energyStored,
            ByteBufCodecs.INT, SyncedBase::maxEnergyStored,

            SyncedBase::new
    );
}
