package io.github.laptop59.concocti.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.laptop59.concocti.client.ConcoctiClient;
import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class DisableDurabilityTooltipMixin {

    @Shadow
    public abstract Item getItem();

    @Inject(
            method = "getTooltipLines", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER, ordinal = 2)
    )
    public void getTooltipLines(Item.TooltipContext tooltipContext, Player player,
        TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> list) {
        if (ConcoctiItems.DISABLED_DURABILITY_TOOLTIP_ITEMS.stream().anyMatch(
                deferredItem -> deferredItem.get() == getItem()
        )) {
            list.removeLast();
        }
    }
}