package io.github.laptop59.concocti.network;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class FluidBarSoundPayloadS2CHandler {
    public static void handleData(final FluidBarSoundPayloadS2C data, final IPayloadContext context) {
        // Try to get the cursor item of the player.
        context.enqueueWork(() -> {
            Player player = context.player();
            player.playSound(data.wasBucketFilled() ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_EMPTY);
        })
        .exceptionally(e -> {
            // Who cares anyway?
            return null;
        });
    }
}
