package io.github.laptop59.concocti.common.recipe;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Random;

public record FluidOutput(FluidStack stack, float chance) {
    public static final Random RANDOM = new Random();

    public FluidOutput {
        Preconditions.checkArgument(chance >= 0, "Chance must be between [0, 1]");
        Preconditions.checkArgument(chance <= 1, "Chance must be between [0, 1]");
    }

    public static final Codec<FluidOutput> STRICT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    FluidStack.CODEC.fieldOf("stack").forGetter(FluidOutput::stack),
                    Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(FluidOutput::chance)
            ).apply(instance, FluidOutput::new)
    );


    public static final Codec<FluidOutput> CODEC = Codec.either(
            FluidStack.CODEC,
            STRICT_CODEC
    ).xmap(
            either -> either.map(
                    FluidOutput::of,
                    o -> o
            ),
            output -> output.chance == 1f ? Either.left(output.stack()) : Either.right(output)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidOutput> STREAM_CODEC = StreamCodec.composite(
            FluidStack.STREAM_CODEC, FluidOutput::stack,
            ByteBufCodecs.FLOAT, FluidOutput::chance,
            FluidOutput::new
    );

    public static FluidOutput of(FluidStack stack, float chance) {
        return new FluidOutput(stack, chance);
    }

    public static FluidOutput of(FluidStack stack) {
        return new FluidOutput(stack, 1.0f);
    }

    public static FluidOutput of(Holder<Fluid> fluid, int amount) {
        return new FluidOutput(new FluidStack(fluid, amount), 1.0f);
    }

    public static FluidOutput of(Fluid fluid, int amount) {
        return new FluidOutput(new FluidStack(fluid, amount), 1.0f);
    }

    public static FluidOutput of(Holder<Fluid> fluid, int amount, float chance) {
        return new FluidOutput(new FluidStack(fluid, amount), chance);
    }

    public static FluidOutput of(Fluid fluid, int amount, float chance) {
        return new FluidOutput(new FluidStack(fluid, amount), chance);
    }

    public boolean roll() {
        return RANDOM.nextFloat() < chance;
    }
}
