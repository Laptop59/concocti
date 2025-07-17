package io.github.laptop59.concocti.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A class representing a mold item.
 */
public class MoldBaseItem extends Item {
    protected MoldItem.Material material;

    public MoldBaseItem(Properties properties, MoldItem.Material material) {
        super(properties);
        this.material = material;
    }

    public MoldItem.Material getMaterial() {
        return material;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        String durability = "∞";
        if (material.durability != Integer.MAX_VALUE)
            durability = String.valueOf(material.durability);
        tooltipComponents.add(Component.translatable("screen.concocti.mold_base_" +
                                "durability_info",
                        Component.literal(durability).withColor(material.getColor())
                ).withStyle(ChatFormatting.DARK_GRAY)
        );
    }
}
