package io.github.laptop59.concocti.common.machine.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.laptop59.concocti.client.gui.AbstractConcoctiMachineScreen;
import io.github.laptop59.concocti.client.gui.components.*;
import io.github.laptop59.concocti.common.abstraction.ConcoctiMachineComplexion;
import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.AbstractConcoctiMachineBlock;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineOnlyItemsFluidsSolarBlockEntity;
import io.github.laptop59.concocti.common.block.entity.DynamicEnergyStorage;
import io.github.laptop59.concocti.common.detail.DetailCodec;
import io.github.laptop59.concocti.common.detail.DetailHolder;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluid;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluidTankSlotTypedHandler;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.machine.*;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import io.github.laptop59.concocti.common.menu.ResultSlot;
import io.github.laptop59.concocti.common.recipe.*;
import net.minecraft.advancements.Criterion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiSolarCollector extends ConcoctiMachine<
        ConcoctiSolarCollector.BlockEntity,
        ConcoctiSolarCollector.Menu,
        ItemsFluidsSolarInputValue,
        ItemsFluidsSolarRecipeInput,
        ConcoctiSolarCollector.Recipe,
        ConcoctiSolarCollector.Recipe.Serializer,
        ConcoctiSolarCollector.Block,
        ConcoctiSolarCollector.Screen,
        ConcoctiSolarCollector.RecipeCategory
        > {
    // Slots
    protected static final int INPUT_SLOT = 2;
    protected static final int OUTPUT_SLOT = 3;
    static ConcoctiSolarCollector INSTANCE;

    // Details
    public final static String ID = "concocti_solar_collector";

    public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, ItemsFluidsSolarInputValue, ItemsFluidsSolarRecipeInput, Recipe>> getDetails() {
        return () -> new ConcoctiMachineDetails<>(
                BlockEntity.class,
                10_000,
                10_000,
                4,
                5.0f,
                DynamicEnergyStorage.Mode.INPUT_ONLY.toSupplier(),
                INSTANCE.RECIPE_TYPE,
                Component.translatable("block.concocti.concocti_solar_collector"),
                List.of(SlotType.ITEM_INPUT, SlotType.FLUID_INPUT, SlotType.SOLAR_INPUT, SlotType.ITEM_OUTPUT, SlotType.FLUID_OUTPUT, SlotType.ITEM_INPUT_OUTPUT),
                Menu::new,
                blockEntity -> blockEntity.dataAccess,
                new EnumMap<>(
                        Map.of(
                                SlotType.ITEM_INPUT, List.of(INPUT_SLOT),
                                SlotType.ITEM_OUTPUT, List.of(OUTPUT_SLOT),
                                SlotType.ITEM_INPUT_OUTPUT, List.of(INPUT_SLOT, OUTPUT_SLOT)
                        )
                ),
                new EnumMap<>(
                        Map.of(
                                SlotType.FLUID_INPUT, List.of(blockEntity -> blockEntity.fluidOutput.get()),
                                SlotType.FLUID_OUTPUT, List.of(blockEntity -> blockEntity.fluidOutput.get())
                        )
                ),
                InputOutput.of(
                        blockEntity -> List.of(INPUT_SLOT),
                        blockEntity -> List.of(OUTPUT_SLOT)
                ),
                InputOutput.of(
                        blockEntity -> List.of(blockEntity.fluidInput.get()),
                        blockEntity -> List.of(blockEntity.fluidOutput.get())
                ),
                null
        );
    }

    public ConcoctiSolarCollector() {
        super(
                ID,
                BlockBehaviour.Properties
                        .of()
                        .mapColor(DyeColor.MAGENTA)
                        .requiresCorrectToolForDrops()
                        .explosionResistance(100f)
                        .strength(5f)
                        .lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 15 : 0),
                new ConcoctiBlocks.BlockData(ConcoctiBlocks.BlockToolRank.IRON, ConcoctiBlocks.BlockToolType.PICKAXE)
        );
        INSTANCE = this;
    }

    public static class BlockEntity extends AbstractConcoctiMachineOnlyItemsFluidsSolarBlockEntity<BlockEntity, Menu, Recipe> {

        private final DetailHolder<FluidTank> fluidInput = new DetailHolder<>(
                DetailCodec.FLUID_TANK, "fluid_input", new FluidTank(TANK_CAPACITY), this
        );
        private final DetailHolder<FluidTank> fluidOutput = new DetailHolder<>(
                DetailCodec.FLUID_TANK, "fluid_output", new FluidTank(TANK_CAPACITY), this
        );

        // Properties
        public final Property<SolarState> SOLAR = Properties.SOLAR_STATE.newWithLinker(solar::get);
        public final Property<FluidStack> FLUID_INPUT = Properties.FLUID_INPUT.newWithLinker(() -> fluidInput.get().getFluid());
        public final Property<FluidStack> FLUID_OUTPUT = Properties.FLUID_OUTPUT.newWithLinker(() -> fluidOutput.get().getFluid());
        public final Property<Long> SOLAR_PRODUCTION_RATE = Properties.SOLAR_PRODUCTION_RATE.newWithLinker(this::solarEarnedPerTick);

        // at coefficient = 1 (coefficient can be less or greater than 1)
        public final long NORMALIZED_SOLAR_UNITS_PER_TICK = 1_000L;
        public static final long SOLAR_PER_MOLTEN_SOLARIUM_MILLIBUCKET = 1_000L; // constant, DO NOT CHANGE! WON'T BE CHANGEABLE, EVEN IN CONFIG!

        @Override
        protected ConcoctiSolarCollector getMachineInstance() {
            return INSTANCE;
        }

        @Override
        public List<IFluidHandler> getIndexedFluidHandlers() {
            return List.of(fluidInput.get(), fluidOutput.get());
        }

        public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, ItemsFluidsSolarInputValue, ItemsFluidsSolarRecipeInput, Recipe>> getUncachedMachineDetails() {
            return INSTANCE.getDetails();
        }

        @Override
        protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
            return false;
        }

        protected final ConcoctiMachineComplexion dataAccess = new ConcoctiMachineComplexion(
                this,
                SOLAR.of(new SolarState(0, 0)),
                FLUID_INPUT.of(FluidStack.EMPTY),
                FLUID_OUTPUT.of(FluidStack.EMPTY),
                SOLAR_PRODUCTION_RATE.of(0L)
        );

        public BlockEntity(BlockPos pos, BlockState blockState) {
            super(
                    INSTANCE.BLOCK_ENTITY, pos, blockState
            );
        }

        public long calculateMaxSolarAmount() {
            return (long) (Math.pow(ConcoctiUpgradeSlot.getUpgradeUnits(getItem(UPGRADE_SLOT)), 2) + 1) * 1_000_000;
        }

        @Override
        public void onLoad() {
            onUpgradeUnitsChange();
        }

        @Override
        public void onUpgradeUnitsChange() {
            super.onUpgradeUnitsChange();
            solar.get().setMaxSolarAmount(calculateMaxSolarAmount());
        }

        public long solarEarnedPerTick() {
            assert getLevel() != null;

            BlockPos checkedPos = getBlockPos().above();
            if (!getLevel().canSeeSky(checkedPos) || !getLevel().isDay() || getLevel().dimensionType().hasFixedTime()) return 0L; // No sun!

            float rainLevel = Mth.clamp(getLevel().getRainLevel(1.0f), 0, 1);
            float thunderLevel = Mth.clamp(getLevel().getRainLevel(1.0f), 0, 1);
            Biome biome = getLevel().getBiome(checkedPos).getDelegate().value();
            float humidity = 0f;
            switch (biome.getPrecipitationAt(checkedPos)) {
                case RAIN -> humidity = 0.75f;
                case SNOW -> humidity = 0.9f;
            }
            humidity = Mth.clamp(humidity, 0, 1);

            double relativeTime = getLevel().getDayTime() / 6000.0 - 1.0;
            double timeScale = 1.0 - relativeTime * relativeTime;
            double tempScale = getTemperatureScale(biome);

            // Formula is: (not 100% realistic)
            double coefficient = timeScale * tempScale * (1 - 0.2 * rainLevel - 0.3 * thunderLevel) * (1 - 0.15 * humidity);
            return (long) (NORMALIZED_SOLAR_UNITS_PER_TICK * coefficient);
        }

        /** Returns a scale due to temperature in the range (0, 2).
         * Higher the temperature, higher will be the scale.
         * This function satisfies the following conditions:
         * <ul>
         * <li>f(x) ∈ (0, 2) for any x ∈ R</li>
         * <li>f(0) = 0.5</li>
         * <li>f(1) = 1.0</li>
         * <li>f(2) = 1.5</li>
         * </ul>
         */
        private double getTemperatureScale(Biome biome) {
            float temperatureDelta = biome.getBaseTemperature() - 1.0f; // ranges between -1f to +1f (for normal biomes)
            return 2.0 / (1.0 + Math.pow(3.0, -temperatureDelta));
        }

        @Override
        public @Nullable IFluidHandler getSidedFluidHandler(Direction direction) {
            SlotType slotType = machineSettings.getSlot(direction);
            if (slotType != null && slotType.isSet(SlotFlag.SOLAR_FROM_MOLTEN_SOLARIUM)) {
                // A fake handler to allow input of molten solarium to be converted to solar on the spot.
                return new IFluidHandler() {
                    @Override
                    public int getTanks() {
                        return 1;
                    }

                    @Override
                    public @NotNull FluidStack getFluidInTank(int tank) {
                        return FluidStack.EMPTY;
                    }

                    @Override
                    public int getTankCapacity(int tank) {
                        long capacity = solar.get().getMaxSolarAmount() / SOLAR_PER_MOLTEN_SOLARIUM_MILLIBUCKET;
                        return capacity > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) capacity;
                    }

                    @Override
                    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
                        return stack.is(ConcoctiFluids.MOLTEN_SOLARIUM);
                    }

                    @Override
                    public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
                        if (!resource.is(ConcoctiFluids.MOLTEN_SOLARIUM)) return 0; // We only accept this fluid.

                        long unfilledSolar = solar.get().getMaxSolarAmount() - solar.get().getSolarAmount();
                        long allowedAmount = unfilledSolar / SOLAR_PER_MOLTEN_SOLARIUM_MILLIBUCKET;
                        int allowedAmountInt = allowedAmount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) allowedAmount;

                        int filledAmount = Math.min(resource.getAmount(), allowedAmountInt);
                        if (action.execute())
                            solar.get().receiveSolar((long) filledAmount * SOLAR_PER_MOLTEN_SOLARIUM_MILLIBUCKET, false);
                        return filledAmount;
                    }

                    @Override
                    public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
                        return FluidStack.EMPTY.copy();
                    }

                    @Override
                    public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
                        return FluidStack.EMPTY.copy();
                    }
                };
            }
            return super.getSidedFluidHandler(direction);
        }

        @Override
        public boolean canProcess() {
            if (!super.canProcess()) return false;
            Recipe recipe = getRecipe(new ItemsFluidsSolarInputValue(inputItemHandler.get(), inputFluidHandler.get(), solar.get()));
            if (recipe == null) return false;
            // Check whether the fluids obtained from this item will not exceed our fluid limit.
            ItemStack currentItemOutput = getItem(OUTPUT_SLOT);
            if (
                    !currentItemOutput.isEmpty() &&
                    recipe.getItemOutput() != null &&
                    (!ItemStack.isSameItemSameComponents(recipe.getItemOutput().stack(), currentItemOutput) ||
                            recipe.getItemOutput().stack().getCount() + currentItemOutput.getCount() > currentItemOutput.getMaxStackSize())
            )
                return false;

            return recipe.getFluidOutput() == null || fluidOutput.get().fill(recipe.getFluidOutput().stack(), IFluidHandler.FluidAction.SIMULATE) == recipe.getFluidOutput().stack().getAmount();
        }

        @Override
        protected ItemsFluidsSolarRecipeInput recipeInputFrom(ItemsFluidsSolarInputValue inputValue) {
            return new ItemsFluidsSolarRecipeInput(inputValue.itemHandler(), inputValue.fluidHandler(), inputValue.solarStorage());
        }

        @Override
        public void tick(Level level, BlockPos pos, BlockState state) {
            solar.get().receiveSolar(solarEarnedPerTick(), false);
            super.tick(level, pos, state);
        }

        @Override
        protected void processConsumableTicks(int ticksConsumed) {
            super.processConsumableTicks(ticksConsumed);
            solar.get().extractSolar(lastRecipe.solarInputPerTick, false);
        }

        @Override
        protected void onRecipeCompleted(Recipe recipe) {
            recipeInputFrom(getInput()).consume(
                    recipe.getItemInput() == null ? List.of() : List.of(recipe.getItemInput()),
                    recipe.getFluidInput() == null ? List.of() : List.of(recipe.getFluidInput())
            );
            if (recipe.getItemOutput() != null && recipe.getItemOutput().roll()) {
                ItemStack output = getItem(OUTPUT_SLOT);
                if (output.isEmpty()) {
                    setItem(OUTPUT_SLOT, recipe.getItemOutput().stack().copy());
                } else {
                    output.setCount(output.getCount() + recipe.getItemOutput().stack().getCount());
                }
            }
            if (recipe.getFluidOutput() != null && recipe.getFluidOutput().roll())
                fluidOutput.get().fill(recipe.getFluidOutput().stack().copy(), IFluidHandler.FluidAction.EXECUTE);
        }

        @Override
        public List<IFluidTank> getFluidTanks() {
            return List.of(fluidOutput.get());
        }
    }

    public static class Block extends AbstractConcoctiMachineBlock<Block> {
        protected Block(Properties properties) {
            super(properties);
        }

        @Override
        protected ConcoctiSolarCollector getMachineInstance() {
            return INSTANCE;
        }

        @Override
        protected Function<Properties, Block> getBlockConstructor() {
            return Block::new;
        }
    }

    public static class Recipe implements ProcessingRecipe<Recipe, ItemsFluidsSolarRecipeInput> {
        private final @Nullable ItemRecipeIngredient itemInput;
        private final @Nullable FluidRecipeIngredient fluidInput;
        private final @Nullable ItemOutput itemOutput;
        private final @Nullable FluidOutput fluidOutput;
        private final long solarInputPerTick;
        private final ResourceLocation id;
        private final int ticks;

        // Add a constructor that sets all properties.
        public Recipe(ResourceLocation id, @Nullable ItemRecipeIngredient itemInput, @Nullable FluidRecipeIngredient fluidInput, @Nullable ItemOutput itemOutput, @Nullable FluidOutput fluidOutput, long solarInputPerTick, int ticks) {
            this.itemInput = itemInput;
            this.fluidInput = fluidInput;
            this.itemOutput = itemOutput;
            this.fluidOutput = fluidOutput;
            this.ticks = ticks;
            this.solarInputPerTick = solarInputPerTick;
            this.id = id;
        }

        public Recipe(@Nullable ItemRecipeIngredient itemInput, @Nullable FluidRecipeIngredient fluidInput, @Nullable ItemOutput itemOutput, @Nullable FluidOutput fluidOutput, long solarInputPerTick, int ticks) {
            this.itemInput = itemInput;
            this.fluidInput = fluidInput;
            this.itemOutput = itemOutput;
            this.fluidOutput = fluidOutput;
            this.ticks = ticks;
            this.solarInputPerTick = solarInputPerTick;
            this.id = getWouldBeResourceLocation(itemInput, fluidInput, itemOutput, fluidOutput);
        }

        public static ResourceLocation getWouldBeResourceLocation(@Nullable ItemRecipeIngredient itemInput, @Nullable FluidRecipeIngredient fluidInput, @Nullable ItemOutput itemOutput, @Nullable FluidOutput fluidOutput) {
            return ResourceLocation.fromNamespaceAndPath(
                    MODID,
                    "solar_collecting/" + String.format("%08x%08x%08x%08x",
                            Objects.hashCode(itemInput),
                            Objects.hashCode(itemOutput),
                            Objects.hashCode(fluidInput),
                            Objects.hashCode(fluidOutput)
                    )
            );
        }

        @Nullable
        public ItemOutput getItemOutput() {
            return itemOutput;
        }

        @Nullable
        public FluidOutput getFluidOutput() {
            return fluidOutput;
        }

        @Nullable
        public ItemRecipeIngredient getItemInput() {
            return itemInput;
        }

        @Nullable
        public FluidRecipeIngredient getFluidInput() {
            return fluidInput;
        }

        public long getSolarInputPerTick() {
            return solarInputPerTick;
        }

        // Grid-based recipes should return whether their recipe can fit in the given dimensions.
        // We don't have a grid, so we just return if any item can be placed in there.
        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return false;
        }

        // Check whether the given input matches this recipe. The first parameter matches the generic.
        // We check our block state and our item stack, and only return true if both match.
        @Override
        public boolean matches(@NotNull ItemsFluidsSolarRecipeInput input, @NotNull Level level) {
            return matches(input);
        }

        public boolean matches(@NotNull ItemsFluidsSolarRecipeInput input) {
            return input.test(
                    itemInput == null ? List.of() : List.of(itemInput),
                    fluidInput == null ? List.of() : List.of(fluidInput),
                    solarInputPerTick
            );
        }

        // Return an UNMODIFIABLE version of your result here. The result of this method is mainly intended
        // for the recipe book, and commonly used by JEI and other recipe viewers as well.
        @Override
        public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
            return itemOutput == null ? ItemStack.EMPTY : itemOutput.stack();
        }

        // Return the result of the recipe here, based on the given input. The first parameter matches the generic.
        // IMPORTANT: Always call .copy() if you use an existing result! If you don't, things can and will break,
        // as the result exists once per recipe, but the assembled stack is created each time the recipe is crafted.
        @Override
        public @NotNull ItemStack assemble(@NotNull ItemsFluidsSolarRecipeInput input, HolderLookup.@NotNull Provider registries) {
            return (itemOutput == null ? ItemStack.EMPTY : itemOutput.stack()).copy();
        }

        @Override
        public @NotNull RecipeSerializer<?> getSerializer() {
            return INSTANCE.RECIPE_SERIALIZER.get();
        }

        @Override
        public @NotNull RecipeType<?> getType() {
            return INSTANCE.RECIPE_TYPE.get();
        }

        @Override
        public int getTicks() {
            return ticks;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        public static class Builder implements RecipeBuilder {
            private final @Nullable ItemRecipeIngredient itemInput;
            private final @Nullable FluidRecipeIngredient fluidInput;
            private final @Nullable ItemOutput itemOutput;
            private final @Nullable FluidOutput fluidOutput;
            private final long inputSolarPerTick;
            private final int ticks;

            public Builder(@Nullable ItemRecipeIngredient itemInput, @Nullable FluidRecipeIngredient fluidInput, @Nullable ItemOutput itemOutput, @Nullable FluidOutput fluidOutput, long inputSolarPerTick, int ticks) {
                this.itemInput = itemInput;
                this.fluidInput = fluidInput;
                this.itemOutput = itemOutput;
                this.fluidOutput = fluidOutput;
                this.inputSolarPerTick = inputSolarPerTick;
                this.ticks = ticks;
            }

            @Override
            public @NotNull RecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
                return this;
            }

            @Override
            public @NotNull ConcoctiSolarCollector.Recipe.Builder group(@org.jetbrains.annotations.Nullable String group) {
                return this; // No recipe book groups required.
            }

            // Vanilla wants an Item here, not an ItemStack. You still can and should use the ItemStack
            // for serializing the recipes.
            @Override
            public @NotNull Item getResult() {
                return itemOutput == null ? Items.AIR : itemOutput.stack().getItem();
            }

            @Override
            public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
                Recipe recipe = new Recipe(
                        id,
                        this.itemInput,
                        this.fluidInput,
                        this.itemOutput,
                        this.fluidOutput,
                        this.inputSolarPerTick,
                        this.ticks
                );
                recipeOutput.accept(id, recipe, null);
            }
        }


        public static class Serializer implements RecipeSerializer<Recipe> {
            public static final MapCodec<Recipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    ItemRecipeIngredient.CODEC.optionalFieldOf("input_item").forGetter(recipe -> Optional.ofNullable(recipe.getItemInput())),
                    FluidRecipeIngredient.CODEC.optionalFieldOf("input_fluid").forGetter(recipe -> Optional.ofNullable(recipe.getFluidInput())),
                    ItemOutput.CODEC.optionalFieldOf("output_item").forGetter(recipe -> Optional.ofNullable(recipe.getItemOutput())),
                    FluidOutput.CODEC.optionalFieldOf("output_fluid").forGetter(recipe -> Optional.ofNullable(recipe.getFluidOutput())),
                    Codec.LONG.fieldOf("solar_input_per_tick").forGetter(Recipe::getSolarInputPerTick),
                    Codec.INT.fieldOf("ticks").forGetter(Recipe::getTicks)
            ).apply(inst, Recipe::fromOptionals));

            public static final StreamCodec<RegistryFriendlyByteBuf, Recipe> STREAM_CODEC =
                    StreamCodec.composite(
                            ItemRecipeIngredient.STREAM_CODEC.apply(ByteBufCodecs::optional), recipe -> Optional.ofNullable(recipe.getItemInput()),
                            FluidRecipeIngredient.STREAM_CODEC.apply(ByteBufCodecs::optional), recipe -> Optional.ofNullable(recipe.getFluidInput()),
                            ItemOutput.STREAM_CODEC.apply(ByteBufCodecs::optional), recipe -> Optional.ofNullable(recipe.getItemOutput()),
                            FluidOutput.STREAM_CODEC.apply(ByteBufCodecs::optional), recipe -> Optional.ofNullable(recipe.getFluidOutput()),
                            ByteBufCodecs.VAR_LONG, Recipe::getSolarInputPerTick,
                            ByteBufCodecs.VAR_INT, Recipe::getTicks,
                            Recipe::fromOptionals
                    );

            // Return our map codec.
            @Override
            public @NotNull MapCodec<Recipe> codec() {
                return CODEC;
            }

            // Return our stream codec.
            @Override
            public @NotNull StreamCodec<RegistryFriendlyByteBuf, Recipe> streamCodec() {
                return STREAM_CODEC;
            }
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        protected static Recipe fromOptionals(Optional<ItemRecipeIngredient> itemRecipeIngredient, Optional<FluidRecipeIngredient> fluidRecipeIngredient, Optional<ItemOutput> itemOutput, Optional<FluidOutput> fluidOutput, long solarInput, int ticks) {
            return new Recipe(
                    itemRecipeIngredient.orElse(null),
                    fluidRecipeIngredient.orElse(null),
                    itemOutput.orElse(null),
                    fluidOutput.orElse(null),
                    solarInput,
                    ticks
            );
        }
    }

    public static class Menu extends AbstractConcoctiMachineMenu<Menu> {

        @Contract(pure = true)
        @Override
        public List<Property<?>> getMachineSpecificProperties() {
            return List.of(
                    Properties.SOLAR_STATE,
                    Properties.FLUID_INPUT,
                    Properties.FLUID_OUTPUT,
                    Properties.SOLAR_PRODUCTION_RATE
            );
        }

        // Client
        public Menu(
                int containerId, Inventory playerInventory
        ) {
            super(containerId, playerInventory, 4, INSTANCE.MENU);
        }

        // Server
        public Menu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
            super(containerId, playerInventory, container, data, INSTANCE.MENU);
        }

        @Override
        protected void addOtherSlots() {
            this.addSlot(new Slot(container, INPUT_SLOT, 54 + 11, 46));
            this.addSlot(new ResultSlot(null, container, OUTPUT_SLOT, 106 + 11, 46));
        }

        @Override
        public @org.jetbrains.annotations.Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
            if (this.moveItemStackTo(movedStack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
            return null;
        }

        public FluidStack getFluidInput() {
            return viewer.get(Properties.FLUID_INPUT);
        }

        public int getMaxFluidInput() {
            return AbstractConcoctiMachineBlockEntity.TANK_CAPACITY;
        }

        public FluidStack getFluidOutput() {
            return viewer.get(Properties.FLUID_OUTPUT);
        }

        public int getMaxFluidOutput() {
            return AbstractConcoctiMachineBlockEntity.TANK_CAPACITY;
        }

        /**
         * Returns the amount of solar/maximum solar left in this block.
         */
        public long getNumberSolarLeft(boolean max) {
            SolarState state = viewer.get(Properties.SOLAR_STATE);
            return max ? state.getMaxSolarAmount() : state.getSolarAmount();
        }

        public Long getProductionRate() {
            return viewer.get(Properties.SOLAR_PRODUCTION_RATE);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Screen extends AbstractConcoctiMachineScreen<Menu> {
        private final FluidBars.Square<Menu> inputFluid = new FluidBars.Square<>(54 + 11, 29, this, menu, 0);
        private final FluidBars.Square<Menu> outputFluid = new FluidBars.Square<>(106 + 11, 29, this, menu, 1);

        private final EnergyBar<Menu> energyBar = new EnergyBar<>(10, 18, this, menu);
        private final SolarBar solarBar = new SolarBar(30, 18, this);
        private final ArrowProgress arrowProgress = new ArrowProgress(94 - 6, 37);

        public Screen(
                Menu menu,
                Inventory playerInventory,
                Component title
        ) {
            super(menu, playerInventory, title);
        }

        public List<Renderable> getUniqueChildren() {
            return List.of(
                    arrowProgress,
                    energyBar,
                    solarBar,
                    inputFluid,
                    outputFluid
            );
        }

        @Override
        public List<Renderable> getChildren() {
            ArrayList<Renderable> children = new ArrayList<>(super.getChildren());
            children.addAll(getUniqueChildren());
            return List.copyOf(children);
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, RenderInfo renderInfo) {
            // Don't forget to first render the abstract screen!
            super.render(guiGraphics, renderInfo);

            arrowProgress.update(menu.getProgress());

            energyBar.update(menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true));
            solarBar.update(menu.getNumberSolarLeft(false), menu.getNumberSolarLeft(true), menu.getProductionRate());

            // Render the fluids.
            inputFluid.update(menu.getFluidInput(), menu.getMaxFluidInput());
            outputFluid.update(menu.getFluidOutput(), menu.getMaxFluidOutput());

            renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());
        }
    }

    public static class RecipeCategory extends AbstractConcoctiRecipeCategory<Recipe> {
        public final SolarBar bar = new SolarBar(0, 0, 2, 2);

        @Override
        protected ConcoctiSolarCollector getMachineInstance() {
            return INSTANCE;
        }

        @Override
        public @NotNull Object getJeiRecipeType() {
            return INSTANCE.getJeiRecipeType();
        }

        @Override
        public @NotNull Component getTitle() {
            return Component.translatable("block.concocti.concocti_solar_collector");
        }

        @Override
        public void set(@NotNull io.github.laptop59.concocti.common.machine.RecipeBuilder builder, @NotNull Recipe recipe) {
            bar.updateAsRecipeIngredient(recipe.getSolarInputPerTick(), recipe.getTicks());

            if (recipe.getFluidInput() == null)
                builder.addInputSlot(52, 16, true);
            else
                builder.addInputSlot(52, 16, recipe.getFluidInput());
            if (recipe.getItemInput() == null)
                builder.addInputSlot(52, 16 + 18, false);
            else
                builder.addInputSlot(52, 16 + 18, recipe.getItemInput());

            if (recipe.getFluidOutput() == null)
                builder.addOutputSlot(112, 16, true);
            else
                builder.addOutputSlot(112, 16, recipe.getFluidOutput());
            if (recipe.getItemOutput() == null)
                builder.addOutputSlot(112, 16 + 18, false);
            else
                builder.addOutputSlot(112, 16 + 18, recipe.getItemOutput());

            builder.addSolarInput(recipe.getSolarInputPerTick() * recipe.getTicks());
        }

        @Override
        public void render(@NotNull Recipe recipe, GuiGraphics guiGraphics, double mouseX, double mouseY) {
            super.render(recipe, guiGraphics, mouseX, mouseY);
            Renderable.renderChildAbsolute(
                    guiGraphics,
                    new RenderInfo((int) mouseX, (int) mouseY, 0, 0, Minecraft.getInstance().font),
                    bar
            );
        }

        @Override
        public void tooltip(@NotNull List<Component> tooltipBuilder, @NotNull Recipe recipe, double mouseX, double mouseY) {
            super.tooltip(tooltipBuilder, recipe, mouseX, mouseY);
            RenderInfo renderInfo = new RenderInfo((int) mouseX, (int) mouseY, 0, 0, Minecraft.getInstance().font);
            if (bar.isHovered(renderInfo))
                tooltipBuilder.addAll(bar.getTooltipComponents());
        }

        @Override
        public int getHeight(@NotNull Recipe recipe) {
            return super.getHeight(recipe);
        }

        @Override
        public int getSlotsHeight(@NotNull Recipe recipe) {
            return 2;
        }

        @Override
        public int getHorizontalArrowOffset(@NotNull Recipe recipe) {
            return 6;
        }
    }

    public BlockEntityConstructor<BlockEntity> getBlockEntityConstructor() {
        return BlockEntity::new;
    }

    public BlockConstructor<Block> getBlockConstructor() {
        return Block::new;
    }

    public MenuClientConstructor<Menu> getMenuClientConstructor() {
        return Menu::new;
    }

    public ScreenConstructor<Menu, Screen> getScreenConstructor() {
        return Screen::new;
    }

    public RecipeCategoryConstructor<RecipeCategory, Recipe, ItemsFluidsSolarRecipeInput> getRecipeCategoryConstructor() {
        return RecipeCategory::new;
    }

    public RecipeSerializerConstructor<Recipe.Serializer, Recipe, ItemsFluidsSolarRecipeInput> getRecipeSerializerConstructor() {
        return Recipe.Serializer::new;
    }

    public Class<Recipe> getRecipeClass() {
        return Recipe.class;
    }
}
