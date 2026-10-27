package com.lowdragmc.lowdraglib.transfer.recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;

public final class RecipeJson {
    private RecipeJson() {}
    public static JsonElement encode(Recipe<?> recipe, HolderLookup.Provider registries, ICondition... conditions) {
        var json = Recipe.DIRECT_CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), recipe).getOrThrow();
        ICondition.writeConditions(registries, json.getAsJsonObject(), conditions);
        return json;
    }
}
