package io.github.laptop59.concocti.common.machine.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.laptop59.concocti.client.gui.AbstractConcoctiMachineScreen;
import io.github.laptop59.concocti.client.gui.components.*;
import io.github.laptop59.concocti.common.block.AbstractConcoctiMachineBlock;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.block.entity.DynamicEnergyStorage;
import io.github.laptop59.concocti.common.detail.DetailCodec;
import io.github.laptop59.concocti.common.detail.DetailHolder;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.machine.AbstractConcoctiRecipeCategory;
import io.github.laptop59.concocti.common.machine.ConcoctiMachine;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetails;
import io.github.laptop59.concocti.common.machine.InputOutput;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenuSyncedExtra;
import io.github.laptop59.concocti.common.recipe.FluidOutput;
import io.github.laptop59.concocti.common.recipe.LightningRecipeInput;
import io.github.laptop59.concocti.common.recipe.LightningState;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.Criterion;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

    // Fluids
    public static final int FLUID_OUTPUT = 0;

    public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, LightningState, LightningRecipeInput, Recipe>> getDetails() {
        return () -> new ConcoctiMachineDetails<>(
                BlockEntity.class,
                100_000,
                100_000,
                2,
                100.0f,
                DynamicEnergyStorage.Mode.INPUT_ONLY.toSupplier(),
                INSTANCE.RECIPE_TYPE,
                Component.translatable("block.concocti.concocti_electron_collector"),
                List.of(SlotType.FLUID_OUTPUT),
                Menu::new,
                new EnumMap<>(SlotType.class),
                new EnumMap<>(
                        Map.of(
                                SlotType.FLUID_OUTPUT, List.of(blockEntity -> blockEntity.fluidOutput.get())
                        )
                ),
                InputOutput.empty(),
                InputOutput.onlyOutputs(blockEntity -> List.of(blockEntity.fluidOutput.get())),
                null
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

    public static class BlockEntity extends AbstractConcoctiMachineBlockEntity
            <BlockEntity, Menu, LightningState, LightningRecipeInput, Recipe> {

        private final DetailHolder<FluidTank> fluidOutput = new DetailHolder<>(
                DetailCodec.FLUID_TANK, "fluid_output", new FluidTank(TANK_CAPACITY), this
        );
        private final DetailHolder<LightningState> lightningState = new DetailHolder<>(
                DetailCodec.LIGHTNING_STATE, "lightning_state", new LightningState(false), this
        );

        @Override
        protected ConcoctiElectronCollector getMachineInstance() {
            return INSTANCE;
        }

        @Override
        public List<IFluidHandler> getIndexedFluidHandlers() {
            return List.of(fluidOutput.get());
        }

        public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, LightningState, LightningRecipeInput, Recipe>> getUncachedMachineDetails() {
            return INSTANCE.getDetails();
        }

        @Override
        protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
            return false;
        }

        public BlockEntity(BlockPos pos, BlockState blockState) {
            super(
                    INSTANCE.BLOCK_ENTITY, pos, blockState
            );
        }

        @Override
        public boolean canProcess() {
            if (!super.canProcess()) return false;
            RecipeHolder<Recipe> recipe = getRecipe(lightningState.get());
            if (recipe == null) return false;
            // Check whether the fluids obtained from this item will not exceed our fluid limit.
            FluidStack result = recipe.value().getOutput().stack().copy();
            return fluidOutput.get().fill(result, IFluidHandler.FluidAction.SIMULATE) == result.getAmount();
        }

        @Override
        protected LightningRecipeInput recipeInputFrom(LightningState lightningState) {
            return new LightningRecipeInput(lightningState);
        }

        @Override
        protected LightningState getInput() {
            return lightningState.get();
        }

        @Override
        protected void onRecipeCompleted(Recipe recipe) {
            if (Math.random() < recipe.getOutput().chance())
                fluidOutput.get().fill(recipe.getOutput().stack(), IFluidHandler.FluidAction.EXECUTE);
            lightningState.get().setLightningCollected(false);
        }

        @Override
        public List<IFluidTank> getFluidTanks() {
            return List.of(fluidOutput.get());
        }

        public void markLightningState() {
            lightningState.get().setLightningCollected(true);
        }
    }

    public static class Block extends AbstractConcoctiMachineBlock<Block> {
        protected Block(Properties properties) {
            super(properties);
        }

        @Override
        protected ConcoctiElectronCollector getMachineInstance() {
            return INSTANCE;
        }

        @Override
        protected Function<Properties, Block> getBlockConstructor() {
            return Block::new;
        }
    }

    public static class Recipe implements ProcessingRecipe<Recipe, LightningRecipeInput> {
        // An in-code representation of our recipe data. This can be basically anything you want.
        // Common things to have here is a processing time integer of some kind, or an experience reward.
        // Note that we now use an ingredient instead of an item stack for the input.
        private final FluidOutput output;
        private final int ticks;

        // Add a constructor that sets all properties.
        public Recipe(FluidOutput output, int ticks) {
            this.output = output;
            this.ticks = ticks;
        }

        @NotNull
        public FluidOutput getOutput() {
            return output;
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

        public static class Builder implements RecipeBuilder {
            private final FluidOutput output;
            private final int ticks;

            public Builder(FluidOutput output, int ticks) {
                this.output = output;
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
                        this.output,
                        this.ticks
                );
                recipeOutput.accept(id, recipe, null);
            }
        }


        public static class Serializer implements RecipeSerializer<Recipe> {
            public static final MapCodec<Recipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    FluidOutput.CODEC.fieldOf("output").forGetter(Recipe::getOutput),
                    Codec.INT.fieldOf("ticks").forGetter(Recipe::getTicks)
            ).apply(inst, Recipe::new));

            public static final StreamCodec<RegistryFriendlyByteBuf, Recipe> STREAM_CODEC =
                    StreamCodec.composite(
                            FluidOutput.STREAM_CODEC, Recipe::getOutput,
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

    public static class Menu extends AbstractConcoctiMachineMenuSyncedExtra<Menu, Extra> {
        // Client
        public Menu(
                int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf
        ) {
            super(containerId, playerInventory, 2, buf, INSTANCE.MENU);
        }

        // Server
        public Menu(int containerId, Inventory playerInventory, Container container) {
            super(containerId, playerInventory, container, INSTANCE.MENU);
        }

        @Override
        protected void addOtherSlots() {
        }

        @Override
        public @org.jetbrains.annotations.Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
            return null;
        }

        public FluidStack getFluidOutput() {
            return syncedFluids.get(FLUID_OUTPUT);
        }

        public int getMaxFluidOutput() {
            return AbstractConcoctiMachineBlockEntity.TANK_CAPACITY;
        }

        public LightningState getLightningState() {
            return syncedExtra.lightningState();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Screen extends AbstractConcoctiMachineScreen<Menu> {
        private final FluidBars.Tall<Menu> outputFluid = new FluidBars.Tall<>((getWidth() + 50) / 2, 25, this, menu, 0);

        private final EnergyBar<Menu> energyBar = new EnergyBar<>(10, 18, this, menu);
        private final ArrowProgress arrowProgress = new ArrowProgress(94 - 17, 37);

        public static final ResourceLocation LIGHTNING_STATE_ON_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/lightning_state/on");
        public static final ResourceLocation LIGHTNING_STATE_OFF_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/lightning_state/off");

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
                    outputFluid,
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

            ResourceLocation lightningStateSprite;
            if (menu.getLightningState().getLightningCollected()) {
                lightningStateSprite = LIGHTNING_STATE_ON_SPRITE;
            } else {
                lightningStateSprite = LIGHTNING_STATE_OFF_SPRITE;
            }
            guiGraphics.blitSprite(
                    lightningStateSprite,
                    renderInfo.left() + 55,
                    renderInfo.top() + 37,
                    16,
                    16
            );

            arrowProgress.update(menu.getProgress());

            energyBar.update(menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true));

            // Render the fluids.
            outputFluid.update(menu.getFluidOutput(), menu.getMaxFluidOutput());

            renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());
        }
    }

    public static class RecipeCategory extends AbstractConcoctiRecipeCategory<Recipe> {

        @Override
        protected ConcoctiElectronCollector getMachineInstance() {
            return INSTANCE;
        }

        @Override
        public @NotNull Object getJeiRecipeType() {
            return INSTANCE.getJeiRecipeType();
        }

        @Override
        public @NotNull Component getTitle() {
            return Component.translatable("block.concocti.concocti_electron_collector");
        }

        @Override
        public int getWidth(@NotNull Recipe recipe) {
            return 108 - 8;
        }

        @Override
        public void set(@NotNull io.github.laptop59.concocti.common.machine.RecipeBuilder builder, @NotNull Recipe recipe) {
            // Add the fluid output.
            builder.addOutputSlot(getWidth(recipe) - 6 - 18, 16, recipe.getOutput());

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

            builder.addCatalystSlot(8, 16, Ingredient.of(rod));
        }
    }

    public record Extra(LightningState lightningState) {
        public static StreamCodec<ByteBuf, Extra> STREAM_CODEC = LightningState.STREAM_CODEC.map(
                Extra::new,
                Extra::lightningState
        );
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ?> getExtraDataStreamCodec() {
        return Extra.STREAM_CODEC;
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
}
