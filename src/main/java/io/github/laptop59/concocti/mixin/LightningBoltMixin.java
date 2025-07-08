package io.github.laptop59.concocti.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.ConductiviumLightningRodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin {
    @Inject(
            method = "powerLightningRod",
            at = @At(value = "TAIL")
    )
    private void powerLightningRod(CallbackInfo ci, @Local BlockPos blockpos, @Local BlockState blockstate) {
        // Check for our rod.
        LightningBolt lightningBolt = ((LightningBolt) (Object) this);
        if (blockstate.is(ConcoctiBlocks.CONDUCTIVIUM_LIGHTNING_ROD)) {
            ((ConductiviumLightningRodBlock) blockstate.getBlock()).onLightningStrike(blockstate, lightningBolt.level(), blockpos);
        }
    }
}
