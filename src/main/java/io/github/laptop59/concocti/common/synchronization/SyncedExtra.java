package io.github.laptop59.concocti.common.synchronization;

import io.github.laptop59.concocti.common.machine.ConcoctiMachine;
import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/// Extra machine data which is synchronized depending on the machine itself.
public record SyncedExtra(
        @Nullable ConcoctiMachine machine,
        @Nullable Object data
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncedExtra> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull SyncedExtra decode(@NotNull RegistryFriendlyByteBuf buffer) {
            if (buffer.readByte() == 0) {
                return new SyncedExtra(null, null);
            }

            String machineId = ByteBufCodecs.STRING_UTF8.decode(buffer);
            var machine = ConcoctiMachines.MACHINES.get(machineId);
            if (machine == null) {
                throw new RuntimeException("Concocti machine " + machineId + " does not exist on the client!");
            }

            Object data = null;
            if (machine.MACHINE_SPECIFIC_SYNCED_DATA_STREAM_CODEC != null)
                data = machine.MACHINE_SPECIFIC_SYNCED_DATA_STREAM_CODEC.decode(buffer);

            return new SyncedExtra(
                    machine,
                    data
            );
        }

        @Override
        @SuppressWarnings("unchecked")
        public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull SyncedExtra value) {
            if (value.machine != null) {
                buffer.writeByte(1);
                ByteBufCodecs.STRING_UTF8.encode(buffer, value.machine.ID);

                StreamCodec<RegistryFriendlyByteBuf, Object> codec = value.machine.MACHINE_SPECIFIC_SYNCED_DATA_STREAM_CODEC;
                if (codec != null) {
                    codec.encode(buffer, value.data);
                }
            } else {
                buffer.writeByte(0);
            }
        }
    };
}
