package io.github.laptop59.concocti.common.item;

import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.ConcoctiRegisters;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.ConcoctiHatchBlock;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.*;
import java.util.function.Function;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiItems {
    public static final Collection<DeferredItem<? extends Item>> ITEM_LIST = new HashSet<>();

    // Create a Deferred Register to hold Items which will all be registered under the "concocti" namespace (done in ConcoctiRegisters).
    // Now, we can register!

    public static final DeferredItem<Item> DIAMETHYST_CRYSTAL = registerItem("diamethyst_crystal", new Item.Properties());
    public static final DeferredItem<BlockItem> DIAMETHYST_BLOCK = registerBlockItem(ConcoctiBlocks.DIAMETHYST_BLOCK);

    public static final DeferredItem<Item> CONCOCTI_SEEDS = registerItem("concocti_seeds", new Item.Properties());
    public static final DeferredItem<Item> INFINITY_CONCOCTI_SEEDS = registerItem("infinity_concocti_seeds", new Item.Properties());

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

    public static final DeferredItem<Item> CONDUCTIVIUM_NUGGET = registerItem("conductivium_nugget", new Item.Properties());
    public static final DeferredItem<Item> CONDUCTIVIUM_INGOT = registerItem("conductivium_ingot", new Item.Properties());
    public static final DeferredItem<BlockItem> CONDUCTIVIUM_BLOCK = registerBlockItem(ConcoctiBlocks.CONDUCTIVIUM_BLOCK);
    public static final DeferredItem<BlockItem> CONDUCTIVIUM_LIGHTNING_ROD = registerBlockItem(ConcoctiBlocks.CONDUCTIVIUM_LIGHTNING_ROD);

    public static final DeferredItem<Item> CRYSTALIUM_NUGGET = registerItem("crystalium_nugget", new Item.Properties());
    public static final DeferredItem<Item> CRYSTALIUM_INGOT = registerItem("crystalium_ingot", new Item.Properties());
    public static final DeferredItem<BlockItem> CRYSTALIUM_BLOCK = registerBlockItem(ConcoctiBlocks.CRYSTALIUM_BLOCK);
    public static final DeferredItem<Item> RAW_CRYSTALIUM = registerItem("raw_crystalium", new Item.Properties());
    public static final DeferredItem<BlockItem> CRYSTALIUM_ORE = registerBlockItem(ConcoctiBlocks.CRYSTALIUM_ORE);

    public static final DeferredItem<Item> LATTICIUM_NUGGET = registerItem("latticium_nugget", new Item.Properties());
    public static final DeferredItem<Item> LATTICIUM_INGOT = registerItem("latticium_ingot", new Item.Properties());
    public static final DeferredItem<BlockItem> LATTICIUM_BLOCK = registerBlockItem(ConcoctiBlocks.LATTICIUM_BLOCK);

    public static final DeferredItem<Item> ELECTROSTATIC_CONDUCTIVIUM_NUGGET = registerItem("electrostatic_conductivium_nugget", new Item.Properties());
    public static final DeferredItem<Item> ELECTROSTATIC_CONDUCTIVIUM_INGOT = registerItem("electrostatic_conductivium_ingot", new Item.Properties());

    public static final DeferredItem<Item> SOLARIUM_CATALYST = registerItem("solarium_catalyst", new Item.Properties());
    public static final DeferredItem<Item> CONDUCTIVE_SOLARIUM_CATALYST = registerItem("conductive_solarium_catalyst", new Item.Properties());
    public static final DeferredItem<Item> ELECTROPHILIC_CATALYST = registerItem("electrophilic_catalyst", new Item.Properties());
    public static final DeferredItem<Item> SOLARIUM_NUGGET = registerItem("solarium_nugget", new Item.Properties());
    public static final DeferredItem<Item> SOLARIUM_INGOT = registerItem("solarium_ingot", new Item.Properties());
    public static final DeferredItem<BlockItem> SOLARIUM_BLOCK = registerBlockItem(ConcoctiBlocks.SOLARIUM_BLOCK);

    public static final DeferredItem<Item> SMALL_SEED_CATALYST = registerItem("small_seed_catalyst", new Item.Properties());
    public static final DeferredItem<Item> MEDIUM_SEED_CATALYST = registerItem("medium_seed_catalyst", new Item.Properties());
    public static final DeferredItem<Item> LARGE_SEED_CATALYST = registerItem("large_seed_catalyst", new Item.Properties());
    public static final DeferredItem<Item> SEED_BASE = registerItem("seed_base", new Item.Properties());

    // Buckets of fluids are now automatically defined by the ConcoctiFluid class.

    public static final DeferredItem<? extends Item> COMPACT_CONCOCTI_UPGRADE = registerItem("compact_concocti_upgrade", ConcoctiUpgradeItem::new, new Item.Properties().stacksTo(8));
    public static final DeferredItem<? extends Item> COMPACTER_CONCOCTI_UPGRADE = registerItem("compacter_concocti_upgrade", ConcoctiUpgradeItem::new, new Item.Properties().stacksTo(8));
    public static final DeferredItem<? extends Item> COMPACTEST_CONCOCTI_UPGRADE = registerItem("compactest_concocti_upgrade", ConcoctiUpgradeItem::new, new Item.Properties().stacksTo(8));

    public static final DeferredItem<BlockItem> BASIC_CONCOCTI_FRAME = registerBlockItem(ConcoctiBlocks.BASIC_CONCOCTI_FRAME);
    public static final DeferredItem<BlockItem> ADVANCED_CONCOCTI_FRAME = registerBlockItem(ConcoctiBlocks.ADVANCED_CONCOCTI_FRAME);

    public static final DeferredItem<BlockItem> CONCOCTI_BRICKS = registerBlockItem(ConcoctiBlocks.CONCOCTI_BRICKS);
    public static final DeferredItem<BlockItem> TOUGH_CONCOCTI_BRICKS = registerBlockItem(ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS);

    public static final DeferredItem<Item> DENSE_CONCOCTI_PELLET = registerItem("dense_concocti_pellet", new Item.Properties());

    public static final List<DeferredItem<BlockItem>> HATCHES =
            ConcoctiBlocks.HATCHES_LIST.stream().map(ConcoctiItems::registerBlockItem).toList();


    public static final Set<DeferredItem<? extends Item>> DISABLED_DURABILITY_TOOLTIP_ITEMS = new HashSet<>();

    public static class Tags {
        /// ITEM TAGS
        public static final TagKey<Item> CONCOCTI_UPGRADES = TagKey.create(
                Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "concocti_upgrades")
        );
        public static final TagKey<Item> CONCOCTI_HATCHES = TagKey.create(
                Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "concocti_hatches")
        );
        public static final Map<MoldItem.Type, TagKey<Item>> MOLDS = new TreeMap<>();
    }

    public static final Map<MoldItem.Material, Map<MoldItem.Type, DeferredItem<? extends Item>>> MOLDS = registerAllMolds();
    public static final Map<MoldItem.Material, DeferredItem<? extends Item>> MOLD_BASES = registerAllMoldBases();

    static {
        for (MoldItem.Type type : MoldItem.Type.values())
            Tags.MOLDS.put(type, TagKey.create(
                    Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "molds/" + type.id)
            ));
    }

    /// REGISTERING METHODS

    /**
     * Registers all mold bases of all materials.
     */
    private static Map<MoldItem.Material, DeferredItem<? extends Item>> registerAllMoldBases() {
        Map<MoldItem.Material, DeferredItem<? extends Item>> molds = new EnumMap<>(MoldItem.Material.class);
        for (MoldItem.Material material : MoldItem.Material.values()) {
            DeferredItem<Item> item = ConcoctiRegisters.ITEMS.registerItem(MoldItem.getBaseIdentifier(material),
                    properties -> new MoldBaseItem(properties, material), new Item.Properties());
            molds.put(material, item);
            addToItemList(item);
        }
        return molds;
    }

    /**
     * Registers all molds of all materials.
     *
     * @return A two-dimensional map of {@link DeferredItem}s.
     */
    public static Map<MoldItem.Material, Map<MoldItem.Type, DeferredItem<? extends Item>>> registerAllMolds() {
        Map<MoldItem.Material, Map<MoldItem.Type, DeferredItem<? extends Item>>> molds = new EnumMap<>(MoldItem.Material.class);
        for (MoldItem.Material material : MoldItem.Material.values()) {
            molds.put(material, registerMolds(material));
        }
        return molds;
    }

    /**
     * Registers all molds of a particular material.
     *
     * @return A map of already registered {@link DeferredItem}s.
     */
    public static Map<MoldItem.Type, DeferredItem<? extends Item>> registerMolds(MoldItem.Material material) {
        Map<MoldItem.Type, DeferredItem<? extends Item>> molds = new EnumMap<>(MoldItem.Type.class);
        for (MoldItem.Type type : MoldItem.Type.values()) {
            molds.put(type, registerMold(material, type));
        }
        return molds;
    }

    /**
     * Registers a mold of a particular material and type.
     *
     * @return A map of the registered {@link DeferredItem}.
     */
    public static DeferredItem<? extends Item> registerMold(MoldItem.Material material, MoldItem.Type type) {
        Item.Properties moldProperties = new Item.Properties();
        if (material.durability != Integer.MAX_VALUE) moldProperties.durability(material.durability);
        DeferredItem<Item> item = ConcoctiRegisters.ITEMS.registerItem(
                MoldItem.getIdentifier(material, type),
                (properties -> new MoldItem(properties, material, type)),
                moldProperties
        );
        DISABLED_DURABILITY_TOOLTIP_ITEMS.add(item);
        addToItemList(item);
        return item;
    }

    /**
     * Registers a block item.
     *
     * @param block The block to create an item for.
     * @return A {@link DeferredItem} for the registered item.
     */
    public static DeferredItem<BlockItem> registerBlockItem(DeferredBlock<? extends Block> block) {
        DeferredItem<BlockItem> item = ConcoctiRegisters.ITEMS.registerSimpleBlockItem(block.getId().getPath(), block);
        addToItemList(item);
        return item;
    }

    /**
     * Registers a simple item (an item using the base {@link Item} class).
     *
     * @param name  The name of the item.
     * @param props The properties of the item.
     * @return A {@link DeferredItem} for the registered item.
     */
    public static DeferredItem<Item> registerItem(String name, Item.Properties props) {
        DeferredItem<Item> item = ConcoctiRegisters.ITEMS.registerSimpleItem(name, props);
        addToItemList(item);
        return item;
    }

    /**
     * Registers an item (an item using the {@link Item} class or any of its subclasses).
     *
     * @param name  The name of the item.
     * @param func  The factory for this item. For simple items, this is {@code Item::new}.
     * @param props The properties of the item.
     * @return A {@link DeferredItem} for the registered item.
     */
    public static DeferredItem<? extends Item> registerItem(String name, Function<Item.Properties, ? extends Item> func, Item.Properties props) {
        DeferredItem<Item> item = ConcoctiRegisters.ITEMS.registerItem(name, func, props);
        addToItemList(item);
        return item;
    }

    /**
     * Registers a bucket item (an item using the {@link BucketItem} class).
     *
     * @param name The name of the fluid.
     * @return A {@link DeferredItem} for the registered item.
     */
    public static DeferredItem<BucketItem> registerBucketItem(String name, DeferredHolder<Fluid, FlowingFluid> fluid) {
        DeferredItem<BucketItem> item = ConcoctiRegisters.ITEMS.registerItem(name, (props) -> new BucketItem(fluid.get(), props),
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
        acceptStack(output, DIAMETHYST_BLOCK);

        acceptStack(output, CONCOCTI_SEEDS);
        acceptStack(output, INFINITY_CONCOCTI_SEEDS);
        acceptStack(output, CONCOCTI_BRICKS);
        acceptStack(output, TOUGH_CONCOCTI_BRICKS);

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

        acceptStack(output, CONDUCTIVIUM_NUGGET);
        acceptStack(output, CONDUCTIVIUM_INGOT);
        acceptStack(output, CONDUCTIVIUM_BLOCK);
        acceptStack(output, CONDUCTIVIUM_LIGHTNING_ROD);

        acceptStack(output, RAW_CRYSTALIUM);
        acceptStack(output, CRYSTALIUM_ORE);
        acceptStack(output, CRYSTALIUM_NUGGET);
        acceptStack(output, CRYSTALIUM_INGOT);
        acceptStack(output, CRYSTALIUM_BLOCK);

        acceptStack(output, LATTICIUM_NUGGET);
        acceptStack(output, LATTICIUM_INGOT);
        acceptStack(output, LATTICIUM_BLOCK);

        acceptStack(output, ELECTROSTATIC_CONDUCTIVIUM_NUGGET);
        acceptStack(output, ELECTROSTATIC_CONDUCTIVIUM_INGOT);

        acceptStack(output, SOLARIUM_CATALYST);
        acceptStack(output, SOLARIUM_NUGGET);
        acceptStack(output, SOLARIUM_INGOT);
        acceptStack(output, SOLARIUM_BLOCK);

        acceptStack(output, CONDUCTIVE_SOLARIUM_CATALYST);
        acceptStack(output, ELECTROPHILIC_CATALYST);

        acceptStack(output, SMALL_SEED_CATALYST);
        acceptStack(output, MEDIUM_SEED_CATALYST);
        acceptStack(output, LARGE_SEED_CATALYST);
        acceptStack(output, SEED_BASE);

        ConcoctiMachines.forEach(concoctiMachine ->
            acceptStack(output, concoctiMachine.ITEM)
        );

        acceptStack(output, COMPACT_CONCOCTI_UPGRADE);
        acceptStack(output, COMPACTER_CONCOCTI_UPGRADE);
        acceptStack(output, COMPACTEST_CONCOCTI_UPGRADE);

        acceptStack(output, BASIC_CONCOCTI_FRAME);
        acceptStack(output, ADVANCED_CONCOCTI_FRAME);

        ConcoctiFluids.forEach(concoctiFluid -> {
            if (concoctiFluid.BUCKET != null) acceptStack(output, concoctiFluid.BUCKET);
        });

        for (Map.Entry<MoldItem.Material, Map<MoldItem.Type, DeferredItem<? extends Item>>> entry : MOLDS.entrySet()) {
            acceptStack(output, MOLD_BASES.get(entry.getKey()));
            for (Map.Entry<MoldItem.Type, DeferredItem<? extends Item>> entry2 : entry.getValue().entrySet()) {
                acceptStack(output, entry2.getValue());
            }
        }

        for (DeferredItem<BlockItem> hatch : HATCHES) {
            acceptStack(output, hatch);
        }
    }

    private static void acceptStack(CreativeModeTab.Output output, DeferredItem<? extends Item> item) {
        output.accept(item.get().getDefaultInstance());
    }
}
