package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.Identifier;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public class ChanceLogicComponent implements RecipeComponent<ChanceLogic> {

    // spotless:off
    public static final ChanceLogicComponent CHANCE_LOGIC = new ChanceLogicComponent();
    public static final net.minecraft.resources.ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(GTCEu.id("chance_logic"));
    // spotless:on

    @Override
    public Codec<ChanceLogic> codec() {
        return GTRegistries.CHANCE_LOGICS.byNameCodec();
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(ChanceLogic.class)
                .or(TypeInfo.of(Identifier.class))
                .or(TypeInfo.of(String.class));
    }

    @Override
    public String toString() {
        return "chance_logic";
    }

    @Override
    public net.minecraft.resources.ResourceKey<RecipeComponentType<?>> type() {
        return TYPE;
    }
}
