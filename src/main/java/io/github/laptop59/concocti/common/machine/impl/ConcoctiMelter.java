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
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineOnlyItemsFluidsBlockEntity;
import io.github.laptop59.concocti.common.block.entity.DynamicEnergyStorage;
import io.github.laptop59.concocti.common.detail.DetailCodec;
import io.github.laptop59.concocti.common.detail.DetailHolder;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetails;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineOnlyItemsFluids;
import io.github.laptop59.concocti.common.machine.InputOutput;
import io.github.laptop59.concocti.common.machine.ItemsFluidsInputValue;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.recipe.ItemsFluidsRecipeInput;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.integration.jei.AbstractConcoctiRecipeCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.advancements.Criterion;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
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
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiMelter extends ConcoctiMachineOnlyItemsFluids<
        ConcoctiMelter.BlockEntity,
        ConcoctiMelter.Menu,
        ConcoctiMelter.Recipe,
        ConcoctiMelter.Recipe.Serializer,
        ConcoctiMelter.Block,
        ConcoctiMelter.Screen,
        ConcoctiMelter.RecipeCategory
        > {
    // Slots
    protected static final int INPUT_SLOT = 2;
    static ConcoctiMelter INSTANCE;

    // Details
    public final static String ID = "concocti_melter";

    public ConcoctiMelter() {
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

    public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, ItemsFluidsInputValue, ItemsFluidsRecipeInput, Recipe>> getDetails() {
        return () -> new ConcoctiMachineDetails<>(
                BlockEntity.class,
                10_000,
                10_000,
                3,
                25.0f,
                DynamicEnergyStorage.Mode.INPUT_ONLY.toSupplier(),
                INSTANCE.RECIPE_TYPE,
                Component.translatable("block.concocti." + ID),
                List.of(
                        SlotType.ITEM_INPUT,
                        SlotType.PURE_FLUID_OUTPUT,
                        SlotType.BYPRODUCT_FLUID_OUTPUT,
                        SlotType.BOTH_FLUIDS_OUTPUT
                ),
                Menu::new,
                blockEntity -> blockEntity.dataAccess,
                new EnumMap<>(
                        Map.of(
                                SlotType.ITEM_INPUT, List.of(INPUT_SLOT)
                        )
                ),
                new EnumMap<>(
                        Map.of(
                                SlotType.PURE_FLUID_OUTPUT, List.of(BlockEntity::getPureFluidOutput),
                                SlotType.BYPRODUCT_FLUID_OUTPUT, List.of(BlockEntity::getByproductFluidOutput),
                                SlotType.BOTH_FLUIDS_OUTPUT, List.of(
                                        BlockEntity::getPureFluidOutput,
                                        BlockEntity::getByproductFluidOutput
                                )
                        )
                ),
                InputOutput.onlyInputs(
                        blockEntity -> List.of(INPUT_SLOT)
                ),
                InputOutput.onlyOutputs(
                        blockEntity -> List.of(
                                blockEntity.pureFluidOutput.get(),
                                blockEntity.byproductFluidOutput.get()
                        )
                ),
                ConcoctiSounds.CONCOCTI_MELTER_FIRE_CRACKLE.get()
        );
    }

    public static class BlockEntity extends AbstractConcoctiMachineOnlyItemsFluidsBlockEntity
            <BlockEntity, Menu, Recipe> {

        private final DetailHolder<FluidTank> pureFluidOutput = new DetailHolder<>(
                DetailCodec.FLUID_TANK, "pure_fluid_output", new FluidTank(TANK_CAPACITY), this
        );
        private final DetailHolder<FluidTank> byproductFluidOutput = new DetailHolder<>(
                DetailCodec.FLUID_TANK, "byproduct_fluid_output", new FluidTank(TANK_CAPACITY), this
        );

        // Properties
        public final Property<FluidStack> PURE_FLUID_OUTPUT = Properties.PURE_FLUID_OUTPUT.newWithLinker(() -> pureFluidOutput.get().getFluid());
        public final Property<FluidStack> BYPRODUCT_FLUID_OUTPUT = Properties.BYPRODUCT_FLUID_OUTPUT.newWithLinker(() -> byproductFluidOutput.get().getFluid());

        @Override
        public List<IFluidHandler> getIndexedFluidHandlers() {
            return List.of(
                    getPureFluidOutput(),
                    getByproductFluidOutput()
            );
        }

        @Override
        protected ConcoctiMelter getMachineInstance() {
            return INSTANCE;
        }

        public FluidTank getPureFluidOutput() {
            return pureFluidOutput.get();
        }

        public FluidTank getByproductFluidOutput() {
            return byproductFluidOutput.get();
        }

        private int recipeFill(@NotNull FluidStack resource) {
            return outputFluidHandler.get().fill(resource, IFluidHandler.FluidAction.SIMULATE);
        }

        private void recipeResultFillSingle(boolean isPureOutput, @NotNull FluidStack resource) {
            int filled;
            FluidTank output = (isPureOutput ? pureFluidOutput : byproductFluidOutput).get();
            if (output.isEmpty() || output.getFluid().getFluid().isSame(resource.getFluid())) {
                // Set the fluid to be the resource's fluid.
                int amount = output.isEmpty() ? 0 : output.getFluidAmount();
                FluidStack copy = resource.copy();
                if (isPureOutput)
                    pureFluidOutput.get().setFluid(copy);
                else
                    byproductFluidOutput.get().setFluid(copy);
                filled = Math.min(resource.getAmount(), TANK_CAPACITY - amount);
                copy.setAmount(amount + filled);
                resource.setAmount(resource.getAmount() - filled);
            }
        }

        private void recipeResultFill(@NotNull FluidStack resource) {
            recipeResultFillSingle(true, resource);
            if (resource.isEmpty()) return;
            // No need to check for overflowing.
            recipeResultFillSingle(false, resource);
        }

        @Override
        protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
            return slot == INPUT_SLOT;
        }

        protected final ConcoctiMachineComplexion dataAccess = new ConcoctiMachineComplexion(
                this,
                PURE_FLUID_OUTPUT.of(FluidStack.EMPTY),
                BYPRODUCT_FLUID_OUTPUT.of(FluidStack.EMPTY)
        );

        public BlockEntity(BlockPos pos, BlockState blockState) {
            super(
                    INSTANCE.BLOCK_ENTITY, pos, blockState
            );
        }

        public ItemStack getInputStack() {
            return this.getItem(INPUT_SLOT);
        }

        @Override
        public boolean canProcess() {
            if (!super.canProcess()) return false;
            Recipe recipe = getRecipe(getInput());
            // Check whether the fluids obtained from this item will not exceed our fluid limit.
            FluidStack resultPureFluid = recipe.getOutputPureFluid().copy();
            FluidStack resultByproductFluid = recipe.getOutputByproductFluid().copy();
            if (resultPureFluid.getAmount() > recipeFill(resultPureFluid))
                return false;
            if (!resultByproductFluid.isEmpty() &&
                    resultByproductFluid.getAmount() > recipeFill(resultByproductFluid))
                return false;
            return true;
        }

        @Override
        protected void onRecipeCompleted(Recipe recipe) {
            getInputStack().shrink(recipe.inputItem.count());
            recipeResultFill(recipe.getOutputPureFluid().copy());
            recipeResultFill(recipe.getOutputByproductFluid().copy());
        }

        @Override
        public List<IFluidTank> getFluidTanks() {
            return List.of(
                    getPureFluidOutput(),
                    getByproductFluidOutput()
            );
        }
    }

    public static class Block extends AbstractConcoctiMachineBlock<Block> {
        protected Block(Properties properties) {
            super(properties);
        }

        @Override
        protected ConcoctiMelter getMachineInstance() {
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
        private final SizedIngredient inputItem;
        private final FluidStack outputPureFluid;
        private final FluidStack outputByproductFluid;
        private final int ticks;

        private static final HashMap<SizedIngredient, ResourceLocation> idMap = new HashMap<>();

        // Add a constructor that sets all properties.
        public Recipe(ResourceLocation id, SizedIngredient inputItem, FluidStack outputPureFluid, FluidStack outputByproductFluid, int ticks) {
            this.inputItem = inputItem;
            this.outputPureFluid = outputPureFluid;
            this.outputByproductFluid = outputByproductFluid;
            this.ticks = ticks;
            idMap.put(inputItem, id);
        }

        public Recipe(SizedIngredient inputItem, FluidStack outputPureFluid, FluidStack outputByproductFluid, int ticks) {
            this.inputItem = inputItem;
            this.outputPureFluid = outputPureFluid;
            this.outputByproductFluid = outputByproductFluid;
            this.ticks = ticks;
            idMap.put(inputItem, Arrays.stream(inputItem.getItems()).findFirst()
                    .map(s -> BuiltInRegistries.ITEM.getKey(s.getItem()))
                    .map(l -> ResourceLocation.fromNamespaceAndPath(
                            l.getNamespace(), "melting/" + l.getPath()
                    ))
                    .orElse(null)
            );
        }

        public SizedIngredient getInputItem() {
            return inputItem;
        }

        public FluidStack getOutputPureFluid() {
            return outputPureFluid;
        }

        public FluidStack getOutputByproductFluid() {
            return outputByproductFluid;
        }

        // Check whether the given input matches this recipe. The first parameter matches the generic.
        // We check our block state and our item stack, and only return true if both match.
        @Override
        public boolean matches(ItemsFluidsRecipeInput input, Level level) {
            return input.test(List.of(inputItem), List.of());
        }

        // Return the result of the recipe here, based on the given input. The first parameter matches the generic.
        // IMPORTANT: Always call .copy() if you use an existing result! If you don't, things can and will break,
        // as the result exists once per recipe, but the assembled stack is created each time the recipe is crafted.
        @Override
        public @NotNull ItemStack assemble(ItemsFluidsRecipeInput input, HolderLookup.Provider registries) {
            return ItemStack.EMPTY; // Only creates fluids.
        }

        // Grid-based recipes should return whether their recipe can fit in the given dimensions.
        // We don't have a grid, so we just return if any item can be placed in there.
        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return width * height >= 1;
        }

        // Return an UNMODIFIABLE version of your result here. The result of this method is mainly intended
        // for the recipe book, and commonly used by JEI and other recipe viewers as well.
        @Override
        public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
            return ItemStack.EMPTY; // Only creates fluids.
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
            protected final SizedIngredient inputItem;
            protected final FluidStack pureResult;
            protected final FluidStack byproductResult;
            protected final int ticks;

            public Builder(SizedIngredient inputItem, FluidStack pureResult, FluidStack byproductResult, int ticks) {
                this.inputItem = inputItem;
                this.pureResult = pureResult;
                this.byproductResult = byproductResult;
                this.ticks = ticks;
            }

            @Override
            public @NotNull RecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
                return this;
            }

            @Override
            public @NotNull Recipe.Builder group(@org.jetbrains.annotations.Nullable String group) {
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
                        "melting/" + BuiltInRegistries.ITEM.getKey(firstStack.getItem()).getPath());
            }

            @Override
            public void save(@NotNull RecipeOutput recipeOutput) {
                this.save(recipeOutput, getDefaultRecipeId(inputItem.ingredient()));
            }

            @Override
            public void save(@NotNull RecipeOutput recipeOutput, @NotNull String id) {
                ResourceLocation resourceLocation = getDefaultRecipeId(inputItem.ingredient());
                ResourceLocation idLocation = ResourceLocation.parse(id);
                if (ResourceLocation.parse(id).equals(resourceLocation)) {
                    throw new IllegalStateException("Recipe " + id + " should remove its 'save' argument as it is equal to default one");
                } else {
                    this.save(recipeOutput, idLocation);
                }
            }

            @Override
            public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
                Recipe recipe = new Recipe(id, this.inputItem, this.pureResult, this.byproductResult, this.ticks);
                recipeOutput.accept(id, recipe, null);
            }
        }


        public static class Serializer implements RecipeSerializer<Recipe> {
            public static final MapCodec<Recipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    SizedIngredient.FLAT_CODEC.fieldOf("ingredient").forGetter(Recipe::getInputItem),
                    FluidStack.CODEC.fieldOf("pure_result").forGetter(Recipe::getOutputPureFluid),
                    FluidStack.OPTIONAL_CODEC.fieldOf("byproduct_result").forGetter(Recipe::getOutputByproductFluid),
                    Codec.INT.fieldOf("ticks").forGetter(Recipe::getTicks)
            ).apply(inst, Recipe::new));

            public static final StreamCodec<RegistryFriendlyByteBuf, Recipe> STREAM_CODEC =
                    StreamCodec.composite(
                            SizedIngredient.STREAM_CODEC, Recipe::getInputItem,
                            FluidStack.STREAM_CODEC, Recipe::getOutputPureFluid,
                            FluidStack.OPTIONAL_STREAM_CODEC, Recipe::getOutputByproductFluid,
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
                    Properties.PURE_FLUID_OUTPUT,
                    Properties.BYPRODUCT_FLUID_OUTPUT
            );
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
            this.addSlot(new Slot(container, 2, 44, 39 + 5));
        }

        @Override
        public @Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
            // index 2 - dirty concocti tank
            if (!this.getSlot(2).hasItem() && !this.moveItemStackTo(movedStack, 2, 3, true)) {
                return ItemStack.EMPTY;
            }
            return null;
        }

        public FluidStack getPureFluidStack() {
            return viewer.get(Properties.PURE_FLUID_OUTPUT);
        }

        public FluidStack getByproductFluidStack() {
            return viewer.get(Properties.BYPRODUCT_FLUID_OUTPUT);
        }

        public int getMaxFluidLeft() {
            return BlockEntity.TANK_CAPACITY;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Screen extends AbstractConcoctiMachineScreen<Menu> {
        private final FluidBars.Tall<Menu> pureFluid = new FluidBars.Tall<>(105, 28, this, menu, 0);
        private final FluidBars.Tall<Menu> byproductFluid = new FluidBars.Tall<>(129, 28, this, menu, 1);

        private final EnergyBar<Menu> energyBar = new EnergyBar<>(10, 18, this, menu);
        private final ArrowProgress arrowProgress = new ArrowProgress(79 - 7, 34 + 10);

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
                    pureFluid,
                    byproductFluid
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
            pureFluid.update(menu.getPureFluidStack(), menu.getMaxFluidLeft());
            byproductFluid.update(menu.getByproductFluidStack(), menu.getMaxFluidLeft());

            renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());
        }
    }

    public static class RecipeCategory extends AbstractConcoctiRecipeCategory<Recipe> {

        @Override
        protected ConcoctiMelter getMachineInstance() {
            return INSTANCE;
        }

        public RecipeCategory(IGuiHelper guiHelper) {
            super(guiHelper, new ItemStack(INSTANCE.BLOCK.get()));
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull mezz.jei.api.recipe.RecipeType<Recipe> getRecipeType() {
            return (mezz.jei.api.recipe.RecipeType<Recipe>) INSTANCE.getJeiRecipeType();
        }

        @Override
        public @NotNull Component getTitle() {
            return Component.translatable("block.concocti." + ID);
        }

        @Override
        public void setRecipe(@NotNull IRecipeLayoutBuilder builder, Recipe recipe, @NotNull IFocusGroup focuses) {
            // Add the recipe input.
            builder.addSlot(RecipeIngredientRole.INPUT, 22, 6)
                    .addItemStacks(List.of(recipe.getInputItem().getItems()))
                    .setSlotName("input");
            // Add the fluid outputs.
            FluidStack[] outputs = new FluidStack[]{recipe.getOutputPureFluid().copy(), recipe.getOutputByproductFluid().copy()};
            int i = 0;
            for (FluidStack fluid : outputs) {
                if (!fluid.isEmpty()) builder.addSlot(RecipeIngredientRole.OUTPUT, 113 + i * 18, 6)
                        .addIngredient(NeoForgeTypes.FLUID_STACK, fluid)
                        .setSlotName("fluid" + i);
                i++;
            }
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
