package io.github.laptop59.concocti.common.block;

import com.mojang.serialization.MapCodec;
import io.github.laptop59.concocti.common.block.entity.ConcoctiSolidifierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

public class ConcoctiSolidifierBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public static final MapCodec<ConcoctiSolidifierBlock> CODEC = simpleCodec(ConcoctiSolidifierBlock::new);

    protected ConcoctiSolidifierBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.FALSE));
    }

    @Override
    public @NotNull MapCodec<ConcoctiSolidifierBlock> codec() {
        return CODEC;
    }

    // Return a new instance of our block entity here.
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ConcoctiSolidifierBlockEntity(pos, state);
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
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ConcoctiSolidifierBlockEntity e) {
            if (stack.is(Items.BUCKET)) {
                // Drain if possible.
                FluidStack fluid = e.tank.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                if (!fluid.isEmpty() && fluid.getAmount() == 1000) {
                    e.tank.drain(fluid, IFluidHandler.FluidAction.EXECUTE);
                    stack.shrink(1);
                    player.addItem(new ItemStack(fluid.getFluid().getBucket()));
                    player.playSound(SoundEvents.BUCKET_EMPTY);
                    return ItemInteractionResult.SUCCESS;
                }
            }
            if (stack.getItem() instanceof BucketItem bucketItem) {
                // Fill if possible.
                FluidStack bucketFluid = new FluidStack(bucketItem.content, 1000);
                int filled = e.tank.fill(bucketFluid, IFluidHandler.FluidAction.SIMULATE);
                if (filled == 1000) {
                    e.tank.fill(bucketFluid, IFluidHandler.FluidAction.EXECUTE);
                    stack.shrink(1);
                    player.addItem(new ItemStack(Items.BUCKET));
                    player.playSound(SoundEvents.BUCKET_FILL);
                    return ItemInteractionResult.SUCCESS;
                }
            }
            IFluidHandlerItem c = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (c != null) {
                FluidStack fluid = e.tank.getFluid();
                FluidStack drained = e.tank.drain(fluid, IFluidHandler.FluidAction.SIMULATE);
                if (!drained.isEmpty()) {
                    int filled = c.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                    if (filled > 0) {
                        c.fill(e.tank.drain(fluid, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
