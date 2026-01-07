package io.github.laptop59.concocti.common.recipe;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Random;

public record ItemOutput(ItemStack stack, float chance) {
    public static final Random RANDOM = new Random();

    public ItemOutput {
        Preconditions.checkArgument(chance >= 0, "Chance must be between [0, 1]");
        Preconditions.checkArgument(chance <= 1, "Chance must be between [0, 1]");
    }

    public static final Codec<ItemOutput> STRICT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ItemStack.CODEC.fieldOf("stack").forGetter(ItemOutput::stack),
                    Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(ItemOutput::chance)
            ).apply(instance, ItemOutput::new)
    );

    public static final Codec<ItemOutput> CODEC = Codec.either(
            ItemStack.CODEC,
            STRICT_CODEC
    ).xmap(
            either -> either.map(
                    ItemOutput::of,
                    o -> o
            ),
            output -> output.chance == 1f ? Either.left(output.stack()) : Either.right(output)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemOutput> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ItemOutput::stack,
            ByteBufCodecs.FLOAT, ItemOutput::chance,
            ItemOutput::new
    );

    public static ItemOutput of(ItemStack stack, float chance) {
        return new ItemOutput(stack, chance);
    }

    public static ItemOutput of(ItemStack stack) {
        return new ItemOutput(stack, 1.0f);
    }

    public static ItemOutput of(ItemLike item) {
        return new ItemOutput(new ItemStack(item, 1), 1.0f);
    }

    public static ItemOutput of(ItemLike item, int count, float chance) {
        return new ItemOutput(new ItemStack(item, count), chance);
    }

    public static ItemOutput of(ItemLike item, int count) {
        return new ItemOutput(new ItemStack(item, count), 1.0f);
    }

    public boolean roll() {
        if (chance == 1f) return true;
        return RANDOM.nextFloat() < chance;
    }
}
