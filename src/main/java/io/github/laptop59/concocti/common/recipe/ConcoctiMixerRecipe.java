package io.github.laptop59.concocti.common.recipe;

import com.google.common.primitives.UnsignedLong;
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
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static io.github.laptop59.concocti.common.Concocti.MODID;

// The generic parameter for Recipe<T> is SingleRecipeInput.
public class ConcoctiMixerRecipe implements ProcessingRecipe<ConcoctiMixerRecipe, ItemsFluidsRecipeInput> {
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
    public ConcoctiMixerRecipe(ResourceLocation id, List<SizedIngredient> inputItems, ItemStack outputItem, List<SizedFluidIngredient> inputFluids, FluidStack outputFluid, int ticks) {
        this.inputItems = inputItems;
        this.outputItem = outputItem;
        this.inputFluids = inputFluids;
        this.outputFluid = outputFluid;
        this.ticks = ticks;
        this.id = id;
    }

    public ConcoctiMixerRecipe(List<SizedIngredient> inputItems, ItemStack outputItem, List<SizedFluidIngredient> inputFluids, FluidStack outputFluid, int ticks) {
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

    @Nullable
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

    @Nullable
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
        return ConcoctiRecipes.CONCOCTI_MIXER_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ConcoctiRecipes.CONCOCTI_MIXER_RECIPE_TYPE.get();
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
        public @NotNull Builder group(@Nullable String group) {
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
            ConcoctiMixerRecipe recipe = new ConcoctiMixerRecipe(
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


    public static class Serializer implements RecipeSerializer<ConcoctiMixerRecipe> {
        public static final MapCodec<ConcoctiMixerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                SizedIngredient.FLAT_CODEC.listOf().fieldOf("input_items").forGetter(ConcoctiMixerRecipe::getInputItems),
                ItemStack.OPTIONAL_CODEC.fieldOf("output_item").forGetter(ConcoctiMixerRecipe::getOutputItemOrEmpty),
                SizedFluidIngredient.FLAT_CODEC.listOf().fieldOf("input_fluids").forGetter(ConcoctiMixerRecipe::getInputFluids),
                FluidStack.OPTIONAL_CODEC.fieldOf("output_fluid").forGetter(ConcoctiMixerRecipe::getOutputFluidOrEmpty),
                Codec.INT.fieldOf("ticks").forGetter(ConcoctiMixerRecipe::getTicks)
        ).apply(inst, ConcoctiMixerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ConcoctiMixerRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), ConcoctiMixerRecipe::getInputItems,
                        ItemStack.OPTIONAL_STREAM_CODEC, ConcoctiMixerRecipe::getOutputItemOrEmpty,
                        SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), ConcoctiMixerRecipe::getInputFluids,
                        FluidStack.OPTIONAL_STREAM_CODEC, ConcoctiMixerRecipe::getOutputFluidOrEmpty,
                        ByteBufCodecs.INT, ConcoctiMixerRecipe::getTicks,
                        ConcoctiMixerRecipe::new
                );

        // Return our map codec.
        @Override
        public @NotNull MapCodec<ConcoctiMixerRecipe> codec() {
            return CODEC;
        }

        // Return our stream codec.
        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ConcoctiMixerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}