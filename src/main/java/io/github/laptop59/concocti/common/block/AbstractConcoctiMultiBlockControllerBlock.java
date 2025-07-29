package io.github.laptop59.concocti.common.block;

import com.mojang.serialization.MapCodec;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMultiblockBlockEntity;
import io.github.laptop59.concocti.common.machine.ConcoctiMachine;
import io.github.laptop59.concocti.common.menu.ConcoctiFrameSlot;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity.FRAME_SLOT;
import static io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity.UPGRADE_SLOT;

/**
 * A class to represent the block of a Concocti Machine.
 */
public abstract class AbstractConcoctiMultiBlockControllerBlock<B extends AbstractConcoctiMultiBlockControllerBlock<B>> extends AbstractConcoctiMachineBlock<B> implements EntityBlock {
    protected AbstractConcoctiMultiBlockControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.FALSE));
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level,
                            @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private boolean attemptItemClick(int slot, ItemStack playerStack, Player player, Level level,
                                     BlockPos pos, Predicate<ItemStack> valid, boolean singleItem) {
        if (valid.test(playerStack)) {
            AbstractConcoctiMachineBlockEntity<?, ?, ?, ?, ?> entity =
                    (AbstractConcoctiMachineBlockEntity<?, ?, ?, ?, ?>) level.getBlockEntity(pos);
            // Check for an item match.
            assert entity != null;
            ItemStack upgradeStack = entity.getItem(slot);
            if (upgradeStack.isEmpty()) {
                // Direct replacement.
                if (singleItem) {
                    entity.setItem(slot, playerStack.copyWithCount(1));
                    playerStack.shrink(1);
                } else
                    entity.setItem(slot, playerStack.copyAndClear());
            } else {
                // Swap items.
                if (!singleItem) {
                    entity.setItem(slot, playerStack.copyAndClear());
                    player.setItemSlot(EquipmentSlot.MAINHAND, upgradeStack.copyAndClear());
                } else {
                    return false;
                }
            } // Do nothing
            return true;
        }
        return false;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        // We should do some logic for every machine first.
        // Concocti upgrade.
        if (attemptItemClick(UPGRADE_SLOT, stack, player, level, pos, ConcoctiUpgradeSlot::mayPlaceItem, false))
            return ItemInteractionResult.SUCCESS;
        if (attemptItemClick(FRAME_SLOT, stack, player, level, pos, ConcoctiFrameSlot::mayPlaceItem, true))
            return ItemInteractionResult.SUCCESS;
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    abstract protected Function<Properties, B> getBlockConstructor();

    abstract protected @Nullable SoundEvent getCracklingSoundEvent();

    public final MapCodec<B> CODEC = simpleCodec(getBlockConstructor());

    @Override
    public @NotNull MapCodec<B> codec() {
        return CODEC;
    }

    // Return a new instance of our block entity here.
    @Override
    public abstract BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (state.getValue(LIT)) {
            double x = pos.getX() + 0.5;
            double y = pos.getY();
            double z = pos.getZ() + 0.5;
            if (random.nextDouble() < 0.1 && getCracklingSoundEvent() != null) {
                level.playLocalSound(x, y, z, getCracklingSoundEvent(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
            Direction direction = state.getValue(FACING);
            Direction.Axis axis = direction.getAxis();
            double d = random.nextDouble() * 0.6 - 0.3;
            double dx = axis == Direction.Axis.X ? (double) direction.getStepX() * 0.52 : d;
            double dy = random.nextDouble() * 9.0 / 16.0;
            double dz = axis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.52 : d;
            level.addParticle(ParticleTypes.SMOKE, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            MenuProvider provider = this.getMenuProvider(state, level, pos);
            if (provider != null) {
                player.openMenu(provider);
            }

            return InteractionResult.CONSUME;
        }
    }

    public abstract BlockEntityType<? extends BlockEntity> getBlockEntityType();

    public AbstractConcoctiMultiblockBlockEntity<?, ?> getBlockEntity(@NotNull Level level, @NotNull BlockPos blockPos) {
        return (AbstractConcoctiMultiblockBlockEntity<?, ?>) level.getBlockEntity(blockPos);
    }

    @Override
    public MenuProvider getMenuProvider(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        return getBlockEntity(level, pos);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, getBlockEntityType(), (level1, pos, state1, blockEntity) -> {
            getBlockEntity(level, pos).tick(level1, pos, state1);
        });
    }
}
