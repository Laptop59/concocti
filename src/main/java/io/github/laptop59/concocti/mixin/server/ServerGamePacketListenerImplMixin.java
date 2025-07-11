package io.github.laptop59.concocti.mixin.server;

import io.github.laptop59.concocti.network.ConcoctizedEntitiesPayloadHandler;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(
            method = "<init>", at = @At("TAIL")
    )
    private void concocti$init(MinecraftServer server, Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        ConcoctizedEntitiesPayloadHandler.updateEntities(player.level(), player);
    }
}
