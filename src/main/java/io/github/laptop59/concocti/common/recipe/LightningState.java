package io.github.laptop59.concocti.common.recipe;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class LightningState {
    private boolean lightningCollected;

    public final Codec<LightningState> CODEC = Codec.BOOL.xmap(
            LightningState::new,
            LightningState::getLightningCollected
    );

    public final StreamCodec<ByteBuf, LightningState> STREAM_CODEC = ByteBufCodecs.BOOL.map(
            LightningState::new,
            LightningState::getLightningCollected
    );

    public LightningState(boolean lightningCollected) {
        this.lightningCollected = lightningCollected;
    }

    public void setLightningCollected(boolean lightningCollected) {
        this.lightningCollected = lightningCollected;
    }

    public boolean getLightningCollected() {
        return lightningCollected;
    }
}
