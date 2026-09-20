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
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineOnlyItemsFluidsBlockEntity;
import io.github.laptop59.concocti.common.block.entity.DynamicEnergyStorage;
import io.github.laptop59.concocti.common.block.entity.FluidHandlerBlockEntity;
import io.github.laptop59.concocti.common.detail.DetailCodec;
import io.github.laptop59.concocti.common.detail.DetailHolder;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetails;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineOnlyItemsFluids;
import io.github.laptop59.concocti.common.machine.InputOutput;
import io.github.laptop59.concocti.common.machine.ItemsFluidsInputValue;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.menu.ResultSlot;
import io.github.laptop59.concocti.common.recipe.FluidRecipeIngredient;
import io.github.laptop59.concocti.common.recipe.ItemRecipeIngredient;
import io.github.laptop59.concocti.common.recipe.ItemsFluidsRecipeInput;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.common.machine.AbstractConcoctiRecipeCategory;
import net.minecraft.advancements.Criterion;
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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConcoctiCrystallizer extends ConcoctiMachineOnlyItemsFluids<
        ConcoctiCrystallizer.BlockEntity,
        ConcoctiCrystallizer.Menu,
        ConcoctiCrystallizer.Recipe,
        ConcoctiCrystallizer.Recipe.Serializer,
        ConcoctiCrystallizer.Block,
        ConcoctiCrystallizer.Screen,
        ConcoctiCrystallizer.RecipeCategory
        > {
    static ConcoctiCrystallizer INSTANCE;

    // Details
    public final static String ID = "concocti_crystallizer";

    // Slots
    private static final int SEED_CRYSTAL_INPUT_SLOT = 2;
    private static final int OUTPUT_SLOT = 3;

    // Fluids
    private static final int FLUID_INPUT = 0;

    public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, ItemsFluidsInputValue, ItemsFluidsRecipeInput, Recipe>> getDetails() {
        return () -> new ConcoctiMachineDetails<>(
                BlockEntity.class,
                1_000,
                1_000,
                4,
                5.0f,
                DynamicEnergyStorage.Mode.INPUT_ONLY.toSupplier(),
                INSTANCE.RECIPE_TYPE,
                Component.translatable("block.concocti.concocti_crystallizer"),
                List.of(
                        SlotType.FLUID_INPUT,
                        SlotType.SEED_CRYSTAL_ITEM_INPUT,
                        SlotType.ITEM_OUTPUT
                ),
                Menu::new,
                blockEntity -> blockEntity.dataAccess,
                new EnumMap<>(
                        Map.of(
                                SlotType.ITEM_OUTPUT, List.of(OUTPUT_SLOT),
                                SlotType.SEED_CRYSTAL_ITEM_INPUT, List.of(SEED_CRYSTAL_INPUT_SLOT)
                        )
                ),
                new EnumMap<>(
                        Map.of(
                                SlotType.FLUID_INPUT, List.of(blockEntity -> blockEntity.fluidInput.get())
                        )
                ),
                InputOutput.of(
                        blockEntity -> List.of(
                                SEED_CRYSTAL_INPUT_SLOT
                        ),
                        blockEntity -> List.of(
                                OUTPUT_SLOT
                        )
                ),
                InputOutput.onlyInputs(
                        blockEntity -> List.of(
                                blockEntity.fluidInput.get()
                        )
                ),
                null
        );
    }

    public ConcoctiCrystallizer() {
        super(
                ID,
                BlockBehaviour.Properties
                        .of()
                        .mapColor(DyeColor.MAGENTA)
                        .requiresCorrectToolForDrops()
                        .explosionResistance(1.5f)
                        .strength(2f)
                        .lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 9 : 0),
                new ConcoctiBlocks.BlockData(ConcoctiBlocks.BlockToolRank.STONE, ConcoctiBlocks.BlockToolType.PICKAXE)
        );
        INSTANCE = this;
    }

    public static class BlockEntity extends AbstractConcoctiMachineOnlyItemsFluidsBlockEntity
            <BlockEntity, Menu, Recipe>
            implements FluidHandlerBlockEntity {

        private final DetailHolder<FluidTank> fluidInput = new DetailHolder<>(
                DetailCodec.FLUID_TANK, "fluid_input", new FluidTank(TANK_CAPACITY), this
        );

        // Properties
        public final Property<FluidStack> FLUID_INPUT = Properties.FLUID_INPUT.newWithLinker(() -> fluidInput.get().getFluid());

        protected final ConcoctiMachineComplexion dataAccess = new ConcoctiMachineComplexion(
                this,
                FLUID_INPUT.of(FluidStack.EMPTY)
        );

        @Override
        protected ConcoctiCrystallizer getMachineInstance() {
            return INSTANCE;
        }

        public BlockEntity(BlockPos pos, BlockState blockState) {
            super(
                    INSTANCE.BLOCK_ENTITY, pos, blockState
            );
        }

        @Override
        protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
            return true;
        }

        @Override
        public boolean canProcess() {
            if (!super.canProcess()) return false;
            RecipeHolder<Recipe> recipeHolder = getRecipe(getInput());
            if (recipeHolder == null) return false;
            // Check whether the resulting item can be placed in the tank.
            ItemStack output = getItem(OUTPUT_SLOT);
            if (!output.isEmpty()) {
                return output.getCount() + recipeHolder.value().getOutputItem().getCount() <= output.getMaxStackSize();
            }
            return true;
        }

        @Override
        protected void onRecipeCompleted(Recipe recipe) {
            ItemStack seed = getItem(SEED_CRYSTAL_INPUT_SLOT);
            ItemStack output = getItem(OUTPUT_SLOT);
            if (output.isEmpty()) {
                setItem(OUTPUT_SLOT, recipe.getOutputItem().copy());
            } else {
                output.setCount(output.getCount() + recipe.getOutputItem().getCount());
            }
            // Consume the seed.
            recipe.getSeedCrystal().consume(seed);
            // Reduce the fluids.
            recipe.getInputFluid().consume(fluidInput.get());
        }

        @Override
        public List<IFluidHandler> getIndexedFluidHandlers() {
            return List.of(fluidInput.get());
        }

        @Override
        public List<IFluidTank> getFluidTanks() {
            return List.of(fluidInput.get());
        }
    }

    public static class Block extends AbstractConcoctiMachineBlock<Block> {
        protected Block(Properties properties) {
            super(properties);
        }

        @Override
        protected ConcoctiCrystallizer getMachineInstance() {
            return INSTANCE;
        }

        @Override
        protected Function<Properties, Block> getBlockConstructor() {
            return Block::new;
        }
    }

    public static class Recipe implements ProcessingRecipe<Recipe, ItemsFluidsRecipeInput> {
        private final ItemRecipeIngredient seedCrystal;
        private final FluidRecipeIngredient inputFluid;
        private final ItemStack outputItem;
        private final int ticks;

        public Recipe(ItemRecipeIngredient seedCrystal, FluidRecipeIngredient inputFluid, ItemStack outputItem, int ticks) {
            this.seedCrystal = seedCrystal;
            this.inputFluid = inputFluid;
            this.outputItem = outputItem;
            this.ticks = ticks;
        }

        public ItemRecipeIngredient getSeedCrystal() {
            return seedCrystal;
        }

        public FluidRecipeIngredient getInputFluid() {
            return inputFluid;
        }

        public ItemStack getOutputItem() {
            return outputItem;
        }

        @Override
        public @NotNull NonNullList<Ingredient> getIngredients() {
            return NonNullList.create();
        }

        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return width * height >= 1;
        }

        @Override
        public boolean matches(ItemsFluidsRecipeInput input, @NotNull Level level) {
            return this.seedCrystal.test(input.getItem(SEED_CRYSTAL_INPUT_SLOT)) &&
                    this.inputFluid.test(input.getFluid(0));
        }

        @Override
        public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
            return outputItem;
        }

        @Override
        public @NotNull ItemStack assemble(@NotNull ItemsFluidsRecipeInput input, HolderLookup.@NotNull Provider registries) {
            return this.outputItem.copy();
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

        public static class Builder implements RecipeBuilder {
            protected ItemRecipeIngredient seedCrystal;
            protected FluidRecipeIngredient inputFluid;
            protected ItemStack outputItem;
            protected int ticks;

            public Builder(ItemRecipeIngredient seedCrystal, FluidRecipeIngredient inputFluid, ItemStack outputItem, int ticks) {
                this.seedCrystal = seedCrystal;
                this.inputFluid = inputFluid;
                this.outputItem = outputItem;
                this.ticks = ticks;
            }

            @Override
            public @NotNull RecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
                return this;
            }

            @Override
            public @NotNull ConcoctiCrystallizer.Recipe.Builder group(@org.jetbrains.annotations.Nullable String group) {
                return this; // No recipe book groups required.
            }

            // Vanilla wants an Item here, not an ItemStack. You still can and should use the ItemStack
            // for serializing the recipes.
            @Override
            public @NotNull Item getResult() {
                return outputItem.getItem();
            }

            @Override
            public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
                Recipe recipe = new Recipe(this.seedCrystal, this.inputFluid, this.outputItem, this.ticks);
                recipeOutput.accept(id, recipe, null);
            }
        }

        public static class Serializer implements RecipeSerializer<Recipe> {
            public static final MapCodec<Recipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    ItemRecipeIngredient.CODEC.fieldOf("seed_crystal").forGetter(Recipe::getSeedCrystal),
                    FluidRecipeIngredient.CODEC.fieldOf("input_fluid").forGetter(Recipe::getInputFluid),
                    ItemStack.CODEC.fieldOf("output_item").forGetter(Recipe::getOutputItem),
                    Codec.INT.fieldOf("ticks").forGetter(Recipe::getTicks)
            ).apply(inst, Recipe::new));

            public static final StreamCodec<RegistryFriendlyByteBuf, Recipe> STREAM_CODEC =
                    StreamCodec.composite(
                            ItemRecipeIngredient.STREAM_CODEC, Recipe::getSeedCrystal,
                            FluidRecipeIngredient.STREAM_CODEC, Recipe::getInputFluid,
                            ItemStack.STREAM_CODEC, Recipe::getOutputItem,
                            ByteBufCodecs.INT, Recipe::getTicks,
                            Recipe::new
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
    }

    public static class Menu extends AbstractConcoctiMachineMenu<Menu> {
        @Contract(pure = true)
        @Override
        public List<Property<?>> getMachineSpecificProperties() {
            return List.of(
                    Properties.FLUID_INPUT
            );
        }

        // Client
        public Menu(
                int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf
        ) {
            super(containerId, playerInventory, 4, buf, INSTANCE.MENU);
        }

        // Server
        public Menu(int containerId, Inventory playerInventory, Container container) {
            super(containerId, playerInventory, container, INSTANCE.MENU);
        }

        @Override
        protected void addOtherSlots() {
            // Mold tank
            this.addSlot(new Slot(container, 2, 58, 37));
            // Output tank
            this.addSlot(new ResultSlot(null, container, 3, 120, 37));
        }

        @Override
        public @org.jetbrains.annotations.Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
            if (this.moveItemStackTo(movedStack, 2, 3, true))
                return ItemStack.EMPTY;
            else
                return null;
        }

        public FluidStack getInputFluidStack() {
            return syncedFluids.get(FLUID_INPUT);
        }

        public int getMaxFluidLeft() {
            return BlockEntity.TANK_CAPACITY;
        }
    }

    public static class Screen extends AbstractConcoctiMachineScreen<Menu> {

        private final FluidBars.Tall<Menu> fluid = new FluidBars.Tall<>(30, 28, this, menu, 0);

        private final EnergyBar<Menu> energyBar = new EnergyBar<>(10, 18, this, menu);
        private final ArrowProgress arrowProgress = new ArrowProgress(85, 36);

        public Screen(Menu menu, Inventory playerInventory, Component title) {
            super(menu, playerInventory, title);
        }

        public List<Renderable> getUniqueChildren() {
            return List.of(
                    arrowProgress,
                    fluid,
                    energyBar
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

            energyBar.update(menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true));
            arrowProgress.update(menu.getProgress());

            // Render the fluids.
            fluid.update(menu.getInputFluidStack(), menu.getMaxFluidLeft());

            renderChildrenAbsolute(guiGraphics, renderInfo, this.getUniqueChildren());
        }
    }

    public static class RecipeCategory extends AbstractConcoctiRecipeCategory<Recipe> {
        @Override
        protected ConcoctiCrystallizer getMachineInstance() {
            return INSTANCE;
        }

        @Override
        public @NotNull Object getJeiRecipeType() {
            return INSTANCE.getJeiRecipeType();
        }

        @Override
        public @NotNull Component getTitle() {
            return Component.translatable("block.concocti.concocti_crystallizer");
        }

        @Override
        protected int getHorizontalArrowOffset(@NotNull ConcoctiCrystallizer.Recipe recipe) {
            return 16;
        }

        @Override
        public void set(@NotNull io.github.laptop59.concocti.common.machine.RecipeBuilder builder, @NotNull Recipe recipe) {
            // Add the recipe inputs (fluid + tank + base item).
            builder.addInputSlot(30, 16, recipe.getInputFluid());
            builder.addInputSlot(48, 16, recipe.getSeedCrystal());
            // Add the item output.
            builder.addOutputSlot(137 - 9, 16, recipe.getOutputItem());
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
