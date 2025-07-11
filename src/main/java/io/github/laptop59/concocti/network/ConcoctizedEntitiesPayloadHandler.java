package io.github.laptop59.concocti.network;

import io.github.laptop59.concocti.client.ConcoctiClient;
import io.github.laptop59.concocti.common.Concocti;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConcoctizedEntitiesPayloadHandler {
    public static void handleData(final ConcoctizedEntitiesPayload data, final IPayloadContext context) {
        Set<UUID> set = data.entities().stream().map(UUID::fromString).collect(Collectors.toSet());
        context.enqueueWork(() -> {
                    ConcoctiClient.concoctizedEntities = set;
                })
                .exceptionally(e -> {
                    // Who cares anyway?
                    return null;
                });
    }

    public static void updateEntities(Level level, ServerPlayer player) {
        Vec3 pos = player.getBlockPosBelowThatAffectsMyMovement().getCenter();
        AABB aabb = new AABB(pos.add(100, 100, 100), pos.add(-100, -100, -100));
        PacketDistributor.sendToPlayer(player, new ConcoctizedEntitiesPayload(
                level.getEntitiesOfClass(LivingEntity.class, aabb, le -> le.hasEffect(Concocti.CONCOCTIZED))
                        .stream().map(Entity::getStringUUID)
                        .collect(Collectors.toSet())
        ));
    }
}