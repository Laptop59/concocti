package io.github.laptop59.concocti.mixin.client;

import io.github.laptop59.concocti.client.ConcoctiClient;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(
            method = "getOverlayCoords", at = @At(value = "HEAD"),
            cancellable = true)
    private static void concocti$getOverlayCoords(LivingEntity livingEntity, float u, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(
                // OverlayTexture.v(livingEntity.hurtTime > 0 || livingEntity.deathTime > 0)
                OverlayTexture.pack(OverlayTexture.u(u), ((livingEntity.hurtTime > 0 || livingEntity.deathTime > 0) ? 3 : (concocti$hasConcoctizedEffect(livingEntity) ? 2 : 10)))
        );
    }

    @Unique
    private static boolean concocti$hasConcoctizedEffect(LivingEntity livingEntity) {
        return ConcoctiClient.concoctizedEntities.contains(livingEntity.getUUID());
    }
}
