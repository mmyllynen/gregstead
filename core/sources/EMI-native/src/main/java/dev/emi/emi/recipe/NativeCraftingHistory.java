package dev.emi.emi.recipe;

import java.util.Optional;
import dev.emi.emi.runtime.NativeRecipeContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Identify the consumed recipe from synced contents before the native slot removes inputs. */
public final class NativeCraftingHistory {
    private NativeCraftingHistory() {}
    public static Optional<Identifier> recipe(CraftingInput input, ItemStack crafted, Level level) {
        if (input.isEmpty() || crafted.isEmpty() || !NativeRecipeContents.isReady()) return Optional.empty();
        return NativeRecipeContents.map().getRecipesFor(RecipeType.CRAFTING, input, level)
            .filter(holder -> ItemStack.isSameItemSameComponents(holder.value().assemble(input), crafted))
            .map(holder -> holder.id().identifier()).findFirst();
    }
}
