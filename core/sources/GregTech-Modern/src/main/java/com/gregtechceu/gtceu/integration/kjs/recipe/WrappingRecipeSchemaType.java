package com.gregtechceu.gtceu.integration.kjs.recipe;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;


public class WrappingRecipeSchemaType extends RecipeSchemaType {

    public WrappingRecipeSchemaType(RecipeNamespace namespace, Identifier id, RecipeSchema schema,
                                    RecipeSerializer<?> serializer) {
        super(namespace, id, schema, serializer);
    }
}
