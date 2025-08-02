package io.github.laptop59.concocti.common.multiblock;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.ConcoctiHatchBlock;
import io.github.laptop59.concocti.common.block.entity.ConcoctiHatchBlockEntity;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.function.Supplier;

public final class MultiblockToughConcoctiBrickLikePredicate implements MultiblockBlockPredicate {
    private final Supplier<? extends ConcoctiHatchBlock>[] blocks;

    @SafeVarargs
    public MultiblockToughConcoctiBrickLikePredicate(Supplier<? extends ConcoctiHatchBlock>... blocks) {
        this.blocks = blocks;
    }

    @Override
    public MultiblockResult getResult(Level level, BlockPos absolutePos, Direction controllerDirection) {
        BlockState blockState = level.getBlockState(absolutePos);
        Block currentBlock = blockState.getBlock();
        boolean insteadWasAir = currentBlock == Blocks.AIR;
        if (currentBlock == ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS.get() ||
               Arrays.stream(blocks).anyMatch(block -> block.get() == currentBlock))
            return null;
        return new MultiblockResult(
                ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS.get().defaultBlockState(),
                insteadWasAir
        );
    }

    @Override
    public Data getExtraData(Level level, BlockPos absolutePos, Direction controllerDirection) {
        BlockEntity blockEntity = level.getBlockEntity(absolutePos);
        if (blockEntity instanceof ConcoctiHatchBlockEntity entity) return new Data(entity);
        return null;
    }

    public record Data(ConcoctiHatchBlockEntity entity) {}

    @Override
    public Item getIcon() {
        return ConcoctiItems.TOUGH_CONCOCTI_BRICKS.get();
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1
    ) {
        this.blocks = new Supplier[]{s1};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2
    ) {
        this.blocks = new Supplier[]{s1, s2};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3
    ) {
        this.blocks = new Supplier[]{s1, s2, s3};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18,
            Supplier<? extends ConcoctiHatchBlock> s19
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18,
            Supplier<? extends ConcoctiHatchBlock> s19,
            Supplier<? extends ConcoctiHatchBlock> s20
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18,
            Supplier<? extends ConcoctiHatchBlock> s19,
            Supplier<? extends ConcoctiHatchBlock> s20,
            Supplier<? extends ConcoctiHatchBlock> s21
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18,
            Supplier<? extends ConcoctiHatchBlock> s19,
            Supplier<? extends ConcoctiHatchBlock> s20,
            Supplier<? extends ConcoctiHatchBlock> s21,
            Supplier<? extends ConcoctiHatchBlock> s22
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21, s22};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18,
            Supplier<? extends ConcoctiHatchBlock> s19,
            Supplier<? extends ConcoctiHatchBlock> s20,
            Supplier<? extends ConcoctiHatchBlock> s21,
            Supplier<? extends ConcoctiHatchBlock> s22,
            Supplier<? extends ConcoctiHatchBlock> s23
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21, s22, s23};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18,
            Supplier<? extends ConcoctiHatchBlock> s19,
            Supplier<? extends ConcoctiHatchBlock> s20,
            Supplier<? extends ConcoctiHatchBlock> s21,
            Supplier<? extends ConcoctiHatchBlock> s22,
            Supplier<? extends ConcoctiHatchBlock> s23,
            Supplier<? extends ConcoctiHatchBlock> s24
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21, s22, s23, s24};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18,
            Supplier<? extends ConcoctiHatchBlock> s19,
            Supplier<? extends ConcoctiHatchBlock> s20,
            Supplier<? extends ConcoctiHatchBlock> s21,
            Supplier<? extends ConcoctiHatchBlock> s22,
            Supplier<? extends ConcoctiHatchBlock> s23,
            Supplier<? extends ConcoctiHatchBlock> s24,
            Supplier<? extends ConcoctiHatchBlock> s25
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21, s22, s23, s24, s25};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18,
            Supplier<? extends ConcoctiHatchBlock> s19,
            Supplier<? extends ConcoctiHatchBlock> s20,
            Supplier<? extends ConcoctiHatchBlock> s21,
            Supplier<? extends ConcoctiHatchBlock> s22,
            Supplier<? extends ConcoctiHatchBlock> s23,
            Supplier<? extends ConcoctiHatchBlock> s24,
            Supplier<? extends ConcoctiHatchBlock> s25,
            Supplier<? extends ConcoctiHatchBlock> s26
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21, s22, s23, s24, s25, s26};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18,
            Supplier<? extends ConcoctiHatchBlock> s19,
            Supplier<? extends ConcoctiHatchBlock> s20,
            Supplier<? extends ConcoctiHatchBlock> s21,
            Supplier<? extends ConcoctiHatchBlock> s22,
            Supplier<? extends ConcoctiHatchBlock> s23,
            Supplier<? extends ConcoctiHatchBlock> s24,
            Supplier<? extends ConcoctiHatchBlock> s25,
            Supplier<? extends ConcoctiHatchBlock> s26,
            Supplier<? extends ConcoctiHatchBlock> s27
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21, s22, s23, s24, s25, s26, s27};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18,
            Supplier<? extends ConcoctiHatchBlock> s19,
            Supplier<? extends ConcoctiHatchBlock> s20,
            Supplier<? extends ConcoctiHatchBlock> s21,
            Supplier<? extends ConcoctiHatchBlock> s22,
            Supplier<? extends ConcoctiHatchBlock> s23,
            Supplier<? extends ConcoctiHatchBlock> s24,
            Supplier<? extends ConcoctiHatchBlock> s25,
            Supplier<? extends ConcoctiHatchBlock> s26,
            Supplier<? extends ConcoctiHatchBlock> s27,
            Supplier<? extends ConcoctiHatchBlock> s28
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21, s22, s23, s24, s25, s26, s27, s28};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18,
            Supplier<? extends ConcoctiHatchBlock> s19,
            Supplier<? extends ConcoctiHatchBlock> s20,
            Supplier<? extends ConcoctiHatchBlock> s21,
            Supplier<? extends ConcoctiHatchBlock> s22,
            Supplier<? extends ConcoctiHatchBlock> s23,
            Supplier<? extends ConcoctiHatchBlock> s24,
            Supplier<? extends ConcoctiHatchBlock> s25,
            Supplier<? extends ConcoctiHatchBlock> s26,
            Supplier<? extends ConcoctiHatchBlock> s27,
            Supplier<? extends ConcoctiHatchBlock> s28,
            Supplier<? extends ConcoctiHatchBlock> s29
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21, s22, s23, s24, s25, s26, s27, s28, s29};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18,
            Supplier<? extends ConcoctiHatchBlock> s19,
            Supplier<? extends ConcoctiHatchBlock> s20,
            Supplier<? extends ConcoctiHatchBlock> s21,
            Supplier<? extends ConcoctiHatchBlock> s22,
            Supplier<? extends ConcoctiHatchBlock> s23,
            Supplier<? extends ConcoctiHatchBlock> s24,
            Supplier<? extends ConcoctiHatchBlock> s25,
            Supplier<? extends ConcoctiHatchBlock> s26,
            Supplier<? extends ConcoctiHatchBlock> s27,
            Supplier<? extends ConcoctiHatchBlock> s28,
            Supplier<? extends ConcoctiHatchBlock> s29,
            Supplier<? extends ConcoctiHatchBlock> s30
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21, s22, s23, s24, s25, s26, s27, s28, s29, s30};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18,
            Supplier<? extends ConcoctiHatchBlock> s19,
            Supplier<? extends ConcoctiHatchBlock> s20,
            Supplier<? extends ConcoctiHatchBlock> s21,
            Supplier<? extends ConcoctiHatchBlock> s22,
            Supplier<? extends ConcoctiHatchBlock> s23,
            Supplier<? extends ConcoctiHatchBlock> s24,
            Supplier<? extends ConcoctiHatchBlock> s25,
            Supplier<? extends ConcoctiHatchBlock> s26,
            Supplier<? extends ConcoctiHatchBlock> s27,
            Supplier<? extends ConcoctiHatchBlock> s28,
            Supplier<? extends ConcoctiHatchBlock> s29,
            Supplier<? extends ConcoctiHatchBlock> s30,
            Supplier<? extends ConcoctiHatchBlock> s31
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21, s22, s23, s24, s25, s26, s27, s28, s29, s30, s31};
    }

    @SuppressWarnings("unchecked")
    public MultiblockToughConcoctiBrickLikePredicate(
            Supplier<? extends ConcoctiHatchBlock> s1,
            Supplier<? extends ConcoctiHatchBlock> s2,
            Supplier<? extends ConcoctiHatchBlock> s3,
            Supplier<? extends ConcoctiHatchBlock> s4,
            Supplier<? extends ConcoctiHatchBlock> s5,
            Supplier<? extends ConcoctiHatchBlock> s6,
            Supplier<? extends ConcoctiHatchBlock> s7,
            Supplier<? extends ConcoctiHatchBlock> s8,
            Supplier<? extends ConcoctiHatchBlock> s9,
            Supplier<? extends ConcoctiHatchBlock> s10,
            Supplier<? extends ConcoctiHatchBlock> s11,
            Supplier<? extends ConcoctiHatchBlock> s12,
            Supplier<? extends ConcoctiHatchBlock> s13,
            Supplier<? extends ConcoctiHatchBlock> s14,
            Supplier<? extends ConcoctiHatchBlock> s15,
            Supplier<? extends ConcoctiHatchBlock> s16,
            Supplier<? extends ConcoctiHatchBlock> s17,
            Supplier<? extends ConcoctiHatchBlock> s18,
            Supplier<? extends ConcoctiHatchBlock> s19,
            Supplier<? extends ConcoctiHatchBlock> s20,
            Supplier<? extends ConcoctiHatchBlock> s21,
            Supplier<? extends ConcoctiHatchBlock> s22,
            Supplier<? extends ConcoctiHatchBlock> s23,
            Supplier<? extends ConcoctiHatchBlock> s24,
            Supplier<? extends ConcoctiHatchBlock> s25,
            Supplier<? extends ConcoctiHatchBlock> s26,
            Supplier<? extends ConcoctiHatchBlock> s27,
            Supplier<? extends ConcoctiHatchBlock> s28,
            Supplier<? extends ConcoctiHatchBlock> s29,
            Supplier<? extends ConcoctiHatchBlock> s30,
            Supplier<? extends ConcoctiHatchBlock> s31,
            Supplier<? extends ConcoctiHatchBlock> s32
    ) {
        this.blocks = new Supplier[]{s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21, s22, s23, s24, s25, s26, s27, s28, s29, s30, s31, s32};
    }
}
