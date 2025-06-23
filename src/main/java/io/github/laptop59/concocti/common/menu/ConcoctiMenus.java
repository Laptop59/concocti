package io.github.laptop59.concocti.common.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);

    public static final Supplier<MenuType<ConcoctiMelterMenu>> CONCOCTI_MELTER_MENU =
            MENUS.register("concocti_melter_menu", () -> new MenuType<>(ConcoctiMelterMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<ConcoctiSolidifierMenu>> CONCOCTI_SOLIDIFIER_MENU =
            MENUS.register("concocti_solidifier_menu", () -> new MenuType<>(ConcoctiSolidifierMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
