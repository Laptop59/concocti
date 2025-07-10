package io.github.laptop59.concocti.common.machine;

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
import io.github.laptop59.concocti.common.block.entity.DynamicEnergyStorage;
import io.github.laptop59.concocti.common.block.entity.FluidHandlerBlockEntity;
import io.github.laptop59.concocti.common.menu.*;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.integration.jei.AbstractConcoctiRecipeCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.advancements.Criterion;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiSolidifier extends ConcoctiMachine <
        ConcoctiSolidifier.BlockEntity,
        ConcoctiSolidifier.Menu,
        ConcoctiSolidifier.BlockEntity.InputValue,
        ConcoctiSolidifier.Recipe.Input,
        ConcoctiSolidifier.Recipe,
        ConcoctiSolidifier.Recipe.Serializer,
        ConcoctiSolidifier.Block,
        ConcoctiSolidifier.Screen,
        ConcoctiSolidifier.RecipeCategory
>  {
    static ConcoctiSolidifier INSTANCE;

    // Details
    public final static String ID = "concocti_solidifier";

    // Slots
    private static final int MOLD_SLOT = 2;
    private static final int BASE_ITEM_SLOT = 3;
    private static final int OUTPUT_SLOT = 4;

    public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, BlockEntity.InputValue, Recipe.Input, Recipe>> getDetails() {
        return () -> new ConcoctiMachineDetails<BlockEntity, Menu, BlockEntity.InputValue, Recipe.Input, Recipe>(
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
                Menu.class,
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
                                SlotType.FLUID_INPUT, List.of(blockEntity -> blockEntity.tank)
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
                                blockEntity.tank
                        )
                )
        );
    }

    ConcoctiSolidifier() {
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

    public BlockEntityConstructor<BlockEntity> getBlockEntityConstructor() {
        return BlockEntity::new;
    }

    public BlockConstructor<Block> getBlockConstructor() {
        return Block::new;
    }

    public MenuConstructor<Menu> getMenuConstructor() {
        return Menu::new;
    }

    public ScreenConstructor<Menu, Screen> getScreenConstructor() {
        return Screen::new;
    }

    public RecipeCategoryConstructor<RecipeCategory, Recipe, ConcoctiSolidifier.Recipe.Input> getRecipeCategoryConstructor() {
        return RecipeCategory::new;
    }

    public RecipeSerializerConstructor<Recipe.Serializer, Recipe, ConcoctiSolidifier.Recipe.Input> getRecipeSerializerConstructor() {
        return Recipe.Serializer::new;
    }

    public Class<Recipe> getRecipeClass() {
        return Recipe.class;
    }

    public static class BlockEntity extends AbstractConcoctiMachineBlockEntity
            <BlockEntity, Menu, BlockEntity.InputValue,
                    Recipe.Input, Recipe>
        implements FluidHandlerBlockEntity {

        public record InputValue(ItemStack mold, FluidStack fluid, ItemStack baseItem) {}

        public final FluidTank tank = new FluidTank(TANK_CAPACITY);

        // Properties
        public final Property<FluidStack> FLUID_INPUT = Properties.FLUID_INPUT.newWithLinker(tank::getFluid);

        protected final ConcoctiMachineComplexion dataAccess = new ConcoctiMachineComplexion(
                this,
                FLUID_INPUT.of(FluidStack.EMPTY)
        );

        public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, InputValue, Recipe.Input, Recipe>> getUncachedMachineDetails() {
            return INSTANCE.getDetails();
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
        protected Recipe.Input recipeInputFrom(InputValue input) {
            return new Recipe.Input(input.mold, input.baseItem, input.fluid);
        }

        @Override
        protected InputValue getInput() {
            return new InputValue(getItem(MOLD_SLOT), tank.getFluid(), getItem(BASE_ITEM_SLOT));
        }

        @Override
        protected ResourceLocation getRecipeIdFrom(InputValue input) {
            return Recipe.makeResourceLocation(input.baseItem, input.mold, input.fluid);
        }

        @Override
        public boolean canProcess() {
            if (!super.canProcess()) return false;
            Recipe recipe = getCurrentRecipe(getInput());
            // Check whether the resulting item can be placed in the slot.
            ItemStack output = getItem(OUTPUT_SLOT);
            if (!output.isEmpty()) {
                return output.getCount() + recipe.getOutputItem().getCount() <= output.getMaxStackSize();
            }
            return true;
        }

        @Override
        protected void onRecipeCompleted(Recipe recipe) {
            // Consume a base item.
            getItem(BASE_ITEM_SLOT).shrink(1);
            ItemStack mold = getItem(MOLD_SLOT);
            // Give a solidified item.
            ItemStack output = getItem(OUTPUT_SLOT);
            if (output.isEmpty()) {
                setItem(OUTPUT_SLOT, recipe.getOutputItem().copy());
            } else {
                output.setCount(output.getCount() + recipe.getOutputItem().getCount());
            }
            // Damage the mold.
            if (mold.isDamageableItem()) {
                mold.setDamageValue(mold.getDamageValue() + 1);
                if (mold.getDamageValue() >= mold.getMaxDamage()) mold.shrink(1);
            }
            // Reduce the fluids.
            tank.drain(recipe.getInputFluid().amount(), IFluidHandler.FluidAction.EXECUTE);
        }

        @Override
        public List<IFluidHandler> getIndexedFluidHandlers() {
            return List.of(tank);
        }

        @Override
        protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
            super.loadAdditional(tag, registries);
            tank.setFluid(parseFluidStack((CompoundTag) tag.get("fluid_input"), registries));
        }

        @Override
        protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
            super.saveAdditional(tag, registries);
            saveFluidStack("fluid_input", tag, tank, registries);
        }

        @Override
        public List<IFluidTank> getFluidTanks() {
            return List.of(
                    tank
            );
        }
    }

    public static class Block extends AbstractConcoctiMachineBlock {
        public static final BooleanProperty LIT = BlockStateProperties.LIT;
    
        public static final MapCodec<Block> CODEC = simpleCodec(Block::new);
    
        protected Block(Properties properties) {
            super(properties);
            this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.FALSE));
        }
    
        @Override
        public @NotNull MapCodec<Block> codec() {
            return CODEC;
        }
    
        // Return a new instance of our block entity here.
        @Override
        public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
            return new BlockEntity(pos, state);
        }
    
        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
            builder.add(FACING, LIT);
        }
    
        @Override
        protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
            return RenderShape.MODEL;
        }
    
        @Override
        protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            } else {
                MenuProvider provider = this.getMenuProvider(state, level, pos);
                if (provider != null) {
                    player.openMenu(provider);
                }
    
                return InteractionResult.CONSUME;
            }
        }
    
        @Override
        protected @NotNull ItemInteractionResult useItemOnMachine(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                                                  @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
    
        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }
    
        @Nullable
        protected static <T extends net.minecraft.world.level.block.entity.BlockEntity> BlockEntityTicker<T> createTicker(
                Level level, BlockEntityType<T> serverType, BlockEntityType<? extends BlockEntity> clientType
        ) {
            return level.isClientSide ? null : createTickerHelper(serverType, clientType, BlockEntity::serverTick);
        }
    
        public <T extends net.minecraft.world.level.block.entity.BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
            return createTicker(level, blockEntityType, INSTANCE.BLOCK_ENTITY.get());
        }
    }

    public static class Recipe implements ProcessingRecipe<Recipe, Recipe.Input> {
        private final Ingredient mold;
        private final Ingredient baseItem;
        private final SizedFluidIngredient inputFluid;
        private final ItemStack outputItem;
        private final int ticks;

        private static final HashMap<Recipe, ResourceLocation> idMap = new HashMap<>();

        static ResourceLocation makeResourceLocation(Recipe recipe) {
            return makeResourceLocation(recipe.baseItem, recipe.mold, recipe.inputFluid);
        }

        static ResourceLocation makeResourceLocation(Ingredient baseItem, Ingredient mold, SizedFluidIngredient inputFluid) {
            ResourceLocation fluidLoc = ResourceLocation.fromNamespaceAndPath(MODID, BuiltInRegistries.FLUID.getKey(
                    Arrays.stream(inputFluid.getFluids()).findFirst().orElseThrow().getFluid()).getPath());
            fluidLoc = fluidLoc.withPrefix("solidifying/").withSuffix("_with_" + getId(mold));
            if (!baseItem.hasNoItems()) {
                fluidLoc = fluidLoc.withSuffix("_on_" + getId(baseItem));
            }
            return fluidLoc;
        }

        public static ResourceLocation makeResourceLocation(ItemStack baseItem, ItemStack mold, FluidStack inputFluid) {
            ResourceLocation fluidLoc = BuiltInRegistries.FLUID.getKey(inputFluid.getFluid());
            fluidLoc = fluidLoc.withSuffix("_" + getId(mold));
            if (!baseItem.isEmpty()) {
                fluidLoc = fluidLoc.withSuffix(getId(baseItem));
            }
            return fluidLoc;
        }

        public Recipe(ResourceLocation id, Ingredient baseItem, Ingredient mold, SizedFluidIngredient inputFluid, ItemStack outputItem, int ticks) {
            this.mold = mold;
            this.baseItem = baseItem;
            this.inputFluid = inputFluid;
            this.outputItem = outputItem;
            this.ticks = ticks;
            idMap.put(this, id);
        }

        public Recipe(Ingredient baseItem, Ingredient mold, SizedFluidIngredient inputFluid, ItemStack outputItem, int ticks) {
            this.mold = mold;
            this.baseItem = baseItem;
            this.inputFluid = inputFluid;
            this.outputItem = outputItem;
            this.ticks = ticks;
            idMap.put(this, makeResourceLocation(this));
        }

        static String getId(Ingredient ingredient) {
            Ingredient.Value[] values = ingredient.getValues();
            if (values[0] instanceof Ingredient.TagValue(net.minecraft.tags.TagKey<Item> tag)) {
                return tag.location().getPath().replace('/', '_');
            } else {
                ItemStack firstStack = Arrays.stream(ingredient.getItems()).findFirst().orElseThrow();
                return BuiltInRegistries.ITEM.getKey(firstStack.getItem()).getPath();
            }
        }

        static String getId(ItemStack stack) {
            return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        }

        public Ingredient getMold() {
            return mold;
        }

        public SizedFluidIngredient getInputFluid() {
            return inputFluid;
        }

        public Ingredient getBaseItem() {
            return baseItem;
        }

        public ItemStack getOutputItem() {
            return outputItem;
        }

        @Override
        public @NotNull NonNullList<Ingredient> getIngredients() {
            NonNullList<Ingredient> list = NonNullList.create();
            list.add(this.mold);
            if (!this.baseItem.isEmpty()) list.add(this.baseItem);
            return list;
        }

        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return width * height >= 1;
        }

        @Override
        public boolean matches(Input input, @NotNull Level level) {
            return this.mold.test(input.mold()) && this.baseItem.test(input.base())
                    && this.inputFluid.test(input.inputFluid);
        }

        @Override
        public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
            return outputItem;
        }

        @Override
        public @NotNull ItemStack assemble(@NotNull ConcoctiSolidifier.Recipe.Input input, HolderLookup.@NotNull Provider registries) {
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

        public record Input(ItemStack mold, ItemStack base, FluidStack inputFluid) implements RecipeInput {
            @Override
            public @NotNull ItemStack getItem(int index) {
                return switch (index) {
                    case 0 -> this.mold;
                    case 1 -> this.base;
                    default -> throw new IllegalArgumentException("Recipe does not contain item slot " + index);
                };
            }

            @Override
            public int size() {
                return 2;
            }

            @Override
            public boolean isEmpty() {
                return this.base.isEmpty() && this.mold.isEmpty();
            }
        }

        public static class Builder implements RecipeBuilder {
            protected Ingredient mold;
            protected Ingredient baseItem;
            protected SizedFluidIngredient inputFluid;
            protected ItemStack outputItem;
            protected int ticks;

            public Builder(Ingredient baseItem, Ingredient mold, SizedFluidIngredient inputFluid, ItemStack outputItem, int ticks) {
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
                    Ingredient.CODEC.fieldOf("base_item").forGetter(Recipe::getBaseItem),
                    Ingredient.CODEC.fieldOf("mold").forGetter(Recipe::getMold),
                    SizedFluidIngredient.FLAT_CODEC.fieldOf("input_fluid").forGetter(Recipe::getInputFluid),
                    ItemStack.CODEC.fieldOf("output_item").forGetter(Recipe::getOutputItem),
                    Codec.INT.fieldOf("ticks").forGetter(Recipe::getTicks)
            ).apply(inst, Recipe::new));

            public static final StreamCodec<RegistryFriendlyByteBuf, Recipe> STREAM_CODEC =
                    StreamCodec.composite(
                            Ingredient.CONTENTS_STREAM_CODEC, Recipe::getBaseItem,
                            Ingredient.CONTENTS_STREAM_CODEC, Recipe::getMold,
                            SizedFluidIngredient.STREAM_CODEC, Recipe::getInputFluid,
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
            // Mold slot
            this.addSlot(new IconSlot.Generic(container, 2, 88, 60, IconSlot.Icon.MOLD));
            // Base item slot
            this.addSlot(new Slot(container, 3, 55, 37));
            // Output slot
            this.addSlot(new ResultSlot(null, container, 4, 120, 37));
        }

        @Override
        public @org.jetbrains.annotations.Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
            // index 2 - mold slot
            if (!this.getSlot(2).hasItem() && !this.moveItemStackTo(movedStack, 2, 3, true)) {
                // index 3 - base slot
                if (!this.getSlot(3).hasItem() && !this.moveItemStackTo(movedStack, 3, 4, true)) {
                    return ItemStack.EMPTY;
                }
            }
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

        private final FluidBar<Menu> fluid = new FluidBar<>(30, 28, this, menu, 0);

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

        private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/jei/concocti_solidifier.png");

        public RecipeCategory(IGuiHelper guiHelper) {
            super(guiHelper, new ItemStack(INSTANCE.BLOCK.get()));
        }

        @Override
        public @NotNull mezz.jei.api.recipe.RecipeType<Recipe> getRecipeType() {
            return INSTANCE.JEI_RECIPE_TYPE;
        }

        @Override
        public @NotNull Component getTitle() {
            return Component.translatable("block.concocti.concocti_solidifier");
        }

        @Override
        protected ResourceLocation getTexture() {
            return texture;
        }

        @Override
        protected int getHorizontalArrowOffset(@NotNull ConcoctiSolidifier.Recipe recipe) { return 16; }

        @Override
        public void setRecipe(@NotNull IRecipeLayoutBuilder builder, Recipe recipe, @NotNull IFocusGroup focuses) {
            // Add the recipe inputs (fluid + slot + base item).
            addSizedFluidIngredientSlot(builder, RecipeIngredientRole.INPUT, 12, 6, "input_fluid", recipe.getInputFluid());
            builder.addSlot(RecipeIngredientRole.CATALYST, 30, 6)
                    .addItemStacks(Arrays.asList(recipe.getMold().getItems()))
                    .setSlotName("mold");
            builder.addSlot(RecipeIngredientRole.INPUT, 48, 6)
                    .addItemStacks(Arrays.asList(recipe.getBaseItem().getItems()))
                    .setSlotName("base_item");
            // Add the item output.
            builder.addSlot(RecipeIngredientRole.OUTPUT, 137 - 9, 6)
                    .addIngredient(VanillaTypes.ITEM_STACK, recipe.getOutputItem())
                    .setSlotName("output");
        }
    }
}
