package io.github.laptop59.concocti.common.block.frame;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.laptop59.concocti.common.menu.ConcoctiFrameSlot.FRAME_MAP;

/**
 * A block that can act as a frame for Concocti Machines.
 */
public class FrameBlock extends Block {

    public FrameBlock(Properties properties) {
        super(properties);
    }

    private String makePercentValue(float value) {
        return String.format("%.1f", value * 100);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        FrameAttributes attributes = FRAME_MAP.get(stack.getItem());
        if (attributes != null) {
            tooltipComponents.add(
                    Component.translatable("screen.concocti.frame_info_rate", Component.literal(makePercentValue(attributes.rate() - 1)).withColor(0x99FFCF))
                            .withStyle(ChatFormatting.GRAY)
            );
            tooltipComponents.add(
                    Component.translatable("screen.concocti.frame_info_efficiency", Component.literal(makePercentValue(attributes.efficiency())).withColor(0xDD9DFF))
                            .withStyle(ChatFormatting.GRAY)
            );
        }
    }
}
