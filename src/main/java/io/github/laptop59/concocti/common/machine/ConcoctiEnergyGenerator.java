package io.github.laptop59.concocti.common.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.laptop59.concocti.client.gui.AbstractConcoctiMachineScreen;
import io.github.laptop59.concocti.client.gui.components.*;
import io.github.laptop59.concocti.common.ConcoctiSounds;
import io.github.laptop59.concocti.common.abstraction.ConcoctiMachineComplexion;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.AbstractConcoctiMachineBlock;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.block.entity.DynamicEnergyStorage;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.integration.jei.AbstractConcoctiRecipeCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.advancements.Criterion;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.*;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.Concocti.MODID;
import static io.github.laptop59.concocti.common.machine.ConcoctiEnergyGenerator.Block.LIT;

public class ConcoctiEnergyGenerator extends ConcoctiMachine <
        ConcoctiEnergyGenerator.BlockEntity,
        ConcoctiEnergyGenerator.Menu,
        ItemStack,
        SingleRecipeInput,
        ConcoctiEnergyGenerator.Recipe,
        ConcoctiEnergyGenerator.Recipe.Serializer,
        ConcoctiEnergyGenerator.Block,
        ConcoctiEnergyGenerator.Screen,
        ConcoctiEnergyGenerator.RecipeCategory
> {
    static ConcoctiEnergyGenerator INSTANCE;

    // Details
    public final static String ID = "concocti_energy_generator";

    public final static int INPUT_SLOT = 2;
    public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, ItemStack, SingleRecipeInput, Recipe>> getDetails() {
        return () -> new ConcoctiMachineDetails<BlockEntity, Menu, ItemStack, SingleRecipeInput, Recipe>(
                100_000,
                100_000,
                3,
                50.0f,
                DynamicEnergyStorage.Mode.OUTPUT_ONLY.toSupplier(),
                INSTANCE.RECIPE_TYPE,
                Component.translatable("block.concocti.concocti_energy_generator"),
                List.of(
                        SlotType.ITEM_INPUT,
                        SlotType.ENERGY_OUTPUT
                ),
                Menu.class,
                blockEntity -> blockEntity.dataAccess,
                new EnumMap<>(
                        Map.of(
                                SlotType.ITEM_INPUT, List.of(INPUT_SLOT)
                        )
                ),
                new EnumMap<>(SlotType.class),
                InputOutput.onlyInputs(blockEntity -> List.of(INPUT_SLOT)),
                InputOutput.empty()
        );
    }

    ConcoctiEnergyGenerator() {
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

    public RecipeCategoryConstructor<RecipeCategory, Recipe, SingleRecipeInput> getRecipeCategoryConstructor() {
        return RecipeCategory::new;
    }

    public RecipeSerializerConstructor<Recipe.Serializer, Recipe, SingleRecipeInput> getRecipeSerializerConstructor() {
        return Recipe.Serializer::new;
    }

    public Class<Recipe> getRecipeClass() {
        return Recipe.class;
    }

    public static class BlockEntity extends AbstractConcoctiMachineBlockEntity
            <BlockEntity, Menu, ItemStack, SingleRecipeInput, Recipe> {
        // Properties: None

        public static int energyPerTick = 50;

        public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, ItemStack, SingleRecipeInput, Recipe>> getUncachedMachineDetails() {
            return INSTANCE.getDetails();
        }

        @Override
        protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
            return slot == INPUT_SLOT;
        }

        protected final ConcoctiMachineComplexion dataAccess = new ConcoctiMachineComplexion(this);

        public BlockEntity(BlockPos pos, BlockState blockState) {
            super(
                    INSTANCE.BLOCK_ENTITY, pos, blockState
            );
        }

        public ItemStack getInputStack() {
            return this.getItem(INPUT_SLOT);
        }

        /** Returns 0 if the input item is not a fuel, otherwise return the number of ticks it would burn for. */
        public int getFuelTicksFromOneInputItem() {
            ItemStack input = getInputStack();
            Holder<Item> holder = input.getItemHolder();
            FurnaceFuel fuel = holder.getData(NeoForgeDataMaps.FURNACE_FUELS);
            if (fuel == null) return 0;
            return fuel.burnTime();
        }

        @Override
        public boolean canProcess() {
            return ticksLeft <= 0 && getFuelTicksFromOneInputItem() > 0 && energy.getEnergyStored() < energy.getMaxEnergyStored();
        }

        @Override
        public void tick(Level level, BlockPos pos, BlockState state) {
            BlockEntity entity = this;

            int currentUpgradeUnits = ConcoctiUpgradeSlot.getUpgradeUnits(entity.getItem(UPGRADE_SLOT));
            if (currentUpgradeUnits != entity.lastUpgradeUnits) {
                entity.lastUpgradeUnits = currentUpgradeUnits;
                entity.setNewEnergyMultiplier(entity.getInefficientEnergyMultiplier());
            }
            if (entity.ticksLeft >= entity.totalTicks) entity.lastRecipeId = null;
            if (--entity.autoCooldown <= 0) {
                entity.autoCooldown = AUTO_COOLDOWN;
                entity.attemptToPull();
                entity.attemptToEject();
            }
            int consumableTicks = entity.getTickMultiplier();
            while (consumableTicks > 0) {
                if (entity.ticksLeft > 0) {
                    int ticksConsumed = consumableTicks;
                    if (ticksConsumed > entity.ticksLeft) ticksConsumed = entity.ticksLeft;
                    consumableTicks -= ticksConsumed;
                    entity.energy.forceReceiveEnergy(energyPerTick * ticksConsumed, false);
                    entity.ticksLeft -= ticksConsumed;
                } else {
                    entity.lastRecipeId = null;
                }
                if (entity.canProcess()) {
                    ItemStack input = entity.getInput();
                    ResourceLocation toBeProcessed = entity.getRecipeIdFrom(input);
                    int ticksToBurn = getFuelTicksFromOneInputItem();
                    if ((entity.lastRecipeId == null || !entity.lastRecipeId.equals(toBeProcessed))) {
                        entity.lastRecipeId = toBeProcessed;
                        entity.totalTicks = ticksToBurn;
                        entity.ticksLeft += entity.totalTicks;
                        ItemStack previousItemStack = getInputStack().copy();
                        getInputStack().shrink(1);
                        if (getInputStack().isEmpty()) {
                            setItem(INPUT_SLOT, previousItemStack.getCraftingRemainingItem());
                        }
                        entity.attemptToPull();
                        entity.attemptToEject();
                    }
                } else break;
            }
            if (state.getValue(LIT) != (entity.ticksLeft > 0)) {
                level.setBlock(pos, state.setValue(LIT, (entity.ticksLeft > 0)), 1 | 2);
            }
        }

        @Override
        protected SingleRecipeInput recipeInputFrom(ItemStack input) {
            return new SingleRecipeInput(input);
        }

        @Override
        protected ItemStack getInput() {
            return getInputStack();
        }

        @Override
        protected ResourceLocation getRecipeIdFrom(ItemStack input) {
            return BuiltInRegistries.ITEM.getKey(input.getItem());
        }

        @Override
        protected void onRecipeCompleted(Recipe recipe) {}

        @Override
        protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
            super.loadAdditional(tag, registries);
        }

        @Override
        protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
            super.saveAdditional(tag, registries);
        }

        @Override
        public List<IFluidTank> getFluidTanks() {
            return List.of();
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
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        @Override
        public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
            if (state.getValue(LIT)) {
                double x = pos.getX() + 0.5;
                double y = pos.getY();
                double z = pos.getZ() + 0.5;
                if (random.nextDouble() < 0.1) {
                    level.playLocalSound(x, y, z, ConcoctiSounds.CONCOCTI_ENERGY_GENERATOR_FIRE_CRACKLE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
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

    // This serves as a dummy Recipe.
    public static class Recipe implements ProcessingRecipe<Recipe, SingleRecipeInput> {
        // An in-code representation of our recipe data. This can be basically anything you want.
        // Common things to have here is a processing time integer of some kind, or an experience reward.
        // Note that we now use an ingredient instead of an item stack for the input.
        private final Ingredient inputItem;
        private final int ticks;

        private static final HashMap<Ingredient, ResourceLocation> idMap = new HashMap<>();

        // Add a constructor that sets all properties.
        public Recipe(ResourceLocation id, Ingredient inputItem, int ticks) {
            this.inputItem = inputItem;
            this.ticks = ticks;
            idMap.put(inputItem, id);
        }

        public Recipe(Ingredient inputItem, int ticks) {
            this.inputItem = inputItem;
            this.ticks = ticks;
            idMap.put(inputItem, Arrays.stream(inputItem.getItems()).findFirst()
                    .map(s -> BuiltInRegistries.ITEM.getKey(s.getItem()))
                    .map(l -> ResourceLocation.fromNamespaceAndPath(
                            l.getNamespace(), "energy_generating/" + l.getPath()
                    ))
                    .orElse(null)
            );
        }

        public Ingredient getInputItem() {
            return inputItem;
        }

        // A list of our ingredients. Does not need to be overridden if you have no ingredients
        // (the default implementation returns an empty list here). It makes sense to cache larger lists in a field.
        @Override
        public @NotNull NonNullList<Ingredient> getIngredients() {
            NonNullList<Ingredient> list = NonNullList.create();
            list.add(this.inputItem);
            return list;
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
        public boolean matches(SingleRecipeInput input, @NotNull Level level) {
            return this.inputItem.test(input.item());
        }

        // Return an UNMODIFIABLE version of your result here. The result of this method is mainly intended
        // for the recipe book, and commonly used by JEI and other recipe viewers as well.
        @Override
        public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
            return ItemStack.EMPTY; // Only creates energy.
        }

        // Return the result of the recipe here, based on the given input. The first parameter matches the generic.
        // IMPORTANT: Always call .copy() if you use an existing result! If you don't, things can and will break,
        // as the result exists once per recipe, but the assembled stack is created each time the recipe is crafted.
        @Override
        public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input, HolderLookup.@NotNull Provider registries) {
            return ItemStack.EMPTY.copy(); // Only creates energy.
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
            return idMap.getOrDefault(inputItem, null);
        }


        public static class Builder implements RecipeBuilder {
            protected final Ingredient inputItem;
            protected final int ticks;

            public Builder(Ingredient inputItem, int ticks) {
                this.inputItem = inputItem;
                this.ticks = ticks;
            }

            @Override
            public @NotNull RecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
                return this;
            }

            @Override
            public @NotNull ConcoctiEnergyGenerator.Recipe.Builder group(@org.jetbrains.annotations.Nullable String group) {
                return this; // No recipe book groups required.
            }

            // Vanilla wants an Item here, not an ItemStack. You still can and should use the ItemStack
            // for serializing the recipes.
            @Override
            public @NotNull Item getResult() {
                return Items.AIR;
            }

            static ResourceLocation getDefaultRecipeId(Ingredient ingredient) {
                ItemStack firstStack = Arrays.stream(ingredient.getItems()).findFirst().orElseThrow();
                return ResourceLocation.fromNamespaceAndPath(MODID,
                        "energy_generating/" + BuiltInRegistries.ITEM.getKey(firstStack.getItem()).getPath());
            }

            @Override
            public void save(@NotNull RecipeOutput recipeOutput) {
                this.save(recipeOutput, getDefaultRecipeId(inputItem));
            }

            @Override
            public void save(@NotNull RecipeOutput recipeOutput, @NotNull String id) {
                ResourceLocation resourceLocation = getDefaultRecipeId(inputItem);
                ResourceLocation idLocation = ResourceLocation.parse(id);
                if (ResourceLocation.parse(id).equals(resourceLocation)) {
                    throw new IllegalStateException("Recipe " + id + " should remove its 'save' argument as it is equal to default one");
                } else {
                    this.save(recipeOutput, idLocation);
                }
            }

            @Override
            public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
                Recipe recipe = new Recipe(id, this.inputItem, this.ticks);
                recipeOutput.accept(id, recipe, null);
            }
        }


        public static class Serializer implements RecipeSerializer<Recipe> {
            public static final MapCodec<Recipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(Recipe::getInputItem),
                    Codec.INT.fieldOf("ticks").forGetter(Recipe::getTicks)
            ).apply(inst, Recipe::new));

            public static final StreamCodec<RegistryFriendlyByteBuf, Recipe> STREAM_CODEC =
                    StreamCodec.composite(
                            Ingredient.CONTENTS_STREAM_CODEC, Recipe::getInputItem,
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
            return List.of();
        }

        // Client
        public Menu(
                int containerId, Inventory playerInventory
        ) {
            super(containerId, playerInventory, 3, INSTANCE.MENU);
        }

        // Server
        public Menu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
            super(containerId, playerInventory, container, data, INSTANCE.MENU);
        }

        @Override
        protected void addOtherSlots() {
            // Fuel item slot
            this.addSlot(new Slot(container, 2, 75, 28));
        }

        @Override
        public @org.jetbrains.annotations.Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
            // index 2
            if (!this.getSlot(2).hasItem() && !this.moveItemStackTo(movedStack, 2, 3, true)) {
                return ItemStack.EMPTY;
            }
            return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Screen extends AbstractConcoctiMachineScreen<Menu> {
        private final EnergyBar<Menu> energyBar = new EnergyBar<>(10, 18, this, menu);
        private final FlameProgress flameProgress = new FlameProgress(76, 28 + 18 + 2);

        public Screen(
                Menu menu,
                Inventory playerInventory,
                Component title
        ) {
            super(menu, playerInventory, title);
        }

        public List<Renderable> getUniqueChildren() {
            return List.of(
                    flameProgress,
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
        public void render(@NotNull GuiGraphics guiGraphics, RenderInfo renderInfo) {
            // Don't forget to first render the abstract screen!
            super.render(guiGraphics, renderInfo);

            flameProgress.update(menu.getProgress());

            energyBar.update(menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true));

            renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());
        }
    }

    // This serves as a dummy RecipeCategory.
    public static class RecipeCategory extends AbstractConcoctiRecipeCategory<Recipe> {

        private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/jei/concocti_energy_generator.png");

        public RecipeCategory(IGuiHelper guiHelper) {
            super(guiHelper, new ItemStack(INSTANCE.BLOCK.get()));
        }

        @Override
        public @NotNull mezz.jei.api.recipe.RecipeType<Recipe> getRecipeType() {
            return INSTANCE.JEI_RECIPE_TYPE;
        }

        @Override
        public @NotNull Component getTitle() {
            return Component.translatable("block.concocti.concocti_energy_generator");
        }

        @Override
        public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull ConcoctiEnergyGenerator.Recipe recipe,
                               @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {}

        @Override
        protected ResourceLocation getTexture() { return texture; }

        @Override
        public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull Recipe recipe, @NotNull IFocusGroup focuses) {}
    }
}
