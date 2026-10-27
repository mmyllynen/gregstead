package com.gregtechceu.gtceu.core.mixins.kubejs;

import com.gregtechceu.gtceu.integration.kjs.KJSRecyclingFinalizer;
import com.gregtechceu.gtceu.integration.kjs.KJSDeferredRecipeRemovals;
import dev.latvian.mods.kubejs.recipe.filter.RecipeFilter;
import dev.latvian.mods.rhino.Context;
import org.spongepowered.asm.mixin.Unique;
import com.google.gson.JsonElement;
import net.minecraft.resources.RegistryOps;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value=RecipesKubeEvent.class, remap=false)
public abstract class RecipesKubeEventMixin implements KJSDeferredRecipeRemovals {
    @Unique private final java.util.List<Removal> gtceu$deferredRemovals = new java.util.ArrayList<>();

    @Override
    public java.util.List<Removal> gtceu$getDeferredRemovals() { return java.util.List.copyOf(gtceu$deferredRemovals); }

    @Inject(method="remove", at=@At("HEAD"))
    private void gtceu$rememberBaseRecipeRemoval(Context context, RecipeFilter filter, CallbackInfo callback) {
        gtceu$deferredRemovals.add(new Removal(context, filter));
    }

    @Inject(method="post", at=@At("TAIL"))
    private void gtceu$finishDeferredRecycling(RegistryOps<JsonElement> ops,
                                              Map<Identifier, JsonElement> recipes, CallbackInfo callback) {
        KJSRecyclingFinalizer.finish((RecipesKubeEvent)(Object)this, ops, recipes);
    }
}
