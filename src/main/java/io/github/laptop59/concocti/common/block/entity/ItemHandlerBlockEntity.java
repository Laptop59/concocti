package io.github.laptop59.concocti.common.block.entity;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public interface ItemHandlerBlockEntity {
    @Nullable
    IItemHandler getSidedItemHandler(Direction direction);
}
