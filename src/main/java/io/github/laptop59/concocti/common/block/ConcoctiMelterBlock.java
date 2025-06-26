package io.github.laptop59.concocti.common.block;

import com.mojang.serialization.MapCodec;
import io.github.laptop59.concocti.common.ConcoctiSounds;
import io.github.laptop59.concocti.common.block.entity.ConcoctiMelterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

import javax.annotation.Nullable;

public class ConcoctiMelterBlock extends AbstractConcoctiMachineBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public static final MapCodec<ConcoctiMelterBlock> CODEC = simpleCodec(ConcoctiMelterBlock::new);

    protected ConcoctiMelterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.FALSE));
    }

    @Override
    public @NotNull MapCodec<ConcoctiMelterBlock> codec() {
        return CODEC;
    }

    // Return a new instance of our block entity here.
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ConcoctiMelterBlockEntity(pos, state);
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
    public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (state.getValue(LIT)) {
            double x = pos.getX() + 0.5;
            double y = pos.getY();
            double z = pos.getZ() + 0.5;
            if (random.nextDouble() < 0.1) {
                level.playLocalSound(x, y, z, ConcoctiSounds.CONCOCTI_MELTER_FIRE_CRACKLE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
            Direction direction = state.getValue(FACING);
            Direction.Axis axis = direction.getAxis();
            double d = random.nextDouble() * 0.6 - 0.3;
            double dx = axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52 : d;
            double dy = random.nextDouble() * 9.0 / 16.0;
            double dz = axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52 : d;
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

    @Override
    protected @NotNull ItemInteractionResult useItemOnMachine(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos,
            @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ConcoctiMelterBlockEntity e) {
            if (stack.is(Items.BUCKET)) {
                FluidStack fluidStack = e.fluids.getFluidInTank(e.fluids.getFluidInTank(0).getAmount() < 1000 ? 1 : 0)
                        .copy();
                fluidStack.setAmount(1000);
                Item item = fluidStack.getFluid().getBucket();
                FluidStack fluid = e.fluids.drain(fluidStack, IFluidHandler.FluidAction.SIMULATE);
                if (!fluid.isEmpty() && fluid.getAmount() == 1000) {
                    e.fluids.drain(fluid, IFluidHandler.FluidAction.EXECUTE);
                    stack.shrink(1);
                    player.addItem(new ItemStack(item));
                    player.playSound(SoundEvents.BUCKET_EMPTY);
                    return ItemInteractionResult.SUCCESS;
                }
            }
            IFluidHandlerItem c = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (c != null) {
                FluidStack drained = e.fluids.drain(ConcoctiMelterBlockEntity.TANK_CAPACITY, IFluidHandler.FluidAction.SIMULATE);
                if (!drained.isEmpty()) {
                    int filled = c.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                    if (filled > 0) {
                        c.fill(e.fluids.drain(drained, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public MenuProvider getMenuProvider(@NotNull BlockState state, Level level, @NotNull BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof ConcoctiMelterBlockEntity entity ? entity : null;
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(
            Level level, BlockEntityType<T> serverType, BlockEntityType<? extends ConcoctiMelterBlockEntity> clientType
    ) {
        return level.isClientSide ? null : createTickerHelper(serverType, clientType, ConcoctiMelterBlockEntity::serverTick);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return createTicker(level, blockEntityType, ConcoctiBlocks.CONCOCTI_MELTER_BLOCK_ENTITY.get());
    }
}
