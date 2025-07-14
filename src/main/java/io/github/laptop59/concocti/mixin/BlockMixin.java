package io.github.laptop59.concocti.mixin;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(
            method = "playerWillDestroy", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/world/level/block/Block;spawnDestroyParticles(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V")
    )
    private void concocti$playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<BlockState> cir) {
        // Check for blocks guarded by endermen.
        if (state.is(ConcoctiBlocks.Tags.GUARDED_BY_ENDERMEN)) {
            concocti$angerNearbyEndermen(player);
        }
    }

    @Unique
    private static void concocti$angerNearbyEndermen(Player player) {
        List<EnderMan> list = player.level().getEntitiesOfClass(EnderMan.class, player.getBoundingBox().inflate(16.0));
        list.forEach(e -> {
            e.setPersistentAngerTarget(player.getUUID());
            e.startPersistentAngerTimer();
        });
    }
}
