package io.github.laptop59.concocti.common.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public record MultiblockBlockTagPredicate(TagKey<Block> tagKey) implements MultiblockBlockPredicate {
    @Override
    public MultiblockResult getResult(Level level, BlockPos absolutePos, Direction controllerDirection) {
        Block currentBlock = level.getBlockState(absolutePos).getBlock();
        Optional<HolderSet.Named<Block>> blocksInTagOpt = BuiltInRegistries.BLOCK.getTag(tagKey);
        boolean insteadWasAir = currentBlock == Blocks.AIR;
        if (blocksInTagOpt.isPresent()) {
            HolderSet.Named<Block> blocksInTag = blocksInTagOpt.get();
            for (Holder<Block> holder : blocksInTag) {
                if (holder.value() == currentBlock) return null;
            }
            return new MultiblockResult(
                    blocksInTag.size() == 0 ? Blocks.BARRIER.defaultBlockState() : blocksInTag.get(0).value().defaultBlockState(),
                    insteadWasAir
            );
        }
        return new MultiblockResult(Blocks.BARRIER.defaultBlockState(), insteadWasAir);
    }
}
