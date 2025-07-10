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

/** A class representing a mold item. */
public class MoldItem extends Item {
    /** Material of a mold. */
    public enum Material implements Comparable<Material> {
        COPPER(16, "copper", 19),
        DIAMOND(64, "diamond", 184);

        public final int durability;
        public final String prefix;
        public final float hue;

        Material(int durability, String prefix, float hue) {
            this.durability = durability;
            this.prefix = prefix;
            this.hue = hue;
        }

        public int getColor() {
            return Color.getHSBColor(hue / 360f, 0.16f, 1f).getRGB();
        }
    }

    /** Type of mold. */
    public enum Type implements Comparable<Type> {
        NUGGET("nugget", ResourceLocation.fromNamespaceAndPath("c", "nuggets")),
        INGOT("ingot", ResourceLocation.fromNamespaceAndPath("c", "ingots"));

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

    public Type getType() { return type; }
    public Material getMaterial() { return material; }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        int total = getMaxDamage(stack);
        int left = total - getDamage(stack);

        int color = material.getColor();

        tooltipComponents.add(Component.translatable("screen.concocti.durability_info",
                        Component.literal(String.valueOf(left)).withColor(color),
                        Component.literal(String.valueOf(total)).withColor(color)
                ).withStyle(ChatFormatting.DARK_GRAY)
        );
    }
}
