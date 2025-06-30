package io.github.laptop59.concocti.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public record ConcoctizedEntitiesPayload(Set<String> entities) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ConcoctizedEntitiesPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "concoctized_entities"));

    // Each pair of elements defines the stream codec of the element to encode/decode and the getter for the element to encode
    // The final parameter takes in the previous parameters in the order they are provided to construct the payload object
    public static final StreamCodec<ByteBuf, ConcoctizedEntitiesPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(
                    HashSet::new,
                    ByteBufCodecs.STRING_UTF8
            ), ConcoctizedEntitiesPayload::entities, ConcoctizedEntitiesPayload::new
    );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}