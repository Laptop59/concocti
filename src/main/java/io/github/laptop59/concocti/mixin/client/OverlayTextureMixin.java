package io.github.laptop59.concocti.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OverlayTexture.class)
public class OverlayTextureMixin {
    @Inject(
            method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;setPixelRGBA(III)V", shift = At.Shift.AFTER)
    )
    private void concocti$setOverlayColor(CallbackInfo ci, @Local(ordinal = 0) int i, @Local(ordinal = 1) int j, @Local(name = "nativeimage") NativeImage image) {
        if (i < 3) {
            // We tint the mob "purple".
            image.setPixelRGBA(j, i, -1295765639);
        }
    }
}
