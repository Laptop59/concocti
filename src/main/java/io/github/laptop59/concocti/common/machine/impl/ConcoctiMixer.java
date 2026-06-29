package io.github.laptop59.concocti.common.machine.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.laptop59.concocti.client.gui.AbstractConcoctiMachineScreen;
import io.github.laptop59.concocti.client.gui.components.*;
import io.github.laptop59.concocti.common.ConcoctiSounds;
import io.github.laptop59.concocti.common.abstraction.ConcoctiMachineComplexion;
import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.AbstractConcoctiMachineBlock;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineOnlyItemsFluidsBlockEntity;
import io.github.laptop59.concocti.common.block.entity.DynamicEnergyStorage;
import io.github.laptop59.concocti.common.detail.DetailCodec;
import io.github.laptop59.concocti.common.detail.DetailHolder;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetails;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineOnlyItemsFluids;
import io.github.laptop59.concocti.common.machine.InputOutput;
import io.github.laptop59.concocti.common.machine.ItemsFluidsInputValue;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.menu.ResultSlot;
import io.github.laptop59.concocti.common.recipe.*;
import io.github.laptop59.concocti.common.machine.AbstractConcoctiRecipeCategory;
import net.minecraft.advancements.Criterion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiMixer extends ConcoctiMachineOnlyItemsFluids<
        ConcoctiMixer.BlockEntity,
        ConcoctiMixer.Menu,
        ConcoctiMixer.Recipe,
        ConcoctiMixer.Recipe.Serializer,
        ConcoctiMixer.Block,
        ConcoctiMixer.Screen,
        ConcoctiMixer.RecipeCategory
        > {
    static ConcoctiMixer INSTANCE;

    // Details
    public final static String ID = "concocti_mixer";

    // Slots
    private static final int OUTPUT_SLOT = 6;
    private static final int INPUT_SLOT_1 = 2;
    private static final int INPUT_SLOT_2 = 3;
    private static final int INPUT_SLOT_3 = 4;
    private static final int INPUT_SLOT_4 = 5;

    public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, ItemsFluidsInputValue, ItemsFluidsRecipeInput, Recipe>> getDetails() {
        return () -> new ConcoctiMachineDetails<>(
                BlockEntity.class,
                5_000,
                5_000,
                7,
                10.0f,
                DynamicEnergyStorage.Mode.INPUT_ONLY.toSupplier(),
                INSTANCE.RECIPE_TYPE,
                Component.translatable("block.concocti.concocti_mixer"),
                List.of(
                        SlotType.ITEM_INPUT_1,
                        SlotType.ITEM_INPUT_2,
                        SlotType.ITEM_INPUT_3,
                        SlotType.ITEM_INPUT_4,
                        SlotType.FLUID_INPUT_1,
                        SlotType.FLUID_INPUT_2,
                        SlotType.FLUID_INPUT_3,
                        SlotType.FLUID_INPUT_4,
                        SlotType.ITEM_OUTPUT,
                        SlotType.FLUID_OUTPUT,
                        SlotType.ALL_ITEM_INPUTS,
                        SlotType.ALL_FLUID_INPUTS,
                        SlotType.ALL_ITEM_INPUTS_AND_OUTPUTS,
                        SlotType.ALL_FLUID_INPUTS_AND_OUTPUTS,
                        SlotType.ALL_INPUTS_AND_OUTPUTS
                ),
                Menu::new,
                blockEntity -> blockEntity.dataAccess,
                new EnumMap<>(
                        Map.of(
                                SlotType.ITEM_INPUT_1, List.of(INPUT_SLOT_1),
                                SlotType.ITEM_INPUT_2, List.of(INPUT_SLOT_2),
                                SlotType.ITEM_INPUT_3, List.of(INPUT_SLOT_3),
                                SlotType.ITEM_INPUT_4, List.of(INPUT_SLOT_4),
                                SlotType.ALL_ITEM_INPUTS, List.of(INPUT_SLOT_1, INPUT_SLOT_2, INPUT_SLOT_3, INPUT_SLOT_4),
                                SlotType.ITEM_OUTPUT, List.of(OUTPUT_SLOT),
                                SlotType.ALL_ITEM_INPUTS_AND_OUTPUTS, List.of(INPUT_SLOT_1, INPUT_SLOT_2, INPUT_SLOT_3, INPUT_SLOT_4, OUTPUT_SLOT),
                                SlotType.ALL_INPUTS_AND_OUTPUTS, List.of(INPUT_SLOT_1, INPUT_SLOT_2, INPUT_SLOT_3, INPUT_SLOT_4, OUTPUT_SLOT)
                        )
                ),
                new EnumMap<>(
                        Map.of(
                                SlotType.FLUID_INPUT_1, List.of(blockEntity -> blockEntity.fluidInput1.get()),
                                SlotType.FLUID_INPUT_2, List.of(blockEntity -> blockEntity.fluidInput2.get()),
                                SlotType.FLUID_INPUT_3, List.of(blockEntity -> blockEntity.fluidInput3.get()),
                                SlotType.FLUID_INPUT_4, List.of(blockEntity -> blockEntity.fluidInput4.get()),
                                SlotType.ALL_FLUID_INPUTS, List.of(
                                        blockEntity -> blockEntity.fluidInput1.get(),
                                        blockEntity -> blockEntity.fluidInput2.get(),
                                        blockEntity -> blockEntity.fluidInput3.get(),
                                        blockEntity -> blockEntity.fluidInput4.get()
                                ),
                                SlotType.FLUID_OUTPUT, List.of(blockEntity -> blockEntity.fluidOutput.get()),
                                SlotType.ALL_FLUID_INPUTS_AND_OUTPUTS, List.of(
                                        blockEntity -> blockEntity.fluidInput1.get(),
                                        blockEntity -> blockEntity.fluidInput2.get(),
                                        blockEntity -> blockEntity.fluidInput3.get(),
                                        blockEntity -> blockEntity.fluidInput4.get()
                                ),
                                SlotType.ALL_INPUTS_AND_OUTPUTS, List.of(
                                        blockEntity -> blockEntity.fluidInput1.get(),
                                        blockEntity -> blockEntity.fluidInput2.get(),
                                        blockEntity -> blockEntity.fluidInput3.get(),
                                        blockEntity -> blockEntity.fluidInput4.get()
                                )
                        )
                ),
                InputOutput.of(
                        blockEntity -> List.of(
                                INPUT_SLOT_1,
                                INPUT_SLOT_2,
                                INPUT_SLOT_3,
                                INPUT_SLOT_4
                        ),
                        blockEntity -> List.of(
                                OUTPUT_SLOT
                        )
                ),
                InputOutput.of(
                        blockEntity -> List.of(
                                blockEntity.fluidInput1.get(),
                                blockEntity.fluidInput2.get(),
                                blockEntity.fluidInput3.get(),
                                blockEntity.fluidInput4.get()
                        ),
                        blockEntity -> List.of(
                                blockEntity.fluidOutput.get()
                        )
                ),
                ConcoctiSounds.CONCOCTI_MIXER_FIRE_CRACKLE.get()
        );
    }

    public ConcoctiMixer() {
        super(
                ID,
                BlockBehaviour.Properties
                        .of()
                        .mapColor(DyeColor.MAGENTA)
                        .requiresCorrectToolForDrops()
                        .explosionResistance(4.5f)
                        .strength(4f)
                        .lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 13 : 0),
                new ConcoctiBlocks.BlockData(ConcoctiBlocks.BlockToolRank.STONE, ConcoctiBlocks.BlockToolType.PICKAXE)
        );
        INSTANCE = this;
    }

    public static class BlockEntity extends AbstractConcoctiMachineOnlyItemsFluidsBlockEntity
            <BlockEntity, Menu, Recipe> {

        private final DetailHolder<FluidTank> fluidInput1 = new DetailHolder<>(
                DetailCodec.FLUID_TANK, "fluid_input_1", new FluidTank(TANK_CAPACITY), this
        );
        private final DetailHolder<FluidTank> fluidInput2 = new DetailHolder<>(
                DetailCodec.FLUID_TANK, "fluid_input_2", new FluidTank(TANK_CAPACITY), this
        );
        private final DetailHolder<FluidTank> fluidInput3 = new DetailHolder<>(
                DetailCodec.FLUID_TANK, "fluid_input_3", new FluidTank(TANK_CAPACITY), this
        );
        private final DetailHolder<FluidTank> fluidInput4 = new DetailHolder<>(
                DetailCodec.FLUID_TANK, "fluid_input_4", new FluidTank(TANK_CAPACITY), this
        );
        private final DetailHolder<FluidTank> fluidOutput = new DetailHolder<>(
                DetailCodec.FLUID_TANK, "fluid_output", new FluidTank(TANK_CAPACITY * 2), this
        );

        // Properties
        public final Property<FluidStack> FLUID_INPUT_1 = Properties.FLUID_INPUT_1.newWithLinker(fluidInput1.get()::getFluid);
        public final Property<FluidStack> FLUID_INPUT_2 = Properties.FLUID_INPUT_2.newWithLinker(fluidInput2.get()::getFluid);
        public final Property<FluidStack> FLUID_INPUT_3 = Properties.FLUID_INPUT_3.newWithLinker(fluidInput3.get()::getFluid);
        public final Property<FluidStack> FLUID_INPUT_4 = Properties.FLUID_INPUT_4.newWithLinker(fluidInput4.get()::getFluid);
        public final Property<FluidStack> FLUID_OUTPUT = Properties.FLUID_OUTPUT.newWithLinker(fluidOutput.get()::getFluid);

        @Override
        protected ConcoctiMixer getMachineInstance() {
            return INSTANCE;
        }

        @Override
        public List<IFluidHandler> getIndexedFluidHandlers() {
            return List.of(
                    fluidInput1.get(),
                    fluidInput2.get(),
                    fluidInput3.get(),
                    fluidInput4.get(),
                    fluidOutput.get()
            );
        }

        @Override
        protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
            return true;
        }

        protected final ConcoctiMachineComplexion dataAccess = new ConcoctiMachineComplexion(
                this,
                FLUID_INPUT_1.of(FluidStack.EMPTY.copy()),
                FLUID_INPUT_2.of(FluidStack.EMPTY.copy()),
                FLUID_INPUT_3.of(FluidStack.EMPTY.copy()),
                FLUID_INPUT_4.of(FluidStack.EMPTY.copy()),
                FLUID_OUTPUT.of(FluidStack.EMPTY.copy())
        );

        public BlockEntity(BlockPos pos, BlockState blockState) {
            super(
                    INSTANCE.BLOCK_ENTITY, pos, blockState
            );
        }

        @Override
        public boolean canProcess() {
            if (!super.canProcess()) return false;
            Recipe recipe = getRecipe(getInput());
            if (recipe == null) return false;
            if (!recipe.matches(new ItemsFluidsRecipeInput(inputItemHandler.get(), inputFluidHandler.get())))
                return false;
            // Check whether the fluids obtained from this item will not exceed our fluid limit.
            ItemStack outputSlotItems = getItem(OUTPUT_SLOT);
            ItemOutput resultItems = recipe.getOutputItem();
            if (!outputSlotItems.isEmpty() &&
                    resultItems != null &&
                    outputSlotItems.getCount() + resultItems.stack().getCount() > outputSlotItems.getMaxStackSize())
                return false;

            FluidOutput resultFluid = recipe.getOutputFluid();
            if (resultFluid != null && !resultFluid.stack().isEmpty() ) {
                int actuallyFilled = fluidOutput.get().fill(resultFluid.stack(), IFluidHandler.FluidAction.SIMULATE);
                int requiredFilled = resultFluid.stack().getAmount();
                if (actuallyFilled != requiredFilled)
                    return false;
            }
            return true;
        }

        @Override
        protected void onRecipeCompleted(Recipe recipe) {
            recipeInputFrom(getInput()).consume(recipe.getInputItems(), recipe.getInputFluids());
            if (recipe.getOutputItem() != null && recipe.getOutputItem().roll())
                itemHandler.insertItem(OUTPUT_SLOT, recipe.getOutputItem().stack().copy(), false);
            if (recipe.getOutputFluid() != null && recipe.getOutputFluid().roll())
                fluidOutput.get().fill(recipe.getOutputFluid().stack().copy(), IFluidHandler.FluidAction.EXECUTE);
        }

        @Override
        public List<IFluidTank> getFluidTanks() {
            return List.of(
                    fluidInput1.get(),
                    fluidInput2.get(),
                    fluidInput3.get(),
                    fluidInput4.get(),
                    fluidOutput.get()
            );
        }
    }

    public static class Block extends AbstractConcoctiMachineBlock<Block> {
        protected Block(Properties properties) {
            super(properties);
        }

        @Override
        protected ConcoctiMixer getMachineInstance() {
            return INSTANCE;
        }

        @Override
        protected Function<Properties, Block> getBlockConstructor() {
            return Block::new;
        }
    }

    public static class Recipe implements ProcessingRecipe<Recipe, ItemsFluidsRecipeInput> {
        // An in-code representation of our recipe data. This can be basically anything you want.
        // Common things to have here is a processing time integer of some kind, or an experience reward.
        // Note that we now use an ingredient instead of an item stack for the input.
        private final List<ItemRecipeIngredient> inputItems;
        private final ItemOutput outputItem;
        private final List<FluidRecipeIngredient> inputFluids;
        private final FluidOutput outputFluid;

        private final ResourceLocation id;

        private final int ticks;

        // Add a constructor that sets all properties.
        public Recipe(ResourceLocation id, List<ItemRecipeIngredient> inputItems, ItemOutput outputItem, List<FluidRecipeIngredient> inputFluids, FluidOutput outputFluid, int ticks) {
            this.inputItems = inputItems;
            this.outputItem = outputItem;
            this.inputFluids = inputFluids;
            this.outputFluid = outputFluid;
            this.ticks = ticks;
            this.id = id;
        }

        public Recipe(List<ItemRecipeIngredient> inputItems, ItemOutput outputItem, List<FluidRecipeIngredient> inputFluids, FluidOutput outputFluid, int ticks) {
            this.inputItems = inputItems;
            this.outputItem = outputItem;
            this.inputFluids = inputFluids;
            this.outputFluid = outputFluid;
            this.ticks = ticks;
            this.id = getWouldBeResourceLocation(inputItems, inputFluids);
        }

        public static ResourceLocation getWouldBeResourceLocation(List<ItemRecipeIngredient> inputItems, List<FluidRecipeIngredient> inputFluids) {
            int[] hashes = new int[2];
            ArrayList<Object> objects = new ArrayList<>(inputItems);
            hashes[0] = Objects.hash(objects.toArray());
            objects.clear();
            objects.addAll(inputFluids);
            hashes[1] = Objects.hash(objects.toArray());
            long longHash = ((long) hashes[0] << 32) | hashes[1];
            return ResourceLocation.fromNamespaceAndPath(MODID, String.format("mixing/%016x", longHash));
        }

        public List<ItemRecipeIngredient> getInputItems() {
            return inputItems;
        }

        @Nullable
        public ItemOutput getOutputItem() {
            return outputItem;
        }

        public List<FluidRecipeIngredient> getInputFluids() {
            return inputFluids;
        }

        @Nullable
        public FluidOutput getOutputFluid() {
            return outputFluid;
        }

        // A list of our ingredients. Does not need to be overridden if you have no ingredients
        // (the default implementation returns an empty list here). It makes sense to cache larger lists in a field.
        @Override
        public @NotNull NonNullList<Ingredient> getIngredients() {
            return NonNullList.create();
        }

        // Grid-based recipes should return whether their recipe can fit in the given dimensions.
        // We don't have a grid, so we just return if any item can be placed in there.
        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return width * height >= 1;
        }

        // Check whether the given input matches this recipe. The first parameter matches the generic.
        // We check our block state and our item stack, and only return true if both match.
        @Override
        public boolean matches(@NotNull ItemsFluidsRecipeInput input, @NotNull Level level) {
            return matches(input);
        }

        public boolean matches(@NotNull ItemsFluidsRecipeInput input) {
            return input.test(inputItems, inputFluids);
        }

        // Return an UNMODIFIABLE version of your result here. The result of this method is mainly intended
        // for the recipe book, and commonly used by JEI and other recipe viewers as well.
        @Override
        public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
            return outputItem == null ? ItemStack.EMPTY : outputItem.stack();
        }

        // Return the result of the recipe here, based on the given input. The first parameter matches the generic.
        // IMPORTANT: Always call .copy() if you use an existing result! If you don't, things can and will break,
        // as the result exists once per recipe, but the assembled stack is created each time the recipe is crafted.
        @Override
        public @NotNull ItemStack assemble(@NotNull ItemsFluidsRecipeInput input, HolderLookup.@NotNull Provider registries) {
            if (matches(input)) return getResultItem(registries).copy();
            return ItemStack.EMPTY.copy();
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
            protected final List<ItemRecipeIngredient> inputItems;
            protected final ItemOutput outputItem;
            protected final List<FluidRecipeIngredient> inputFluids;
            protected final FluidOutput outputFluid;
            protected final ResourceLocation resourceLocation;
            protected final int ticks;

            public Builder(ResourceLocation resourceLocation, List<ItemRecipeIngredient> inputItems, ItemOutput outputItem, List<FluidRecipeIngredient> inputFluids, FluidOutput outputFluid, int ticks) {
                this.resourceLocation = resourceLocation;
                this.inputItems = inputItems;
                this.outputItem = outputItem;
                this.inputFluids = inputFluids;
                this.outputFluid = outputFluid;
                this.ticks = ticks;
            }

            @Override
            public @NotNull RecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
                return this;
            }

            @Override
            public @NotNull ConcoctiMixer.Recipe.Builder group(@org.jetbrains.annotations.Nullable String group) {
                return this; // No recipe book groups required.
            }

            // Vanilla wants an Item here, not an ItemStack. You still can and should use the ItemStack
            // for serializing the recipes.
            @Override
            public @NotNull Item getResult() {
                return outputItem == null ? Items.AIR : outputItem.stack().getItem();
            }

            @Override
            public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
                Recipe recipe = new Recipe(
                        id,
                        this.inputItems,
                        this.outputItem,
                        this.inputFluids,
                        this.outputFluid,
                        this.ticks
                );
                recipeOutput.accept(resourceLocation, recipe, null);
            }
        }


        public static class Serializer implements RecipeSerializer<Recipe> {
            public static final MapCodec<Recipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    ItemRecipeIngredient.CODEC.listOf().fieldOf("input_items").forGetter(Recipe::getInputItems),
                    ItemOutput.CODEC.optionalFieldOf("output_item").forGetter(recipe -> Optional.ofNullable(recipe.getOutputItem())),
                    FluidRecipeIngredient.CODEC.listOf().fieldOf("input_fluids").forGetter(Recipe::getInputFluids),
                    FluidOutput.CODEC.optionalFieldOf("output_fluid").forGetter(recipe -> Optional.ofNullable(recipe.getOutputFluid())),
                    Codec.INT.fieldOf("ticks").forGetter(Recipe::getTicks)
            ).apply(inst, Recipe::fromOptionals));

            public static final StreamCodec<RegistryFriendlyByteBuf, Recipe> STREAM_CODEC =
                    StreamCodec.composite(
                            ItemRecipeIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), Recipe::getInputItems,
                            ItemOutput.STREAM_CODEC.apply(ByteBufCodecs::optional), recipe -> Optional.ofNullable(recipe.getOutputItem()),
                            FluidRecipeIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), Recipe::getInputFluids,
                            FluidOutput.STREAM_CODEC.apply(ByteBufCodecs::optional), recipe -> Optional.ofNullable(recipe.getOutputFluid()),
                            ByteBufCodecs.INT, Recipe::getTicks,
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

        private static Recipe fromOptionals(List<ItemRecipeIngredient> itemRecipeIngredients, Optional<ItemOutput> itemOutput, List<FluidRecipeIngredient> fluidRecipeIngredients, Optional<FluidOutput> fluidOutput, int ticks) {
            return new Recipe(
                    itemRecipeIngredients,
                    itemOutput.orElse(null),
                    fluidRecipeIngredients,
                    fluidOutput.orElse(null),
                    ticks
            );
        }
    }

    public static class Menu extends AbstractConcoctiMachineMenu<Menu> {
        @Contract(pure = true)
        @Override
        public List<Property<?>> getMachineSpecificProperties() {
            return List.of(
                    Properties.FLUID_INPUT_1,
                    Properties.FLUID_INPUT_2,
                    Properties.FLUID_INPUT_3,
                    Properties.FLUID_INPUT_4,
                    Properties.FLUID_OUTPUT
            );
        }

        // Client
        public Menu(
                int containerId, Inventory playerInventory
        ) {
            super(containerId, playerInventory, 7, INSTANCE.MENU);
        }

        // Server
        public Menu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
            super(containerId, playerInventory, container, data, INSTANCE.MENU);
        }

        @Override
        protected void addOtherSlots() {
            // Input slots
            int y = 37 + 6;
            for (int i = 0; i < 4; i++) {
                int slot = i + 2;
                this.addSlot(new Slot(container, slot, 30 + i * 18, y));
            }
            // Output tank
            this.addSlot(new ResultSlot(null, container, 6, 30 + 104, y));
        }

        @Override
        public @org.jetbrains.annotations.Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
            if (this.moveItemStackTo(movedStack, 2, 6, false)) {
                return ItemStack.EMPTY;
            }
            return null;
        }

        public List<FluidStack> getInputFluidStacks() {
            return List.of(
                    viewer.get(Properties.FLUID_INPUT_1),
                    viewer.get(Properties.FLUID_INPUT_2),
                    viewer.get(Properties.FLUID_INPUT_3),
                    viewer.get(Properties.FLUID_INPUT_4)
            );
        }

        public FluidStack getOutputFluidStack() {
            return viewer.get(Properties.FLUID_OUTPUT);
        }

        public int getInputFluidStackSize() {
            return AbstractConcoctiMachineBlockEntity.TANK_CAPACITY;
        }

        public int getOutputFluidStackSize() {
            return AbstractConcoctiMachineBlockEntity.TANK_CAPACITY * 2;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Screen extends AbstractConcoctiMachineScreen<Menu> {
        private final FluidBars.Square<Menu> inputFluid1 = new FluidBars.Square<>(30, 26, this, menu, 0);
        private final FluidBars.Square<Menu> inputFluid2 = new FluidBars.Square<>(30 + 18, 26, this, menu, 1);
        private final FluidBars.Square<Menu> inputFluid3 = new FluidBars.Square<>(30 + 36, 26, this, menu, 2);
        private final FluidBars.Square<Menu> inputFluid4 = new FluidBars.Square<>(30 + 54, 26, this, menu, 3);

        private final FluidBars.Square<Menu> outputFluid = new FluidBars.Square<>(30 + 104, 26, this, menu, 4);

        private final EnergyBar<Menu> energyBar = new EnergyBar<>(10, 18, this, menu);
        private final ArrowProgress arrowProgress = new ArrowProgress(106, 34);

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
                    inputFluid1,
                    inputFluid2,
                    inputFluid3,
                    inputFluid4,
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
        public void render(@NotNull GuiGraphics guiGraphics, RenderInfo renderInfo, float partialTick) {
            // Don't forget to first render the abstract screen!
            super.render(guiGraphics, renderInfo, partialTick);

            arrowProgress.update(menu.getProgress());

            energyBar.update(menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true));

            // Render the fluids.
            outputFluid.update(menu.getOutputFluidStack(), menu.getOutputFluidStackSize());
            List<FluidBars.Square<Menu>> fluidBars = List.of(inputFluid1, inputFluid2, inputFluid3, inputFluid4);
            List<FluidStack> fluidStacks = menu.getInputFluidStacks();
            for (int i = 0; i < 4; i++) {
                fluidBars.get(i).update(fluidStacks.get(i), menu.getInputFluidStackSize());
            }

            renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());
        }
    }

    public static class RecipeCategory extends AbstractConcoctiRecipeCategory<Recipe> {
        @Override
        protected ConcoctiMixer getMachineInstance() {
            return INSTANCE;
        }

        private final int WIDTH = 176 - 8;

        @Override
        public @NotNull Object getJeiRecipeType() {
            return INSTANCE.getJeiRecipeType();
        }

        @Override
        public @NotNull Component getTitle() {
            return Component.translatable("block.concocti.concocti_mixer");
        }

        @Override
        public int getSlotsHeight(@NotNull Recipe recipe) {
            return 2;
        }

        @Override
        public void set(@NotNull io.github.laptop59.concocti.common.machine.RecipeBuilder builder, @NotNull Recipe recipe) {
            int left = WIDTH / 2 - 5 * 18 + 18;
            int top = 16 + 18;

            int x;
            for (int i = 0; i < 4; i++) {
                x = left + i * 18;
                List<ItemRecipeIngredient> ingredients = recipe.getInputItems();
                if (i < ingredients.size())
                    builder.addInputSlot(x, top, ingredients.get(i));
                else
                    builder.addInputSlot(x, top, false);
            }

            {
                ItemOutput output = recipe.getOutputItem();
                x = WIDTH / 2 + 21 + 34;
                if (output != null)
                    builder.addOutputSlot(x, top, output);
                else
                    builder.addOutputSlot(x, top, false);
            }

            top -= 18;
            for (int i = 0; i < 4; i++) {
                x = left + i * 18;
                List<FluidRecipeIngredient> ingredients = recipe.getInputFluids();
                if (i < ingredients.size())
                    builder.addInputSlot(x, top, ingredients.get(i));
                else
                    builder.addInputSlot(x, top, true);
            }

            {
                FluidOutput output = recipe.getOutputFluid();
                x = WIDTH / 2 + 21 + 34;
                if (output != null)
                    builder.addOutputSlot(x, top, output);
                else
                    builder.addOutputSlot(x, top, true);
            }
        }

        @Override
        protected int getHorizontalArrowOffset(@NotNull Recipe recipe) {
            return 28;
        }

        @Override
        public int getWidth(@NotNull Recipe recipe) {
            return WIDTH;
        }

        @Override
        public void render(@NotNull ConcoctiMixer.Recipe recipe, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
            super.render(recipe, guiGraphics, mouseX, mouseY);
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

    public RecipeCategoryConstructor<RecipeCategory, Recipe, ItemsFluidsRecipeInput> getRecipeCategoryConstructor() {
        return RecipeCategory::new;
    }

    public RecipeSerializerConstructor<Recipe.Serializer, Recipe, ItemsFluidsRecipeInput> getRecipeSerializerConstructor() {
        return Recipe.Serializer::new;
    }

    public Class<Recipe> getRecipeClass() {
        return Recipe.class;
    }
}
