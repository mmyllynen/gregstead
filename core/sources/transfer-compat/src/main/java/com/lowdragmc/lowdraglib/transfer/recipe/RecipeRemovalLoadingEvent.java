package com.lowdragmc.lowdraglib.transfer.recipe;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;

/** Removal selection for a single native RecipeManager construction. */
public final class RecipeRemovalLoadingEvent extends Event {
    private final HolderLookup.Provider registries;
    private final Set<Identifier> removed = new LinkedHashSet<>();
    public RecipeRemovalLoadingEvent(HolderLookup.Provider registries) { this.registries = registries; }
    public HolderLookup.Provider registries() { return registries; }
    public void remove(Identifier identifier) { removed.add(identifier); }
    public Set<Identifier> removed() { return Set.copyOf(removed); }
}
