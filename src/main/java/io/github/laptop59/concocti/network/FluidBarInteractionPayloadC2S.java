package io.github.laptop59.concocti.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/** A payload called when a player attempts to fill/drain a fluid tank of a block. */
public record FluidBarInteractionPayloadC2S(int containerId, int tankId, int buttonNum) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FluidBarInteractionPayloadC2S> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "fluid_bar_interaction"));

    public static final StreamCodec<ByteBuf, FluidBarInteractionPayloadC2S> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, FluidBarInteractionPayloadC2S::containerId,
        ByteBufCodecs.VAR_INT, FluidBarInteractionPayloadC2S::tankId,
        ByteBufCodecs.VAR_INT, FluidBarInteractionPayloadC2S::buttonNum,
        FluidBarInteractionPayloadC2S::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
