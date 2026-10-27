package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public class RecipeConditionComponent implements RecipeComponent<RecipeCondition<?>> {

    // spotless:off
    public static final RecipeConditionComponent RECIPE_CONDITION = new RecipeConditionComponent();
    public static final net.minecraft.resources.ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(GTCEu.id("recipe_condition"));
    // spotless:on

    @Override
    public Codec<RecipeCondition<?>> codec() {
        return RecipeCondition.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(RecipeCondition.class);
    }

    @Override
    public String toString() {
        return "recipe_condition";
    }

    @Override
    public net.minecraft.resources.ResourceKey<RecipeComponentType<?>> type() {
        return TYPE;
    }
}
