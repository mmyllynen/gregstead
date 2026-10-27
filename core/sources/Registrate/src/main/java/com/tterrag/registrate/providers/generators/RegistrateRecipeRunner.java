package com.tterrag.registrate.providers.generators;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.RegistrateProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RegistrateRecipeRunner implements RegistrateProvider {
    final AbstractRegistrate<?> owner;
    @org.jetbrains.annotations.Nullable RegistrateRecipeProvider provider;
    private final CompletableFuture<DatapackBuiltinEntriesProvider> delegate;

    public RegistrateRecipeRunner(AbstractRegistrate<?> owner, PackOutput output,
            CompletableFuture<HolderLookup.Provider> world, CompletableFuture<HolderLookup.Provider> reloadable) {
        this.owner = owner;
        delegate = reloadable.thenApply(registries -> {
            var builder = new RegistrySetBuilder().add(RecipeProvider.asBootstrap((recipes, advancements) ->
                    new RegistrateRecipeProvider(this, registries, recipes, advancements)));
            return DatapackBuiltinEntriesProvider.forReloadableLayer(output, getName(), world,
                    CompletableFuture.completedFuture(registries), builder, Set.of(owner.getModid()));
        });
    }

    @Override public CompletableFuture<?> run(CachedOutput cache) {
        return delegate.thenCompose(provider -> provider.run(cache));
    }

    @Override public String getName() { return "Registrate recipes for " + owner.getModid(); }

    public RegistrateRecipeProvider getRecipeProvider() {
        if (provider == null) throw new IllegalStateException("Recipe Provider is not available now");
        return provider;
    }
}
