package dev.emi.emi.recipe;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

public class EmiFuelRecipe implements EmiRecipe {
	private final EmiIngredient stack;
	private final net.minecraft.world.item.component.CookingFuel fuel;
	private final Identifier id;

	public EmiFuelRecipe(EmiIngredient stack, net.minecraft.world.item.component.CookingFuel fuel, Identifier id) {
		this.stack = stack;
		this.fuel = fuel;
		this.id = id;
		if (stack.getEmiStacks().get(0).getItemStack().getItem().equals(Items.LAVA_BUCKET)) {
			stack.getEmiStacks().get(0).setRemainder(EmiStack.of(Items.BUCKET));
		}
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.FUEL;
	}

	@Override
	public @Nullable Identifier getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(stack);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of();
	}

	@Override
	public int getDisplayWidth() {
		return 144;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.EMPTY_FLAME, 1, 1);
        if (fuel.burnTime() instanceof net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.Constant time
                && fuel.speedMultiplier() instanceof net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat.Constant speed) {
            widgets.addAnimatedTexture(EmiTexture.FULL_FLAME, 1, 1, Math.max(1, (int)Math.min(Integer.MAX_VALUE, 50L * time.value())), false, true, true);
            widgets.addText(EmiPort.translatable("emi.fuel_time.items",
                EmiRenderHelper.TEXT_FORMAT.format(time.value() * speed.value() / 200f)), 38, 5, -1, true);
        } else {
            widgets.addText(EmiPort.literal("Context-dependent fuel"), 38, 5, -1, true);
        }
        widgets.addSlot(stack, 18, 0).recipeContext(this);
	}
}
