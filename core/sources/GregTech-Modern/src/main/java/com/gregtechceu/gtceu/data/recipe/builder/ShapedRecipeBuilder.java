package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.api.recipe.StrictShapedRecipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Accessors(chain = true, fluent = true)
public class ShapedRecipeBuilder {

    @Setter
    protected ItemStack output = ItemStack.EMPTY;
    @Setter
    protected Identifier id;
    @Setter
    protected String group;
    @Setter
    protected RecipeCategory category = RecipeCategory.MISC;

    protected final List<String> rows = Lists.newArrayList();
    protected final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    @Setter
    protected boolean isStrict;
    @Setter
    protected boolean isStrictSize;
    @Setter
    protected boolean allowLargerGrid;

    public ShapedRecipeBuilder(@Nullable Identifier id) {
        this.id = id;
    }

    public ShapedRecipeBuilder() {
        this(null);
    }

    public ShapedRecipeBuilder pattern(String slice) {
        rows.add(slice);
        return this;
    }

    public ShapedRecipeBuilder define(char cha, TagKey<Item> itemStack) {
        key.put(cha, com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks.of(itemStack));
        return this;
    }

    public ShapedRecipeBuilder define(char cha, ItemStack itemStack) {
        if (!itemStack.getComponentsPatch().isEmpty()) {
            key.put(cha, DataComponentIngredient.of(true, itemStack));
        } else {
            key.put(cha, com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks.of(itemStack));
        }
        return this;
    }

    public ShapedRecipeBuilder define(char cha, ItemLike itemLike) {
        key.put(cha, com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks.of(itemLike));
        return this;
    }

    public ShapedRecipeBuilder define(char cha, Ingredient ingredient) {
        key.put(cha, ingredient);
        return this;
    }

    public ShapedRecipeBuilder output(ItemStack itemStack, int count) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        return this;
    }

    protected Identifier defaultId() {
        return BuiltInRegistries.ITEM.getKey(output.getItem());
    }

    public void save(RecipeOutput consumer) {
        var recipeId = id == null ? defaultId() : id;
        ShapedRecipe recipe;
        if (isStrictSize) {
            recipe = new com.gregtechceu.gtceu.api.recipe.StrictSizeShapedRecipe(
                    new net.minecraft.world.item.crafting.Recipe.CommonInfo(false),
                    new net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo(RecipeBuilder.determineCraftingBookCategory(this.category), Objects.requireNonNullElse(this.group, "")),
                    new ShapedRecipePattern.Data(Map.copyOf(key), List.copyOf(rows)),
                    net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(this.output),this.allowLargerGrid);
        } else if (isStrict) {
            recipe = new StrictShapedRecipe(
                    Objects.requireNonNullElse(this.group, ""),
                    RecipeBuilder.determineCraftingBookCategory(this.category),
                    ShapedRecipePattern.of(key, rows),
                    this.output,
                    false);
        } else {
            recipe = new ShapedRecipe(
                    new net.minecraft.world.item.crafting.Recipe.CommonInfo(false),
                    new net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo(
                            RecipeBuilder.determineCraftingBookCategory(this.category), Objects.requireNonNullElse(this.group, "")),
                    ShapedRecipePattern.of(key, rows),
                    net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(this.output));
        }
        consumer.accept(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE,
                recipeId.withPrefix("shaped/")), recipe, null);
    }
}
