package com.lowdragmc.lowdraglib.transfer.recipe;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Recipe;

/** A per-load filtered view; the supplied recipe registry is never modified. */
public final class FilteredRecipeLookup implements HolderLookup.RegistryLookup.Delegate<Recipe<?>> {
    private final HolderLookup.RegistryLookup<Recipe<?>> parent;
    private final Set<Identifier> removed;
    public FilteredRecipeLookup(HolderLookup.RegistryLookup<Recipe<?>> parent, Set<Identifier> removed) {
        this.parent = parent;
        this.removed = Set.copyOf(removed);
    }
    @Override public HolderLookup.RegistryLookup<Recipe<?>> parent() { return parent; }
    @Override public Optional<Holder.Reference<Recipe<?>>> get(ResourceKey<Recipe<?>> key) {
        return removed.contains(key.identifier()) ? Optional.empty() : parent.get(key);
    }
    @Override public Stream<Holder.Reference<Recipe<?>>> listElements() {
        return parent.listElements().filter(holder -> !removed.contains(holder.key().identifier()));
    }
    @Override public Optional<HolderSet.Named<Recipe<?>>> get(TagKey<Recipe<?>> tag) {
        return parent.get(tag).map(this::filterTag);
    }
    @Override public Stream<HolderSet.Named<Recipe<?>>> listTags() { return parent.listTags().map(this::filterTag); }
    private HolderSet.Named<Recipe<?>> filterTag(HolderSet.Named<Recipe<?>> source) {
        return new HolderSet.Named<>(parent, source.key()) {
            @Override public List<Holder<Recipe<?>>> contents() {
                if (!source.isBound()) return List.of();
                return source.stream().filter(holder -> holder.unwrapKey()
                        .map(key -> !removed.contains(key.identifier())).orElse(true)).toList();
            }
            @Override public boolean isBound() { return source.isBound(); }
            @Override public boolean contains(Holder<Recipe<?>> holder) { return contents().contains(holder); }
            @Override public void addInvalidationListener(Runnable listener) { source.addInvalidationListener(listener); }
        };
    }
}
