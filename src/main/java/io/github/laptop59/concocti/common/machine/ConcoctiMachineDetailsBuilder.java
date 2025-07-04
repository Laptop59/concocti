package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.client.gui.components.SlotType;
import io.github.laptop59.concocti.common.abstraction.ConcoctiMachineComplexion;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.block.entity.ConcoctiMelterBlockEntity;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.fluids.IFluidTank;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

public class ConcoctiMachineDetailsBuilder<
        T extends AbstractConcoctiMachineBlockEntity<T, M, V, I, R>,
        M extends AbstractContainerMenu,
        V,
        I extends RecipeInput,
        R extends ProcessingRecipe<R, I>
        > {
    protected int maxEnergy;
    protected int maxEnergyTransfer;
    protected int slots;
    protected float rateConsumption;

    protected Supplier<RecipeType<R>> recipeType;

    protected Component defaultName;
    protected List<SlotType> allowedSlotTypes;
    protected Class<M> menuClass;

    protected Supplier<ConcoctiMachineComplexion> complexion;
    protected EnumMap<SlotType, List<Integer>> itemSlotsMap;
    protected EnumMap<SlotType, List<Supplier<IFluidTank>>> fluidSlotsMap;

    public ConcoctiMachineDetailsBuilder() {
        // Let the programmer use methods to set values.
    }

    public static <
            T extends AbstractConcoctiMachineBlockEntity<T, M, V, I, R>,
            M extends AbstractContainerMenu,
            V,
            I extends RecipeInput,
            R extends ProcessingRecipe<R, I>
    > ConcoctiMachineDetailsBuilder<T, M, V, I, R> create() {
        return new ConcoctiMachineDetailsBuilder<>();
    }

    @SuppressWarnings("unchecked")
    public ConcoctiMachineDetails<T, M, V, I, R> build() {
        // Test for non-null.
        try {
            Class<? extends ConcoctiMachineDetailsBuilder<T, M, V, I, R>> clazz =
                    (Class<? extends ConcoctiMachineDetailsBuilder<T, M, V, I, R>>) this.getClass();

            ArrayList<Field> nullFields = new ArrayList<>();
            HashMap<Field, Object> setFields = new HashMap<>();
            for (Field field : clazz.getDeclaredFields()) try {
                    if (field.get(this) == null)
                        nullFields.add(field);
                    else
                        setFields.put(field, field.get(this));
            } catch (IllegalAccessException ignored) {}

            if (!nullFields.isEmpty()) {
                CrashReport crashReport = new CrashReport("Incomplete building of ConcoctiMachineDetails", new IllegalStateException(
                        "Please report this exception to the Concocti Devs!"
                ));
                CrashReportCategory category1 = crashReport.addCategory("Null properties");
                for (Field unset : nullFields) category1.setDetail(unset.getName(), "null");
                CrashReportCategory category2 = crashReport.addCategory("Set properties");
                for (Map.Entry<Field, Object> set : setFields.entrySet()) {
                    category2.setDetail(set.getKey().getName(), set.getValue().toString());
                }
                throw new ReportedException(crashReport);
            }

        } catch (SecurityException e) {
            // Ignore the error and move on.
        }
        // Now build the details.
        return new ConcoctiMachineDetails<>(
                maxEnergy,
                maxEnergyTransfer,
                slots,
                rateConsumption,
                recipeType,
                defaultName,
                allowedSlotTypes,
                menuClass,
                complexion,
                itemSlotsMap,
                fluidSlotsMap
        );
    }

    public ConcoctiMachineDetailsBuilder<T, M, V, I, R> withMaxEnergy(int maxEnergy) {
        this.maxEnergy = maxEnergy;
        return this;
    }

    public ConcoctiMachineDetailsBuilder<T, M, V, I, R> withMaxEnergyTransfer(int maxEnergyTransfer) {
        this.maxEnergyTransfer = maxEnergyTransfer;
        return this;
    }

    public ConcoctiMachineDetailsBuilder<T, M, V, I, R> withSlots(int slots) {
        this.slots = slots;
        return this;
    }

    public ConcoctiMachineDetailsBuilder<T, M, V, I, R> withRateConsumption(float rateConsumption) {
        this.rateConsumption = rateConsumption;
        return this;
    }

    public ConcoctiMachineDetailsBuilder<T, M, V, I, R> withRecipeType(Supplier<RecipeType<R>> recipeType) {
        this.recipeType = recipeType;
        return this;
    }

    public ConcoctiMachineDetailsBuilder<T, M, V, I, R> withDefaultName(Component defaultName) {
        this.defaultName = defaultName;
        return this;
    }

    public ConcoctiMachineDetailsBuilder<T, M, V, I, R> withAllowedSlotTypes(SlotType... types) {
        this.allowedSlotTypes = List.of(types);
        return this;
    }

    public ConcoctiMachineDetailsBuilder<T, M, V, I, R> withMenuClass(Class<M> menuClass) {
        this.menuClass = menuClass;
        return this;
    }

    public ConcoctiMachineDetailsBuilder<T, M, V, I, R> withItemSlotsMap(EnumMap<SlotType, List<Integer>> slots) {
        this.itemSlotsMap = slots;
        return this;
    }

    public ConcoctiMachineDetailsBuilder<T, M, V, I, R> withFluidSlotsMap(EnumMap<SlotType, List<Supplier<IFluidTank>>> fluids) {
        this.fluidSlotsMap = fluids;
        return this;
    }

    public ConcoctiMachineDetailsBuilder<T, M, V, I, R> withComplexion(Supplier<ConcoctiMachineComplexion> complexion) {
        this.complexion = complexion;
        return this;
    }
}
