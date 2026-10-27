package dev.latvian.mods.kubejs.core.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SimpleJsonResourceReloadListener.class)
public class SimpleJsonResourceReloadListenerMixin {
    @WrapOperation(method = "scanDirectory", at = @At(value = "INVOKE",
            target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"))
    private static <T> DataResult<T> onScanDirectory(Codec<T> codec, DynamicOps<JsonElement> ops,
            Object input, Operation<DataResult<T>> original, @Local(name = "id") Identifier id) {
        var result = original.call(codec, ops, input);
        var json = (JsonElement) input;
		if (result instanceof DataResult.Error<T> error && RecipesKubeEvent.INSTANCE.isBound()) {
			RecipesKubeEvent.INSTANCE.get().handleFailedRecipe(id, json, new IllegalStateException(error.message()));
		}
		return result;
	}
}
