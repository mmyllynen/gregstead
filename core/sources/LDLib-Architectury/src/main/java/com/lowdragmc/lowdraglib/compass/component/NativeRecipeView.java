package com.lowdragmc.lowdraglib.compass.component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import java.util.*;

/** Native default compass recipe data; shaped holes and row strides are kept explicitly. */
public record NativeRecipeView(Map<Integer, Ingredient> inputs, ItemStack output) {
    public static List<ItemStack> items(Ingredient input) {
        return input == null ? List.of() : Arrays.asList(com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks.getItems(input));
    }
    public static NativeRecipeView of(Recipe<?> recipe, Level level) {
        var displays = recipe.display();
        if (displays.isEmpty()) throw new UnsupportedOperationException("Recipe requires a native display or a custom compass RecipeViewCreator: " + recipe.getSerializer());
        var results = displays.getFirst().result().resolveForStacks(SlotDisplayContext.fromLevel(level));
        var placement = recipe.placementInfo();
        var inputs = new LinkedHashMap<Integer, Ingredient>();
        var slots = placement.slotsToIngredientIndex();
        var ingredients = placement.ingredients();
        for (int i = 0; i < slots.size(); i++) {
            int ingredient = slots.getInt(i);
            if (ingredient == PlacementInfo.EMPTY_SLOT) continue;
            int slot = recipe instanceof ShapedRecipe shaped ? i % shaped.getWidth() + (i / shaped.getWidth()) * 3 : i;
            inputs.put(slot, ingredients.get(ingredient));
        }
        return new NativeRecipeView(Collections.unmodifiableMap(inputs), results.isEmpty() ? ItemStack.EMPTY : results.getFirst().copy());
    }
}
