package io.github.laptop59.concocti.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/** A payload called when a player attempts to change a slot of a {@code MachineSettings}.
 * <p></p>
 * For the direction, false indicates previous while true indicates next.
 * */
public record ConcoctiMachineSettingsSlotChangeC2S(Direction direction, boolean wasRightClicked, int containerId) implements CustomPacketPayload {

    public static final Type<ConcoctiMachineSettingsSlotChangeC2S> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "concocti_machine_settings_slot_change"));

    public static final StreamCodec<ByteBuf, ConcoctiMachineSettingsSlotChangeC2S> STREAM_CODEC = StreamCodec.composite(
            Direction.STREAM_CODEC, ConcoctiMachineSettingsSlotChangeC2S::direction,
            ByteBufCodecs.BOOL, ConcoctiMachineSettingsSlotChangeC2S::wasRightClicked,
            ByteBufCodecs.VAR_INT, ConcoctiMachineSettingsSlotChangeC2S::containerId,
            ConcoctiMachineSettingsSlotChangeC2S::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
