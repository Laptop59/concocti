package io.github.laptop59.concocti.common;

import io.github.laptop59.concocti.network.ConcoctizedEntitiesPayloadHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;

@EventBusSubscriber(modid = "concocti", bus = EventBusSubscriber.Bus.GAME)
public class ConcoctiEventHandler {
    @SubscribeEvent
    private static void onLivingConversion(final LivingConversionEvent.Post event) {
        // Update the list of concoctized mods.
        Level level = event.getEntity().level();
        if (level.isClientSide()) return;
        if (level instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players())
                ConcoctizedEntitiesPayloadHandler.updateEntities(level, player);
        }
    }
}