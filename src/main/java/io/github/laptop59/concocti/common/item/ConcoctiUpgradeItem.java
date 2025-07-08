package io.github.laptop59.concocti.common.item;

import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** A class representing a Concocti Upgrade item. */
public class ConcoctiUpgradeItem extends Item {
    public ConcoctiUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        int units = ConcoctiUpgradeSlot.getUpgradeUnits(stack.copyWithCount(1));
        int totalUnits = ConcoctiUpgradeSlot.getUpgradeUnits(stack);
        if (stack.getCount() > 1) tooltipComponents.add(Component.translatable("screen.concocti.upgrade_info", Component.literal(Integer.toString(totalUnits)).withColor(0xB69DFF))
                .withStyle(ChatFormatting.GRAY)
        );
        tooltipComponents.add(Component.translatable("screen.concocti.single_upgrade_info", Component.literal(Integer.toString(units)).withColor(0xC6ADFF))
                .withStyle(ChatFormatting.DARK_GRAY)
        );
        int ticks = AbstractConcoctiMachineBlockEntity.getTickMultiplier(totalUnits);
        tooltipComponents.add(Component.translatable("screen.concocti.rate", Component.literal(Integer.toString(ticks)).withColor(0xF5B8E7))
                .withColor(0xF58EDD)
        );
    }
}
