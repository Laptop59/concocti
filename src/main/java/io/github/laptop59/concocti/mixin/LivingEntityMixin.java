package io.github.laptop59.concocti.mixin;

import io.github.laptop59.concocti.network.ConcoctizedEntitiesPayloadHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(
            method = "refreshDirtyAttributes",
            at = @At(value = "TAIL")
    )
    private void concocti$refreshDirtyAttributes(CallbackInfo ci) {
        // Update the list of concoctized entities.
        LivingEntity e = ((LivingEntity) (Object) this);
        if (!e.level().isClientSide() && e.level() instanceof ServerLevel level) {
            for (ServerPlayer player : level.players()) ConcoctizedEntitiesPayloadHandler.updateEntities(level, player);
        }
    }
}
