package io.github.laptop59.concocti.common.item;

import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public final class ConcoctiItemsInfo {
    public static final List<DeferredItem<? extends Item>> ITEMS_WITH_INGREDIENT_INFO = List.of(
            ConcoctiItems.CONCOCTI_SEEDS,
            ConcoctiItems.INFINITY_CONCOCTI_SEEDS,
            ConcoctiItems.DIRTY_CONCOCTI_NUGGET,
            ConcoctiItems.DIRTY_CONCOCTI_INGOT,
            ConcoctiItems.CONDUCTIVIUM_LIGHTNING_ROD,
            ConcoctiMachines.ELECTRON_COLLECTOR.ITEM,
            ConcoctiMachines.SOLAR_COLLECTOR.ITEM
    );
}
