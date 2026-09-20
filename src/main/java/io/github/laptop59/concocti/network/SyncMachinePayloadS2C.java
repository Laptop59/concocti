package io.github.laptop59.concocti.network;

import io.github.laptop59.concocti.common.menu.SyncedMachineData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public record SyncMachinePayloadS2C(
    SyncedMachineData data
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncMachinePayloadS2C> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_machine"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMachinePayloadS2C> STREAM_CODEC =
            StreamCodec.composite(
                    SyncedMachineData.STREAM_CODEC, SyncMachinePayloadS2C::data,
                    SyncMachinePayloadS2C::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
