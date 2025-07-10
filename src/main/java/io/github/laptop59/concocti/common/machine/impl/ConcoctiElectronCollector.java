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
import io.github.laptop59.concocti.common.block.entity.DynamicEnergyStorage;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.machine.ConcoctiMachine;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetails;
import io.github.laptop59.concocti.common.machine.InputOutput;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.recipe.*;
import io.github.laptop59.concocti.integration.jei.AbstractConcoctiRecipeCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.advancements.Criterion;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiElectronCollector extends ConcoctiMachine<
        ConcoctiElectronCollector.BlockEntity,
        ConcoctiElectronCollector.Menu,
        LightningState,
        LightningRecipeInput,
        ConcoctiElectronCollector.Recipe,
        ConcoctiElectronCollector.Recipe.Serializer,
        ConcoctiElectronCollector.Block,
        ConcoctiElectronCollector.Screen,
        ConcoctiElectronCollector.RecipeCategory
> {
    static ConcoctiElectronCollector INSTANCE;

    // Details
    public final static String ID = "concocti_electron_collector";

    public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, LightningState, LightningRecipeInput, Recipe>> getDetails() {
        return () -> new ConcoctiMachineDetails<BlockEntity, Menu, LightningState, LightningRecipeInput, Recipe>(
                100_000,
                100_000,
                2,
                100.0f,
                DynamicEnergyStorage.Mode.INPUT_ONLY.toSupplier(),
                INSTANCE.RECIPE_TYPE,
                Component.translatable("block.concocti.concocti_electron_collector"),
                List.of(SlotType.FLUID_OUTPUT),
                Menu::new,
                blockEntity -> blockEntity.dataAccess,
                new EnumMap<>(SlotType.class),
                new EnumMap<>(
                        Map.of(
                                SlotType.FLUID_OUTPUT, List.of(blockEntity -> blockEntity.fluidOutput)
                        )
                ),
                InputOutput.empty(),
                InputOutput.onlyOutputs(blockEntity -> List.of(blockEntity.fluidOutput))
        );
    }

    public ConcoctiElectronCollector() {
        super(
                ID,
                BlockBehaviour.Properties
                        .of()
                        .mapColor(DyeColor.MAGENTA)
                        .requiresCorrectToolForDrops()
                        .explosionResistance(1000f)
                        .strength(20f)
                        .lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 15 : 0),
                new ConcoctiBlocks.BlockData(ConcoctiBlocks.BlockToolRank.IRON, ConcoctiBlocks.BlockToolType.PICKAXE)
        );
        INSTANCE = this;
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

    public RecipeCategoryConstructor<RecipeCategory, Recipe, LightningRecipeInput> getRecipeCategoryConstructor() {
        return RecipeCategory::new;
    }

    public RecipeSerializerConstructor<Recipe.Serializer, Recipe, LightningRecipeInput> getRecipeSerializerConstructor() {
        return Recipe.Serializer::new;
    }

    public Class<Recipe> getRecipeClass() {
        return Recipe.class;
    }

    public static class BlockEntity extends AbstractConcoctiMachineBlockEntity
            <BlockEntity, Menu, LightningState, LightningRecipeInput, Recipe> {

        private final FluidTank fluidOutput = new FluidTank(TANK_CAPACITY);
        private final LightningState lightningState = new LightningState(false);

        // Slots: NONE

        // Properties
        public final Property<FluidStack> FLUID_OUTPUT = Properties.FLUID_OUTPUT.newWithLinker(fluidOutput::getFluid);

        @Override
        protected ConcoctiElectronCollector getMachineInstance() {
            return INSTANCE;
        }

        @Override
        public List<IFluidHandler> getIndexedFluidHandlers() {
            return List.of(fluidOutput);
        }

        public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, LightningState, LightningRecipeInput, Recipe>> getUncachedMachineDetails() {
            return INSTANCE.getDetails();
        }

        @Override
        protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
            return false;
        }

        protected final ConcoctiMachineComplexion dataAccess = new ConcoctiMachineComplexion(
                this,
                FLUID_OUTPUT.of(FluidStack.EMPTY)
        );

        public BlockEntity(BlockPos pos, BlockState blockState) {
            super(
                    INSTANCE.BLOCK_ENTITY, pos, blockState
            );
            fluidOutput.getFluid().limitSize(TANK_CAPACITY);
        }

        @Override
        public boolean canProcess() {
            if (!super.canProcess()) return false;
            Recipe recipe = getRecipe(lightningState);
            // Check whether the fluids obtained from this item will not exceed our fluid limit.
            FluidStack result = recipe.getOutputFluid().copy();
            return fluidOutput.fill(result, IFluidHandler.FluidAction.SIMULATE) == result.getAmount();
        }

        @Override
        protected LightningRecipeInput recipeInputFrom(LightningState lightningState) {
            return new LightningRecipeInput(lightningState);
        }

        @Override
        protected LightningState getInput() {
            return lightningState;
        }

        @Override
        protected Recipe getRecipe(LightningState input) {
            Optional<RecipeHolder<Recipe>> optional = level.getRecipeManager().getRecipeFor(
                    // The recipe type.
                    getRecipeType().get(),
                    recipeInputFrom(input),
                    level
            );
            return optional.map(RecipeHolder::value).orElse(null);
        }

        @Override
        protected void onRecipeCompleted(Recipe recipe) {
            if (Math.random() < recipe.getChance())
                fluidOutput.fill(recipe.getOutputFluid(), IFluidHandler.FluidAction.EXECUTE);
            lightningState.setLightningCollected(false);
        }

        @Override
        protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
            super.loadAdditional(tag, registries);
            fluidOutput.setFluid(parseFluidStack((CompoundTag) tag.get("fluid_output"), registries));
            lightningState.setLightningCollected(tag.getBoolean("lightning_collected"));
        }

        @Override
        protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
            super.saveAdditional(tag, registries);
            saveFluidStack("fluid_output", tag, fluidOutput, registries);
            tag.putBoolean("lightning_collected", lightningState.getLightningCollected());
        }

        @Override
        public List<IFluidTank> getFluidTanks() {
            return List.of(fluidOutput);
        }

        public void markLightningState() {
            lightningState.setLightningCollected(true);
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

    public static class Recipe implements ProcessingRecipe<Recipe, LightningRecipeInput> {
        // An in-code representation of our recipe data. This can be basically anything you want.
        // Common things to have here is a processing time integer of some kind, or an experience reward.
        // Note that we now use an ingredient instead of an item stack for the input.
        private final float chance;
        private final FluidStack outputFluid;

        private final ResourceLocation id;

        private final int ticks;

        // Add a constructor that sets all properties.
        public Recipe(ResourceLocation id, float chance, FluidStack outputFluid, int ticks) {
            this.chance = chance;
            this.outputFluid = outputFluid;
            this.ticks = ticks;
            this.id = id;
        }

        public Recipe(float chance, FluidStack outputFluid, int ticks) {
            this.chance = chance;
            this.outputFluid = outputFluid;
            this.ticks = ticks;
            this.id = getWouldBeResourceLocation(outputFluid);
        }

        public static ResourceLocation getWouldBeResourceLocation(FluidStack outputFluid) {
            return ResourceLocation.fromNamespaceAndPath(MODID, "electron_collecting/" + BuiltInRegistries.FLUID.getKey(outputFluid.getFluid()).getPath());
        }

        @NotNull
        public FluidStack getOutputFluid() {
            return outputFluid;
        }

        public float getChance() {
            return chance;
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
        public boolean matches(@NotNull LightningRecipeInput input, @NotNull Level level) {
            return matches(input);
        }

        public boolean matches(@NotNull LightningRecipeInput input) {
            return input.test();
        }

        // Return an UNMODIFIABLE version of your result here. The result of this method is mainly intended
        // for the recipe book, and commonly used by JEI and other recipe viewers as well.
        @Override
        public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
            return ItemStack.EMPTY;
        }

        // Return the result of the recipe here, based on the given input. The first parameter matches the generic.
        // IMPORTANT: Always call .copy() if you use an existing result! If you don't, things can and will break,
        // as the result exists once per recipe, but the assembled stack is created each time the recipe is crafted.
        @Override
        public @NotNull ItemStack assemble(@NotNull LightningRecipeInput input, HolderLookup.@NotNull Provider registries) {
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
            private final float chance;
            private final FluidStack outputFluid;
            private final int ticks;

            public Builder(float chance, FluidStack outputFluid, int ticks) {
                this.chance = chance;
                this.outputFluid = outputFluid;
                this.ticks = ticks;
            }

            @Override
            public @NotNull RecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
                return this;
            }

            @Override
            public @NotNull ConcoctiElectronCollector.Recipe.Builder group(@org.jetbrains.annotations.Nullable String group) {
                return this; // No recipe book groups required.
            }

            // Vanilla wants an Item here, not an ItemStack. You still can and should use the ItemStack
            // for serializing the recipes.
            @Override
            public @NotNull Item getResult() {
                return Items.AIR;
            }

            @Override
            public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
                Recipe recipe = new Recipe(
                        id,
                        this.chance,
                        this.outputFluid,
                        this.ticks
                );
                recipeOutput.accept(getWouldBeResourceLocation(recipe.outputFluid), recipe, null);
            }
        }


        public static class Serializer implements RecipeSerializer<Recipe> {
            public static final MapCodec<Recipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.FLOAT.fieldOf("chance").forGetter(Recipe::getChance),
                    FluidStack.CODEC.fieldOf("output_fluid").forGetter(Recipe::getOutputFluid),
                    Codec.INT.fieldOf("ticks").forGetter(Recipe::getTicks)
            ).apply(inst, Recipe::new));

            public static final StreamCodec<RegistryFriendlyByteBuf, Recipe> STREAM_CODEC =
                    StreamCodec.composite(
                            ByteBufCodecs.FLOAT, Recipe::getChance,
                            FluidStack.STREAM_CODEC, Recipe::getOutputFluid,
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
                    Properties.FLUID_OUTPUT
            );
        }

        // Client
        public Menu(
                int containerId, Inventory playerInventory
        ) {
            super(containerId, playerInventory, 2, INSTANCE.MENU);
        }

        // Server
        public Menu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
            super(containerId, playerInventory, container, data, INSTANCE.MENU);
        }

        @Override
        protected void addOtherSlots() {}

        @Override
        public @org.jetbrains.annotations.Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
            return null;
        }

        public FluidStack getFluidOutput() {
            return viewer.get(Properties.FLUID_OUTPUT);
        }

        public int getMaxFluidOutput() {
            return AbstractConcoctiMachineBlockEntity.TANK_CAPACITY;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Screen extends AbstractConcoctiMachineScreen<Menu> {
        private final FluidBar<Menu> outputFluid = new FluidBar<>((getWidth() + 20) / 2, 23, this, menu, 0);

        private final EnergyBar<Menu> energyBar = new EnergyBar<>(10, 18, this, menu);
        private final ArrowProgress arrowProgress = new ArrowProgress(79 - 17, 37);

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
            outputFluid.update(menu.getFluidOutput(), menu.getMaxFluidOutput());

            renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());
        }
    }

    public static class RecipeCategory extends AbstractConcoctiRecipeCategory<Recipe> {

        private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/jei/concocti_electron_collector.png");

        public RecipeCategory(IGuiHelper guiHelper) {
            super(guiHelper, new ItemStack(INSTANCE.BLOCK.get()));
        }

        @Override
        public @NotNull mezz.jei.api.recipe.RecipeType<Recipe> getRecipeType() {
            return INSTANCE.JEI_RECIPE_TYPE;
        }

        @Override
        public @NotNull Component getTitle() {
            return Component.translatable("block.concocti.concocti_electron_collector");
        }

        @Override
        public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull ConcoctiElectronCollector.Recipe recipe,
                               @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
            // Show the duration, if needed.
            if (isCursorTouchingArrow(mouseX, mouseY, recipe)) {
                String chance = String.format("%.2f", recipe.getChance() * 100);
                tooltip.add(Component.translatable("screen.concocti.duration", (double) getTicks(recipe) / 20));
                tooltip.add(Component.translatable("screen.concocti.chance", chance));
            }
        }

        @Override
        protected ResourceLocation getTexture() {
            return texture;
        }

        @Override
        public void setRecipe(@NotNull IRecipeLayoutBuilder builder, Recipe recipe, @NotNull IFocusGroup focuses) {
            // Add the fluid output.
            builder.addSlot(RecipeIngredientRole.OUTPUT, 113, 6)
                    .addIngredient(NeoForgeTypes.FLUID_STACK, recipe.getOutputFluid())
                    .setSlotName("output_fluid");

            ItemStack rod = new ItemStack(ConcoctiItems.CONDUCTIVIUM_LIGHTNING_ROD.get());
            ArrayList<MutableComponent> mutableComponents = new ArrayList<>();

            mutableComponents.add(Component.translatable("screen.concocti.requirements").withColor(0xC7C7C7));
            mutableComponents.add(Component.translatable("screen.concocti.directly_on_top_of_machine").withColor(0xEEEEEE));
            mutableComponents.add(Component.translatable("screen.concocti.struck_by_lightning").withColor(0xEEEEEE));

            mutableComponents.replaceAll(mutableComponent -> mutableComponent.withStyle(
                    mutableComponent.getStyle().withItalic(false)
            ));

            ArrayList<Component> components = new ArrayList<>(mutableComponents.size());
            components.addAll(mutableComponents);

            ItemLore itemLore = new ItemLore(components);
            rod.set(DataComponents.LORE, itemLore);

            builder.addSlot(RecipeIngredientRole.CATALYST, 40, 6)
                    .setSlotName("lightning_rod")
                    .addIngredients(Ingredient.of(rod));
        }
    }
}
