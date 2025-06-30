package io.github.laptop59.concocti.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/** A payload called when a player successfully filled/drained a fluid tank of a block, and a sound is about to play. */
public record FluidBarSoundPayloadS2C(boolean wasBucketFilled) implements CustomPacketPayload {

    public static final Type<FluidBarSoundPayloadS2C> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "fluid_bar_sound"));

    public static final StreamCodec<ByteBuf, FluidBarSoundPayloadS2C> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, FluidBarSoundPayloadS2C::wasBucketFilled,
        FluidBarSoundPayloadS2C::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
