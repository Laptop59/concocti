package io.github.laptop59.concocti.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;

import static io.github.laptop59.concocti.common.Concocti.MODID;

// The generic parameter for Recipe<T> is our own input.
public class ConcoctiSolidifierRecipe implements ProcessingRecipe<ConcoctiSolidifierRecipe, ConcoctiSolidifierRecipe.Input> {
    private final Ingredient mold;
    private final Ingredient baseItem;
    private final SizedFluidIngredient inputFluid;
    private final ItemStack outputItem;
    private final int ticks;

    private static final HashMap<ConcoctiSolidifierRecipe, ResourceLocation> idMap = new HashMap<>();

    static ResourceLocation makeResourceLocation(ConcoctiSolidifierRecipe recipe) {
        return makeResourceLocation(recipe.baseItem, recipe.mold, recipe.inputFluid);
    }

    static ResourceLocation makeResourceLocation(Ingredient baseItem, Ingredient mold, SizedFluidIngredient inputFluid) {
        ResourceLocation fluidLoc = ResourceLocation.fromNamespaceAndPath(MODID, BuiltInRegistries.FLUID.getKey(
                Arrays.stream(inputFluid.getFluids()).findFirst().orElseThrow().getFluid()).getPath());
        fluidLoc = fluidLoc.withPrefix("solidifying/").withSuffix("_with_" + getId(mold));
        if (!baseItem.hasNoItems()) {
            fluidLoc = fluidLoc.withSuffix("_on_" + getId(baseItem));
        }
        return fluidLoc;
    }

    public static ResourceLocation makeResourceLocation(ItemStack baseItem, ItemStack mold, FluidStack inputFluid) {
        ResourceLocation fluidLoc = BuiltInRegistries.FLUID.getKey(inputFluid.getFluid());
        fluidLoc = fluidLoc.withSuffix("_" + getId(mold));
        if (!baseItem.isEmpty()) {
            fluidLoc = fluidLoc.withSuffix(getId(baseItem));
        }
        return fluidLoc;
    }

    public ConcoctiSolidifierRecipe(ResourceLocation id, Ingredient baseItem, Ingredient mold, SizedFluidIngredient inputFluid, ItemStack outputItem, int ticks) {
        this.mold = mold;
        this.baseItem = baseItem;
        this.inputFluid = inputFluid;
        this.outputItem = outputItem;
        this.ticks = ticks;
        idMap.put(this, id);
    }

    public ConcoctiSolidifierRecipe(Ingredient baseItem, Ingredient mold, SizedFluidIngredient inputFluid, ItemStack outputItem, int ticks) {
        this.mold = mold;
        this.baseItem = baseItem;
        this.inputFluid = inputFluid;
        this.outputItem = outputItem;
        this.ticks = ticks;
        idMap.put(this, makeResourceLocation(this));
    }

    static String getId(Ingredient ingredient) {
        Ingredient.Value[] values = ingredient.getValues();
        if (values[0] instanceof Ingredient.TagValue(net.minecraft.tags.TagKey<Item> tag)) {
            return tag.location().getPath().replace('/', '_');
        } else {
            ItemStack firstStack = Arrays.stream(ingredient.getItems()).findFirst().orElseThrow();
            return BuiltInRegistries.ITEM.getKey(firstStack.getItem()).getPath();
        }
    }

    static String getId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
    }

    public Ingredient getMold() {
        return mold;
    }

    public SizedFluidIngredient getInputFluid() {
        return inputFluid;
    }

    public Ingredient getBaseItem() {
        return baseItem;
    }

    public ItemStack getOutputItem() {
        return outputItem;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.mold);
        if (!this.baseItem.isEmpty()) list.add(this.baseItem);
        return list;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public boolean matches(Input input, @NotNull Level level) {
        return this.mold.test(input.mold()) && this.baseItem.test(input.base())
                && this.inputFluid.test(input.inputFluid);
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return outputItem;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Input input, HolderLookup.@NotNull Provider registries) {
        return this.outputItem.copy();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ConcoctiRecipes.CONCOCTI_SOLIDIFIER_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ConcoctiRecipes.CONCOCTI_SOLIDIFIER_RECIPE_TYPE.get();
    }

    @Override
    public int getTicks() {
        return ticks;
    }

    @Override
    public ResourceLocation getId() {
        return idMap.get(this);
    }

    public record Input(ItemStack mold, ItemStack base, FluidStack inputFluid) implements RecipeInput {
        @Override
        public @NotNull ItemStack getItem(int index) {
            return switch (index) {
                case 0 -> this.mold;
                case 1 -> this.base;
                default -> throw new IllegalArgumentException("Recipe does not contain item slot " + index);
            };
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            return this.base.isEmpty() && this.mold.isEmpty();
        }
    }

    public static class Builder implements RecipeBuilder {
        protected Ingredient mold;
        protected Ingredient baseItem;
        protected SizedFluidIngredient inputFluid;
        protected ItemStack outputItem;
        protected int ticks;

        public Builder(Ingredient baseItem, Ingredient mold, SizedFluidIngredient inputFluid, ItemStack outputItem, int ticks) {
            this.mold = mold;
            this.baseItem = baseItem;
            this.inputFluid = inputFluid;
            this.outputItem = outputItem;
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
            return outputItem.getItem();
        }

        @Override
        public void save(@NotNull RecipeOutput recipeOutput) {
            this.save(recipeOutput, makeResourceLocation(baseItem, mold, inputFluid));
        }

        @Override
        public void save(@NotNull RecipeOutput recipeOutput, @NotNull String id) {
            ResourceLocation resourceLocation = makeResourceLocation(baseItem, mold, inputFluid);
            ResourceLocation idLocation = ResourceLocation.parse(id);
            if (ResourceLocation.parse(id).equals(resourceLocation)) {
                throw new IllegalStateException("Recipe " + id + " should remove its 'save' argument as it is equal to default one");
            } else {
                this.save(recipeOutput, idLocation);
            }
        }

        @Override
        public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
            ConcoctiSolidifierRecipe recipe = new ConcoctiSolidifierRecipe(id, this.baseItem, this.mold, this.inputFluid, this.outputItem, this.ticks);
            recipeOutput.accept(id, recipe, null);
        }
    }

    public static class Serializer implements RecipeSerializer<ConcoctiSolidifierRecipe> {
        public static final MapCodec<ConcoctiSolidifierRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("base_item").forGetter(ConcoctiSolidifierRecipe::getBaseItem),
                Ingredient.CODEC.fieldOf("mold").forGetter(ConcoctiSolidifierRecipe::getMold),
                SizedFluidIngredient.FLAT_CODEC.fieldOf("input_fluid").forGetter(ConcoctiSolidifierRecipe::getInputFluid),
                ItemStack.CODEC.fieldOf("output_item").forGetter(ConcoctiSolidifierRecipe::getOutputItem),
                Codec.INT.fieldOf("ticks").forGetter(ConcoctiSolidifierRecipe::getTicks)
        ).apply(inst, ConcoctiSolidifierRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ConcoctiSolidifierRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, ConcoctiSolidifierRecipe::getBaseItem,
                        Ingredient.CONTENTS_STREAM_CODEC, ConcoctiSolidifierRecipe::getMold,
                        SizedFluidIngredient.STREAM_CODEC, ConcoctiSolidifierRecipe::getInputFluid,
                        ItemStack.STREAM_CODEC, ConcoctiSolidifierRecipe::getOutputItem,
                        ByteBufCodecs.INT, ConcoctiSolidifierRecipe::getTicks,
                        ConcoctiSolidifierRecipe::new
                );

        // Return our map codec.
        @Override
        public @NotNull MapCodec<ConcoctiSolidifierRecipe> codec() {
            return CODEC;
        }

        // Return our stream codec.
        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ConcoctiSolidifierRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}