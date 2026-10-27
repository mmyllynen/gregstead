package com.lowdragmc.lowdraglib.transfer.recipe;

import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;

/** Live removal filter with complete native output/context delegation. */
public final class FilteredRecipeOutput implements RecipeOutput {
    private final RecipeOutput delegate;
    private final Set<Identifier> removed;
    public FilteredRecipeOutput(RecipeOutput delegate, Set<Identifier> removed) {
        this.delegate = delegate;
        this.removed = removed;
    }
    @Override public Advancement.Builder advancement() { return delegate.advancement(); }
    @Override public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) { return delegate.lookup(key); }
    @Override public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
        return delegate.listContextElements(key);
    }
    @Override public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, AdvancementHolder advancement, ICondition... conditions) {
        if (!removed.contains(key.identifier())) delegate.accept(key, recipe, advancement, conditions);
    }
}
