package io.github.laptop59.concocti.common.effect;

import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ConcoctizedMobEffect extends MobEffect {
    public ConcoctizedMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onMobHurt(@NotNull LivingEntity entity, int amplifier, @NotNull DamageSource damageSource, float amount) {
        // Spawn dirty nuggets.
        Level level = entity.level();
        if (!level.isClientSide()) {
            // Give 0-2 nuggets per HP. (for amplifier II)
            int nuggets = Math.clamp(Math.round(amount * (amplifier + 1) * level.getRandom().nextFloat()), 0, 64);
            entity.heal(amount / 2.0f);
            level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(),
                    new ItemStack(ConcoctiItems.DIRTY_CONCOCTI_NUGGET.get(), nuggets)
            ));
            // Chance to drop an ingot.
            if (Math.random() < Math.clamp((0.1 * amplifier + 0.15) * Math.clamp(nuggets + 0.25f, 0, 1), 0, 0.5))
                level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(),
                        new ItemStack(ConcoctiItems.DIRTY_CONCOCTI_INGOT.get(), 1 + nuggets / 2)
                ));
        }
    }
}
