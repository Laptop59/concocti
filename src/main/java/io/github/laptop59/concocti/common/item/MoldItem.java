package io.github.laptop59.concocti.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

/**
 * A class representing a mold item.
 */
public class MoldItem extends Item {
    /**
     * Material of a mold.
     */
    public enum Material implements Comparable<Material> {
        COPPER(32, "copper", 19, 40),
        DIAMOND(128, "diamond", 184, 160),
        LATTICIUM(Integer.MAX_VALUE, "latticium", 0, 0, 0.3f, 640);

        public final int durability;
        public final String prefix;
        public final float hue;
        public final float saturation;
        public final float brightness;
        public final int ticksToMake;

        Material(int durability, String prefix, float hue, int ticksToMake) {
            this.durability = durability;
            this.prefix = prefix;
            this.hue = hue;
            this.saturation = 0.40f;
            this.brightness = 1f;
            this.ticksToMake = ticksToMake;
        }

        Material(int durability, String prefix, float hue, float saturation, float brightness,  int ticksToMake) {
            this.durability = durability;
            this.prefix = prefix;
            this.hue = hue;
            this.saturation = saturation;
            this.brightness = brightness;
            this.ticksToMake = ticksToMake;
        }

        public int getColor() {
            return Color.getHSBColor(hue / 360f, saturation, brightness).getRGB();
        }
    }

    /**
     * Type of mold.
     */
    public enum Type implements Comparable<Type> {
        NUGGET("nugget", ResourceLocation.fromNamespaceAndPath("c", "nuggets")),
        INGOT("ingot", ResourceLocation.fromNamespaceAndPath("c", "ingots")),
        DUST("dust", ResourceLocation.fromNamespaceAndPath("c", "dusts"));

        public final String id;
        public final ResourceLocation tag;
        private TagKey<Item> tagKey;

        Type(String id, ResourceLocation tag) {
            this.id = id;
            this.tag = tag;
        }

        public TagKey<Item> getTag() {
            if (tagKey != null) return tagKey;
            return tagKey = TagKey.create(Registries.ITEM, tag);
        }
    }

    protected Type type;
    protected Material material;

    public MoldItem(Properties properties, MoldItem.Material material, MoldItem.Type type) {
        super(properties);
        this.material = material;
        this.type = type;
    }

    public static String getIdentifier(MoldItem.Material material, MoldItem.Type type) {
        return material.prefix + "_" + type.id + "_mold";
    }

    public static String getBaseIdentifier(MoldItem.Material material) {
        return material.prefix + "_mold_base";
    }

    public Type getType() {
        return type;
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        int total = getMaxDamage(stack);
        int left = total - getDamage(stack);

        int color = material.getColor();

        if (material.durability != Integer.MAX_VALUE) {
            tooltipComponents.add(Component.translatable("screen.concocti.durability_info",
                            Component.literal(String.valueOf(left)).withColor(color),
                            Component.literal(String.valueOf(total)).withColor(color)
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );
        } else {
            tooltipComponents.add(Component.translatable("screen.concocti.infinite_durability_info",
                            Component.literal("∞").withColor(color)
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }
}
