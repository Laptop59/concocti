package io.github.laptop59.concocti.common.fluid;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MODID);

    public static final DeferredHolder<Fluid, FlowingFluid> MOLTEN_CONCOCTI = FLUIDS.register("molten_concocti", (loc) -> new MoltenConcoctiFluid.Source());
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_MOLTEN_CONCOCTI = FLUIDS.register("flowing_molten_concocti", (loc) -> new MoltenConcoctiFluid.Flowing());

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_CONCOCTI_FLUID_TYPE = FLUID_TYPES.register("molten_concocti",
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

    public static final DeferredBlock<LiquidBlock> MOLTEN_CONCOCTI_BLOCK = ConcoctiBlocks.registerFluidBlock("molten_concoctized_dirt", MOLTEN_CONCOCTI,
            BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.PURPLE)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> MOLTEN_CONCOCTIZED_DIRT = FLUIDS.register("molten_concoctized_dirt", (loc) -> new MoltenConcoctizedDirtFluid.Source());
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_MOLTEN_CONCOCTIZED_DIRT = FLUIDS.register("flowing_molten_concoctized_dirt", (loc) -> new MoltenConcoctizedDirtFluid.Flowing());

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_CONCOCTIZED_DIRT_FLUID_TYPE = FLUID_TYPES.register("molten_concoctized_dirt",
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

    public static final DeferredBlock<LiquidBlock> MOLTEN_CONCOCTIZED_DIRT_BLOCK = ConcoctiBlocks.registerFluidBlock("molten_concoctized_dirt_block", MOLTEN_CONCOCTIZED_DIRT,
            BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).mapColor(DyeColor.PURPLE)
    );
}
