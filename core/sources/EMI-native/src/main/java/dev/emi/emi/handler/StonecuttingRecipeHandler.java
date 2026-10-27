package dev.emi.emi.handler;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;

public class StonecuttingRecipeHandler implements StandardRecipeHandler<StonecutterMenu> {

	@Override
	public List<Slot> getInputSources(StonecutterMenu handler) {
		List<Slot> list = Lists.newArrayList();
		list.add(handler.getSlot(0));
		int invStart = 2;
		for (int i = invStart; i < invStart + 36; i++) { 
			list.add(handler.getSlot(i));
		}
		return list;
	}

	@Override
	public List<Slot> getCraftingSlots(StonecutterMenu handler) {
		return List.of(handler.slots.get(0));
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return recipe.getCategory() == VanillaEmiRecipeCategories.STONECUTTING;
	}

	@Override
	public @Nullable Slot getOutputSlot(StonecutterMenu handler) {
		return handler.getSlot(1);
	}

	@Override
	public boolean craft(EmiRecipe recipe, EmiCraftContext<StonecutterMenu> context) {
		Minecraft client = Minecraft.getInstance();
        if (recipe.getId() == null || recipe.getInputs().isEmpty() || recipe.getInputs().getFirst().getEmiStacks().isEmpty()) return false;
        var holder = dev.emi.emi.runtime.NativeRecipeContents.get(recipe.getId());
        if (client.level == null || holder == null || !(holder.value() instanceof StonecutterRecipe)) return false;
        @SuppressWarnings("unchecked")
        var target = (RecipeHolder<StonecutterRecipe>) holder;
        var input = recipe.getInputs().getFirst().getEmiStacks().getFirst().getItemStack();
        int index = dev.emi.emi.recipe.NativeStonecuttingSelection.index(target, client.level.recipeAccess().stonecutterRecipes(), input);
        if (index < 0 || !StandardRecipeHandler.super.craft(recipe, context)) return false;
        StonecutterMenu menu = context.getScreenHandler();
        client.gameMode.handleInventoryButtonClick(menu.containerId, index);
        if (context.getDestination() == EmiCraftContext.Destination.CURSOR) {
            client.gameMode.handleContainerInput(menu.containerId, 1, 0, ContainerInput.PICKUP, client.player);
        } else if (context.getDestination() == EmiCraftContext.Destination.INVENTORY) {
            client.gameMode.handleContainerInput(menu.containerId, 1, 0, ContainerInput.QUICK_MOVE, client.player);
        }
        return true;
    }
}
