package dev.emi.emi.recipe;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.PotionIngredient;

/** Native ingredient representatives filtered by the actual brewing component predicate. */
public final class NativeBrewingData {
    private NativeBrewingData() {}
    public static List<ItemStack> choices(PotionIngredient ingredient, RegistryAccess registries) {
        var result = new ArrayList<ItemStack>();
        for (var base : NativeRecipeData.items(ingredient.ingredient())) {
            if (ingredient.potions().isEmpty()) {
                if (ingredient.test(base)) result.add(base.copy());
                continue;
            }
            if (ingredient.test(base)) result.add(base.copy());
            registries.lookupOrThrow(Registries.POTION).listElements().forEach(potion -> {
                var stack = base.copy();
                stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
                if (ingredient.test(stack) && result.stream().noneMatch(other -> ItemStack.isSameItemSameComponents(other, stack)))
                    result.add(stack);
            });
        }
        return List.copyOf(result);
    }
}
