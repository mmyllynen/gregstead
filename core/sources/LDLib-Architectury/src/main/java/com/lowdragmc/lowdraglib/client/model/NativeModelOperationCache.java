package com.lowdragmc.lowdraglib.client.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

/** Nested model operations must not run while a ConcurrentHashMap bin is reserved. */
public final class NativeModelOperationCache<K, V> extends ConcurrentHashMap<K, V> {
    private final transient ThreadLocal<Set<K>> computing = ThreadLocal.withInitial(HashSet::new);
    private final transient ConcurrentHashMap<K, CompletableFuture<V>> pending = new ConcurrentHashMap<>();

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> operation) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(operation);
        V cached = get(key);
        if (cached != null) return cached;
        Set<K> active = computing.get();
        if (active.contains(key)) throw new IllegalStateException("Recursive model operation");
        var result = new CompletableFuture<V>();
        var existing = pending.putIfAbsent(key, result);
        if (existing != null) {
            if (active.isEmpty()) computing.remove();
            try { return existing.join(); }
            catch (CompletionException failure) {
                if (failure.getCause() instanceof RuntimeException runtime) throw runtime;
                if (failure.getCause() instanceof Error error) throw error;
                throw failure;
            }
        }
        active.add(key);
        try {
            // Separate keys may hash to the same bin. Computing outside the map's
            // reservation permits dependencies. The separate future preserves one
            // computation per key without holding a hash-bin lock during baking.
            cached = get(key);
            if (cached != null) { result.complete(cached); return cached; }
            V value = operation.apply(key);
            if (value == null) { result.complete(null); return null; }
            V winner = putIfAbsent(key, value);
            V published = winner != null ? winner : value;
            result.complete(published);
            return published;
        } catch (RuntimeException | Error failure) {
            result.completeExceptionally(failure);
            throw failure;
        } finally {
            pending.remove(key, result);
            active.remove(key);
            if (active.isEmpty()) computing.remove();
        }
    }
}
