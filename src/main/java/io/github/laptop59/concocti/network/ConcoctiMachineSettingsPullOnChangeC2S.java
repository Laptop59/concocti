package io.github.laptop59.concocti.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A payload called when a player attempts to change a tank of a {@code MachineSettings}.
 * <p></p>
 * For the direction, false indicates previous while true indicates next.
 */
public record ConcoctiMachineSettingsPullOnChangeC2S(int containerId) implements CustomPacketPayload {

    public static final Type<ConcoctiMachineSettingsPullOnChangeC2S> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "concocti_machine_settings_pull_on_change"));

    public static final StreamCodec<ByteBuf, ConcoctiMachineSettingsPullOnChangeC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ConcoctiMachineSettingsPullOnChangeC2S::containerId,
            ConcoctiMachineSettingsPullOnChangeC2S::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
