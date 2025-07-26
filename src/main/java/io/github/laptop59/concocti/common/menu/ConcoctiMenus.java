package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.common.ConcoctiRegisters;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ConcoctiMenus {
    public static DeferredHolder<MenuType<?>, MenuType<ConcoctiItemHatchMenu>> CONCOCTI_ITEM_HATCH_MENU = ConcoctiRegisters.MENUS.register(
            "concocti_item_hatch",
            () -> new MenuType<>(ConcoctiItemHatchMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static DeferredHolder<MenuType<?>, MenuType<ConcoctiFluidHatchMenu>> CONCOCTI_FLUID_HATCH_MENU = ConcoctiRegisters.MENUS.register(
            "concocti_fluid_hatch",
            () -> new MenuType<>(ConcoctiFluidHatchMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static DeferredHolder<MenuType<?>, MenuType<ConcoctiEnergyHatchMenu>> CONCOCTI_ENERGY_HATCH_MENU = ConcoctiRegisters.MENUS.register(
            "concocti_energy_hatch",
            () -> new MenuType<>(ConcoctiEnergyHatchMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );
}
