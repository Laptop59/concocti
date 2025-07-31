package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * A type of tank which only allows Concocti upgrades, or only items
 * specified in the {@code #concocti:concocti_upgrade} item tag.
 */
public class ConcoctiUpgradeSlot extends Slot implements IconSlot {
    public ConcoctiUpgradeSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return mayPlaceItem(stack);
    }

    public static HashMap<Item, Integer> upgradeUnits = new HashMap<>();

    static {
        registerUpgrade(ConcoctiItems.COMPACT_CONCOCTI_UPGRADE, 1);
        registerUpgrade(ConcoctiItems.COMPACTER_CONCOCTI_UPGRADE, 3);
        registerUpgrade(ConcoctiItems.COMPACTEST_CONCOCTI_UPGRADE, 5);
    }

    private static void registerUpgrade(Supplier<? extends Item> item, int units) {
        upgradeUnits.put(item.get(), units);
    }

    public static int getUpgradeUnits(ItemStack stack) {
        return Math.clamp((long) upgradeUnits.getOrDefault(stack.getItem(), 0) * stack.getCount(), 0, 100);
    }

    /**
     * Returns {@code true} if the given {@link net.minecraft.world.item.ItemStack} can be used as an upgrade.
     */
    public static boolean mayPlaceItem(ItemStack itemStack) {
        return itemStack.is(ConcoctiItems.Tags.CONCOCTI_UPGRADES);
    }

    @Override
    public Icon getIcon() {
        return Icon.CONCOCTI_UPGRADE;
    }
}