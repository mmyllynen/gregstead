package com.gregtechceu.gtceu.api.recipe;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/** Shaped crafting with offsets and blank cells, without horizontal mirroring. */
public class StrictShapedRecipe extends ShapedRecipe {
    private final ItemStackTemplate resultTemplate;

    public StrictShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                              ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
        this.resultTemplate = result;
    }

    public StrictShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                              ItemStack result, boolean showNotification) {
        this(new Recipe.CommonInfo(showNotification), new CraftingRecipe.CraftingBookInfo(category, group),
                pattern, ItemStackTemplate.fromNonEmptyStack(result));
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        for (int x = 0; x <= input.width() - getWidth(); x++) {
            for (int y = 0; y <= input.height() - getHeight(); y++) {
                if (matchesAt(input, x, y)) return true;
            }
        }
        return false;
    }

    private boolean matchesAt(CraftingInput input, int offsetX, int offsetY) {
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                int patternX = x - offsetX;
                int patternY = y - offsetY;
                Optional<Ingredient> ingredient = Optional.empty();
                if (patternX >= 0 && patternY >= 0 && patternX < getWidth() && patternY < getHeight()) {
                    ingredient = pattern.ingredients().get(patternX + patternY * getWidth());
                }
                if (!Ingredient.testOptionalIngredient(ingredient, input.getItem(x, y))) return false;
            }
        }
        return true;
    }

    // ShapedRecipe's concrete generic return type requires an erased override for a custom serializer.
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RecipeSerializer getSerializer() {
        return Serializer.INSTANCE;
    }

    public static final class Serializer {
        private Serializer() {}
        public static final MapCodec<StrictShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
                CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
                ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.resultTemplate))
                .apply(instance, StrictShapedRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, StrictShapedRecipe> STREAM_CODEC = StreamCodec.composite(
                Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
                CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
                ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
                ItemStackTemplate.STREAM_CODEC, recipe -> recipe.resultTemplate,
                StrictShapedRecipe::new);
        public static final RecipeSerializer<StrictShapedRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
