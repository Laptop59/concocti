package io.github.laptop59.concocti.common.block;

import io.github.laptop59.concocti.common.block.entity.ConcoctiMelterBlockEntity;
import io.github.laptop59.concocti.common.block.entity.ConcoctiSolidifierBlockEntity;
import io.github.laptop59.concocti.common.block.frame.FrameBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiBlocks {
    public static final Map<DeferredBlock<? extends Block>, BlockData> BLOCK_MAP = new HashMap<>();

    // Create 2 Deferred Registers to hold Blocks and Block Entities which will all be registered under the "concocti" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

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

    public static final DeferredBlock<Block> CONCOCTI_MELTER = registerBlock("concocti_melter", ConcoctiMelterBlock::new,
            BlockBehaviour.Properties.of().mapColor(DyeColor.MAGENTA).requiresCorrectToolForDrops().explosionResistance(4.5f).strength(4f)
                    .lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 13 : 0),
            new BlockData(BlockToolRank.STONE, BlockToolType.PICKAXE));

    // FRAME BLOCKS

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

    public static final Supplier<BlockEntityType<ConcoctiMelterBlockEntity>> CONCOCTI_MELTER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "concocti_melter",
            // The block entity type, created using a builder.
            () -> BlockEntityType.Builder.of(
                            // The supplier to use for constructing the block entity instances.
                            ConcoctiMelterBlockEntity::new,
                            // A vararg of blocks that can have this block entity.
                            ConcoctiBlocks.CONCOCTI_MELTER.get()
                    )
                    // Build using null; vanilla does some datafixer shenanigans with the parameter that we don't need.
                    .build(null)
    );

    public static final DeferredBlock<Block> CONCOCTI_SOLIDIFIER = registerBlock("concocti_solidifier", ConcoctiSolidifierBlock::new,
            BlockBehaviour.Properties.of().mapColor(DyeColor.MAGENTA).requiresCorrectToolForDrops().explosionResistance(4.5f).strength(4f)
                    .lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 13 : 0),
            new BlockData(BlockToolRank.STONE, BlockToolType.PICKAXE));
    public static final Supplier<BlockEntityType<ConcoctiSolidifierBlockEntity>> CONCOCTI_SOLIDIFIER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "concocti_solidifier", () -> BlockEntityType.Builder.of(ConcoctiSolidifierBlockEntity::new, ConcoctiBlocks.CONCOCTI_SOLIDIFIER.get())
                    .build(null)
    );

    // REGISTERING METHODS

    public static DeferredBlock<Block> registerBlock(String name, Function<BlockBehaviour.Properties, ? extends Block> func, BlockBehaviour.Properties props, BlockData data) {
        DeferredBlock<Block> block = BLOCKS.registerBlock(name, func, props);
        BLOCK_MAP.put(block, data);
        return block;
    }

    public static DeferredBlock<LiquidBlock> registerFluidBlock(String name, DeferredHolder<Fluid, FlowingFluid> flowingFluid, BlockBehaviour.Properties props) {
        return BLOCKS.registerBlock(name, (p) -> new LiquidBlock(flowingFluid.get(), p), props);
    }

    /**
     * Stores data related to how tags should describe this block.
     * @param toolRank The rank of tool (stone/iron).
     * @param toolType The type of tool (pickaxe).
     */
    public record BlockData(BlockToolRank toolRank, BlockToolType toolType) {}

    public enum BlockToolRank {
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
