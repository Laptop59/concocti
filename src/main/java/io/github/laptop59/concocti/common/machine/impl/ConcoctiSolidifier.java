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
import io.github.laptop59.concocti.common.menu.IconSlot;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiSolidifier extends ConcoctiMachineOnlyItemsFluids<
        ConcoctiSolidifier.BlockEntity,
        ConcoctiSolidifier.Menu,
        ConcoctiSolidifier.Recipe,
        ConcoctiSolidifier.Recipe.Serializer,
        ConcoctiSolidifier.Block,
        ConcoctiSolidifier.Screen,
        ConcoctiSolidifier.RecipeCategory
        > {
    static ConcoctiSolidifier INSTANCE;

    // Details
    public final static String ID = "concocti_solidifier";

    // Slots
    private static final int MOLD_SLOT = 2;
    private static final int BASE_ITEM_SLOT = 3;
    private static final int OUTPUT_SLOT = 4;

    public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, ItemsFluidsInputValue, ItemsFluidsRecipeInput, Recipe>> getDetails() {
        return () -> new ConcoctiMachineDetails<>(
                BlockEntity.class,
                10_000,
                10_000,
                5,
                20.0f,
                DynamicEnergyStorage.Mode.INPUT_ONLY.toSupplier(),
                INSTANCE.RECIPE_TYPE,
                Component.translatable("block.concocti.concocti_solidifier"),
                List.of(
                        SlotType.FLUID_INPUT,
                        SlotType.MOLD_ITEM_INPUT,
                        SlotType.BASE_ITEM_INPUT,
                        SlotType.ITEM_OUTPUT
                ),
                Menu::new,
                blockEntity -> blockEntity.dataAccess,
                new EnumMap<>(
                        Map.of(
                                SlotType.ITEM_OUTPUT, List.of(OUTPUT_SLOT),
                                SlotType.MOLD_ITEM_INPUT, List.of(MOLD_SLOT),
                                SlotType.BASE_ITEM_INPUT, List.of(BASE_ITEM_SLOT)
                        )
                ),
                new EnumMap<>(
                        Map.of(
                                SlotType.FLUID_INPUT, List.of(blockEntity -> blockEntity.fluidInput.get())
                        )
                ),
                InputOutput.of(
                        blockEntity -> List.of(
                                MOLD_SLOT,
                                BASE_ITEM_SLOT
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

    public ConcoctiSolidifier() {
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
        protected ConcoctiSolidifier getMachineInstance() {
            return INSTANCE;
        }

        public BlockEntity(BlockPos pos, BlockState blockState) {
            super(
                    INSTANCE.BLOCK_ENTITY, pos, blockState
            );
        }

        @Override
        protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
            return slot == MOLD_SLOT || slot == BASE_ITEM_SLOT;
        }

        @Override
        public boolean canProcess() {
            if (!super.canProcess()) return false;
            Recipe recipe = getRecipe(getInput());
            // Check whether the resulting item can be placed in the tank.
            ItemStack output = getItem(OUTPUT_SLOT);
            if (!output.isEmpty()) {
                return output.getCount() + recipe.getOutputItem().getCount() <= output.getMaxStackSize();
            }
            return true;
        }

        @Override
        protected void onRecipeCompleted(Recipe recipe) {
            // Consume a base item.
            if (recipe.getBaseItem().isPresent())
                recipe.getBaseItem().get().consume(getItem(BASE_ITEM_SLOT));
            if (recipe.getMold() != null) {
                recipe.getMold().consume(getItem(MOLD_SLOT));
            }
            // Reduce the fluids.
            recipe.getInputFluid().consume(fluidInput.get());
            // Give a solidified item.
            ItemStack output = getItem(OUTPUT_SLOT);
            if (output.isEmpty()) {
                setItem(OUTPUT_SLOT, recipe.getOutputItem().copy());
            } else {
                output.setCount(output.getCount() + recipe.getOutputItem().getCount());
            }
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
        protected ConcoctiSolidifier getMachineInstance() {
            return INSTANCE;
        }

        @Override
        protected Function<Properties, Block> getBlockConstructor() {
            return Block::new;
        }
    }

    public static class Recipe implements ProcessingRecipe<Recipe, ItemsFluidsRecipeInput> {
        private final ItemRecipeIngredient mold;
        private final Optional<ItemRecipeIngredient> baseItem;
        private final FluidRecipeIngredient inputFluid;
        private final ItemStack outputItem;
        private final int ticks;

        private static final HashMap<Recipe, ResourceLocation> idMap = new HashMap<>();

        static ResourceLocation makeResourceLocation(Recipe recipe) {
            return makeResourceLocation(recipe.baseItem, recipe.mold, recipe.inputFluid);
        }

        static ResourceLocation makeResourceLocation(Optional<ItemRecipeIngredient> baseItem, ItemRecipeIngredient mold, FluidRecipeIngredient inputFluid) {
            ResourceLocation fluidLoc = ResourceLocation.fromNamespaceAndPath(MODID, String.format("%08x", inputFluid.hashCode()));
            fluidLoc = fluidLoc.withPrefix("solidifying/").withSuffix("_with_" + String.format("%08x", mold.hashCode()));
            if (baseItem.isPresent()) {
                fluidLoc = fluidLoc.withSuffix("_on_" + String.format("%08x", baseItem.hashCode()));
            }
            return fluidLoc;
        }

        public Recipe(ResourceLocation id, Optional<ItemRecipeIngredient> baseItem, ItemRecipeIngredient mold, FluidRecipeIngredient inputFluid, ItemStack outputItem, int ticks) {
            this.mold = mold;
            this.baseItem = baseItem;
            this.inputFluid = inputFluid;
            this.outputItem = outputItem;
            this.ticks = ticks;
            idMap.put(this, id);
        }

        public Recipe(Optional<ItemRecipeIngredient> baseItem, ItemRecipeIngredient mold, FluidRecipeIngredient inputFluid, ItemStack outputItem, int ticks) {
            this.mold = mold;
            this.baseItem = baseItem;
            this.inputFluid = inputFluid;
            this.outputItem = outputItem;
            this.ticks = ticks;
            idMap.put(this, makeResourceLocation(this));
        }

        static String getId(Ingredient ingredient) {
            Ingredient.Value[] values = ingredient.getValues();
            if (values[0] instanceof Ingredient.TagValue(TagKey<Item> tag)) {
                return tag.location().getPath().replace('/', '_');
            } else {
                ItemStack firstStack = Arrays.stream(ingredient.getItems()).findFirst().orElseThrow();
                return BuiltInRegistries.ITEM.getKey(firstStack.getItem()).getPath();
            }
        }

        static String getId(ItemStack stack) {
            return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        }

        public ItemRecipeIngredient getMold() {
            return mold;
        }

        public FluidRecipeIngredient getInputFluid() {
            return inputFluid;
        }

        public Optional<ItemRecipeIngredient> getBaseItem() {
            return baseItem;
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
            return this.mold.test(input.getItem(MOLD_SLOT)) &&
                (this.baseItem.isEmpty() || this.baseItem.get().test(input.getItem(BASE_ITEM_SLOT))) &&
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

        @Override
        public ResourceLocation getId() {
            return idMap.get(this);
        }

        public static class Builder implements RecipeBuilder {
            protected ItemRecipeIngredient mold;
            protected Optional<ItemRecipeIngredient> baseItem;
            protected FluidRecipeIngredient inputFluid;
            protected ItemStack outputItem;
            protected int ticks;

            public Builder(Optional<ItemRecipeIngredient> baseItem, ItemRecipeIngredient mold, FluidRecipeIngredient inputFluid, ItemStack outputItem, int ticks) {
                this.mold = mold;
                this.baseItem = baseItem;
                this.inputFluid = inputFluid;
                this.outputItem = outputItem;
                this.ticks = ticks;
            }

            @Override
            public @NotNull RecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
                return this;
            }

            @Override
            public @NotNull ConcoctiSolidifier.Recipe.Builder group(@org.jetbrains.annotations.Nullable String group) {
                return this; // No recipe book groups required.
            }

            // Vanilla wants an Item here, not an ItemStack. You still can and should use the ItemStack
            // for serializing the recipes.
            @Override
            public @NotNull Item getResult() {
                return outputItem.getItem();
            }

            @Override
            public void save(@NotNull RecipeOutput recipeOutput) {
                this.save(recipeOutput, makeResourceLocation(baseItem, mold, inputFluid));
            }

            @Override
            public void save(@NotNull RecipeOutput recipeOutput, @NotNull String id) {
                ResourceLocation resourceLocation = makeResourceLocation(baseItem, mold, inputFluid);
                ResourceLocation idLocation = ResourceLocation.parse(id);
                if (ResourceLocation.parse(id).equals(resourceLocation)) {
                    throw new IllegalStateException("Recipe " + id + " should remove its 'save' argument as it is equal to default one");
                } else {
                    this.save(recipeOutput, idLocation);
                }
            }

            @Override
            public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
                Recipe recipe = new Recipe(id, this.baseItem, this.mold, this.inputFluid, this.outputItem, this.ticks);
                recipeOutput.accept(id, recipe, null);
            }
        }

        public static class Serializer implements RecipeSerializer<Recipe> {
            public static final MapCodec<Recipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.optionalField("base_item", ItemRecipeIngredient.CODEC, false).forGetter(Recipe::getBaseItem),
                    ItemRecipeIngredient.CODEC.fieldOf("mold").forGetter(Recipe::getMold),
                    FluidRecipeIngredient.CODEC.fieldOf("input_fluid").forGetter(Recipe::getInputFluid),
                    ItemStack.CODEC.fieldOf("output_item").forGetter(Recipe::getOutputItem),
                    Codec.INT.fieldOf("ticks").forGetter(Recipe::getTicks)
            ).apply(inst, Recipe::new));

            public static final StreamCodec<RegistryFriendlyByteBuf, Recipe> STREAM_CODEC =
                    StreamCodec.composite(
                            ItemRecipeIngredient.STREAM_CODEC.apply(ByteBufCodecs::optional), Recipe::getBaseItem,
                            ItemRecipeIngredient.STREAM_CODEC, Recipe::getMold,
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
                int containerId, Inventory playerInventory
        ) {
            super(containerId, playerInventory, 5, INSTANCE.MENU);
        }

        // Server
        public Menu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
            super(containerId, playerInventory, container, data, INSTANCE.MENU);
        }

        @Override
        protected void addOtherSlots() {
            // Mold tank
            this.addSlot(new IconSlot.Generic(container, 2, 88, 60, IconSlot.Icon.MOLD));
            // Base item tank
            this.addSlot(new Slot(container, 3, 55, 37));
            // Output tank
            this.addSlot(new ResultSlot(null, container, 4, 120, 37));
        }

        @Override
        public @org.jetbrains.annotations.Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
            // index 3 - base tank
            if (this.moveItemStackTo(movedStack, 2, 4, true))
                return ItemStack.EMPTY;
            else
                return null;
        }

        public FluidStack getInputFluidStack() {
            return viewer.get(Properties.FLUID_INPUT);
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
                    energyBar,
                    fluid
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

            energyBar.update(menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true));
            arrowProgress.update(menu.getProgress());

            // Render the fluids.
            fluid.update(menu.getInputFluidStack(), menu.getMaxFluidLeft());

            renderChildrenAbsolute(guiGraphics, renderInfo, this.getUniqueChildren());
        }
    }

    public static class RecipeCategory extends AbstractConcoctiRecipeCategory<Recipe> {

        @Override
        protected ConcoctiSolidifier getMachineInstance() {
            return INSTANCE;
        }

        @Override
        public @NotNull Object getJeiRecipeType() {
            return INSTANCE.getJeiRecipeType();
        }

        @Override
        public @NotNull Component getTitle() {
            return Component.translatable("block.concocti.concocti_solidifier");
        }

        @Override
        protected int getHorizontalArrowOffset(@NotNull ConcoctiSolidifier.Recipe recipe) {
            return 16;
        }

        @Override
        public void set(@NotNull io.github.laptop59.concocti.common.machine.RecipeBuilder builder, @NotNull Recipe recipe) {
            // Add the recipe inputs (fluid + tank + base item).
            builder.addInputSlot(12, 6, recipe.getInputFluid());
            builder.addInputSlot(30, 6, recipe.getMold());
            builder.addInputSlot(48, 6, recipe.getBaseItem().orElse(null));
            builder.addOutputSlot(137 - 9, 6, recipe.getOutputItem());
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
