package io.github.laptop59.concocti.common.fluid;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConcoctiFluids {
    public static final List<ConcoctiFluidParent> FLUIDS = new ArrayList<>();

    public static final ConcoctiFluidParent MOLTEN_CONCOCTI = register(
        "molten_concocti",
        FluidType.Properties.create()
            .temperature(300)
            .lightLevel(13)
            .viscosity(1500)
            .density(10)
            .canConvertToSource(false)
            .canDrown(false)
            .canSwim(true),
        BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.MAGENTA),
        5,
        true,
        true
    );

    public static final ConcoctiFluidParent MOLTEN_TOUGH_CONCOCTI = register(
        "molten_tough_concocti",
        FluidType.Properties.create()
            .temperature(500)
            .lightLevel(12)
            .viscosity(1250)
            .density(15)
            .canConvertToSource(false)
            .canDrown(false)
            .canSwim(true),
        BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.PURPLE),
        5,
        true,
        true
    );

    public static final ConcoctiFluidParent MOLTEN_CONCOCTIZED_DIRT = register(
        "molten_concoctized_dirt",
        FluidType.Properties.create()
                .temperature(200)
                .lightLevel(10)
                .viscosity(1000)
                .density(5)
                .canConvertToSource(false)
                .canDrown(false)
                .canSwim(true),
        BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.GRAY),
        5,
        true,
        true
    );

    public static final ConcoctiFluidParent MOLTEN_COPPER = register(
        "molten_copper",
        FluidType.Properties.create()
                .temperature(1084)
                .lightLevel(12)
                .viscosity(1000)
                .density(3)
                .canConvertToSource(false)
                .canDrown(false)
                .canSwim(true),
        BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.ORANGE),
        5,
        true,
        true
    );

    public static final ConcoctiFluidParent MOLTEN_CONDUCTIVIUM = register(
        "molten_conductivium",
        FluidType.Properties.create()
                .temperature(2000)
                .lightLevel(15)
                .viscosity(1000)
                .density(3)
                .canConvertToSource(false)
                .canDrown(false)
                .canSwim(true),
        BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.CYAN),
        5,
        true,
        true
    );

    public static final ConcoctiFluidParent MOLTEN_LIGHTNING = register(
        "molten_lightning",
        FluidType.Properties.create()
                .temperature(1000000000)
                .lightLevel(15)
                .viscosity(10)
                .density(33)
                .canConvertToSource(false)
                .canDrown(false)
                .canSwim(true),
        null,
        5,
        true,
        false
    );

    public static final ConcoctiFluidParent CRYSTALIUM_SOLUTION = register(
        "crystalium_solution",
        FluidType.Properties.create()
                .temperature(50)
                .lightLevel(1)
                .viscosity(40)
                .density(2)
                .canConvertToSource(false)
                .canDrown(false)
                .canSwim(true),
        BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(DyeColor.PINK),
        5,
        false,
        true
    );

    public static final ConcoctiFluidParent SUPERSATURATED_CRYSTALIUM_SOLUTION = register(
        "supersaturated_crystalium_solution",
        FluidType.Properties.create()
                .temperature(50)
                .lightLevel(2)
                .viscosity(30)
                .density(5)
                .canConvertToSource(false)
                .canDrown(false)
                .canSwim(true),
        BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(DyeColor.PINK),
        5,
        false,
        true
    );

    public static final ConcoctiFluidParent MOLTEN_LATTICIUM = register(
        "molten_latticium",
        FluidType.Properties.create()
                .temperature(2500)
                .lightLevel(0)
                .viscosity(10)
                .density(10)
                .canConvertToSource(false)
                .canDrown(false)
                .canSwim(true),
        BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.BLACK),
        5,
        true,
        true
    );

    private static ConcoctiFluidParent register(String id, FluidType.Properties fluidProps, BlockBehaviour.Properties blockProps, int tickRate, boolean isMolten, boolean hasBucket) {
        ConcoctiFluidParent parent = new ConcoctiFluidParent(id, tickRate, isMolten, fluidProps, blockProps, hasBucket);
        FLUIDS.add(parent);
        return parent;
    }

    public static void forEach(Consumer<ConcoctiFluidParent> consumer) {
        FLUIDS.forEach(consumer);
    }
}
