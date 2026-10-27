package dev.latvian.mods.kubejs.core.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.latvian.mods.kubejs.server.NativeRecipeLifecycle;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.server.*;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.List;
import java.util.concurrent.*;

@Mixin(ReloadableServerResources.class)
public abstract class RecipeLifecycleMixin {
    @WrapOperation(method = "loadResources", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/server/ReloadableServerRegistries;reload(Lnet/minecraft/core/LayeredRegistryAccess;Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static CompletableFuture<ReloadableServerRegistries.LoadResult> kjs$finalRecipes(
            LayeredRegistryAccess<RegistryLayer> context, List<Registry.PendingTags<?>> tags,
            ResourceManager resources, Executor executor, Operation<CompletableFuture<ReloadableServerRegistries.LoadResult>> original) {
        return original.call(context, tags, resources, executor)
                .thenCompose(initial -> NativeRecipeLifecycle.finalizeRecipes(initial, resources, tags, executor));
    }
    @WrapOperation(method = "*", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/core/component/DataComponentInitializers;build(Lnet/minecraft/core/HolderLookup$Provider;)Ljava/util/List;"))
    private static List<DataComponentInitializers.PendingComponents<?>> kjs$preparedComponents(
            DataComponentInitializers initializers, HolderLookup.Provider context,
            Operation<List<DataComponentInitializers.PendingComponents<?>>> original) {
        var prepared = NativeRecipeLifecycle.takePrepared(context);
        return prepared == null ? original.call(initializers, context) : prepared;
    }
}
