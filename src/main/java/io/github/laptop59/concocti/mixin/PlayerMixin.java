package io.github.laptop59.concocti.mixin;

import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(
            method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;", shift = At.Shift.AFTER)
    )
    private void concocti$attack(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide() && player.getWeaponItem().is(ConcoctiItems.CONCOCTI_SEEDS)) {
            if (!(target instanceof EnderDragon) && target instanceof LivingEntity entity) {
                if (!player.isCreative()) player.getWeaponItem().consume(1, player);
                entity.addEffect(
                        new MobEffectInstance(Concocti.CONCOCTIZED, 20 * 60, 1)
                );
            }
        }
    }
}
