package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.client.gui.AbstractConcoctiMachineScreen;
import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.ConcoctiRegisters;
import io.github.laptop59.concocti.common.block.AbstractConcoctiMachineBlock;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.common.util.Lazy;
import io.github.laptop59.concocti.integration.jei.AbstractConcoctiRecipeCategory;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.ConcoctiRegisters.*;
import static io.github.laptop59.concocti.common.block.ConcoctiBlocks.registerBlock;
import static io.github.laptop59.concocti.common.item.ConcoctiItems.registerBlockItem;

public abstract class ConcoctiMachine<
        T extends AbstractConcoctiMachineBlockEntity<T, M, V, I, R>,
        M extends AbstractConcoctiMachineMenu<M>,
        V,
        I extends RecipeInput,
        R extends ProcessingRecipe<R, I>,
        Z extends RecipeSerializer<R>,
        B extends AbstractConcoctiMachineBlock<B>,
        S extends AbstractConcoctiMachineScreen<M>,
        C extends AbstractConcoctiRecipeCategory<R>
        > {
    public final DeferredBlock<Block> BLOCK;
    public final DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> BLOCK_ENTITY;
    public final DeferredItem<BlockItem> ITEM;
    public final Supplier<RecipeType<R>> RECIPE_TYPE;
    public final Supplier<Z> RECIPE_SERIALIZER;
    public final Supplier<MenuType<M>> MENU;
    public final Lazy<?> JEI_RECIPE_TYPE;
    public final BlockBehaviour.Properties BLOCK_BEHAVIOUR_PROPERTIES;
    public final ConcoctiBlocks.BlockData BLOCK_DATA;
    public final String ID;

    public interface BlockEntityConstructor<T extends AbstractConcoctiMachineBlockEntity<T, ?, ?, ?, ?>> extends BlockEntityType.BlockEntitySupplier<T> {
    }

    public interface BlockConstructor<B extends AbstractConcoctiMachineBlock<B>> {
        B create(BlockBehaviour.Properties properties);
    }

    public interface MenuClientConstructor<M extends AbstractConcoctiMachineMenu<M>> extends MenuType.MenuSupplier<M> {
    }

    @OnlyIn(Dist.CLIENT)
    public interface ScreenConstructor<M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> extends MenuScreens.ScreenConstructor<M, S> {
    }

    public interface RecipeSerializerConstructor<Z extends RecipeSerializer<R>, R extends Recipe<I>, I extends RecipeInput> extends Supplier<Z> {
    }

    public interface RecipeCategoryConstructor<C extends AbstractConcoctiRecipeCategory<R>, R extends ProcessingRecipe<R, I>, I extends RecipeInput> {
        C create(IGuiHelper guiHelper);
    }

    protected ConcoctiMachine(String id, BlockBehaviour.Properties properties, ConcoctiBlocks.BlockData blockData) {
        ID = id;
        BLOCK = registerBlock(id, this::newBlock, properties, blockData);
        BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
                id,
                // The block entity type, created using a builder.
                () -> BlockEntityType.Builder.of(
                                // The supplier to use for constructing the block entity instances.
                                getBlockEntityConstructor(),
                                // A vararg of blocks that can have this block entity.
                                BLOCK.get()
                        )
                        // Build using null; vanilla does some datafixer shenanigans with the parameter that we don't need.
                        .build(null)
        );
        ITEM = registerBlockItem(BLOCK);
        RECIPE_TYPE = RECIPE_TYPES.register(
                id,
                // We need the qualifying generic here due to generics being generics.
                () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Concocti.MODID, id))
        );
        RECIPE_SERIALIZER = ConcoctiRegisters.RECIPE_SERIALIZERS.register(id, getRecipeSerializerConstructor());
        MENU = MENUS.register(id + "_menu", () -> new MenuType<>(getMenuClientConstructor(), FeatureFlags.DEFAULT_FLAGS));
        JEI_RECIPE_TYPE = new Lazy<>(() -> {
            try {
                Class<?> recipeTypeJeiClass = Class.forName("mezz.jei.api.recipe.RecipeType");
                Method method = recipeTypeJeiClass.getMethod("create", String.class, String.class, Class.class);
                return method.invoke(null, Concocti.MODID, id, getRecipeClass());
                // mezz.jei.api.recipe.RecipeType.create(Concocti.MODID, id, getRecipeClass());
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        BLOCK_BEHAVIOUR_PROPERTIES = properties;
        BLOCK_DATA = blockData;
        ConcoctiBlocks.BLOCK_MAP.put(BLOCK, BLOCK_DATA);
        Concocti.LOGGER.info("Registered machine: {} ({})", this.getClass(), id);
    }

    /**
     * Creates a new block entity of this machine.
     */
    public T newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return getBlockEntityConstructor().create(blockPos, blockState);
    }

    /**
     * Creates a new block of this machine.
     */
    public B newBlock(BlockBehaviour.Properties properties) {
        return getBlockConstructor().create(properties);
    }

    /**
     * Creates a new recipe category of this machine.
     */
    public C newRecipeCategory(IGuiHelper guiHelper) {
        return getRecipeCategoryConstructor().create(guiHelper);
    }


    /**
     * Get the general details of this machine.
     */
    public abstract Supplier<ConcoctiMachineDetails<T, M, V, I, R>> getDetails();

    /**
     * Get the constructor of this machine's block entity constructor.
     */
    public abstract BlockEntityConstructor<T> getBlockEntityConstructor();

    /**
     * Get the constructor of this machine's block entity class.
     */
    public final Class<T> getBlockEntityClass() {
        return getDetails().get().blockEntityClass();
    }

    public final Object getJeiRecipeType() {
        return JEI_RECIPE_TYPE.get();
    }

    /**
     * Get the constructor of this machine's block class.
     */
    public abstract BlockConstructor<B> getBlockConstructor();

    /**
     * Get the constructor of this machine's menu class.
     */
    public abstract MenuClientConstructor<M> getMenuClientConstructor();

    /**
     * Get the constructor of this machine's screen class.
     */
    public abstract ScreenConstructor<M, S> getScreenConstructor();

    /**
     * Get the constructor of this machine's screen class.
     */
    public abstract RecipeSerializerConstructor<Z, R, I> getRecipeSerializerConstructor();

    /**
     * Get the constructor of this machine's screen class.
     */
    public abstract RecipeCategoryConstructor<C, R, I> getRecipeCategoryConstructor();

    /**
     * Get the class of this machine's recipe class.
     */
    public abstract Class<R> getRecipeClass();
}
