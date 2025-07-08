package io.github.laptop59.concocti.common.block;

import com.mojang.serialization.MapCodec;
import io.github.laptop59.concocti.common.block.entity.ConcoctiElectronCollectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ConcoctiElectronCollectorBlock extends AbstractConcoctiMachineBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public static final MapCodec<ConcoctiElectronCollectorBlock> CODEC = simpleCodec(ConcoctiElectronCollectorBlock::new);

    protected ConcoctiElectronCollectorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.FALSE));
    }

    @Override
    public @NotNull MapCodec<ConcoctiElectronCollectorBlock> codec() {
        return CODEC;
    }

    // Return a new instance of our block entity here.
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ConcoctiElectronCollectorBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
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

    @Override
    protected @NotNull ItemInteractionResult useItemOnMachine(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos,
            @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public MenuProvider getMenuProvider(@NotNull BlockState state, Level level, @NotNull BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof ConcoctiElectronCollectorBlockEntity entity ? entity : null;
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(
            Level level, BlockEntityType<T> serverType, BlockEntityType<? extends ConcoctiElectronCollectorBlockEntity> clientType
    ) {
        return level.isClientSide ? null : createTickerHelper(serverType, clientType, ConcoctiElectronCollectorBlockEntity::serverTick);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return createTicker(level, blockEntityType, ConcoctiBlocks.CONCOCTI_ELECTRON_COLLECTOR_BLOCK_ENTITY.get());
    }
}
