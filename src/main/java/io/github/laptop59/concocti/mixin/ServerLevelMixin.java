package io.github.laptop59.concocti.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.laptop59.concocti.common.poi.ConcoctiPoiTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(
            method = "findLightningRod",
            at = @At(value = "TAIL"),
            cancellable = true)
    private void findLightningRod(BlockPos pos, CallbackInfoReturnable<Optional<BlockPos>> cir, @Local Optional<BlockPos> optional) {
        // Check for our rod.
        ServerLevel concocti$serverLevel = (ServerLevel) (Object) this;
        Optional<BlockPos> concocti$optionalConductivium = concocti$serverLevel.getPoiManager()
                .findClosest(
                        holder -> holder.value() == ConcoctiPoiTypes.CONDUCTIVIUM_LIGHTNING_ROD.get(),
                        pos1 -> pos1.getY() == concocti$serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, pos1.getX(), pos1.getZ()) - 1,
                        pos,
                        256, // more conductive
                        PoiManager.Occupancy.ANY
                );
        // For some clarification of intention, let us explain the following:
        // Let's say that conductivium is twice as 'conductive' as copper.
        // So that means for same resistance, copper rod's distance from the lightning strike's
        // original position is two times that of conductivium rod.
        // (probably not realistically accurate, but this is a block game)
        if (optional.isEmpty() && concocti$optionalConductivium.isPresent())
            cir.setReturnValue(concocti$optionalConductivium.map(op -> op.above(1)));
        else if (concocti$optionalConductivium.isPresent() && optional.isPresent() && concocti$serverLevel.random.nextInt(9) != 0) {
            // We can do the mathematical check.
            int sqDistanceCopper = concocti$sqDistBetween(pos, optional.get());
            int sqDistanceConductivium = concocti$sqDistBetween(pos, concocti$optionalConductivium.get());
            if (sqDistanceConductivium <= sqDistanceCopper * 4)
                cir.setReturnValue(concocti$optionalConductivium.map(op -> op.above(1)));
        }
    }

    @Inject(
            method = "tickChunk",
            at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=iceandsnow")
    )
    private void tickChunk(
            LevelChunk chunk,
            int randomTickSpeed,
            CallbackInfo ci,
            @Local boolean flag,
            @Local(name = "i", ordinal = 0, argsOnly = true) int i,
            @Local(name = "j", ordinal = 0, argsOnly = true) int j) {
        ServerLevel concocti$serverLevel = (ServerLevel) (Object) this;
        if (concocti$serverLevel.random.nextInt(1000) == 0) {
            BlockPos concocti$blockposBeforeChance = concocti$findLightningTargetAroundOnlyConductivium(concocti$serverLevel.getBlockRandomPos(i, 0, j, 15));
            long concocti$rods = concocti$serverLevel.getPoiManager().findAllWithType(
                holder -> {
                    PoiType a = holder.value();
                    PoiType b = ConcoctiPoiTypes.CONDUCTIVIUM_LIGHTNING_ROD.get();
                    return a == b;
                },
                pos -> pos.getY() == concocti$serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) - 1,
                concocti$blockposBeforeChance,
                256,
                PoiManager.Occupancy.ANY
            ).count();
            boolean concocti$flag = flag && concocti$serverLevel.isThundering() && concocti$serverLevel.isRainingAt(concocti$blockposBeforeChance);
            if (!concocti$flag)
                concocti$flag = concocti$serverLevel.random.nextInt(7) == 0; // if no thunderstorm and/or no rain
            if (concocti$flag && concocti$rods > 0) {
            /*
                 Calculate the chance.
                 Doing the math for one chunk 4 rods is optimal for the following:
                 This is what we want:

                 1 rod   -> 1 in 5000
                 2 rods  -> 1 in 400
                 3 rods  -> 1 in 300
                 4 rods  -> 1 in 200  (capped)

                 If we take the common chance first we get:

                 if (1 in 1000) {
                     1 rod -> 1 in 5
                          ...
                    4 rods -> 1 in 2   (capped)
                 }

                 This is important to reduce server lag.
                 Using POI every tick is not performant, even on the best systems.
            */
                int chance = (int) Math.max(6 - concocti$rods, 2);
                if (concocti$serverLevel.random.nextInt(chance) == 0) {
                    LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(concocti$serverLevel);
                    if (lightningbolt != null) {
                        lightningbolt.moveTo(Vec3.atBottomCenterOf(concocti$blockposBeforeChance));
                        lightningbolt.setVisualOnly(false);
                        concocti$serverLevel.addFreshEntity(lightningbolt);
                    }
                }
            }
        }
    }

    @Unique
    private int concocti$sqDistBetween(BlockPos pos1, BlockPos pos2) {
        int x = pos1.getX() - pos2.getX();
        int y = pos1.getY() - pos2.getY();
        int z = pos1.getZ() - pos2.getZ();
        return x * x + y * y + z * z;
    }

    @Unique
    protected BlockPos concocti$findLightningTargetAroundOnlyConductivium(BlockPos pos) {
        ServerLevel serverLevel = (ServerLevel) (Object) this;
        BlockPos blockpos = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
        Optional<BlockPos> optional = serverLevel.getPoiManager()
                .findClosest(
                        holder -> holder.value() == ConcoctiPoiTypes.CONDUCTIVIUM_LIGHTNING_ROD.get(),
                        pos1 -> pos1.getY() == serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, pos1.getX(), pos1.getZ()) - 1,
                        pos,
                        256, // more conductive
                        PoiManager.Occupancy.ANY
                )
                .map(pos1 -> pos1.above(1));
        if (optional.isPresent()) {
            return optional.get();
        } else {
            AABB aabb = AABB.encapsulatingFullBlocks(blockpos, new BlockPos(blockpos.atY(serverLevel.getMaxBuildHeight()))).inflate(3.0);
            List<LivingEntity> list = serverLevel.getEntitiesOfClass(
                    LivingEntity.class, aabb, p_352698_ -> p_352698_ != null && p_352698_.isAlive() && serverLevel.canSeeSky(p_352698_.blockPosition())
            );
            if (!list.isEmpty()) {
                return list.get(serverLevel.random.nextInt(list.size())).blockPosition();
            } else {
                if (blockpos.getY() == serverLevel.getMinBuildHeight() - 1) {
                    blockpos = blockpos.above(2);
                }

                return blockpos;
            }
        }
    }
}
