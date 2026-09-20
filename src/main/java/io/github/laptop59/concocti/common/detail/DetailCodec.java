package io.github.laptop59.concocti.common.detail;

import io.github.laptop59.concocti.common.fluid.ConcoctiFluidTank;
import io.github.laptop59.concocti.common.recipe.LightningState;
import io.github.laptop59.concocti.common.recipe.SolarState;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/* An object which describes how to serialize and deserialize an object. */
public class DetailCodec<T> implements DetailSerializer<T>, DetailDeserializer<T> {
    public static final DetailCodec<ConcoctiFluidTank> FLUID_TANK = new DetailCodec<>(
        (context, holder) -> {
            FluidTank tank = holder.get();
            if (tank.isEmpty()) return;
            context.tag().put(context.id(), tank.getFluid().save(context.registries()));
        },
        (context, holder) -> {
            FluidTank tank = holder.get();
            CompoundTag subTag = (CompoundTag) context.tag().get(context.id());
            if (subTag != null)
                tank.setFluid(FluidStack.parseOptional(context.registries(), subTag).copy());
            else
                tank.setFluid(FluidStack.EMPTY.copy());
        }
    );

    public static final DetailCodec<LightningState> LIGHTNING_STATE = new DetailCodec<>(
        (context, holder) -> {
            LightningState lightningState = holder.get();
            context.tag().putBoolean(context.id(), lightningState.getLightningCollected());
        },
        (context, holder) -> {
            boolean result = context.tag().contains(context.id()) && context.tag().getBoolean(context.id());
            holder.get().setLightningCollected(result);
        }
    );

    public static final DetailCodec<SolarState> SOLAR_STATE = new DetailCodec<>(
            (context, holder) -> {
                SolarState solarState = holder.get();
                context.tag().putLongArray(context.id(), new long[] {solarState.getSolarAmount(), solarState.getMaxSolarAmount()});
            },
            (context, holder) -> {
                long[] amounts = context.tag().contains(context.id()) ? context.tag().getLongArray(context.id()) : new long[] {0, 0};
                holder.get().setSolarAmount(amounts[0]);
                holder.get().setMaxSolarAmount(amounts[1]);
            }
    );

    protected final DetailSerializer<T> serializer;
    protected final DetailDeserializer<T> deserializer;

    public DetailCodec(DetailSerializer<T> serializer, DetailDeserializer<T> deserializer) {
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    @Override
    public void deserialize(DetailContext context, DetailHolder<T> holder) {
        deserializer.deserialize(context, holder);
    }

    @Override
    public void serialize(DetailContext context, DetailHolder<T> holder) {
        serializer.serialize(context, holder);
    }
}
