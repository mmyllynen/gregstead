package com.lowdragmc.lowdraglib.transfer.recipe;

import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;

/** Native recipe writer context for dynamically generated resources. */
public abstract class RegistryRecipeOutput implements RecipeOutput {
    private final HolderLookup.Provider registries;
    protected RegistryRecipeOutput(HolderLookup.Provider registries) { this.registries = registries; }
    @Override public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
        return registries.lookupOrThrow(key);
    }
    @Override public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
        return registries.lookupOrThrow(key).listElements();
    }
    @Override public Advancement.Builder advancement() {
        return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
    }
}
