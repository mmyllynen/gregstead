package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.resources.Identifier;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public class ResourceLocationComponent implements RecipeComponent<Identifier> {

    // spotless:off
    public static final ResourceLocationComponent RESOURCE_LOCATION = new ResourceLocationComponent();
    public static final net.minecraft.resources.ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(GTCEu.id("tag"));
    // spotless:on

    @Override
    public Codec<Identifier> codec() {
        return Identifier.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(Identifier.class).or(TypeInfo.STRING);
    }

    @Override
    public Identifier wrap(dev.latvian.mods.kubejs.recipe.RecipeScriptContext context, Object from) {
        // The union is an input/type hint; stored recipe values must be canonical identifiers.
        return java.util.Objects.requireNonNull(dev.latvian.mods.kubejs.util.ID.mc(from),
                "Recipe identifier cannot be null");
    }

    @Override
    public String toString() {
        return "resource_location";
    }

    @Override
    public net.minecraft.resources.ResourceKey<RecipeComponentType<?>> type() {
        return TYPE;
    }
}
