package io.github.laptop59.concocti.common.machine;

import com.google.common.primitives.UnsignedLong;
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
import io.github.laptop59.concocti.common.block.entity.DynamicEnergyStorage;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.menu.ResultSlot;
import io.github.laptop59.concocti.common.recipe.ItemsFluidsRecipeInput;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.integration.jei.AbstractConcoctiRecipeCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.advancements.Criterion;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiMixer extends ConcoctiMachine <
        ConcoctiMixer.BlockEntity,
        ConcoctiMixer.Menu,
        ConcoctiMixer.BlockEntity.InputValue,
        ItemsFluidsRecipeInput,
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

    public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, BlockEntity.InputValue, ItemsFluidsRecipeInput, Recipe>> getDetails() {
        return () -> new ConcoctiMachineDetails<BlockEntity, Menu, BlockEntity.InputValue, ItemsFluidsRecipeInput, Recipe>(
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
                        SlotType.ALL_FLUID_INPUTS
                ),
                Menu.class,
                blockEntity -> blockEntity.dataAccess,
                new EnumMap<>(
                        Map.of(
                                SlotType.ITEM_INPUT_1, List.of(INPUT_SLOT_1),
                                SlotType.ITEM_INPUT_2, List.of(INPUT_SLOT_2),
                                SlotType.ITEM_INPUT_3, List.of(INPUT_SLOT_3),
                                SlotType.ITEM_INPUT_4, List.of(INPUT_SLOT_4),
                                SlotType.ALL_ITEM_INPUTS, List.of(INPUT_SLOT_1, INPUT_SLOT_2, INPUT_SLOT_3, INPUT_SLOT_4),
                                SlotType.ITEM_OUTPUT, List.of(OUTPUT_SLOT)
                        )
                ),
                new EnumMap<>(
                        Map.of(
                                SlotType.FLUID_INPUT_1, List.of(blockEntity -> blockEntity.fluidInput1),
                                SlotType.FLUID_INPUT_2, List.of(blockEntity -> blockEntity.fluidInput2),
                                SlotType.FLUID_INPUT_3, List.of(blockEntity -> blockEntity.fluidInput3),
                                SlotType.FLUID_INPUT_4, List.of(blockEntity -> blockEntity.fluidInput4),
                                SlotType.ALL_FLUID_INPUTS, List.of(
                                        blockEntity -> blockEntity.fluidInput1,
                                        blockEntity -> blockEntity.fluidInput2,
                                        blockEntity -> blockEntity.fluidInput3,
                                        blockEntity -> blockEntity.fluidInput4
                                ),
                                SlotType.FLUID_OUTPUT, List.of(blockEntity -> blockEntity.fluidOutput)
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
                                blockEntity.fluidInput1,
                                blockEntity.fluidInput2,
                                blockEntity.fluidInput3,
                                blockEntity.fluidInput4
                        ),
                        blockEntity -> List.of(
                                blockEntity.fluidOutput
                        )
                )
        );
    }

    ConcoctiMixer() {
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

    public RecipeCategoryConstructor<RecipeCategory, Recipe, ItemsFluidsRecipeInput> getRecipeCategoryConstructor() {
        return RecipeCategory::new;
    }

    public RecipeSerializerConstructor<Recipe.Serializer, Recipe, ItemsFluidsRecipeInput> getRecipeSerializerConstructor() {
        return Recipe.Serializer::new;
    }

    public Class<Recipe> getRecipeClass() {
        return Recipe.class;
    }

    public static class BlockEntity extends AbstractConcoctiMachineBlockEntity
            <BlockEntity, Menu, BlockEntity.InputValue, ItemsFluidsRecipeInput, Recipe> {

        public record InputValue(
                IItemHandler itemHandler,
                IFluidHandler fluidHandler
        ) {}

        // Tanks
        private final FluidTank fluidInput1 = new FluidTank(TANK_CAPACITY);
        private final FluidTank fluidInput2 = new FluidTank(TANK_CAPACITY);
        private final FluidTank fluidInput3 = new FluidTank(TANK_CAPACITY);
        private final FluidTank fluidInput4 = new FluidTank(TANK_CAPACITY);
        private final FluidTank fluidOutput = new FluidTank(TANK_CAPACITY * 2);

        private final IItemHandler inputItemHandler = itemHandler.whitelistWrapper(
                INPUT_SLOT_1, INPUT_SLOT_2, INPUT_SLOT_3, INPUT_SLOT_4
        );

        private final IFluidHandler inputFluidHandler = fluidHandler.whitelistTanks(
                fluidInput1, fluidInput2, fluidInput3, fluidInput4
        );

        // Properties
        public final Property<FluidStack> FLUID_INPUT_1 = Properties.FLUID_INPUT_1.newWithLinker(fluidInput1::getFluid);
        public final Property<FluidStack> FLUID_INPUT_2 = Properties.FLUID_INPUT_2.newWithLinker(fluidInput2::getFluid);
        public final Property<FluidStack> FLUID_INPUT_3 = Properties.FLUID_INPUT_3.newWithLinker(fluidInput3::getFluid);
        public final Property<FluidStack> FLUID_INPUT_4 = Properties.FLUID_INPUT_4.newWithLinker(fluidInput4::getFluid);
        public final Property<FluidStack> FLUID_OUTPUT = Properties.FLUID_OUTPUT.newWithLinker(fluidOutput::getFluid);

        @Override
        public List<IFluidHandler> getIndexedFluidHandlers() {
            return List.of(fluidInput1, fluidInput2, fluidInput3, fluidInput4, fluidOutput);
        }

        public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, InputValue, ItemsFluidsRecipeInput, Recipe>> getUncachedMachineDetails() {
            return INSTANCE.getDetails();
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
        protected InputValue getInput() {
            return new InputValue(
                    inputItemHandler,
                    inputFluidHandler
            );
        }

        @Override
        public boolean canProcess() {
            if (!super.canProcess()) return false;
            Recipe recipe = getCurrentRecipe(getInput());
            if (!recipe.matches(new ItemsFluidsRecipeInput(inputItemHandler, inputFluidHandler))) return false;
            // Check whether the fluids obtained from this item will not exceed our fluid limit.
            ItemStack outputSlotItems = getItem(OUTPUT_SLOT);
            ItemStack resultItems = recipe.getOutputItem();
            if (!outputSlotItems.isEmpty() &&
                    resultItems != null &&
                    outputSlotItems.getCount() + resultItems.getCount() > outputSlotItems.getMaxStackSize())
                return false;
            FluidStack resultFluid = recipe.getOutputFluid();
            if (resultFluid != null && fluidHandler.fill(resultFluid, IFluidHandler.FluidAction.SIMULATE) != resultFluid.getAmount())
                return false;
            return true;
        }

        @Override
        protected ItemsFluidsRecipeInput recipeInputFrom(InputValue input) {
            return new ItemsFluidsRecipeInput(input.itemHandler, input.fluidHandler);
        }

        @Override
        protected ResourceLocation getRecipeIdFrom(InputValue inputValue) {
            RecipeManager recipeManager = getLevel().getRecipeManager();
            return recipeManager
                    .getRecipeFor(INSTANCE.RECIPE_TYPE.get(), recipeInputFrom(inputValue), getLevel())
                    .map(RecipeHolder::id)
                    .orElse(null);
        }

        @Override
        protected void onRecipeCompleted(Recipe recipe) {
           recipeInputFrom(getInput()).consume(recipe.getInputItems(), recipe.getInputFluids());
           if (recipe.getOutputItem() != null)
               itemHandler.insertItem(OUTPUT_SLOT, recipe.getOutputItem().copy(), false);
           if (recipe.getOutputFluid() != null)
               fluidOutput.fill(recipe.getOutputFluid().copy(), IFluidHandler.FluidAction.EXECUTE);
        }

        @Override
        protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
            super.loadAdditional(tag, registries);
            fluidInput1.setFluid(parseFluidStack((CompoundTag) tag.get("fluid_input_1"), registries));
            fluidInput2.setFluid(parseFluidStack((CompoundTag) tag.get("fluid_input_2"), registries));
            fluidInput3.setFluid(parseFluidStack((CompoundTag) tag.get("fluid_input_3"), registries));
            fluidInput4.setFluid(parseFluidStack((CompoundTag) tag.get("fluid_input_4"), registries));
            fluidOutput.setFluid(parseFluidStack((CompoundTag) tag.get("fluid_output"), registries));
        }

        @Override
        protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
            super.saveAdditional(tag, registries);
            saveFluidStack("fluid_input_1", tag, fluidInput1, registries);
            saveFluidStack("fluid_input_2", tag, fluidInput2, registries);
            saveFluidStack("fluid_input_3", tag, fluidInput3, registries);
            saveFluidStack("fluid_input_4", tag, fluidInput4, registries);
            saveFluidStack("fluid_output", tag, fluidOutput, registries);
        }

        @Override
        public List<IFluidTank> getFluidTanks() {
            return List.of(
                    fluidInput1, fluidInput2, fluidInput3, fluidInput4, fluidOutput
            );
        }
    }

    public static class Block extends AbstractConcoctiMachineBlock {
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
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        @Override
        public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
            if (state.getValue(LIT)) {
                double x = pos.getX() + 0.5;
                double y = pos.getY();
                double z = pos.getZ() + 0.5;
                if (random.nextDouble() < 0.05) {
                    level.playLocalSound(x, y, z, ConcoctiSounds.CONCOCTI_MIXER_FIRE_CRACKLE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
                }
                Direction direction = state.getValue(FACING);
                Direction.Axis axis = direction.getAxis();
                double d = random.nextDouble() * 0.6 - 0.3;
                double dx = axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52 : d;
                double dy = random.nextDouble() * 9.0 / 16.0;
                double dz = axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52 : d;
                level.addParticle(ParticleTypes.SMOKE, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0);
            }
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
        public MenuProvider getMenuProvider(@NotNull BlockState state, Level level, @NotNull BlockPos pos) {
            net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
            return blockEntity instanceof BlockEntity entity ? entity : null;
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

    public static class Recipe implements ProcessingRecipe<Recipe, ItemsFluidsRecipeInput> {
        // An in-code representation of our recipe data. This can be basically anything you want.
        // Common things to have here is a processing time integer of some kind, or an experience reward.
        // Note that we now use an ingredient instead of an item stack for the input.
        private final List<SizedIngredient> inputItems;
        private final ItemStack outputItem;
        private final List<SizedFluidIngredient> inputFluids;
        private final FluidStack outputFluid;

        private final ResourceLocation id;

        private final int ticks;

        // Add a constructor that sets all properties.
        public Recipe(ResourceLocation id, List<SizedIngredient> inputItems, ItemStack outputItem, List<SizedFluidIngredient> inputFluids, FluidStack outputFluid, int ticks) {
            this.inputItems = inputItems;
            this.outputItem = outputItem;
            this.inputFluids = inputFluids;
            this.outputFluid = outputFluid;
            this.ticks = ticks;
            this.id = id;
        }

        public Recipe(List<SizedIngredient> inputItems, ItemStack outputItem, List<SizedFluidIngredient> inputFluids, FluidStack outputFluid, int ticks) {
            this.inputItems = inputItems;
            this.outputItem = outputItem;
            this.inputFluids = inputFluids;
            this.outputFluid = outputFluid;
            this.ticks = ticks;
            this.id = getWouldBeResourceLocation(inputItems, inputFluids);
        }

        public static ResourceLocation getWouldBeResourceLocation(List<SizedIngredient> inputItems, List<SizedFluidIngredient> inputFluids) {
            int[] hashes = new int[2];
            ArrayList<Object> objects = new ArrayList<>(inputItems);
            hashes[0] = Objects.hash(objects.toArray());
            objects.clear();
            objects.addAll(inputFluids);
            hashes[1] = Objects.hash(objects.toArray());
            long longHash = ((long) hashes[0] << 32) | hashes[1];
            UnsignedLong unsignedLong = UnsignedLong.fromLongBits(longHash);
            return ResourceLocation.fromNamespaceAndPath(MODID, "mixing/" + unsignedLong.toString(16));
        }

        public List<SizedIngredient> getInputItems() {
            return inputItems;
        }

        @org.jetbrains.annotations.Nullable
        public ItemStack getOutputItem() {
            return outputItem;
        }

        @NotNull
        public ItemStack getOutputItemOrEmpty() {
            return outputItem == null ? ItemStack.EMPTY : outputItem;
        }

        public List<SizedFluidIngredient> getInputFluids() {
            return inputFluids;
        }

        @org.jetbrains.annotations.Nullable
        public FluidStack getOutputFluid() {
            return outputFluid;
        }

        @NotNull
        public FluidStack getOutputFluidOrEmpty() {
            return outputFluid == null ? FluidStack.EMPTY : outputFluid;
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
            return outputItem == null ? ItemStack.EMPTY : outputItem;
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
            protected final List<SizedIngredient> inputItems;
            protected final ItemStack outputItem;
            protected final List<SizedFluidIngredient> inputFluids;
            protected final FluidStack outputFluid;
            protected final ResourceLocation resourceLocation;
            protected final int ticks;

            public Builder(ResourceLocation resourceLocation, List<SizedIngredient> inputItems, ItemStack outputItem, List<SizedFluidIngredient> inputFluids, FluidStack outputFluid, int ticks) {
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
                return outputItem == null ? Items.AIR : outputItem.getItem();
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
                    SizedIngredient.FLAT_CODEC.listOf().fieldOf("input_items").forGetter(Recipe::getInputItems),
                    ItemStack.OPTIONAL_CODEC.fieldOf("output_item").forGetter(Recipe::getOutputItemOrEmpty),
                    SizedFluidIngredient.FLAT_CODEC.listOf().fieldOf("input_fluids").forGetter(Recipe::getInputFluids),
                    FluidStack.OPTIONAL_CODEC.fieldOf("output_fluid").forGetter(Recipe::getOutputFluidOrEmpty),
                    Codec.INT.fieldOf("ticks").forGetter(Recipe::getTicks)
            ).apply(inst, Recipe::new));

            public static final StreamCodec<RegistryFriendlyByteBuf, Recipe> STREAM_CODEC =
                    StreamCodec.composite(
                            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), Recipe::getInputItems,
                            ItemStack.OPTIONAL_STREAM_CODEC, Recipe::getOutputItemOrEmpty,
                            SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), Recipe::getInputFluids,
                            FluidStack.OPTIONAL_STREAM_CODEC, Recipe::getOutputFluidOrEmpty,
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
            for (int i = 0; i < 4; i++) {
                int slot = i + 2;
                this.addSlot(new Slot(container, slot, 30 + i * 18, 37 + 15));
            }
            // Output slot
            this.addSlot(new ResultSlot(null, container, 6, 30 + 104, 37 + 15));
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
        private final FluidBar<Menu> inputFluid1 = new FluidBar<>(30, 9, this, menu, 0);
        private final FluidBar<Menu> inputFluid2 = new FluidBar<>(30+18, 9, this, menu, 1);
        private final FluidBar<Menu> inputFluid3 = new FluidBar<>(30+36, 9, this, menu, 2);
        private final FluidBar<Menu> inputFluid4 = new FluidBar<>(30+54, 9, this, menu, 3);

        private final FluidBar<Menu> outputFluid = new FluidBar<>(30+104, 9, this, menu, 4);

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
        public void render(@NotNull GuiGraphics guiGraphics, RenderInfo renderInfo) {
            // Don't forget to first render the abstract screen!
            super.render(guiGraphics, renderInfo);

            arrowProgress.update(menu.getProgress());

            energyBar.update(menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true));

            // Render the fluids.
            outputFluid.update(menu.getOutputFluidStack(), menu.getOutputFluidStackSize());
            List<FluidBar<Menu>> fluidBars = List.of(inputFluid1, inputFluid2, inputFluid3, inputFluid4);
            List<FluidStack> fluidStacks = menu.getInputFluidStacks();
            for (int i = 0; i < 4; i++) {
                fluidBars.get(i).update(fluidStacks.get(i), menu.getInputFluidStackSize());
            }

            renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());
        }
    }

    public static class RecipeCategory extends AbstractConcoctiRecipeCategory<Recipe> {
        private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/jei/concocti_mixer.png");
        private final int WIDTH = 176;

        public RecipeCategory(IGuiHelper guiHelper) {
            super(guiHelper, new ItemStack(INSTANCE.BLOCK.get()));
        }

        @Override
        public @NotNull mezz.jei.api.recipe.RecipeType<Recipe> getRecipeType() {
            return INSTANCE.JEI_RECIPE_TYPE;
        }

        @Override
        public @NotNull Component getTitle() {
            return Component.translatable("block.concocti.concocti_mixer");
        }

        @Override
        protected ResourceLocation getTexture() {
            return texture;
        }

        public record DrawInfo(List<Integer> slots, int arrowPos) {}

        public DrawInfo createDrawInfo(Recipe recipe) {
            int drawnSlots = 0;
            if (recipe.getOutputItem() != null && !recipe.getOutputItem().isEmpty()) drawnSlots++;
            if (recipe.getOutputFluid() != null && !recipe.getOutputFluid().isEmpty()) drawnSlots++;
            drawnSlots += recipe.getInputItems().size() + recipe.getInputFluids().size();
            int drawnWidth = drawnSlots * 18 + (11 + 22 + 11);
            int left = (WIDTH - drawnWidth) / 2;
            ArrayList<Integer> toBeDrawnSlots = new ArrayList<>(drawnSlots);
            // Add the recipe inputs.
            for (SizedIngredient ingredient : recipe.getInputItems()) {
                toBeDrawnSlots.add(left);
                left += 18;
            }
            for (SizedFluidIngredient ingredient : recipe.getInputFluids()) {
                toBeDrawnSlots.add(left);
                left += 18;
            }
            left += 11;
            int arrowPos = left;
            left += 22 + 11;
            if (recipe.getOutputItem() != null && !recipe.getOutputItem().isEmpty()) {
                toBeDrawnSlots.add(left);
                left += 18;
            }
            if (recipe.getOutputFluid() != null && !recipe.getOutputFluid().isEmpty()) {
                toBeDrawnSlots.add(left);
            }
            return new DrawInfo(toBeDrawnSlots, arrowPos);
        }

        @Override
        public void setRecipe(@NotNull IRecipeLayoutBuilder builder, Recipe recipe, @NotNull IFocusGroup focuses) {
            int drawnSlots = 0;
            if (recipe.getOutputItem() != null && !recipe.getOutputItem().isEmpty()) drawnSlots++;
            if (recipe.getOutputFluid() != null && !recipe.getOutputFluid().isEmpty()) drawnSlots++;
            drawnSlots += recipe.getInputItems().size() + recipe.getInputFluids().size();
            int drawnWidth = drawnSlots * 18 + (11 + 22 + 11);
            int left = (WIDTH - drawnWidth) / 2;
            // Add the recipe inputs.
            int i = 1;
            for (SizedIngredient ingredient : recipe.getInputItems()) {
                builder.addSlot(RecipeIngredientRole.INPUT, left, 6)
                        .addItemStacks(Arrays.asList(ingredient.getItems()))
                        .setSlotName("input_item_" + i);
                i++;
                left += 18;
            }
            i = 1;
            for (SizedFluidIngredient ingredient : recipe.getInputFluids()) {
                IRecipeSlotBuilder slotBuilder =
                    builder.addSlot(RecipeIngredientRole.INPUT, left, 6)
                        .setSlotName("input_fluid_" + i);
                for (FluidStack fluidStack : ingredient.getFluids())
                    slotBuilder.addFluidStack(fluidStack.getFluid(), fluidStack.getAmount());
                i++;
                left += 18;
            }
            left += 11;
            left += 22 + 11;
            if (recipe.getOutputItem() != null && !recipe.getOutputItem().isEmpty()) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, left, 6)
                        .addItemStack(recipe.getOutputItem())
                        .setSlotName("output_item");
                left += 18;
            }
            if (recipe.getOutputFluid() != null && !recipe.getOutputFluid().isEmpty()) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, left, 6)
                        .addFluidStack(recipe.getOutputFluid().getFluid(), recipe.getOutputFluid().getAmount())
                        .setSlotName("output_fluid");
                // left += 18;
            }
        }

        @Override
        public void draw(@NotNull ConcoctiMixer.Recipe recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                         @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
            super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
            DrawInfo drawInfo = createDrawInfo(recipe);
            for (int x : drawInfo.slots) {
                guiGraphics.blit(slot, x - 1, 6 - 1, 0, 0, 18, 18, 18, 18);
            }
        }

        @Override
        protected int getHorizontalArrowOffset(@NotNull ConcoctiMixer.Recipe recipe) { return createDrawInfo(recipe).arrowPos - 72; }
    }
}
