package com.gregtechceu.gtceu.core;

import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import com.mojang.serialization.Lifecycle;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/** Actual native loot bootstrap context backed by the supplied registry lookups. */
public final class NativeDynamicLootContext {
    private NativeDynamicLootContext() {}
    /** Read predicates from this reload's packs, including overrides, before generating loot. */
    public static RegistryAccess.Frozen loadPredicates(net.minecraft.server.packs.resources.ResourceManager resources,
            RegistryAccess.Frozen context, java.util.List<Registry.PendingTags<?>> pendingTags) {
        var definitions = net.neoforged.neoforge.registries.DataPackRegistriesHooks.getReloadableRegistries().stream()
                .filter(data -> data.key().equals(Registries.PREDICATE)).toList();
        var contextRegistries = context.listRegistries()
                .filter(registry -> !registry.key().equals(Registries.PREDICATE)).toList();
        // This preparation runs inside loadResources; a direct executor avoids waiting
        // on the worker that called us. Final native loading still loads the full set.
        var predicates = net.minecraft.resources.RegistryDataLoader.load(resources, contextRegistries,
                definitions, Runnable::run, pendingTags).join();
        return new RegistryAccess.ImmutableRegistryAccess(Stream.concat(
                context.registries().filter(entry -> !entry.key().equals(Registries.PREDICATE)),
                predicates.registries())).freeze();
    }
    public static LootTableSubProvider.Context create(HolderLookup.Provider lookup,
            BiConsumer<ResourceKey<LootTable>, LootTable> output) {
        return new LootTableSubProvider.Context() {
            private final MappedRegistry<LootTable> generated = new MappedRegistry<>(Registries.LOOT_TABLE, Lifecycle.experimental());
            public <T> HolderGetter<T> lookup(ResourceKey<? extends Registry<? extends T>> key) { return lookup.lookupOrThrow(key); }
            public <T> Stream<Holder.Reference<T>> listContextElements(ResourceKey<? extends Registry<? extends T>> key) { return lookup.lookupOrThrow(key).listElements(); }
            public Holder.Reference<LootTable> accept(ResourceKey<LootTable> key, LootTable.Builder builder) {
                var table = builder.build(); table.setLootTableId(key.identifier());
                var holder = Registry.registerForHolder(generated, key, table);
                output.accept(key, table);
                return holder;
            }
        };
    }
}
