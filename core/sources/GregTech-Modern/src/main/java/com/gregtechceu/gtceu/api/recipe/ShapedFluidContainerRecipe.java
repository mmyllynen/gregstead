package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public class ShapedFluidContainerRecipe extends ShapedRecipe {
    private final ItemStackTemplate resultTemplate;

    public ShapedFluidContainerRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                      ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
        this.resultTemplate = result;
    }

    public ShapedFluidContainerRecipe(String group, CraftingBookCategory category,
                                      ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
        this(new Recipe.CommonInfo(showNotification), new CraftingRecipe.CraftingBookInfo(category, group),
                pattern, ItemStackTemplate.fromNonEmptyStack(result));
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        var remainders = CraftingRecipe.defaultCraftingReminder(input);
        // Choose a complete matching orientation before draining any crafting item.
        for (int x = 0; x <= input.width() - getWidth(); x++) {
            for (int y = 0; y <= input.height() - getHeight(); y++) {
                for (boolean mirrored : new boolean[] { false, true }) {
                    if (!matchesAt(input, x, y, mirrored)) continue;
                    for (int patternY = 0; patternY < getHeight(); patternY++) {
                        for (int patternX = 0; patternX < getWidth(); patternX++) {
                            var ingredient = ingredientAt(patternX, patternY, mirrored);
                            if (ingredient.isPresent() && ingredient.get().getCustomIngredient()
                                    instanceof FluidContainerIngredient fluid) {
                                int slot = x + patternX + (y + patternY) * input.width();
                                remainders.set(slot, fluid.getExtractedStack(input.getItem(slot)));
                            }
                        }
                    }
                    return remainders;
                }
            }
        }
        return remainders;
    }

    private Optional<Ingredient> ingredientAt(int x, int y, boolean mirrored) {
        int patternX = mirrored ? getWidth() - x - 1 : x;
        return pattern.ingredients().get(patternX + y * getWidth());
    }

    private boolean matchesAt(CraftingInput input, int offsetX, int offsetY, boolean mirrored) {
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                int patternX = x - offsetX;
                int patternY = y - offsetY;
                var ingredient = patternX >= 0 && patternY >= 0 && patternX < getWidth() && patternY < getHeight()
                        ? ingredientAt(patternX, patternY, mirrored) : Optional.<Ingredient>empty();
                if (!Ingredient.testOptionalIngredient(ingredient, input.getItem(x, y))) return false;
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RecipeSerializer getSerializer() { return Serializer.INSTANCE; }

    public static final class Serializer {
        private Serializer() {}
        public static final MapCodec<ShapedFluidContainerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
                CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
                ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.resultTemplate))
                .apply(instance, ShapedFluidContainerRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedFluidContainerRecipe> STREAM_CODEC = StreamCodec.composite(
                Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
                CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
                ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
                ItemStackTemplate.STREAM_CODEC, recipe -> recipe.resultTemplate,
                ShapedFluidContainerRecipe::new);
        public static final RecipeSerializer<ShapedFluidContainerRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
