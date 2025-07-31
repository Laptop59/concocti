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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.stream.Stream;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At(value = "TAIL")
    )
    public void concoct$setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;
        if (level instanceof ServerLevel serverLevel) {
            Stream<PoiRecord> records = serverLevel.getPoiManager().getInRange(
                    this::concocti$isMultiblockController,
                    pos,
                    ConcoctiMachines.MAX_RADIUS_SEARCHABLE,
                    PoiManager.Occupancy.ANY
            );
            for (Iterator<PoiRecord> it = records.iterator(); it.hasNext(); ) {
                PoiRecord record = it.next();
                BlockPos controllerPos = record.getPos();
                if (level.getBlockEntity(controllerPos) instanceof ConcoctiMultiBlockMachine.BlockEntity blockEntity) {
                    blockEntity.updateMultiblockState();
                }
            }
        }
    }

    @Unique
    public boolean concocti$isMultiblockController(Holder<PoiType> holder) {
        return ConcoctiPoiTypes.MULTIBLOCK_CONTROLLER.get() == holder.value();
    }
}
