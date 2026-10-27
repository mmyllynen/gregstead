package com.lowdragmc.lowdraglib.transfer.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.bus.api.Event;
import java.util.Objects;

/** Constructor-stage recipe map transformation, before RecipeManager derives learnable recipes. */
public final class RecipeMapLoadingEvent extends Event {
    private final HolderLookup.Provider registries;
    private RecipeMap recipes;
    public RecipeMapLoadingEvent(HolderLookup.Provider registries, RecipeMap recipes) {
        this.registries = registries;
        this.recipes = recipes;
    }
    public HolderLookup.Provider registries() { return registries; }
    public RecipeMap recipes() { return recipes; }
    public void recipes(RecipeMap recipes) { this.recipes = Objects.requireNonNull(recipes); }
}
