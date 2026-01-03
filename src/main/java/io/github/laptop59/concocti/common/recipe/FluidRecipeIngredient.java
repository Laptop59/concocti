package io.github.laptop59.concocti.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/** A fluid ingredient with remainder information as well. */
public final class FluidRecipeIngredient {
    public static final Codec<FluidRecipeIngredient> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            FluidOption.CODEC.listOf().fieldOf("options").forGetter(FluidRecipeIngredient::options)
        ).apply(instance, FluidRecipeIngredient::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidRecipeIngredient> STREAM_CODEC = StreamCodec.composite(
        FluidOption.STREAM_CODEC.apply(ByteBufCodecs.list()), FluidRecipeIngredient::options,
        FluidRecipeIngredient::new
    );

    public static FluidRecipeIngredient of(@NotNull FluidOption... options) {
        return new FluidRecipeIngredient(options);
    }

    @NotNull
    private final List<FluidOption> options;
    private List<FluidStack> cachedStacks;

    public FluidRecipeIngredient(@NotNull List<FluidOption> options) {
        this.options = List.copyOf(options);
    }

    public FluidRecipeIngredient(@NotNull FluidOption... options) {
        this.options = List.of(options);
    }

    public static FluidRecipeIngredient of(FluidIngredient ingredient, long amount, FluidStack remainder) {
        return new FluidRecipeIngredient(FluidOption.of(ingredient, amount, remainder));
    }

    public static FluidRecipeIngredient of(Fluid fluid, long amount) {
        return new FluidRecipeIngredient(FluidOption.of(FluidIngredient.of(fluid), amount));
    }

    public static FluidRecipeIngredient of(FluidIngredient ingredient, long amount) {
        return new FluidRecipeIngredient(FluidOption.of(ingredient, amount));
    }

    public static FluidRecipeIngredient of(FluidStack ingredient, FluidStack remainder) {
        return new FluidRecipeIngredient(FluidOption.of(FluidIngredient.of(ingredient.getFluid()), ingredient.getAmount(), remainder));
    }

    public static FluidRecipeIngredient of(FluidStack ingredient) {
        return new FluidRecipeIngredient(FluidOption.of(FluidIngredient.of(ingredient.getFluid()), ingredient.getAmount()));
    }

    public List<FluidOption> options() {
        return options;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof FluidRecipeIngredient ingredient) {
            return options.equals(ingredient.options);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return options.hashCode();
    }

    @Override
    public String toString() {
        return options.toString();
    }

    public List<FluidStack> getStacks() {
        if (cachedStacks == null)
            cachedStacks = getFluidStacksUncached();
        return cachedStacks;
    }

    private List<FluidStack> getFluidStacksUncached() {
        return options
            .stream()
            .map(FluidOption::intoFluidStackUncached)
            .flatMap(List::stream)
            .toList();
    }

    public Optional<FluidOption> testOpt(FluidStack fluid) {
        for (FluidOption option : options) {
            if (option.matches(fluid)) return Optional.of(option);
        }
        return Optional.empty();
    }

    public Optional<FluidOption> consumeOpt(FluidStack fluid) {
        for (FluidOption option : options) {
            if (option.consume(fluid)) return Optional.of(option);
        }
        return Optional.empty();
    }

    public boolean test(FluidStack fluid) {
        return testOpt(fluid).isPresent();
    }

    public boolean consume(FluidStack fluid) {
        return consumeOpt(fluid).isPresent();
    }

    public Optional<FluidOption> testOpt(IFluidHandler handler) {
        for (FluidOption option : options) {
            long amountLeft = option.amount();
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack fluidStack = handler.getFluidInTank(i);
                if (option.ingredient().test(fluidStack))
                    amountLeft -= handler.drain((int) Math.min(Integer.MAX_VALUE, amountLeft), IFluidHandler.FluidAction.SIMULATE).getAmount();
                if (amountLeft <= 0) return Optional.of(option);
            }
        }
        return Optional.empty();
    }

    public Optional<FluidOption> consumeOpt(IFluidHandler handler) {
        for (FluidOption option : options) {
            long amountLeft = option.amount();
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack fluidStack = handler.getFluidInTank(i);
                if (option.ingredient().test(fluidStack))
                    amountLeft -= handler.drain((int) Math.min(Integer.MAX_VALUE, amountLeft), IFluidHandler.FluidAction.EXECUTE).getAmount();
                if (amountLeft <= 0) return Optional.of(option);
            }
        }
        return Optional.empty();
    }

    public boolean test(IFluidHandler handler) {
        return testOpt(handler).isPresent();
    }

    public boolean consume(IFluidHandler handler) {
        return consumeOpt(handler).isPresent();
    }
}
