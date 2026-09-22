package io.github.laptop59.concocti.common.synchronization;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/// Changed fluid stacks of a machine that are synchronized.
public record SyncedFluids(
        @Nullable FluidStack @NotNull [] fluidStacks
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncedFluids> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull SyncedFluids decode(@NotNull RegistryFriendlyByteBuf buffer) {
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

            return new SyncedFluids(fluidStacks);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buffer, SyncedFluids value) {
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
