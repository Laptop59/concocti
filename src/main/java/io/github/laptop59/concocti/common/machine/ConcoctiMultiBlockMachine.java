package io.github.laptop59.concocti.common.machine;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.laptop59.concocti.client.gui.AbstractConcoctiMachineScreen;
import io.github.laptop59.concocti.client.gui.components.*;
import io.github.laptop59.concocti.common.abstraction.Complexion;
import io.github.laptop59.concocti.common.block.*;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMultiblockBlockEntity;
import io.github.laptop59.concocti.common.block.entity.DynamicEnergyStorage;
import io.github.laptop59.concocti.common.menu.ConcoctiEnergyHatchMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiMultiblockMenu;
import io.github.laptop59.concocti.common.multiblock.MultiblockStructure;
import io.github.laptop59.concocti.common.recipe.*;
import io.github.laptop59.concocti.network.ConcoctiMachineSettingsBuildPreviewChangeC2S;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.Concocti.MODID;

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
                BlockEntity::getDataAccess,
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
        return () -> new RecipeCategory(id);
    }

    @Override
    public Class<Recipe> getRecipeClass() {
        return Recipe.class;
    }

    public static class BlockEntity extends AbstractConcoctiMultiblockBlockEntity<BlockEntity, Recipe> {
        String machineId;

        public BlockEntity(String machineId, Supplier<BlockEntityType<BlockEntity>> blockEntityType, BlockPos pos, BlockState blockState) {
            // Our extra data is our machine ID.
            super(blockEntityType, pos, blockState, machineId, getInstance(machineId).STRUCTURE);
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
            this.structure = (MultiblockStructure) extraData[1];
            updateHatchPositions();
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

        public Recipe(String machineId, ResourceLocation id, List<ItemRecipeIngredient> inputItems, List<ItemOutput> outputItems, List<FluidRecipeIngredient> inputFluids, List<FluidOutput> outputFluids, int ticks) {
            super(id, inputItems, outputItems, inputFluids, outputFluids, ticks);
            this.machineId = machineId;
        }

        public Recipe(String machineId, List<ItemRecipeIngredient> inputItems, List<ItemOutput> outputItems, List<FluidRecipeIngredient> inputFluids, List<FluidOutput> outputFluids, int ticks) {
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
            public Recipe construct(List<ItemRecipeIngredient> inputItems, List<ItemOutput> outputItems, List<FluidRecipeIngredient> inputFluids, List<FluidOutput> outputFluids, int ticks) {
                return new Recipe(machineId, inputItems, outputItems, inputFluids, outputFluids, ticks);
            }
        }

        public static class Builder extends AbstractConcoctiMultiblockRecipe.Builder<Recipe> {
            String machineId;

            public Builder(String machineId, ResourceLocation resourceLocation, List<ItemRecipeIngredient> inputItems, List<ItemOutput> outputItems, List<FluidRecipeIngredient> inputFluids, List<FluidOutput> outputFluids, int ticks) {
                super(resourceLocation, inputItems, outputItems, inputFluids, outputFluids, ticks);
                this.machineId = machineId;
            }

            @Override
            public Recipe construct(ResourceLocation id, List<ItemRecipeIngredient> inputItems, List<ItemOutput> outputItems, List<FluidRecipeIngredient> inputFluids, List<FluidOutput> outputFluids, int ticks) {
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

        public RecipeCategory(String machineId) {
            this.machineId = machineId;
        }

        @Override
        public @NotNull Object getJeiRecipeType() {
            return getMachineInstance().getJeiRecipeType();
        }

        @Override
        public @NotNull Component getTitle() {
            return Component.translatable("block.concocti." + machineId);
        }

        @Override
        protected ConcoctiMachine<?, ?, ?, ?, Recipe, ?, ?, ?, ?> getMachineInstance() {
            return getInstance(machineId);
        }

        @Override
        public void render(@NotNull Recipe recipe, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
            super.render(recipe, guiGraphics, mouseX, mouseY);
            /*
            for (int x : drawInfo.slots()) {
                guiGraphics.blit(SLOT, x - 1, 16 - 1, 0, 0, 18, 18, 18, 18);
            }
             */
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Screen extends AbstractConcoctiMachineScreen<ConcoctiMultiblockMenu> {
        private final ProgressBar<ConcoctiMultiblockMenu> progressBar = new ProgressBar<>(8, 65, this, menu);

        public static final ResourceLocation BUILD_PREVIEW_OFF = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/build_preview_off.png");
        public static final ResourceLocation BUILD_PREVIEW_ON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/build_preview_on.png");

        private long latestSpeedyBarStart = 0;
        private boolean wasSpeedy = false;

        public Screen(
                ConcoctiMultiblockMenu menu,
                Inventory playerInventory,
                Component title
        ) {
            super(menu, playerInventory, title);
        }

        public List<Renderable> getUniqueChildren() {
            return List.of(
                    progressBar
            );
        }

        @Override
        public List<Renderable> getChildren() {
            ArrayList<Renderable> children = new ArrayList<>(super.getChildren());
            children.addAll(getUniqueChildren());
            return List.copyOf(children);
        }

        protected boolean isMachineSettingsCogwheelUsable() {
            return false;
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, RenderInfo renderInfo, float partialTick) {
            // Don't forget to first render the abstract screen!
            super.render(guiGraphics, renderInfo, partialTick);

            float progress = menu.isValid() ? menu.getInterpolatedProgress(partialTick) : 0;

            {
                double perTick = menu.isValid() ? menu.getProgressCompletedPerTick() : 0;
                boolean speedy = perTick >= 0.25;

                guiGraphics.drawString(
                        font,
                        Component.translatable("screen.concocti.progress"),
                        leftPos + 7,
                        topPos + 55,
                        0xFF3C2F47,
                        false
                );

                String progressText = String.format("%.2f/t", perTick);
                guiGraphics.drawString(
                        font,
                        Component.literal(progressText),
                        leftPos + 152 - font.width(progressText),
                        topPos + 55,
                        0xFF3C2F47,
                        false
                );

                if (speedy) {
                    if (!wasSpeedy) {
                        latestSpeedyBarStart = System.currentTimeMillis();
                    }

                    float max = (float) (Math.log10(perTick + 1) / 3);

                    // Don't want to flash the user - we add some animation
                    progressBar.update(
                        Math.clamp((float) (System.currentTimeMillis() - latestSpeedyBarStart) / 2000, 0, Math.max(0, max)),
                        true
                    );
                } else {
                    progressBar.update(progress, false);
                }
                wasSpeedy = speedy;
            }

            renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());

            boolean pullOn = menu.showBuildPreview();
            int color = menu.isValid() ? 0xFF004F00 : 0xFF4F0000;
            guiGraphics.drawString(
                    font,
                    Component.translatable("screen.concocti." + (menu.isValid() ? "valid" : "invalid")),
                    leftPos + 8,
                    topPos + 16,
                    color,
                    false
            );

            {
                RenderInfo pullRenderInfo = renderInfo.offset(imageWidth - 24, 64);
                if (pullRenderInfo.isHovering(16, 16)) {
                    RenderSystem.setShaderColor(1.1f, 1.1f, 1.1f, 1.1f);
                    pullRenderInfo.renderTooltip(
                            guiGraphics, Component.translatable(
                                    "screen.concocti.build_preview_" + (pullOn ? "on" : "off")
                            ).withColor(0xff7a7a7a)
                    );
                }
                guiGraphics.blit(
                        pullOn ? BUILD_PREVIEW_ON : BUILD_PREVIEW_OFF,
                        pullRenderInfo.left(),
                        pullRenderInfo.top(),
                        0, 0,
                        16, 16,
                        16, 16
                );
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            RenderInfo renderInfo = new RenderInfo((int) mouseX, (int) mouseY, leftPos, topPos, font);
            RenderInfo pureRenderInfo = renderInfo.offset(imageWidth - 24, 64);
            if (pureRenderInfo.isHovering(16, 16)) {
                Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value());
                PacketDistributor.sendToServer(new ConcoctiMachineSettingsBuildPreviewChangeC2S(menu.containerId));
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
