package io.github.laptop59.concocti.common.block;

import io.github.laptop59.concocti.common.ConcoctiRegisters;
import io.github.laptop59.concocti.common.block.entity.ConcoctiHatchBlockEntity;
import io.github.laptop59.concocti.common.block.frame.FrameBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.github.laptop59.concocti.common.Concocti.MODID;
import static io.github.laptop59.concocti.common.ConcoctiRegisters.BLOCK_ENTITY_TYPES;

public class ConcoctiBlocks {
    public static final Map<DeferredBlock<? extends Block>, BlockData> BLOCK_MAP = new HashMap<>();

    public static class Tags {
        public static TagKey<Block> GUARDED_BY_ENDERMEN = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MODID, "guarded_by_endermen"));
    }

    public static final DeferredBlock<Block> DIAMETHYST_BLOCK = registerBlock("diamethyst_block", Block::new,
            BlockBehaviour.Properties.of().mapColor(DyeColor.LIGHT_BLUE).requiresCorrectToolForDrops().explosionResistance(6).strength(6f),
            new BlockData(BlockToolRank.IRON, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> DIRTY_CONCOCTI_BLOCK = registerBlock("dirty_concocti_block", Block::new,
            BlockBehaviour.Properties.of().mapColor(DyeColor.PURPLE).requiresCorrectToolForDrops().explosionResistance(8).strength(3f),
            new BlockData(BlockToolRank.STONE, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> PURIFIED_CONCOCTI_BLOCK = registerBlock("purified_concocti_block", Block::new,
            BlockBehaviour.Properties.of().mapColor(DyeColor.MAGENTA).requiresCorrectToolForDrops().explosionResistance(10).strength(3.5f),
            new BlockData(BlockToolRank.IRON, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> TOUGH_CONCOCTI_BLOCK = registerBlock("tough_concocti_block", Block::new,
            BlockBehaviour.Properties.of().mapColor(DyeColor.MAGENTA).requiresCorrectToolForDrops().explosionResistance(20).strength(9f),
            new BlockData(BlockToolRank.DIAMOND, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> COMPRESSED_CONCOCTI_BLOCK = registerBlock("compressed_concocti_block", Block::new,
            BlockBehaviour.Properties.of().mapColor(DyeColor.PURPLE).requiresCorrectToolForDrops().explosionResistance(30).strength(15f),
            new BlockData(BlockToolRank.NETHERITE, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> CONDUCTIVIUM_BLOCK = registerBlock("conductivium_block", Block::new,
            BlockBehaviour.Properties.of().mapColor(DyeColor.CYAN).requiresCorrectToolForDrops().explosionResistance(20).strength(10f),
            new BlockData(BlockToolRank.IRON, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> CRYSTALIUM_BLOCK = registerBlock("crystalium_block", TransparentBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(DyeColor.MAGENTA)
                    .requiresCorrectToolForDrops()
                    .explosionResistance(200)
                    .strength(40f)
                    .noOcclusion()
                    .isViewBlocking((blockState, blockGetter, blockPos) -> false),
            new BlockData(BlockToolRank.DIAMOND, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> LATTICIUM_BLOCK = registerBlock("latticium_block", Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(DyeColor.BLACK)
                    .requiresCorrectToolForDrops()
                    .explosionResistance(200000)
                    .strength(400000f)
                    .noOcclusion()
                    .isViewBlocking((blockState, blockGetter, blockPos) -> false),
            new BlockData(BlockToolRank.NETHERITE, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> CRYSTALIUM_ORE = registerBlock("crystalium_ore", Block::new,
            BlockBehaviour.Properties.of().mapColor(DyeColor.MAGENTA).requiresCorrectToolForDrops().explosionResistance(10).strength(6f),
            new BlockData(BlockToolRank.DIAMOND, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> CONDUCTIVIUM_LIGHTNING_ROD = registerBlock("conductivium_lightning_rod", ConductiviumLightningRodBlock::new,
            BlockBehaviour.Properties.of().mapColor(DyeColor.CYAN).requiresCorrectToolForDrops().explosionResistance(2).strength(2f),
            new BlockData(BlockToolRank.IRON, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> SOLARIUM_BLOCK = registerBlock("solarium_block", Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(DyeColor.YELLOW)
                    .explosionResistance(10000)
                    .strength(5f)
                    .noOcclusion()
                    .isViewBlocking((blockState, blockGetter, blockPos) -> false),
            new BlockData(BlockToolRank.WOODEN, BlockToolType.PICKAXE));

    // FRAME BLOCKS

    public static final DeferredBlock<Block> LATTICIUM_FRAME = registerBlock("latticium_frame", Block::new,
            BlockBehaviour.Properties
                    .of()
                    .mapColor(DyeColor.BLACK)
                    .requiresCorrectToolForDrops()
                    .explosionResistance(800000)
                    .strength(1600000f),
            new BlockData(BlockToolRank.NETHERITE, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> BASIC_CONCOCTI_FRAME = registerBlock("basic_concocti_frame", FrameBlock::new,
            BlockBehaviour.Properties
                    .of()
                    .mapColor(DyeColor.MAGENTA)
                    .requiresCorrectToolForDrops()
                    .explosionResistance(20)
                    .strength(3.5f),
            new BlockData(BlockToolRank.IRON, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> ADVANCED_CONCOCTI_FRAME = registerBlock("advanced_concocti_frame", FrameBlock::new,
            BlockBehaviour.Properties
                    .of()
                    .mapColor(DyeColor.MAGENTA)
                    .requiresCorrectToolForDrops()
                    .explosionResistance(40)
                    .strength(9f),
            new BlockData(BlockToolRank.DIAMOND, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> CONCOCTI_BRICKS = registerBlock("concocti_bricks", Block::new,
            BlockBehaviour.Properties.of().mapColor(DyeColor.MAGENTA).requiresCorrectToolForDrops().explosionResistance(100).strength(3f),
            new BlockData(BlockToolRank.IRON, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> TOUGH_CONCOCTI_BRICKS = registerBlock("tough_concocti_bricks", Block::new,
            BlockBehaviour.Properties.of().mapColor(DyeColor.MAGENTA).requiresCorrectToolForDrops().explosionResistance(400).strength(4f),
            new BlockData(BlockToolRank.DIAMOND, BlockToolType.PICKAXE));

    public static final DeferredBlock<Block> AUTOCLAVE_FRAME = registerBlock("autoclave_frame", Block::new,
            BlockBehaviour.Properties
                    .of()
                    .mapColor(DyeColor.WHITE)
                    .requiresCorrectToolForDrops()
                    .explosionResistance(1200000)
                    .strength(2400000f),
            new BlockData(BlockToolRank.NETHERITE, BlockToolType.PICKAXE));

    public static final Map<HatchType, Map<HatchPurpose, DeferredBlock<? extends ConcoctiHatchBlock>>> HATCHES = registerHatches(
            BlockBehaviour.Properties.of().mapColor(DyeColor.MAGENTA).requiresCorrectToolForDrops().explosionResistance(500).strength(5f),
            new BlockData(BlockToolRank.DIAMOND, BlockToolType.PICKAXE)
    );

    // BLOCKS ONLY FOR SHOW (NO ITEM EQUIVALENT)
    public static final DeferredBlock<Block> BAD_BLOCK = registerBlock("bad_block", TransparentBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(DyeColor.RED)
                    .requiresCorrectToolForDrops()
                    .explosionResistance(0)
                    .strength(0f)
                    .noOcclusion()
                    .isViewBlocking((blockState, blockGetter, blockPos) -> false),
            new BlockData(BlockToolRank.STONE, BlockToolType.PICKAXE)
    );
    public static final DeferredBlock<Block> GOOD_BLOCK = registerBlock("good_block", TransparentBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(DyeColor.GREEN)
                    .requiresCorrectToolForDrops()
                    .explosionResistance(0)
                    .strength(0f)
                    .noOcclusion()
                    .isViewBlocking((blockState, blockGetter, blockPos) -> false),
            new BlockData(BlockToolRank.STONE, BlockToolType.PICKAXE)
    );

    public static final List<DeferredBlock<? extends ConcoctiHatchBlock>> HATCHES_LIST =
            HATCHES.values().stream().flatMap(map -> map.values().stream()).toList();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConcoctiHatchBlockEntity>> HATCH_BLOCK_ENTITY = registerHatchBlockEntity();

    public static DeferredBlock<? extends ConcoctiHatchBlock> getDeferredHatch(HatchPurpose purpose, HatchType type) {
        return HATCHES.get(type).get(purpose);
    }

    public static ConcoctiHatchBlock getHatch(HatchPurpose purpose, HatchType type) {
        return getDeferredHatch(purpose, type).get();
    }

    // REGISTERING METHODS

    public static DeferredBlock<Block> registerBlock(String name, Function<BlockBehaviour.Properties, ? extends Block> func, BlockBehaviour.Properties props, BlockData data) {
        DeferredBlock<Block> block = ConcoctiRegisters.BLOCKS.registerBlock(name, func, props);
        BLOCK_MAP.put(block, data);
        return block;
    }

    public static DeferredBlock<LiquidBlock> registerFluidBlock(String name, DeferredHolder<Fluid, FlowingFluid> flowingFluid, BlockBehaviour.Properties props) {
        return ConcoctiRegisters.BLOCKS.registerBlock(name, (p) -> new LiquidBlock(flowingFluid.get(), p), props);
    }

    public static Map<HatchType, Map<HatchPurpose, DeferredBlock<? extends ConcoctiHatchBlock>>> registerHatches(BlockBehaviour.Properties props, BlockData data) {
        Map<HatchType, Map<HatchPurpose, DeferredBlock<? extends ConcoctiHatchBlock>>> map = new EnumMap<>(HatchType.class);
        for (HatchType type : HatchType.values()) {
            Map<HatchPurpose, DeferredBlock<? extends ConcoctiHatchBlock>> innerMap = new EnumMap<>(HatchPurpose.class);
            for (HatchPurpose purpose : HatchPurpose.values()) {
                String id = "concocti_" + type.id + "_" + purpose.id + "_hatch";
                Function<BlockBehaviour.Properties, ConcoctiHatchBlock> func = p -> new ConcoctiHatchBlock(p, type, purpose);
                var block = ConcoctiRegisters.BLOCKS.registerBlock(id, func, props);
                innerMap.put(
                        purpose,
                        block
                );
                BLOCK_MAP.put(block, data);
            }
            map.put(type, innerMap);
        }
        return map;
    }

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<ConcoctiHatchBlockEntity>> registerHatchBlockEntity() {
        return BLOCK_ENTITY_TYPES.register(
                "concocti_hatch",
                () -> BlockEntityType.Builder.of(
                        ConcoctiHatchBlockEntity::new,
                        HATCHES_LIST.stream().map(DeferredHolder::get).toArray(ConcoctiHatchBlock[]::new)
                ).build(null)
        );
    }


    /**
     * Stores data related to how tags should describe this block.
     *
     * @param toolRank The rank of tool (stone/iron).
     * @param toolType The type of tool (pickaxe).
     */
    public record BlockData(BlockToolRank toolRank, BlockToolType toolType) {
    }

    public enum BlockToolRank {
        WOODEN("wooden"),
        STONE("stone"),
        IRON("iron"),
        DIAMOND("diamond"),
        NETHERITE("netherite");

        public final String rank;

        BlockToolRank(String rank) {
            this.rank = rank;
        }
    }

    public enum BlockToolType {
        PICKAXE("pickaxe");

        public final String type;

        BlockToolType(String type) {
            this.type = type;
        }
    }
}
