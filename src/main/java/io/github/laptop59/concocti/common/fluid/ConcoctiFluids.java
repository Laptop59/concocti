package io.github.laptop59.concocti.common.fluid;

import io.github.laptop59.concocti.common.ConcoctiRegisters;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ConcoctiFluids {

    //

    public static final DeferredHolder<Fluid, FlowingFluid> MOLTEN_CONCOCTI = ConcoctiRegisters.FLUIDS.register("molten_concocti", (loc) -> new MoltenConcoctiFluid.Source());
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_MOLTEN_CONCOCTI = ConcoctiRegisters.FLUIDS.register("flowing_molten_concocti", (loc) -> new MoltenConcoctiFluid.Flowing());

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_CONCOCTI_FLUID_TYPE = ConcoctiRegisters.FLUID_TYPES.register("molten_concocti",
            (loc) -> new FluidType(
                    FluidType.Properties.create()
                            .temperature(300)
                            .lightLevel(13)
                            .viscosity(1500)
                            .density(10)
                            .canConvertToSource(false)
                            .canDrown(false)
                            .canSwim(true)
            )
    );

    public static final DeferredBlock<LiquidBlock> MOLTEN_CONCOCTI_BLOCK = ConcoctiBlocks.registerFluidBlock("molten_concocti", MOLTEN_CONCOCTI,
            BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.PURPLE)
    );

    //

    public static final DeferredHolder<Fluid, FlowingFluid> MOLTEN_TOUGH_CONCOCTI = ConcoctiRegisters.FLUIDS.register("molten_tough_concocti", (loc) -> new MoltenToughConcoctiFluid.Source());
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_MOLTEN_TOUGH_CONCOCTI = ConcoctiRegisters.FLUIDS.register("flowing_molten_tough_concocti", (loc) -> new MoltenToughConcoctiFluid.Flowing());

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_TOUGH_CONCOCTI_FLUID_TYPE = ConcoctiRegisters.FLUID_TYPES.register("molten_tough_concocti",
            (loc) -> new FluidType(
                    FluidType.Properties.create()
                            .temperature(500)
                            .lightLevel(12)
                            .viscosity(1250)
                            .density(15)
                            .canConvertToSource(false)
                            .canDrown(false)
                            .canSwim(true)
            )
    );

    public static final DeferredBlock<LiquidBlock> MOLTEN_TOUGH_CONCOCTI_BLOCK = ConcoctiBlocks.registerFluidBlock("molten_tough_concocti", MOLTEN_TOUGH_CONCOCTI,
            BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.PURPLE)
    );

    //

    public static final DeferredHolder<Fluid, FlowingFluid> MOLTEN_CONCOCTIZED_DIRT = ConcoctiRegisters.FLUIDS.register("molten_concoctized_dirt", (loc) -> new MoltenConcoctizedDirtFluid.Source());
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_MOLTEN_CONCOCTIZED_DIRT = ConcoctiRegisters.FLUIDS.register("flowing_molten_concoctized_dirt", (loc) -> new MoltenConcoctizedDirtFluid.Flowing());

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_CONCOCTIZED_DIRT_FLUID_TYPE = ConcoctiRegisters.FLUID_TYPES.register("molten_concoctized_dirt",
            (loc) -> new FluidType(
                    FluidType.Properties.create()
                            .temperature(200)
                            .lightLevel(10)
                            .viscosity(1000)
                            .density(5)
                            .canConvertToSource(false)
                            .canDrown(false)
                            .canSwim(true)
            )
    );

    public static final DeferredBlock<LiquidBlock> MOLTEN_CONCOCTIZED_DIRT_BLOCK = ConcoctiBlocks.registerFluidBlock("molten_concoctized_dirt", MOLTEN_CONCOCTIZED_DIRT,
            BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.PURPLE)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> MOLTEN_COPPER = ConcoctiRegisters.FLUIDS.register("molten_copper", (loc) -> new MoltenCopperFluid.Source());
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_MOLTEN_COPPER = ConcoctiRegisters.FLUIDS.register("flowing_molten_copper", (loc) -> new MoltenCopperFluid.Flowing());

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_COPPER_FLUID_TYPE = ConcoctiRegisters.FLUID_TYPES.register("molten_copper",
            (loc) -> new FluidType(
                    FluidType.Properties.create()
                            .temperature(1084)
                            .lightLevel(12)
                            .viscosity(1000)
                            .density(3)
                            .canConvertToSource(false)
                            .canDrown(false)
                            .canSwim(true)
            )
    );

    public static final DeferredBlock<LiquidBlock> MOLTEN_COPPER_BLOCK = ConcoctiBlocks.registerFluidBlock("molten_copper", MOLTEN_COPPER,
            BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.RED)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> MOLTEN_CONDUCTIVIUM = ConcoctiRegisters.FLUIDS.register("molten_conductivium", (loc) -> new MoltenConductiviumFluid.Source());
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_MOLTEN_CONDUCTIVIUM = ConcoctiRegisters.FLUIDS.register("flowing_molten_conductivium", (loc) -> new MoltenConductiviumFluid.Flowing());

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_CONDUCTIVIUM_FLUID_TYPE = ConcoctiRegisters.FLUID_TYPES.register("molten_conductivium",
            (loc) -> new FluidType(
                    FluidType.Properties.create()
                            .temperature(2000)
                            .lightLevel(15)
                            .viscosity(1000)
                            .density(3)
                            .canConvertToSource(false)
                            .canDrown(false)
                            .canSwim(true)
            )
    );

    public static final DeferredHolder<Fluid, FlowingFluid> MOLTEN_LIGHTNING = ConcoctiRegisters.FLUIDS.register("molten_lightning", (loc) -> new MoltenLightningFluid.Source());
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_MOLTEN_LIGHTNING = ConcoctiRegisters.FLUIDS.register("flowing_molten_lightning", (loc) -> new MoltenLightningFluid.Flowing());

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_LIGHTNING_FLUID_TYPE = ConcoctiRegisters.FLUID_TYPES.register("molten_lightning",
            (loc) -> new FluidType(
                    FluidType.Properties.create()
                            .temperature(1000000000)
                            .lightLevel(15)
                            .viscosity(10)
                            .density(33)
                            .canConvertToSource(false)
                            .canDrown(false)
                            .canSwim(true)
            )
    );

    public static final DeferredBlock<LiquidBlock> MOLTEN_LIGHTNING_BLOCK = ConcoctiBlocks.registerFluidBlock("molten_lightning", MOLTEN_LIGHTNING,
            BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.WHITE)
    );

    public static final DeferredBlock<LiquidBlock> MOLTEN_CONDUCTIVIUM_BLOCK = ConcoctiBlocks.registerFluidBlock("molten_conductivium", MOLTEN_CONDUCTIVIUM,
            BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.CYAN)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> CRYSTALIUM_SOLUTION = ConcoctiRegisters.FLUIDS.register("crystalium_solution", (loc) -> new CrystaliumSolutionFluid.Source());
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_CRYSTALIUM_SOLUTION = ConcoctiRegisters.FLUIDS.register("flowing_crystalium_solution", (loc) -> new CrystaliumSolutionFluid.Flowing());

    public static final DeferredHolder<FluidType, FluidType> CRYSTALIUM_SOLUTION_FLUID_TYPE = ConcoctiRegisters.FLUID_TYPES.register("crystalium_solution",
            (loc) -> new FluidType(
                    FluidType.Properties.create()
                            .temperature(50)
                            .lightLevel(1)
                            .viscosity(40)
                            .density(2)
                            .canConvertToSource(false)
                            .canDrown(false)
                            .canSwim(true)
            )
    );

    public static final DeferredBlock<LiquidBlock> CRYSTALIUM_SOLUTION_BLOCK = ConcoctiBlocks.registerFluidBlock("crystalium_solution", CRYSTALIUM_SOLUTION,
            BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(DyeColor.PINK)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> SUPERSATURATED_CRYSTALIUM_SOLUTION = ConcoctiRegisters.FLUIDS.register("supersaturated_crystalium_solution", (loc) -> new SupersaturatedCrystaliumSolutionFluid.Source());
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_SUPERSATURATED_CRYSTALIUM_SOLUTION = ConcoctiRegisters.FLUIDS.register("flowing_supersaturated_crystalium_solution", (loc) -> new SupersaturatedCrystaliumSolutionFluid.Flowing());

    public static final DeferredHolder<FluidType, FluidType> SUPERSATURATED_CRYSTALIUM_SOLUTION_FLUID_TYPE = ConcoctiRegisters.FLUID_TYPES.register("supersaturated_crystalium_solution",
            (loc) -> new FluidType(
                    FluidType.Properties.create()
                            .temperature(50)
                            .lightLevel(2)
                            .viscosity(30)
                            .density(5)
                            .canConvertToSource(false)
                            .canDrown(false)
                            .canSwim(true)
            )
    );

    public static final DeferredBlock<LiquidBlock> SUPERSATURATED_CRYSTALIUM_SOLUTION_BLOCK = ConcoctiBlocks.registerFluidBlock("supersaturated_crystalium_solution", SUPERSATURATED_CRYSTALIUM_SOLUTION,
            BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(DyeColor.PINK)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> MOLTEN_LATTICIUM = ConcoctiRegisters.FLUIDS.register("molten_latticium", (loc) -> new MoltenLatticiumFluid.Source());
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_MOLTEN_LATTICIUM = ConcoctiRegisters.FLUIDS.register("flowing_molten_latticium", (loc) -> new MoltenLatticiumFluid.Flowing());

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_LATTICIUM_FLUID_TYPE = ConcoctiRegisters.FLUID_TYPES.register("molten_latticium",
            (loc) -> new FluidType(
                    FluidType.Properties.create()
                            .temperature(2500)
                            .lightLevel(0)
                            .viscosity(10)
                            .density(10)
                            .canConvertToSource(false)
                            .canDrown(false)
                            .canSwim(true)
            )
    );

    public static final DeferredBlock<LiquidBlock> MOLTEN_LATTICIUM_BLOCK = ConcoctiBlocks.registerFluidBlock("molten_latticium", MOLTEN_LATTICIUM,
            BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.BLACK)
    );
}
