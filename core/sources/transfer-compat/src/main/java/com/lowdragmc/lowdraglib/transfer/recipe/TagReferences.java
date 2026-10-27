package com.lowdragmc.lowdraglib.transfer.recipe;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;

/** A named reference that follows registry tag binding without modifying the registry. */
public final class TagReferences {
    private static final Map<LiveTag<?>, Boolean> REFERENCES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Registry<?>, Map<TagKey<?>, WeakReference<LiveTag<?>>>> CACHE = new WeakHashMap<>();
    private TagReferences() {}

    @SuppressWarnings("unchecked")
    public static synchronized <T> HolderSet.Named<T> get(Registry<T> registry, TagKey<T> key) {
        if (!registry.key().equals(key.registry())) throw new IllegalArgumentException("Tag registry mismatch: " + key);
        var tags = CACHE.computeIfAbsent(registry, unused -> new HashMap<>());
        var cached = tags.get(key);
        var existing = cached == null ? null : cached.get();
        if (existing != null) return (LiveTag<T>) existing;
        var reference = new LiveTag<>(registry, key);
        tags.put(key, new WeakReference<>(reference));
        REFERENCES.put(reference, Boolean.TRUE);
        return reference;
    }

    public static void invalidate() {
        List<LiveTag<?>> references;
        synchronized (REFERENCES) { references = new ArrayList<>(REFERENCES.keySet()); }
        references.forEach(LiveTag::invalidate);
    }

    private static final class LiveTag<T> extends HolderSet.Named<T> {
        private final Registry<T> registry;
        private final List<Runnable> listeners = new ArrayList<>();
        LiveTag(Registry<T> registry, TagKey<T> key) {
            super(registry, key);
            this.registry = registry;
        }
        @Override public List<Holder<T>> contents() {
            if (registry instanceof net.minecraft.core.MappedRegistry<T> mapped && !mapped.allTags.isBound()) {
                return List.of();
            }
            return registry.get(key()).filter(HolderSet.Named::isBound)
                    .map(HolderSet.Named::contents).orElseGet(List::of);
        }
        @Override public boolean isBound() { return true; }
        @Override public boolean contains(Holder<T> holder) { return contents().contains(holder); }
        @Override public void addInvalidationListener(Runnable listener) { listeners.add(listener); }
        void invalidate() { List.copyOf(listeners).forEach(Runnable::run); }
    }
}
