package dev.latvian.mods.kubejs.server;

import dev.latvian.mods.kubejs.core.ScriptManagerHolderKJS;
import dev.latvian.mods.kubejs.util.NativeItemComponentRules;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.crafting.Recipe;
import java.util.function.BiConsumer;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.*;
import net.minecraft.server.packs.resources.ResourceManager;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

/** Script stacks require bound defaults; final recipe values still use the native registry loader. */
public final class NativeRecipeLifecycle {
    private NativeRecipeLifecycle() {}
    private static final Map<HolderLookup.Provider, List<DataComponentInitializers.PendingComponents<?>>> PREPARED =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static CompletableFuture<ReloadableServerRegistries.LoadResult> finalizeRecipes(
            ReloadableServerRegistries.LoadResult initial, ResourceManager resources,
            List<Registry.PendingTags<?>> tags, Executor executor) {
        var manager = resources instanceof ScriptManagerHolderKJS holder ? holder.kjs$getScriptManager() : null;
        if (manager == null) return CompletableFuture.completedFuture(initial);
        var pending = NativeItemComponentRules.scope(resources,
                () -> BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(initial.lookupWithUpdatedTags())).get();
        pending.forEach(DataComponentInitializers.PendingComponents::apply);
        manager.nativeRecipeComponentsReady = true;
        var recipeData = net.neoforged.neoforge.registries.DataPackRegistriesHooks.getReloadableRegistries().stream()
                .filter(data -> data.key().equals(Registries.RECIPE)).toList();
        var context = initial.lookupWithUpdatedTags().listRegistries().filter(registry -> !registry.key().equals(Registries.RECIPE)).toList();
        return RegistryDataLoader.load(resources, context, recipeData, executor, tags).thenApply(recipes -> {
            var previous = initial.layers().getLayer(RegistryLayer.RELOADABLE);
            var entries = Stream.concat(previous.registries().filter(entry -> !entry.key().equals(Registries.RECIPE)), recipes.registries());
            var updated = new RegistryAccess.ImmutableRegistryAccess(entries).freeze();
            var layers = initial.layers().replaceFrom(RegistryLayer.RELOADABLE, updated);
            var lookup = HolderLookup.Provider.create(Stream.concat(context.stream(), recipes.listRegistries()));
            var finalPending = pending.stream().map(entry -> entry.key().equals(Registries.RECIPE)
                    ? rebaseRecipeComponents(entry, recipes.lookupOrThrow(Registries.RECIPE)) : entry).toList();
            PREPARED.put(lookup, finalPending);
            return new ReloadableServerRegistries.LoadResult(layers, lookup);
        });
    }

    /** Replace initial recipe references while retaining their actual computed defaults. */
    @SuppressWarnings("unchecked")
    private static DataComponentInitializers.PendingComponents<Recipe<?>> rebaseRecipeComponents(
            DataComponentInitializers.PendingComponents<?> initial, Registry<Recipe<?>> recipes) {
        var defaults = new HashMap<ResourceKey<Recipe<?>>, DataComponentMap>();
        ((DataComponentInitializers.PendingComponents<Recipe<?>>) initial).forEach(
                (holder, components) -> defaults.put(holder.key(), components));
        var entries = recipes.listElements().map(holder -> Map.entry(holder,
                defaults.getOrDefault(holder.key(), DataComponentMap.EMPTY))).toList();
        return new DataComponentInitializers.PendingComponents<>() {
            public ResourceKey<? extends Registry<? extends Recipe<?>>> key() { return Registries.RECIPE; }
            public void forEach(BiConsumer<Holder.Reference<Recipe<?>>, DataComponentMap> output) {
                entries.forEach(entry -> output.accept(entry.getKey(), entry.getValue()));
            }
            public void apply() { forEach(Holder.Reference::bindComponents); }
        };
    }

    /** Reuse exactly the component build that preceded recipe events; do not execute scripts twice. */
    public static List<DataComponentInitializers.PendingComponents<?>> takePrepared(HolderLookup.Provider context) {
        return PREPARED.remove(context);
    }
}
