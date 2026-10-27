package com.lowdragmc.lowdraglib.transfer.recipe;

import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

/** Native static recipe inputs/result for machine proxy conversion. */
public final class RecipeProjection {
    private RecipeProjection() {}
    public static List<Ingredient> inputs(Recipe<?> recipe) { return recipe.placementInfo().ingredients(); }
    public static ItemStack result(Recipe<?> recipe, RegistryAccess registries) {
        var context = ContextMap.builder().set(SlotDisplayContext.REGISTRIES, registries)
                .buildAndValidate(SlotDisplayContext.CONTEXT);
        for (var display : recipe.display()) {
            var result = display.result().resolveForFirstStack(context);
            if (!result.isEmpty()) return result;
        }
        throw new IllegalArgumentException("Recipe has no static result display: " + recipe.getClass().getName());
    }
}
