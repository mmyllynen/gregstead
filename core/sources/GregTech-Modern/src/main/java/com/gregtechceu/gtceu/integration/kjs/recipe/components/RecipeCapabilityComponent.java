package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public class RecipeCapabilityComponent implements RecipeComponent<RecipeCapability<?>> {

    // spotless:off
    public static final RecipeCapabilityComponent RECIPE_CAPABILITY = new RecipeCapabilityComponent();
    public static final net.minecraft.resources.ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(GTCEu.id("recipe_capability"));
    // spotless:on

    @Override
    public Codec<RecipeCapability<?>> codec() {
        return RecipeCapability.DIRECT_CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(RecipeCapability.class);
    }

    @Override
    public String toString() {
        return "recipe_capability";
    }

    @Override
    public net.minecraft.resources.ResourceKey<RecipeComponentType<?>> type() {
        return TYPE;
    }
}
