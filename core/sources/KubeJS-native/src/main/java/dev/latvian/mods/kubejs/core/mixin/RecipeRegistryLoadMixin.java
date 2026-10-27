package dev.latvian.mods.kubejs.core.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Lifecycle;
import dev.latvian.mods.kubejs.util.NativeRecipeReload;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.packs.resources.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;

/** Executes the original scoped recipe event before the native recipe registry decoder. */
@Mixin(ResourceManagerRegistryLoadTask.class)
public abstract class RecipeRegistryLoadMixin<T> extends RegistryLoadTask<T> {
    @Shadow @Final private ResourceManager resourceManager;
    @Shadow @Final private List<Registry.PendingTags<?>> pendingTags;
    protected RecipeRegistryLoadMixin(RegistryDataLoader.RegistryData<T> data, Lifecycle lifecycle,
                                      Map<ResourceKey<?>, Exception> errors) { super(data, lifecycle, errors); }

    @WrapOperation(method = "*", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/util/thread/ParallelMapTransform;schedule(Ljava/util/Map;Ljava/util/function/BiFunction;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Map<Identifier, PendingRegistration<T>>> kjs$recipeJsons(
            Map<Identifier, Resource> resources,
            BiFunction<Identifier, Resource, PendingRegistration<T>> decode,
            Executor executor,
            Operation<CompletableFuture<Map<Identifier, PendingRegistration<T>>>> original,
            @Local RegistryOps<JsonElement> ops,
            @Local FileToIdConverter lister) {
        if (!registryKey().equals(Registries.RECIPE)) return original.call(resources, decode, executor);
        var changed = NativeRecipeReload.process(resources, lister, resourceManager, ops, pendingTags);
        return original.call(changed, decode, executor);
    }
}
