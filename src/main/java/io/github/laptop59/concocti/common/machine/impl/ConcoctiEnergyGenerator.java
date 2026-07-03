package io.github.laptop59.concocti.common.machine.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.laptop59.concocti.client.gui.AbstractConcoctiMachineScreen;
import io.github.laptop59.concocti.client.gui.components.*;
import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.ConcoctiSounds;
import io.github.laptop59.concocti.common.abstraction.ConcoctiMachineComplexion;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.AbstractConcoctiMachineBlock;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineOnlyItemsFluidsBlockEntity;
import io.github.laptop59.concocti.common.block.entity.DynamicEnergyStorage;
import io.github.laptop59.concocti.common.machine.*;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import io.github.laptop59.concocti.common.recipe.ItemRecipeIngredient;
import io.github.laptop59.concocti.common.recipe.ItemsFluidsRecipeInput;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.common.machine.AbstractConcoctiRecipeCategory;
import net.minecraft.advancements.Criterion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.github.laptop59.concocti.common.Concocti.MODID;
import static io.github.laptop59.concocti.common.machine.impl.ConcoctiEnergyGenerator.Block.LIT;

public class ConcoctiEnergyGenerator extends ConcoctiMachineOnlyItemsFluids<
        ConcoctiEnergyGenerator.BlockEntity,
        ConcoctiEnergyGenerator.Menu,
        ConcoctiEnergyGenerator.Recipe,
        ConcoctiEnergyGenerator.Recipe.Serializer,
        ConcoctiEnergyGenerator.Block,
        ConcoctiEnergyGenerator.Screen,
        ConcoctiEnergyGenerator.RecipeCategory
        > {
    static ConcoctiEnergyGenerator INSTANCE;

    // Details
    public final static String ID = "concocti_energy_generator";

    public final static int INPUT_OUTPUT_SLOT = 2;

    public Supplier<ConcoctiMachineDetails<BlockEntity, Menu, ItemsFluidsInputValue, ItemsFluidsRecipeInput, Recipe>> getDetails() {
        return () -> new ConcoctiMachineDetails<>(
                BlockEntity.class,
                100_000,
                100_000,
                3,
                50.0f,
                DynamicEnergyStorage.Mode.OUTPUT_ONLY.toSupplier(),
                INSTANCE.RECIPE_TYPE,
                Component.translatable("block.concocti.concocti_energy_generator"),
                List.of(
                        SlotType.ITEM_INPUT,
                        SlotType.ITEM_OUTPUT,
                        SlotType.ITEM_INPUT_OUTPUT,
                        SlotType.ENERGY_OUTPUT
                ),
                Menu::new,
                blockEntity -> blockEntity.dataAccess,
                new EnumMap<>(
                        Map.of(
                                SlotType.ITEM_INPUT, List.of(INPUT_OUTPUT_SLOT),
                                SlotType.ITEM_INPUT_OUTPUT, List.of(INPUT_OUTPUT_SLOT),
                                SlotType.ITEM_OUTPUT, List.of(INPUT_OUTPUT_SLOT)
                        )
                ),
                new EnumMap<>(SlotType.class),
                InputOutput.of(
                        blockEntity -> List.of(INPUT_OUTPUT_SLOT),
                        blockEntity -> List.of(INPUT_OUTPUT_SLOT)
                ),
                InputOutput.empty(),
                ConcoctiSounds.CONCOCTI_ENERGY_GENERATOR_FIRE_CRACKLE.get()
        );
    }

    public ConcoctiEnergyGenerator() {
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
        // Properties: None

        public static int DEFAULT_ENERGY_PER_TICK = 25;

        @Override
        protected ConcoctiEnergyGenerator getMachineInstance() {
            return INSTANCE;
        }

        @Override
        protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
            return slot == INPUT_OUTPUT_SLOT;
        }

        protected final ConcoctiMachineComplexion dataAccess = new ConcoctiMachineComplexion(this);

        public BlockEntity(BlockPos pos, BlockState blockState) {
            super(
                    INSTANCE.BLOCK_ENTITY, pos, blockState
            );
        }

        public ItemStack getInputStack() {
            return this.getItem(INPUT_OUTPUT_SLOT);
        }

        /**
         * Returns 0 if the input item is not a fuel, otherwise return the number of ticks it would burn for.
         */
        public int getFuelTicksFromOneInputItem() {
            ItemStack input = getInputStack();
            RecipeHolder<Recipe> recipeHolder = getRecipe(getInput());
            if (recipeHolder != null && recipeHolder.value() instanceof Recipe recipe) {
                return recipe.ticks;
            }
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
                    entity.energy.forceReceiveEnergy(getEnergyPerTick() * ticksConsumed, false);
                    entity.ticksLeft -= ticksConsumed;
                } else {
                    entity.lastRecipe = null;
                }
                if (entity.canProcess()) {
                    int ticksToBurn = getFuelTicksFromOneInputItem();
                    {
                        entity.totalTicks = ticksToBurn;
                        entity.ticksLeft += entity.totalTicks;
                        entity.lastRecipe = getRecipe(getInput());
                        ItemStack previousItemStack = getInputStack().copy();
                        getInputStack().shrink(1);
                        if (getInputStack().isEmpty()) {
                            setItem(INPUT_OUTPUT_SLOT, previousItemStack.getCraftingRemainingItem());
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

        protected int getEnergyPerTick() {
            return this.lastRecipe == null ? DEFAULT_ENERGY_PER_TICK : this.lastRecipe.value().fePerTick;
        }

        @Override
        protected void onRecipeCompleted(Recipe recipe) {
        }

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

    public static class Block extends AbstractConcoctiMachineBlock<Block> {
        protected Block(Properties properties) {
            super(properties);
        }

        @Override
        protected ConcoctiEnergyGenerator getMachineInstance() {
            return INSTANCE;
        }

        @Override
        protected Function<Properties, Block> getBlockConstructor() {
            return Block::new;
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
            // Fuel item tank
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
        public void render(@NotNull GuiGraphics guiGraphics, RenderInfo renderInfo, float partialTick) {
            // Don't forget to first render the abstract screen!
            super.render(guiGraphics, renderInfo, partialTick);

            flameProgress.update(menu.getProgress());

            energyBar.update(menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true));

            renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());
        }
    }

    // This serves as a Recipe mostly used as a proxy.
    public static class Recipe implements ProcessingRecipe<Recipe, ItemsFluidsRecipeInput> {
        // An in-code representation of our recipe data. This can be basically anything you want.
        // Common things to have here is a processing time integer of some kind, or an experience reward.
        // Note that we now use an ingredient instead of an item stack for the input.
        private final Ingredient inputItem;
        private final int ticks;
        private final int fePerTick;

        public Recipe(Ingredient inputItem, int ticks, int fePerTick) {
            this.inputItem = inputItem;
            this.fePerTick = fePerTick;
            this.ticks = ticks;
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
        public boolean matches(ItemsFluidsRecipeInput input, @NotNull Level level) {
            return input.test(List.of(ItemRecipeIngredient.of(inputItem)), List.of());
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
        public @NotNull ItemStack assemble(@NotNull ItemsFluidsRecipeInput input, HolderLookup.@NotNull Provider registries) {
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

        public int getFePerTick() {
            return fePerTick;
        }


        public static class Builder implements RecipeBuilder {
            protected final Ingredient inputItem;
            protected final int ticks;
            protected final int fePerTick;

            public Builder(Ingredient inputItem, int ticks, int fePerTick) {
                this.inputItem = inputItem;
                this.ticks = ticks;
                this.fePerTick = fePerTick;
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
            public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
                Recipe recipe = new Recipe(this.inputItem, this.ticks, this.fePerTick);
                recipeOutput.accept(id, recipe, null);
            }
        }


        public static class Serializer implements RecipeSerializer<Recipe> {
            public static final MapCodec<Recipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(Recipe::getInputItem),
                    Codec.INT.fieldOf("ticks").forGetter(Recipe::getTicks),
                    Codec.INT.fieldOf("fe_per_tick").forGetter(Recipe::getFePerTick)
            ).apply(inst, Recipe::new));

            public static final StreamCodec<RegistryFriendlyByteBuf, Recipe> STREAM_CODEC =
                    StreamCodec.composite(
                            Ingredient.CONTENTS_STREAM_CODEC, Recipe::getInputItem,
                            ByteBufCodecs.INT, Recipe::getTicks,
                            ByteBufCodecs.INT, Recipe::getFePerTick,
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

    // This serves as a dummy RecipeCategory.
    public static class RecipeCategory extends AbstractConcoctiRecipeCategory<Recipe> {
        private final FlameProgress flameProgress = new FlameProgress(0, 10);

        @Override
        protected ConcoctiEnergyGenerator getMachineInstance() {
            return INSTANCE;
        }

        @Override
        public @NotNull Object getJeiRecipeType() {
            return INSTANCE.getJeiRecipeType();
        }

        @Override
        public @NotNull Component getTitle() {
            return Component.translatable("block.concocti.concocti_energy_generator");
        }

        @Override
        public int getWidth(@NotNull Recipe recipe) {
            return 120 - 8;
        }

        @Override
        public int getHeight(@NotNull Recipe recipe) {
            return 32 - 8;
        }

        protected int getTotalArrowTop(@NotNull Recipe recipe) {
            return 5;
        }

        @Override
        public void set(@NotNull io.github.laptop59.concocti.common.machine.RecipeBuilder builder, @NotNull Recipe recipe) {
            // Add the recipe input.
            builder.addInputSlot(4, 4, recipe.getInputItem());
        }

        @Override
        protected int getHorizontalArrowOffset(@NotNull ConcoctiEnergyGenerator.Recipe recipe) {
            return -22;
        }

        public void tooltip(@NotNull List<Component> tooltipBuilder, @NotNull Recipe recipe, double mouseX, double mouseY) {
            // Show the duration, if needed.
            super.tooltip(tooltipBuilder, recipe, mouseX, mouseY);
            if (isCursorTouchingArrow(mouseX, mouseY, recipe))
                tooltipBuilder.add(Component.translatable("screen.concocti.duration", (double) getTicks(recipe) / 20));
        }

        @Override
        public void render(@NotNull Recipe recipe, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
            renderBackground(guiGraphics, recipe);
            // Draw the arrow progress.
            long absoluteTicks = System.currentTimeMillis() / 50;
            int tickDuration = getTicks(recipe);
            long passedTicks = absoluteTicks % tickDuration;
            double progress = (double) passedTicks / tickDuration;
            flameProgress.setGuiLeft(28);
            flameProgress.setGuiTop(5);
            flameProgress.update((float) (progress * 23) / 22);
            Renderable.renderChildAbsolute(guiGraphics, RenderInfo.withNullifiedOffset(null), flameProgress);
            String rate = EnergyBar.formatEnergy(recipe.getFePerTick())  + "/t";
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    rate,
                    110 - Minecraft.getInstance().font.width(rate),
                    3,
                    0xFF2d3366, false
            );
            String totalEnergy = EnergyBar.formatEnergy((long) recipe.getFePerTick() * recipe.getTicks());
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    totalEnergy,
                    110 - Minecraft.getInstance().font.width(totalEnergy),
                    13,
                    0xFF363e7e, false
            );
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

    /** Gets ''fake'' recipes (proxies) of fuels. MUST BE CALLED ONLY WHEN IN-GAME! */
    public List<RecipeHolder<Recipe>> getRecipeProxies() {
        assert Minecraft.getInstance().level != null;

        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        final var itemRegistry = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ITEM);
        var datamap = itemRegistry.getDataMap(NeoForgeDataMaps.FURNACE_FUELS);
        ArrayList<RecipeHolder<Recipe>> proxies = new ArrayList<>();
        Set<Ingredient> unproxiedIngredients = recipeManager
            .getAllRecipesFor(ConcoctiMachines.ENERGY_GENERATOR.RECIPE_TYPE.get())
            .stream()
            .map(RecipeHolder::value)
            .map(ConcoctiEnergyGenerator.Recipe::getInputItem)
            .collect(Collectors.toUnmodifiableSet());
        outer:
        for (Map.Entry<ResourceKey<Item>, FurnaceFuel> entry : datamap.entrySet().stream().sorted(Comparator.comparingInt(
            item -> BuiltInRegistries.ITEM.getId(item.getKey())
        )).toList()) {
            var item = BuiltInRegistries.ITEM.get(entry.getKey());
            for (Ingredient ingredient : unproxiedIngredients) {
                if (ingredient.test(new ItemStack(item, 1))) continue outer;
            }
            Recipe proxy = new ConcoctiEnergyGenerator.Recipe(
                Ingredient.of(item),
                entry.getValue().burnTime(),
                ConcoctiEnergyGenerator.BlockEntity.DEFAULT_ENERGY_PER_TICK
            );
            ResourceLocation resourceLocation = entry.getKey().location();
            proxies.add(new RecipeHolder<>(
                    ResourceLocation.fromNamespaceAndPath(
                            MODID, "/proxy/" + ID + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath()
                    ),
                    proxy
            ));
        }
        return proxies;
    }
}
