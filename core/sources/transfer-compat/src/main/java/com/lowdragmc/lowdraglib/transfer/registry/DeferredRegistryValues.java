package com.lowdragmc.lowdraglib.transfer.registry;

import com.lowdragmc.lowdraglib.transfer.mixin.RegistryFreezeAccessor;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Retains values until registration starts, draining only the event's registry. */
public final class DeferredRegistryValues {
    private final Map<Registry<?>, Map<Identifier,Object>> pending = new IdentityHashMap<>();
    private boolean registrationStarted;

    public <V,T extends V> T register(Registry<V> registry, Identifier id, T value) {
        if (registrationStarted && !((RegistryFreezeAccessor) registry).gtport$isFrozen()) {
            Registry.register(registry,id,value);
        } else {
            pending.computeIfAbsent(registry,ignored -> new LinkedHashMap<>()).put(id,value);
        }
        return value;
    }
    public void beginRegistration() { registrationStarted = true; }
    @SuppressWarnings({"rawtypes","unchecked"})
    public void drain(Registry<?> registry) {
        var values = pending.remove(registry);
        if (values != null) values.forEach((id,value) -> Registry.register((Registry)registry,id,value));
    }
    public int pendingCount(Registry<?> registry) {
        var values = pending.get(registry);
        return values == null ? 0 : values.size();
    }
}
