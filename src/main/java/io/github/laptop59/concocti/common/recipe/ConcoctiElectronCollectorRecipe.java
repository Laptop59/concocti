package io.github.laptop59.concocti.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.github.laptop59.concocti.common.Concocti.MODID;

// The generic parameter for Recipe<T> is SingleRecipeInput.
public class ConcoctiElectronCollectorRecipe implements ProcessingRecipe<ConcoctiElectronCollectorRecipe, LightningRecipeInput> {
    // An in-code representation of our recipe data. This can be basically anything you want.
    // Common things to have here is a processing time integer of some kind, or an experience reward.
    // Note that we now use an ingredient instead of an item stack for the input.
    private final float chance;
    private final FluidStack outputFluid;

    private final ResourceLocation id;

    private final int ticks;

    // Add a constructor that sets all properties.
    public ConcoctiElectronCollectorRecipe(ResourceLocation id, float chance, FluidStack outputFluid, int ticks) {
        this.chance = chance;
        this.outputFluid = outputFluid;
        this.ticks = ticks;
        this.id = id;
    }

    public ConcoctiElectronCollectorRecipe(float chance, FluidStack outputFluid, int ticks) {
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
        return ConcoctiRecipes.CONCOCTI_ELECTRON_COLLECTOR_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ConcoctiRecipes.CONCOCTI_ELECTRON_COLLECTOR_RECIPE_TYPE.get();
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
        public @NotNull Builder group(@Nullable String group) {
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
            ConcoctiElectronCollectorRecipe recipe = new ConcoctiElectronCollectorRecipe(
                    id,
                    this.chance,
                    this.outputFluid,
                    this.ticks
            );
            recipeOutput.accept(getWouldBeResourceLocation(recipe.outputFluid), recipe, null);
        }
    }


    public static class Serializer implements RecipeSerializer<ConcoctiElectronCollectorRecipe> {
        public static final MapCodec<ConcoctiElectronCollectorRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.fieldOf("chance").forGetter(ConcoctiElectronCollectorRecipe::getChance),
                FluidStack.CODEC.fieldOf("output_fluid").forGetter(ConcoctiElectronCollectorRecipe::getOutputFluid),
                Codec.INT.fieldOf("ticks").forGetter(ConcoctiElectronCollectorRecipe::getTicks)
        ).apply(inst, ConcoctiElectronCollectorRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ConcoctiElectronCollectorRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.FLOAT, ConcoctiElectronCollectorRecipe::getChance,
                        FluidStack.STREAM_CODEC, ConcoctiElectronCollectorRecipe::getOutputFluid,
                        ByteBufCodecs.INT, ConcoctiElectronCollectorRecipe::getTicks,
                        ConcoctiElectronCollectorRecipe::new
                );

        // Return our map codec.
        @Override
        public @NotNull MapCodec<ConcoctiElectronCollectorRecipe> codec() {
            return CODEC;
        }

        // Return our stream codec.
        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ConcoctiElectronCollectorRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}