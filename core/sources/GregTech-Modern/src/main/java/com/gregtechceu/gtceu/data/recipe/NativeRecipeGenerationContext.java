package com.gregtechceu.gtceu.data.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BootstrapRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import com.mojang.serialization.Lifecycle;

import java.util.stream.Stream;

/** Native datagen holders resolve recipe-unlock criteria before resources are loaded. */
public final class NativeRecipeGenerationContext {
    private final BootstrapRegistry<Recipe<?>> recipes = new BootstrapRegistry<>(Registries.RECIPE, Lifecycle.experimental());
    private final HolderLookup.Provider lookup;

    public NativeRecipeGenerationContext(HolderLookup.Provider context) {
        lookup = HolderLookup.Provider.create(Stream.concat(
                context.listRegistries().filter(registry -> !registry.key().equals(Registries.RECIPE)), Stream.of(recipes)));
    }

    public HolderLookup.Provider lookup() {
        return lookup;
    }

    public void register(ResourceKey<Recipe<?>> id, Recipe<?> recipe) {
        recipes.getOrThrow(id).bindValue(recipe);
    }

    public void validate() {
        recipes.freeze();
        recipes.listElements().forEach(holder -> {
            if (!holder.isBound()) throw new IllegalStateException("Generated advancement references missing recipe " + holder.key());
        });
    }
}
