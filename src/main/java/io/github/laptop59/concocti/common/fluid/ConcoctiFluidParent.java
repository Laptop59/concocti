package io.github.laptop59.concocti.common.fluid;

import com.mojang.datafixers.util.Either;
import io.github.laptop59.concocti.common.ConcoctiRegisters;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.github.laptop59.concocti.common.item.ConcoctiItems.registerBucketItem;

/** A class allowing easy registration & containment of source and flowing fluid and their bucket item, block, source and flowing fluid and fluid type. */
public class ConcoctiFluidParent implements Holder<Fluid> {
    public final String ID;
    public final DeferredHolder<Fluid, FlowingFluid> SOURCE;
    public final DeferredHolder<Fluid, FlowingFluid> FLOWING;
    public final DeferredHolder<FluidType, FluidType> FLUID_TYPE;
    public final DeferredBlock<LiquidBlock> BLOCK;
    public final DeferredItem<BucketItem> BUCKET;
    public final int TICK_RATE;
    public final boolean IS_MOLTEN;

    public ConcoctiFluidParent(String id, int tickRate, boolean isMolten, FluidType.Properties typeProps, BlockBehaviour.Properties blockProps, boolean hasBucket) {
        ID = id;
        SOURCE = ConcoctiRegisters.FLUIDS.register(ID, (loc) -> new ConcoctiFluid.Source(this));
        FLOWING = ConcoctiRegisters.FLUIDS.register("flowing_" + ID, (loc) -> new ConcoctiFluid.Flowing(this));
        FLUID_TYPE = ConcoctiRegisters.FLUID_TYPES.register(ID, (loc) -> new FluidType(typeProps));
        BLOCK = blockProps == null ? null : ConcoctiBlocks.registerFluidBlock(ID, SOURCE, blockProps);
        BUCKET = hasBucket ? registerBucketItem(ID + "_bucket", SOURCE) : null;

        TICK_RATE = tickRate;
        IS_MOLTEN = isMolten;
    }

    public Fluid get() {
        return SOURCE.get();
    }

    @Override
    public @NotNull Fluid value() {
        return SOURCE.value();
    }

    @Override
    public boolean isBound() {
        return SOURCE.isBound();
    }

    @Override
    public boolean is(@NotNull ResourceLocation location) {
        return SOURCE.is(location);
    }

    @Override
    public boolean is(@NotNull ResourceKey<Fluid> resourceKey) {
        return SOURCE.is(resourceKey);
    }

    @Override
    public boolean is(@NotNull Predicate<ResourceKey<Fluid>> predicate) {
        return SOURCE.is(predicate);
    }

    @Override
    public boolean is(@NotNull TagKey<Fluid> tagKey) {
        return SOURCE.is(tagKey);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean is(@NotNull Holder<Fluid> holder) {
        return SOURCE.is(holder);
    }

    @Override
    public @NotNull Stream<TagKey<Fluid>> tags() {
        return SOURCE.tags();
    }

    @Override
    public @NotNull Either<ResourceKey<Fluid>, Fluid> unwrap() {
        return SOURCE.unwrap();
    }

    @Override
    public @NotNull Optional<ResourceKey<Fluid>> unwrapKey() {
        return SOURCE.unwrapKey();
    }

    @Override
    public @NotNull Kind kind() {
        return SOURCE.kind();
    }

    @Override
    public boolean canSerializeIn(@NotNull HolderOwner<Fluid> owner) {
        return SOURCE.canSerializeIn(owner);
    }
}
