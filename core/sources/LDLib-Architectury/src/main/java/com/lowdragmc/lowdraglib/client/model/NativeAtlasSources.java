package com.lowdragmc.lowdraglib.client.model;

import net.minecraft.resources.Identifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Renderer-owned sprite requests used during native atlas source preparation. */
public final class NativeAtlasSources {
    private static final Map<Object, BiConsumer<Identifier, Consumer<Identifier>>> REQUESTS = new HashMap<>();
    private NativeAtlasSources() {}

    public static synchronized void register(Object renderer, BiConsumer<Identifier, Consumer<Identifier>> request) {
        REQUESTS.put(renderer, request);
    }

    public static Set<Identifier> prepare(Identifier atlas) {
        java.util.List<BiConsumer<Identifier, Consumer<Identifier>>> callbacks;
        synchronized (NativeAtlasSources.class) { callbacks = java.util.List.copyOf(REQUESTS.values()); }
        Set<Identifier> sprites = new HashSet<>();
        callbacks.forEach(callback -> callback.accept(atlas, sprites::add));
        return sprites;
    }
}
