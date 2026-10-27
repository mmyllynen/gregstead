package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.CompoundTag;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public class NbtTagComponent implements RecipeComponent<CompoundTag> {

    // spotless:off
    public static final NbtTagComponent NBT_TAG = new NbtTagComponent();
    public static final net.minecraft.resources.ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(GTCEu.id("nbt_tag"));
    // spotless:on

    @Override
    public Codec<CompoundTag> codec() {
        return CompoundTag.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.RAW_MAP;
    }

    @Override
    public String toString() {
        return "nbt_tag";
    }

    @Override
    public net.minecraft.resources.ResourceKey<RecipeComponentType<?>> type() {
        return TYPE;
    }
}
