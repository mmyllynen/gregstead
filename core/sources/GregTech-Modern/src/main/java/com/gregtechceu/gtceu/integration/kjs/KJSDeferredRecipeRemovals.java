package com.gregtechceu.gtceu.integration.kjs;

import dev.latvian.mods.kubejs.recipe.filter.RecipeFilter;
import dev.latvian.mods.rhino.Context;

import java.util.List;

/** Removal requests also apply to GT base recipes generated after the script callbacks. */
public interface KJSDeferredRecipeRemovals {
    record Removal(Context context, RecipeFilter filter) {}
    List<Removal> gtceu$getDeferredRemovals();
}
