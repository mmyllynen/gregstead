package dev.emi.emi.recipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import java.util.*;

/** Native recipe metadata with explicit holes and detached preview results. */
public final class NativeRecipeData {
    private NativeRecipeData() {}
    public static ItemStack[] items(Ingredient ingredient) {
        if (ingredient == null || ingredient.isEmpty()) return new ItemStack[0];
        if (ingredient.getCustomIngredient() instanceof net.neoforged.neoforge.common.crafting.DataComponentIngredient components) {
            return components.items().map(item -> new ItemStack(item, 1, components.components())).toArray(ItemStack[]::new);
        }
        return ingredient.items().map(ItemStack::new).toArray(ItemStack[]::new);
    }
    public static net.neoforged.neoforge.fluids.FluidStack[] fluids(net.neoforged.neoforge.fluids.crafting.FluidIngredient ingredient, net.minecraft.core.RegistryAccess registries) {
        var context = net.minecraft.util.context.ContextMap.builder().set(SlotDisplayContext.REGISTRIES, registries).buildAndValidate(SlotDisplayContext.CONTEXT);
        return ingredient.display().resolve(context, net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory.INSTANCE)
                .map(net.neoforged.neoforge.fluids.FluidStack::copy).toArray(net.neoforged.neoforge.fluids.FluidStack[]::new);
    }
    public static List<Optional<Ingredient>> inputs(Recipe<?> recipe) {
        var placement = recipe.placementInfo();
        var result = new ArrayList<Optional<Ingredient>>();
        for (int slot : placement.slotsToIngredientIndex()) result.add(slot == PlacementInfo.EMPTY_SLOT ? Optional.empty() : Optional.of(placement.ingredients().get(slot)));
        return List.copyOf(result);
    }
    public static ItemStack remainder(CraftingRecipe recipe, int width, int height, List<ItemStack> items, int slot) {
        var positioned = CraftingInput.ofPositioned(width, height, items);
        var input = positioned.input();
        int x = slot % width - positioned.left(), y = slot / width - positioned.top();
        if (x < 0 || y < 0 || x >= input.width() || y >= input.height()) return ItemStack.EMPTY;
        return recipe.getRemainingItems(input).get(x + y * input.width()).copy();
    }
    public static ItemStack output(Recipe<?> recipe, Level level) {
        for (var display : recipe.display()) {
            var results = display.result().resolveForStacks(SlotDisplayContext.fromLevel(level));
            if (!results.isEmpty()) return results.getFirst().copy();
        }
        // Special dynamic recipes have no fixed result; their dedicated EMI recipe generators provide it.
        return ItemStack.EMPTY;
    }
}
