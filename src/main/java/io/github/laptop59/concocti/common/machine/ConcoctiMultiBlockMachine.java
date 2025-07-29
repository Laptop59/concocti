package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.client.gui.AbstractConcoctiMachineScreen;
import io.github.laptop59.concocti.client.gui.components.*;
import io.github.laptop59.concocti.common.abstraction.Complexion;
import io.github.laptop59.concocti.common.block.AbstractConcoctiMultiBlockControllerBlock;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMultiblockBlockEntity;
import io.github.laptop59.concocti.common.block.entity.DynamicEnergyStorage;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluidTankHandler;
import io.github.laptop59.concocti.common.menu.ConcoctiMultiblockMenu;
import io.github.laptop59.concocti.common.multiblock.MultiblockStructure;
import io.github.laptop59.concocti.common.recipe.AbstractConcoctiMultiblockRecipe;
import io.github.laptop59.concocti.common.recipe.ItemsFluidsRecipeInput;
import io.github.laptop59.concocti.common.util.Lazy;
import io.github.laptop59.concocti.integration.jei.AbstractConcoctiMultiblockRecipeCategory;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConcoctiMultiBlockMachine extends ConcoctiMachineOnlyItemsFluids<
        ConcoctiMultiBlockMachine.BlockEntity,
        ConcoctiMultiblockMenu,
        ConcoctiMultiBlockMachine.Recipe,
        ConcoctiMultiBlockMachine.Recipe.Serializer,
        ConcoctiMultiBlockMachine.Block,
        ConcoctiMultiBlockMachine.Screen,
        ConcoctiMultiBlockMachine.RecipeCategory
> {
    final String id;
    final float rateConsumption;
    public final MultiblockStructure STRUCTURE;

    public static Map<String, ConcoctiMultiBlockMachine> INSTANCES = new HashMap<>();

    public static ConcoctiMultiBlockMachine getInstance(String id) {
        return INSTANCES.get(id);
    }

    public ConcoctiMultiBlockMachine(String id, float rateConsumption, MultiblockStructure multiblockStructure) {
        super(
                id,
                BlockBehaviour.Properties
                        .of()
                        .mapColor(DyeColor.PURPLE)
                        .requiresCorrectToolForDrops()
                        .explosionResistance(150f)
                        .strength(200f)
                        .lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 13 : 0),
                new ConcoctiBlocks.BlockData(ConcoctiBlocks.BlockToolRank.DIAMOND, ConcoctiBlocks.BlockToolType.PICKAXE)
        );
        this.id = id;
        this.rateConsumption = rateConsumption;
        this.STRUCTURE = multiblockStructure;
        INSTANCES.put(id, this);
    }

    public Supplier<ConcoctiMachineDetails<BlockEntity, ConcoctiMultiblockMenu, ItemsFluidsInputValue, ItemsFluidsRecipeInput, Recipe>> getDetails() {
        return () -> new ConcoctiMachineDetails<>(
                BlockEntity.class,
                0,
                0,
                2,
                rateConsumption,
                DynamicEnergyStorage.Mode.NONE.toSupplier(),
                this.RECIPE_TYPE,
                Component.translatable("block.concocti." + id),
                List.of(),
                (a, b, c, d) -> new ConcoctiMultiblockMenu(getInstance(id).MENU, a, b, c, d),
                b -> b.getDataAccess(),
                new EnumMap<>(SlotType.class),
                new EnumMap<>(SlotType.class),
                InputOutput.empty(),
                InputOutput.empty(),
                null
        );
    }

    @Override
    public BlockEntityConstructor<BlockEntity> getBlockEntityConstructor() {
        return (blockPos, blockState) -> new BlockEntity(id, BLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    public BlockConstructor<Block> getBlockConstructor() {
        return (props) -> new Block(id, props);
    }

    @Override
    public MenuClientConstructor<ConcoctiMultiblockMenu> getMenuClientConstructor() {
        return (containerId, inventory) -> new ConcoctiMultiblockMenu(getInstance(id).MENU, containerId, inventory);
    }

    @Override
    public ScreenConstructor<ConcoctiMultiblockMenu, Screen> getScreenConstructor() {
        return Screen::new;
    }

    @Override
    public RecipeSerializerConstructor<Recipe.Serializer, Recipe, ItemsFluidsRecipeInput> getRecipeSerializerConstructor() {
        return () -> new Recipe.Serializer(id);
    }

    @Override
    public RecipeCategoryConstructor<RecipeCategory, Recipe, ItemsFluidsRecipeInput> getRecipeCategoryConstructor() {
        return (helper) -> new RecipeCategory(id, helper, new ItemStack(ITEM.asItem(), 1));
    }

    @Override
    public Class<Recipe> getRecipeClass() {
        return Recipe.class;
    }

    public static class BlockEntity extends AbstractConcoctiMultiblockBlockEntity<BlockEntity, Recipe> {
        String machineId;

        public BlockEntity(String machineId, Supplier<BlockEntityType<BlockEntity>> blockEntityType, BlockPos pos, BlockState blockState) {
            // Our extra data is our machine ID.
            super(blockEntityType, pos, blockState, machineId);
        }

        @Override
        public void postConstructor(Supplier<BlockEntityType<BlockEntity>> blockEntityType, BlockPos pos, BlockState blockState, Object... extraData) {
            // We want to initialize our machine ID before continuing - this is because it needs to
            // fetch our MachineSettings, only possible when the ID is initialized.
            //
            // Also, we are in a potentially dangerous state. Some properties aren't initialized yet here!
            this.machineId = (String) extraData[0];
            // Now the rest of the object is constructed here.
            super.postConstructor(blockEntityType, pos, blockState, extraData);
        }

        public Complexion getDataAccess() {
            return dataAccess;
        }

        @Override
        protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
            return true;
        }

        @Override
        public Supplier<RecipeType<Recipe>> getRecipeType() {
            return getInstance(machineId).RECIPE_TYPE;
        }

        @Override
        protected @NotNull Component getDefaultName() {
            return Component.translatable("block.concocti." + machineId);
        }

        @Override
        public MultiblockStructure getStructure() {
            return getInstance(machineId).STRUCTURE;
        }

        @Override
        public List<IFluidTank> getFluidTanks() {
            return List.of();
        }

        @Override
        protected ConcoctiMachine<BlockEntity, ConcoctiMultiblockMenu, ItemsFluidsInputValue, ItemsFluidsRecipeInput, Recipe, ?, ?, ?, ?> getMachineInstance() {
            return getInstance(machineId);
        }

        @Override
        protected Class<Recipe> getRecipeClass() {
            return getInstance(machineId).getRecipeClass();
        }
    }

    public static class Recipe extends AbstractConcoctiMultiblockRecipe<Recipe> {
        String machineId;

        public Recipe(String machineId, ResourceLocation id, List<SizedIngredient> inputItems, List<ItemStack> outputItems, List<SizedFluidIngredient> inputFluids, List<FluidStack> outputFluids, int ticks) {
            super(id, inputItems, outputItems, inputFluids, outputFluids, ticks);
            this.machineId = machineId;
        }

        public Recipe(String machineId, List<SizedIngredient> inputItems, List<ItemStack> outputItems, List<SizedFluidIngredient> inputFluids, List<FluidStack> outputFluids, int ticks) {
            super(inputItems, outputItems, inputFluids, outputFluids, ticks);
            this.machineId = machineId;
        }

        @Override
        public @NotNull RecipeSerializer<?> getSerializer() {
            return getInstance(machineId).RECIPE_SERIALIZER.get();
        }

        @Override
        public @NotNull RecipeType<?> getType() {
            return getInstance(machineId).RECIPE_TYPE.get();
        }

        public static class Serializer extends AbstractConcoctiMultiblockRecipe.Serializer<Recipe> {
            String machineId;

            public Serializer(String machineId) {
                this.machineId = machineId;
            }

            @Override
            public Recipe construct(List<SizedIngredient> inputItems, List<ItemStack> outputItems, List<SizedFluidIngredient> inputFluids, List<FluidStack> outputFluids, int ticks) {
                return new Recipe(machineId, inputItems, outputItems, inputFluids, outputFluids, ticks);
            }
        }

        public static class Builder extends AbstractConcoctiMultiblockRecipe.Builder<Recipe> {
            String machineId;

            public Builder(String machineId, ResourceLocation resourceLocation, List<SizedIngredient> inputItems, List<ItemStack> outputItems, List<SizedFluidIngredient> inputFluids, List<FluidStack> outputFluids, int ticks) {
                super(resourceLocation, inputItems, outputItems, inputFluids, outputFluids, ticks);
                this.machineId = machineId;
            }

            @Override
            public Recipe construct(ResourceLocation id, List<SizedIngredient> inputItems, List<ItemStack> outputItems, List<SizedFluidIngredient> inputFluids, List<FluidStack> outputFluids, int ticks) {
                return new Recipe(machineId, id, inputItems, outputItems, inputFluids, outputFluids, ticks);
            }
        }
    }

    public static class Block extends AbstractConcoctiMultiBlockControllerBlock<Block> {
        String machineId;

        protected Block(String machineId, Properties properties) {
            super(properties);
            this.machineId = machineId;
        }

        @Override
        protected Function<Properties, Block> getBlockConstructor() {
            return properties -> new Block(machineId, properties);
        }

        @Override
        protected ConcoctiMachine<?, ?, ?, ?, ?, ?, Block, ?, ?> getMachineInstance() {
            return getInstance(machineId);
        }

        @Override
        protected @Nullable SoundEvent getCracklingSoundEvent() {
            return null;
        }

        @Override
        public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
            ConcoctiMultiBlockMachine machine = getInstance(machineId);
            return new BlockEntity(machineId, machine.BLOCK_ENTITY, pos, state);
        }

        @Override
        public BlockEntityType<? extends net.minecraft.world.level.block.entity.BlockEntity> getBlockEntityType() {
            return getMachineInstance().BLOCK_ENTITY.get();
        }
    }

    public static class RecipeCategory extends AbstractConcoctiMultiblockRecipeCategory<Recipe> {
        String machineId;

        public RecipeCategory(String machineId, IGuiHelper guiHelper, ItemStack icon) {
            super(guiHelper, icon);
            this.machineId = machineId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public mezz.jei.api.recipe.@NotNull RecipeType<Recipe> getRecipeType() {
            return (mezz.jei.api.recipe.RecipeType<Recipe>) getMachineInstance().JEI_RECIPE_TYPE.get();
        }

        @Override
        public @NotNull Component getTitle() {
            return Component.translatable("block.concocti." + machineId);
        }

        @Override
        protected ConcoctiMachine<?, ?, ?, ?, Recipe, ?, ?, ?, ?> getMachineInstance() {
            return getInstance(machineId);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Screen extends AbstractConcoctiMachineScreen<ConcoctiMultiblockMenu> {
        private final ArrowProgress arrowProgress = new ArrowProgress(106, 34);

        public Screen(
                ConcoctiMultiblockMenu menu,
                Inventory playerInventory,
                Component title
        ) {
            super(menu, playerInventory, title);
        }

        public List<Renderable> getUniqueChildren() {
            return List.of(
                    arrowProgress
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
            renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());
            guiGraphics.drawString(font, menu.isValid() ? "VALID" : "INVALID", 10, 10, 0xFFFFFFFF);
        }
    }
}
