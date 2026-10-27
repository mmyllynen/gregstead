package dev.emi.emi.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;

/** Select by the native menu's synchronized order, including stripped client recipe holders. */
public final class NativeStonecuttingSelection {
    private NativeStonecuttingSelection() {}
    public static int index(RecipeHolder<StonecutterRecipe> target,
            SelectableRecipe.SingleInputSet<StonecutterRecipe> all, ItemStack input) {
        if (!target.value().input().test(input)) return -1;
        var visible = all.selectByInput(input).entries();
        for (int i = 0; i < visible.size(); i++) {
            var option = visible.get(i).recipe();
            if (option.recipe().isPresent()) {
                if (option.recipe().get().id().equals(target.id())) return i;
            } else if (option.optionDisplay().equals(target.value().resultDisplay())) {
                return i;
            }
        }
        return -1;
    }
}
