package io.github.laptop59.concocti.common.block;

import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.menu.ConcoctiFrameSlot;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity.FRAME_SLOT;
import static io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity.UPGRADE_SLOT;

/**
 * A class to represent the block of a Concocti Machine.
 */
public abstract class AbstractConcoctiMachineBlock extends BaseEntityBlock implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    protected AbstractConcoctiMachineBlock(Properties properties) {
        super(properties);
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
        // Do this machine's specific logic.
        return useItemOnMachine(stack, state, level, pos, player, hand, hitResult);
    }

    /** Called when a machine should do its own specific logic for when an item is right-clicked on it. */
    protected abstract ItemInteractionResult useItemOnMachine(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                                              @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult);
}
