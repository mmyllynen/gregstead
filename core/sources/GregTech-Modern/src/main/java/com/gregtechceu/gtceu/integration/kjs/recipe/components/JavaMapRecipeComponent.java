package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.GTCEu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.HashMap;
import java.util.Map;

public record JavaMapRecipeComponent<K, V>(RecipeComponent<K> key, RecipeComponent<V> value)
        implements RecipeComponent<Map<K, V>> {

    // spotless:off
    public static final net.minecraft.resources.ResourceKey<RecipeComponentType<?>> JAVA_MAP = RecipeComponentType.key(GTCEu.id("java_map"));
    public static final com.mojang.serialization.MapCodec<JavaMapRecipeComponent<?, ?>> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaStorage.COMPONENT_CODEC.fieldOf("key").forGetter(JavaMapRecipeComponent::key),
                    dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaStorage.COMPONENT_CODEC.fieldOf("value").forGetter(JavaMapRecipeComponent::value)
            ).apply(instance, JavaMapRecipeComponent::new));
    // spotless:on

    @Override
    public Map<K, V> replace(RecipeScriptContext cx, Map<K, V> original, ReplacementMatchInfo match,
                             Object with) {
        var map = original;

        for (Map.Entry<K, V> entry : original.entrySet()) {
            var r = value.replace(cx, entry.getValue(), match, with);
            if (r != entry.getValue()) {
                if (map == original) {
                    map = new HashMap<>(original);
                }
                map.put(entry.getKey(), r);
            }
        }

        return map;
    }

    @Override
    public String toString() {
        return "java_map{" + key + ":" + value + "}";
    }

    @Override
    public Codec<Map<K, V>> codec() {
        return Codec.unboundedMap(key.codec(), value.codec());
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.RAW_MAP.withParams(key.typeInfo(), value.typeInfo());
    }

    public @Override net.minecraft.resources.ResourceKey<RecipeComponentType<?>> type() {
        return JAVA_MAP;
    }
}
