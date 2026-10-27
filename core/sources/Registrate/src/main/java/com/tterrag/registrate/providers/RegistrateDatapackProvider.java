package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Generates both native datapack registry layers, preserving their lookup dependency. */
public class RegistrateDatapackProvider implements RegistrateLookupFillerProvider {
    private final DatapackBuiltinEntriesProvider world;
    private final DatapackBuiltinEntriesProvider reloadable;

    public RegistrateDatapackProvider(AbstractRegistrate<?> parent, PackOutput output,
            CompletableFuture<HolderLookup.Provider> worldLookup, CompletableFuture<HolderLookup.Provider> reloadableLookup) {
        var initializer = parent.getDataGenInitializer();
        var mods = Set.of(parent.getModid());
        world = DatapackBuiltinEntriesProvider.forWorldLayer(output, "Registrate world registries",
                worldLookup, initializer.getWorldRegistryProviders(), mods);
        reloadable = DatapackBuiltinEntriesProvider.forReloadableLayer(output, "Registrate reloadable registries",
                world.getRegistryProvider(), reloadableLookup, initializer.getReloadableRegistryProviders(), mods);
    }

    @Override public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(world.run(cache), reloadable.run(cache));
    }

    @Override public String getName() { return "Registrate datapack registries"; }

    @Override public CompletableFuture<HolderLookup.Provider> getFilledProvider() {
        return reloadable.getRegistryProvider();
    }

    public CompletableFuture<HolderLookup.Provider> getWorldProvider() { return world.getRegistryProvider(); }
}
