package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public class EnergyStackComponent implements RecipeComponent<EnergyStack.WithIO> {

    // spotless:off
    public static final EnergyStackComponent ENERGY_STACK = new EnergyStackComponent();
    public static final net.minecraft.resources.ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(GTCEu.id("energy_stack"));
    // spotless:on

    @Override
    public Codec<EnergyStack.WithIO> codec() {
        return EnergyStack.WithIO.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(EnergyStack.WithIO.class)
                .or(TypeInfo.of(EnergyStack.class))
                .or(TypeInfo.PRIMITIVE_LONG)
                .or(TypeInfo.LONG);
    }

    @Override
    public String toString() {
        return "energy_stack";
    }

    @Override
    public net.minecraft.resources.ResourceKey<RecipeComponentType<?>> type() {
        return TYPE;
    }
}
