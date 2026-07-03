package io.github.laptop59.concocti.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public abstract class AbstractConcoctiMultiblockRecipe<T extends AbstractConcoctiMultiblockRecipe<T>> implements ProcessingRecipe<T, ItemsFluidsRecipeInput> {
    // An in-code representation of our recipe data. This can be basically anything you want.
    // Common things to have here is a processing time integer of some kind, or an experience reward.
    // Note that we now use an ingredient instead of an item stack for the input.
    private final List<ItemRecipeIngredient> inputItems;
    private final List<ItemOutput> outputItems;
    private final List<FluidRecipeIngredient> inputFluids;
    private final List<FluidOutput> outputFluids;

    private final ResourceLocation id;

    private final int ticks;

    // Add a constructor that sets all properties.
    public AbstractConcoctiMultiblockRecipe(ResourceLocation id, List<ItemRecipeIngredient> inputItems, List<ItemOutput> outputItems, List<FluidRecipeIngredient> inputFluids, List<FluidOutput> outputFluids, int ticks) {
        this.inputItems = inputItems;
        this.outputItems = outputItems;
        this.inputFluids = inputFluids;
        this.outputFluids = outputFluids;
        this.ticks = ticks;
        this.id = id;
    }

    public AbstractConcoctiMultiblockRecipe(List<ItemRecipeIngredient> inputItems, List<ItemOutput> outputItems, List<FluidRecipeIngredient> inputFluids, List<FluidOutput> outputFluids, int ticks) {
        this.inputItems = inputItems;
        this.outputItems = outputItems;
        this.inputFluids = inputFluids;
        this.outputFluids = outputFluids;
        this.ticks = ticks;
        this.id = getWouldBeResourceLocation(inputItems, inputFluids);
    }

    public static ResourceLocation getWouldBeResourceLocation(List<ItemRecipeIngredient> inputItems, List<FluidRecipeIngredient> inputFluids) {
        int[] hashes = new int[2];
        ArrayList<Object> objects = new ArrayList<>(inputItems);
        hashes[0] = Objects.hash(objects.toArray());
        objects.clear();
        objects.addAll(inputFluids);
        hashes[1] = Objects.hash(objects.toArray());
        long longHash = ((long) hashes[0] << 32) | hashes[1];
        return ResourceLocation.fromNamespaceAndPath(MODID, "mixing/" + String.format("%016x", longHash));
    }

    @NotNull
    public List<ItemRecipeIngredient> getInputItems() {
        return inputItems;
    }

    @NotNull
    public List<ItemOutput> getOutputItems() {
        return outputItems;
    }

    @NotNull
    public List<FluidRecipeIngredient> getInputFluids() {
        return inputFluids;
    }

    @NotNull
    public List<FluidOutput> getOutputFluids() {
        return outputFluids;
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
        return ItemStack.EMPTY;
    }

    // Return the result of the recipe here, based on the given input. The first parameter matches the generic.
    // IMPORTANT: Always call .copy() if you use an existing result! If you don't, things can and will break,
    // as the result exists once per recipe, but the assembled stack is created each time the recipe is crafted.
    @Override
    public @NotNull ItemStack assemble(@NotNull ItemsFluidsRecipeInput input, HolderLookup.@NotNull Provider registries) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public abstract @NotNull RecipeSerializer<?> getSerializer();

    @Override
    public abstract @NotNull RecipeType<?> getType();

    @Override
    public int getTicks() {
        return ticks;
    }

    public abstract static class Builder<T extends AbstractConcoctiMultiblockRecipe<T>> implements RecipeBuilder {
        protected final List<ItemRecipeIngredient> inputItems;
        protected final List<ItemOutput> outputItems;
        protected final List<FluidRecipeIngredient> inputFluids;
        protected final List<FluidOutput> outputFluids;
        protected final ResourceLocation resourceLocation;
        protected final int ticks;

        public Builder(ResourceLocation resourceLocation, List<ItemRecipeIngredient> inputItems, List<ItemOutput> outputItems, List<FluidRecipeIngredient> inputFluids, List<FluidOutput> outputFluids, int ticks) {
            this.resourceLocation = resourceLocation;
            this.inputItems = inputItems;
            this.outputItems = outputItems;
            this.inputFluids = inputFluids;
            this.outputFluids = outputFluids;
            this.ticks = ticks;
        }

        @Override
        public @NotNull RecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
            return this;
        }

        @Override
        public @NotNull Builder<T> group(@Nullable String group) {
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
            T recipe = construct(
                    id,
                    this.inputItems,
                    this.outputItems,
                    this.inputFluids,
                    this.outputFluids,
                    this.ticks
            );
            recipeOutput.accept(resourceLocation, recipe, null);
        }

        public abstract T construct(ResourceLocation id, List<ItemRecipeIngredient> inputItems, List<ItemOutput> outputItems, List<FluidRecipeIngredient> inputFluids, List<FluidOutput> outputFluids, int ticks);
    }


    public abstract static class Serializer<T extends AbstractConcoctiMultiblockRecipe<T>> implements RecipeSerializer<T> {
        public final MapCodec<T> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                ItemRecipeIngredient.CODEC.listOf().fieldOf("input_items").forGetter(AbstractConcoctiMultiblockRecipe::getInputItems),
                ItemOutput.CODEC.listOf().fieldOf("output_item").forGetter(AbstractConcoctiMultiblockRecipe::getOutputItems),
                FluidRecipeIngredient.CODEC.listOf().fieldOf("input_fluids").forGetter(AbstractConcoctiMultiblockRecipe::getInputFluids),
                FluidOutput.CODEC.listOf().fieldOf("output_fluid").forGetter(AbstractConcoctiMultiblockRecipe::getOutputFluids),
                Codec.INT.fieldOf("ticks").forGetter(AbstractConcoctiMultiblockRecipe::getTicks)
        ).apply(inst, this::construct));

        public final StreamCodec<RegistryFriendlyByteBuf, T> STREAM_CODEC =
                StreamCodec.composite(
                        ItemRecipeIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), AbstractConcoctiMultiblockRecipe::getInputItems,
                        ItemOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), AbstractConcoctiMultiblockRecipe::getOutputItems,
                        FluidRecipeIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), AbstractConcoctiMultiblockRecipe::getInputFluids,
                        FluidOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), AbstractConcoctiMultiblockRecipe::getOutputFluids,
                        ByteBufCodecs.INT, AbstractConcoctiMultiblockRecipe::getTicks,
                        this::construct
                );

        // Return our map codec.
        @Override
        public @NotNull MapCodec<T> codec() {
            return CODEC;
        }

        // Return our stream codec.
        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return STREAM_CODEC;
        }

        public abstract T construct(List<ItemRecipeIngredient> inputItems, List<ItemOutput> outputItems, List<FluidRecipeIngredient> inputFluids, List<FluidOutput> outputFluids, int ticks);
    }
}