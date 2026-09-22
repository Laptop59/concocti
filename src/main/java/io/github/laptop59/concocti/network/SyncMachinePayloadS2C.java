package io.github.laptop59.concocti.network;

import io.github.laptop59.concocti.common.synchronization.SyncedMachineDataUpdate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public record SyncMachinePayloadS2C(
    int containerId,
    SyncedMachineDataUpdate update
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncMachinePayloadS2C> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_machine"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMachinePayloadS2C> STREAM_CODEC =
            StreamCodec.composite(
                    // Container ID is always between 0-99 so it fits as one byte in VarInt
                    ByteBufCodecs.VAR_INT, SyncMachinePayloadS2C::containerId,
                    SyncedMachineDataUpdate.STREAM_CODEC, SyncMachinePayloadS2C::update,
                    SyncMachinePayloadS2C::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
