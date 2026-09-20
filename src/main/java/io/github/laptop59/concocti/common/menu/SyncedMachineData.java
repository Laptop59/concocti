package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.client.gui.components.MachineSettingsSlots;
import io.github.laptop59.concocti.common.machine.ConcoctiMachine;
import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/// Data that synchronizes the state of a machine between the server and the client.
public record SyncedMachineData(
        Base base,
        Settings settings,
        Fluids fluids,
        Extra extra
) {
    /// Base properties of a machine that are synchronized.
    public record Base(
            int ticksLeft,
            int totalTicks,
            int tickMultiplier,
            int energyStored,
            int maxEnergyStored
    ) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Base> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Base::ticksLeft,
                ByteBufCodecs.VAR_INT, Base::totalTicks,
                ByteBufCodecs.VAR_INT, Base::tickMultiplier,

                ByteBufCodecs.INT, Base::energyStored,
                ByteBufCodecs.INT, Base::maxEnergyStored,

                Base::new
        );
    }

    /// Settings of a machine that are synchronized.
    public record Settings(
            Optional<Direction> facingDirection,
            MachineSettingsSlots machineSettingsSlots,

            boolean ejectOn,
            boolean pullOn
    ) {
        public static final StreamCodec<ByteBuf, Settings> STREAM_CODEC = StreamCodec.composite(
                Direction.STREAM_CODEC.apply(ByteBufCodecs::optional), Settings::facingDirection,
                MachineSettingsSlots.STREAM_CODEC, Settings::machineSettingsSlots,

                ByteBufCodecs.BOOL, Settings::ejectOn,
                ByteBufCodecs.BOOL, Settings::pullOn,

                Settings::new
        );
    }

    /// Changed fluid stacks of a machine that are synchronized.
    public record Fluids(
             @Nullable FluidStack @NotNull [] fluidStacks
    ) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Fluids> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull Fluids decode(@NotNull RegistryFriendlyByteBuf buffer) {
                // Length of list
                int size = VarInt.read(buffer);

                // Decode the bits
                byte[] bytesForBits = new byte[(size + 7) / 8];
                buffer.readBytes(bytesForBits);

                FluidStack[] fluidStacks = new FluidStack[size];
                for (int i = 0; i < size; i++) {
                    int flag = bytesForBits[i / 8] & (byte) (1 << (i % 8));
                    if (flag != 0) {
                        fluidStacks[i] = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
                    }
                }

                return new Fluids(fluidStacks);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buffer, Fluids value) {
                // Length of list
                VarInt.write(buffer, value.fluidStacks.length);

                // Encode the bits first
                int bytesForBits = (value.fluidStacks.length + 7) / 8;
                byte[] bits = new byte[bytesForBits];

                int i = 0;
                for (FluidStack stack : value.fluidStacks) {
                    if (stack != null) {
                        bits[i / 8] |= (byte) (1 << (i % 8));
                    }
                    i += 1;
                }
                buffer.writeBytes(bits);

                // Encode stacks
                for (FluidStack stack : value.fluidStacks) {
                    if (stack != null) {
                        FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
                    }
                }
            }
        };
    }

    /// Extra machine data which is synchronized depending on the machine itself.
    public record Extra(
            @Nullable ConcoctiMachine machine,
            @Nullable Object data
    ) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Extra> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull Extra decode(@NotNull RegistryFriendlyByteBuf buffer) {
                if (buffer.readByte() == 0) {
                    return new Extra(null, null);
                }

                String machineId = ByteBufCodecs.STRING_UTF8.decode(buffer);
                var machine = ConcoctiMachines.MACHINES.get(machineId);
                if (machine == null) {
                    throw new RuntimeException("Concocti machine " + machineId + " does not exist on the client!");
                }

                Object data = null;
                if (machine.MACHINE_SPECIFIC_SYNCED_DATA_STREAM_CODEC != null)
                    data = machine.MACHINE_SPECIFIC_SYNCED_DATA_STREAM_CODEC.decode(buffer);

                return new Extra(
                        machine,
                        data
                );
            }

            @Override
            @SuppressWarnings("unchecked")
            public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull Extra value) {
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

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncedMachineData> STREAM_CODEC =
            StreamCodec.composite(
                    Base.STREAM_CODEC, SyncedMachineData::base,
                    Settings.STREAM_CODEC, SyncedMachineData::settings,
                    Fluids.STREAM_CODEC, SyncedMachineData::fluids,
                    Extra.STREAM_CODEC, SyncedMachineData::extra,

                    SyncedMachineData::new
            );
}
