package dev.latvian.mods.kubejs.core.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.util.NativeItemComponentRules;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.concurrent.*;
import java.util.function.Supplier;

/** Capture this reload's packs before the asynchronous component initializer executes. */
@Mixin(ReloadableServerResources.class)
public abstract class ItemComponentRulesReloadMixin {
    @WrapOperation(method = "*", at = @At(value = "INVOKE", target =
            "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static <T> CompletableFuture<T> kjs$componentRules(Supplier<T> task, Executor executor,
            Operation<CompletableFuture<T>> original, @Local(argsOnly = true) ResourceManager resources) {
        return original.call(NativeItemComponentRules.scope(resources, task), executor);
    }
}
