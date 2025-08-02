package io.github.laptop59.concocti.mixin;

import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import io.github.laptop59.concocti.common.machine.ConcoctiMultiBlockMachine;
import io.github.laptop59.concocti.common.poi.ConcoctiPoiTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Unique
    PoiManager concocti$poiManager;

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At(value = "TAIL")
    )
    public void concoct$setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;
        if (level instanceof ServerLevel serverLevel) {
            concocti$poiManager = serverLevel.getPoiManager();
            Stream<PoiRecord> records = concocti$getInRange(pos);
            for (Iterator<PoiRecord> it = records.iterator(); it.hasNext(); ) {
                PoiRecord record = it.next();
                BlockPos controllerPos = record.getPos();
                if (level.getBlockEntity(controllerPos) instanceof ConcoctiMultiBlockMachine.BlockEntity blockEntity) {
                    blockEntity.updateMultiblockState();
                }
            }
        }
    }

    // Maybe the following is more optimized?

    @Unique
    public boolean concocti$isMultiblockController(Holder<PoiType> holder) {
        return ConcoctiPoiTypes.MULTIBLOCK_CONTROLLER.get() == holder.value();
    }

    @Unique
    public Stream<PoiRecord> concocti$getInRange(BlockPos pos) {
        int i = Math.floorDiv(ConcoctiMachines.MAX_RADIUS_SEARCHABLE, 16) + 1;
        return ChunkPos.rangeClosed(new ChunkPos(pos), i)
                .flatMap(this::concocti$intoRecords)
                .filter(poiRecord -> {
                    BlockPos blockpos = poiRecord.getPos();
                    int dx = Math.abs(blockpos.getX() - pos.getX());
                    int dy = Math.abs(blockpos.getY() - pos.getY());
                    int dz = Math.abs(blockpos.getZ() - pos.getZ());
                    return dx <= ConcoctiMachines.MAX_X_RADIUS_SEARCHABLE &&
                            dz <= ConcoctiMachines.MAX_Z_RADIUS_SEARCHABLE &&
                            dy <= ConcoctiMachines.MAX_Y_RADIUS_SEARCHABLE &&
                            dx * dx + dy * dy + dz * dz <= ConcoctiMachines.MAX_SQ_RADIUS_SEARCHABLE;
                });
    }

    @Unique
    public Stream<PoiRecord> concocti$intoRecords(ChunkPos chunkPos) {
        return concocti$poiManager.getInChunk(this::concocti$isMultiblockController, chunkPos, PoiManager.Occupancy.ANY);
    }
}
