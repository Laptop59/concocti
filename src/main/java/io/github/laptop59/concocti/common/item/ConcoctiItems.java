package io.github.laptop59.concocti.common.item;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiItems {
    public static final Collection<DeferredItem<? extends Item>> ITEM_LIST = new HashSet<>();

    // Create a Deferred Register to hold Items which will all be registered under the "concocti" namespace
    /// ITEMS
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<Item> DIAMETHYST_CRYSTAL = registerItem("diamethyst_crystal", new Item.Properties());
    public static final DeferredItem<BlockItem> DIAMETHYST_CRYSTAL_BLOCK = registerBlockItem(ConcoctiBlocks.DIAMETHYST_BLOCK);

    public static final DeferredItem<Item> CONCOCTI_SEEDS = registerItem("concocti_seeds", new Item.Properties());

    public static final DeferredItem<Item> DIRTY_CONCOCTI_NUGGET = registerItem("dirty_concocti_nugget", new Item.Properties());
    public static final DeferredItem<Item> DIRTY_CONCOCTI_INGOT = registerItem("dirty_concocti_ingot", new Item.Properties());
    public static final DeferredItem<BlockItem> DIRTY_CONCOCTI_BLOCK = registerBlockItem(ConcoctiBlocks.DIRTY_CONCOCTI_BLOCK);

    public static final DeferredItem<Item> PURIFIED_CONCOCTI_NUGGET = registerItem("purified_concocti_nugget", new Item.Properties());
    public static final DeferredItem<Item> PURIFIED_CONCOCTI_INGOT = registerItem("purified_concocti_ingot", new Item.Properties());
    public static final DeferredItem<BlockItem> PURIFIED_CONCOCTI_BLOCK = registerBlockItem(ConcoctiBlocks.PURIFIED_CONCOCTI_BLOCK);

    public static final DeferredItem<Item> TOUGH_CONCOCTI_NUGGET = registerItem("tough_concocti_nugget", new Item.Properties());
    public static final DeferredItem<Item> TOUGH_CONCOCTI_INGOT = registerItem("tough_concocti_ingot", new Item.Properties());
    public static final DeferredItem<BlockItem> TOUGH_CONCOCTI_BLOCK = registerBlockItem(ConcoctiBlocks.TOUGH_CONCOCTI_BLOCK);

    public static final DeferredItem<Item> COMPRESSED_CONCOCTI_NUGGET = registerItem("compressed_concocti_nugget", new Item.Properties());
    public static final DeferredItem<Item> COMPRESSED_CONCOCTI_INGOT = registerItem("compressed_concocti_ingot", new Item.Properties());
    public static final DeferredItem<BlockItem> COMPRESSED_CONCOCTI_BLOCK = registerBlockItem(ConcoctiBlocks.COMPRESSED_CONCOCTI_BLOCK);

    public static final DeferredItem<BlockItem> CONCOCTI_MELTER = registerBlockItem(ConcoctiBlocks.CONCOCTI_MELTER);
    public static final DeferredItem<BlockItem> CONCOCTI_SOLIDIFIER = registerBlockItem(ConcoctiBlocks.CONCOCTI_SOLIDIFIER);

    public static final DeferredItem<BucketItem> MOLTEN_CONCOCTI_BUCKET = registerBucketItem("molten_concocti_bucket", ConcoctiFluids.MOLTEN_CONCOCTI);
    public static final DeferredItem<BucketItem> MOLTEN_CONCOCTIZED_DIRT_BUCKET = registerBucketItem("molten_concoctized_dirt_bucket", ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT);

    public static final DeferredItem<? extends Item> COMPACT_CONCOCTI_UPGRADE = registerItem("compact_concocti_upgrade", ConcoctiUpgradeItem::new, new Item.Properties().stacksTo(8));
    public static final DeferredItem<? extends Item> COMPACTER_CONCOCTI_UPGRADE = registerItem("compacter_concocti_upgrade", ConcoctiUpgradeItem::new, new Item.Properties().stacksTo(8));
    public static final DeferredItem<? extends Item> COMPACTEST_CONCOCTI_UPGRADE = registerItem("compactest_concocti_upgrade", ConcoctiUpgradeItem::new, new Item.Properties().stacksTo(8));

    public static final DeferredItem<BlockItem> BASIC_CONCOCTI_FRAME = registerBlockItem(ConcoctiBlocks.BASIC_CONCOCTI_FRAME);
    public static final DeferredItem<BlockItem> ADVANCED_CONCOCTI_FRAME = registerBlockItem(ConcoctiBlocks.ADVANCED_CONCOCTI_FRAME);

    public static class Tags {
        /// ITEM TAGS
        public static final TagKey<Item> CONCOCTI_UPGRADES = TagKey.create(
                Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "concocti_upgrades")
        );
        public static final TagKey<Item> CONCOCTI_MOLDS = TagKey.create(
                Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "concocti_molds")
        );
    }

    /// REGISTERING METHODS

    /**
     * Registers a block item.
     * @param block The block to create an item for.
     * @return A {@link DeferredItem} for the registered item.
     */
    public static DeferredItem<BlockItem> registerBlockItem(DeferredBlock<Block> block) {
        DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(block.getId().getPath(), block);
        addToItemList(item);
        return item;
    }

    /**
     * Registers a simple item (an item using the base {@link Item} class).
     * @param name The name of the item.
     * @param props The properties of the item.
     * @return A {@link DeferredItem} for the registered item.
     */
    public static DeferredItem<Item> registerItem(String name, Item.Properties props) {
        DeferredItem<Item> item = ITEMS.registerSimpleItem(name, props);
        addToItemList(item);
        return item;
    }

    /**
     * Registers an item (an item using the {@link Item} class or any of its subclasses).
     * @param name The name of the item.
     * @param func The factory for this item. For simple items, this is {@code Item::new}.
     * @param props The properties of the item.
     * @return A {@link DeferredItem} for the registered item.
     */
    public static DeferredItem<? extends Item> registerItem(String name, Function<Item.Properties, ? extends Item> func, Item.Properties props) {
        DeferredItem<Item> item = ITEMS.registerItem(name, func, props);
        addToItemList(item);
        return item;
    }

    /**
     * Registers a bucket item (an item using the {@link BucketItem} class).
     * @param name The name of the fluid.
     * @return A {@link DeferredItem} for the registered item.
     */
    public static DeferredItem<BucketItem> registerBucketItem(String name, DeferredHolder<Fluid, FlowingFluid> fluid) {
        DeferredItem<BucketItem> item = ITEMS.registerItem(name, (props) -> new BucketItem(fluid.get(), props),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)
        );
        addToItemList(item);
        return item;
    }

    private static void addToItemList(DeferredItem<? extends Item> item) {
        ITEM_LIST.add(item);
    }

    public static void addItemsToCreativeTab(CreativeModeTab.Output output) {
        acceptStack(output, DIAMETHYST_CRYSTAL);
        acceptStack(output, DIAMETHYST_CRYSTAL_BLOCK);

        acceptStack(output, CONCOCTI_SEEDS);

        acceptStack(output, DIRTY_CONCOCTI_NUGGET);
        acceptStack(output, DIRTY_CONCOCTI_INGOT);
        acceptStack(output, DIRTY_CONCOCTI_BLOCK);

        acceptStack(output, PURIFIED_CONCOCTI_NUGGET);
        acceptStack(output, PURIFIED_CONCOCTI_INGOT);
        acceptStack(output, PURIFIED_CONCOCTI_BLOCK);

        acceptStack(output, TOUGH_CONCOCTI_NUGGET);
        acceptStack(output, TOUGH_CONCOCTI_INGOT);
        acceptStack(output, TOUGH_CONCOCTI_BLOCK);

        acceptStack(output, COMPRESSED_CONCOCTI_NUGGET);
        acceptStack(output, COMPRESSED_CONCOCTI_INGOT);
        acceptStack(output, COMPRESSED_CONCOCTI_BLOCK);

        acceptStack(output, CONCOCTI_MELTER);
        acceptStack(output, CONCOCTI_SOLIDIFIER);

        acceptStack(output, MOLTEN_CONCOCTI_BUCKET);
        acceptStack(output, MOLTEN_CONCOCTIZED_DIRT_BUCKET);

        acceptStack(output, COMPACT_CONCOCTI_UPGRADE);
        acceptStack(output, COMPACTER_CONCOCTI_UPGRADE);
        acceptStack(output, COMPACTEST_CONCOCTI_UPGRADE);

        acceptStack(output, BASIC_CONCOCTI_FRAME);
        acceptStack(output, ADVANCED_CONCOCTI_FRAME);
    }

    private static void acceptStack(CreativeModeTab.Output output, DeferredItem<? extends Item> item) {
        output.accept(item.get().getDefaultInstance());
    }
}
