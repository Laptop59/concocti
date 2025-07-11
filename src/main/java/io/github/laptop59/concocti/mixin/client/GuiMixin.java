package io.github.laptop59.concocti.mixin.client;

import io.github.laptop59.concocti.client.ConcoctiClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

import static io.github.laptop59.concocti.common.Concocti.MODID;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    @Nullable
    protected abstract Player getCameraPlayer();

    @Inject(
            method = "renderHeart", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V", shift = At.Shift.AFTER),
            cancellable = true)
    private void concocti$renderHeart(GuiGraphics guiGraphics, Gui.HeartType heartType, int x, int y, boolean hardcore, boolean halfHeart, boolean blinking, CallbackInfo ci) {
        // We don't want to override the container sprites.
        if (heartType != Gui.HeartType.NORMAL) return;
        Player player = getCameraPlayer();
        if (player == null) return;
        if (ConcoctiClient.concoctizedEntities.contains(player.getUUID())) {
            // The player has the Concoctized effect. Use our special hearts!
            // We first get the correct suffix.
            String suffix = hardcore ? "hardcore_" : "";
            suffix += halfHeart ? "half" : "full";
            // Then, draw the sprite.
            guiGraphics.blitSprite(ResourceLocation.fromNamespaceAndPath(MODID, "hud/concoctized_heart_" + suffix), x, y, 9, 9);
            ci.cancel();
        }
    }
}
