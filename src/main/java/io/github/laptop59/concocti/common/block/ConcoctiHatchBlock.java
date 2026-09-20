package io.github.laptop59.concocti.common.block;

import com.mojang.serialization.MapCodec;
import io.github.laptop59.concocti.common.block.entity.ConcoctiHatchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

/**
 * A class to represent the block of a Concocti Machine.
 */
public class ConcoctiHatchBlock extends BaseEntityBlock implements EntityBlock {
    protected HatchType type;
    protected HatchPurpose purpose;

    protected ConcoctiHatchBlock(Properties properties, @NotNull HatchType type, @NotNull HatchPurpose purpose) {
        super(properties);
        this.type = type;
        this.purpose = purpose;
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level,
                            @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public HatchType getType() {
        return type;
    }

    public HatchPurpose getPurpose() {
        return purpose;
    }

    public final MapCodec<ConcoctiHatchBlock> CODEC = simpleCodec(props -> new ConcoctiHatchBlock(props, type, purpose));

    @Override
    public @NotNull MapCodec<ConcoctiHatchBlock> codec() {
        return CODEC;
    }

    // Return a new instance of our block entity here.
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ConcoctiHatchBlockEntity(pos, state);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            openMenu(level, pos, player);
            return InteractionResult.CONSUME;
        }
    }

    public void openMenu(Level level, BlockPos pos, Player player) {
        ConcoctiHatchBlockEntity blockEntity = (ConcoctiHatchBlockEntity) level.getBlockEntity(pos);
        player.openMenu(blockEntity, blockEntity::writeCompleteSyncedDataToBuf);
    }

    @Override
    public ConcoctiHatchBlockEntity getMenuProvider(@NotNull BlockState state, Level level, @NotNull BlockPos pos) {
        return (ConcoctiHatchBlockEntity) level.getBlockEntity(pos);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(
                blockEntityType,
                ConcoctiBlocks.HATCH_BLOCK_ENTITY.get(),
                (l, pos, s, blockEntity) -> blockEntity.tick(l, pos, s)
        );
    }
}
