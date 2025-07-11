package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.common.block.frame.FrameAttributes;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A type of slot which only allows Concocti upgrades, or only items
 * specified in the {@code #concocti:concocti_upgrade} item tag.
 */
public class ConcoctiFrameSlot extends Slot implements IconSlot {
    public ConcoctiFrameSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return mayPlaceItem(stack);
    }

    public static final Map<Item, FrameAttributes> FRAME_MAP = new HashMap<>();

    static {
        addAsFrameItem(ConcoctiItems.BASIC_CONCOCTI_FRAME, new FrameAttributes(2.0f, 0.05f));
        addAsFrameItem(ConcoctiItems.ADVANCED_CONCOCTI_FRAME, new FrameAttributes(3.0f, 0.10f));
    }

    /**
     * Adds an item that can be placed in a machine's frame slot into a list of such items.
     *
     * @param frame      Item to add.
     * @param attributes Attributes on how this item performs as a frame.
     */
    public static void addAsFrameItem(DeferredItem<? extends Item> frame, FrameAttributes attributes) {
        FRAME_MAP.put(frame.asItem(), attributes);
    }

    /**
     * Provides the attributes of a frame on how well it performs as one. Returns {@code Optional.empty} if the item is not a known frame.
     *
     * @param item Item to query.
     */
    public static Optional<FrameAttributes> getFrameAttributes(Item item) {
        return Optional.ofNullable(FRAME_MAP.get(item));
    }

    /**
     * Returns {@code true} if the given {@link net.minecraft.world.item.ItemStack} can be used as a frame.
     */
    public static boolean mayPlaceItem(ItemStack itemStack) {
        return getFrameAttributes(itemStack.getItem()).isPresent();
    }

    @Override
    public Icon getIcon() {
        return Icon.FRAME;
    }
}