package com.gregtechceu.gtceu.data.loader;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTOreVeins;
import com.gregtechceu.gtceu.integration.map.cache.server.ServerCache;

import net.minecraft.core.Registry;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@NotNullByDefault
public class PostRegistryListener extends ContextAwareReloadListener implements PreparableReloadListener {

    public static final PostRegistryListener INSTANCE = new PostRegistryListener();

    private PostRegistryListener() {}

    protected void apply() {
        var registry = GTRegistries.builtinRegistry().lookupOrThrow(GTRegistries.ORE_VEIN_REGISTRY);

        buildVeinGenerators(registry);
        GTOreVeins.updateLargestVeinSize(registry);
        ServerCache.instance.oreVeinDefinitionsChanged(registry);
        WorldGeneratorUtils.invalidateOreVeinCache();
    }

    public static void buildVeinGenerators(Registry<GTOreDefinition> registry) {
        var iterator = registry.listElements().iterator();
        while (iterator.hasNext()) {
            var definition = iterator.next();
            var veinGen = definition.value().veinGenerator();
            if (veinGen != null && definition.value().canGenerate()) {
                veinGen.build();
            }
        }
    }

    @Override
    public CompletableFuture<Void> reload(SharedState currentReload, Executor backgroundExecutor,
                                          PreparationBarrier stage, Executor gameExecutor) {
        return stage.wait(null).thenRunAsync(this::apply, gameExecutor);
    }
}
