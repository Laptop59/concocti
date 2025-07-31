package io.github.laptop59.concocti.mixin;

import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
        if (!player.level().isClientSide()) {
            ItemStack weapon = player.getWeaponItem();
            if (weapon.is(ConcoctiItems.CONCOCTI_SEEDS)) {
                LivingEntity livingTarget = concocti$isValidSeedTarget(target);
                if (livingTarget != null) {
                    if (!player.isCreative()) player.getWeaponItem().consume(1, player);
                    livingTarget.addEffect(
                            new MobEffectInstance(Concocti.CONCOCTIZED, 20 * 60, 1)
                    );
                }
            } else if (weapon.is(ConcoctiItems.INFINITY_CONCOCTI_SEEDS)) {
                LivingEntity livingTarget = concocti$isValidSeedTarget(target);
                if (livingTarget != null) {
                    livingTarget.addEffect(
                            // -1 is infinite duration.
                            new MobEffectInstance(Concocti.CONCOCTIZED, -1, 3)
                    );
                }
            }
        }
    }

    @Unique
    private LivingEntity concocti$isValidSeedTarget(Entity entity) {
        if (!(entity instanceof EnderDragon) && entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }
}
