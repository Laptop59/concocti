package io.github.laptop59.concocti.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.laptop59.concocti.common.machine.SolarStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Ingredient;

public class SolarState implements SolarStorage {
    private long solarAmount;
    private long maxSolarAmount;

    public final Codec<SolarState> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.LONG.fieldOf("solar_amount").forGetter(SolarState::getSolarAmount),
                    Codec.LONG.fieldOf("max_solar_amount").forGetter(SolarState::getMaxSolarAmount)
            ).apply(instance, SolarState::new)
    );

    public static final StreamCodec<ByteBuf, SolarState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, SolarState::getSolarAmount,
            ByteBufCodecs.VAR_LONG, SolarState::getMaxSolarStored,
            SolarState::new
    );

    public SolarState(long solarAmount, long maxSolarAmount) {
        this.solarAmount = solarAmount;
        this.maxSolarAmount = maxSolarAmount;
    }

    public void setSolarAmount(long solarAmount) {
        this.solarAmount = solarAmount;
    }

    public void setMaxSolarAmount(long maxSolarAmount) {
        this.maxSolarAmount = maxSolarAmount;
    }

    public long getSolarAmount() {
        return solarAmount;
    }

    public long getMaxSolarAmount() {
        return maxSolarAmount;
    }

    @Override
    public long receiveSolar(long toReceive, boolean simulate) {
        if (!canReceive() || toReceive <= 0) {
            return 0;
        }

        long energyReceived = Mth.clamp(this.maxSolarAmount - this.solarAmount, 0, Math.min(this.maxSolarAmount, toReceive));
        if (!simulate)
            this.solarAmount += energyReceived;
        return energyReceived;
    }

    @Override
    public long extractSolar(long toExtract, boolean simulate) {
        if (!canExtract() || toExtract <= 0) {
            return 0;
        }

        long energyExtracted = Math.min(this.solarAmount, Math.min(this.maxSolarAmount, toExtract));
        if (!simulate)
            this.solarAmount -= energyExtracted;
        return energyExtracted;
    }

    @Override
    public long getMaxSolarStored() {
        return this.maxSolarAmount;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
