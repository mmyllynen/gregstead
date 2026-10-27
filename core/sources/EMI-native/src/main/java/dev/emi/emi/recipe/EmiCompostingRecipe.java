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
import dev.emi.emi.api.widget.TextWidget.Alignment;
import dev.emi.emi.api.widget.WidgetHolder;

public class EmiCompostingRecipe implements EmiRecipe {
	private static final EmiStack BONE_MEAL = EmiStack.of(Items.BONE_MEAL);
	private final EmiIngredient stack;
	private final net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt layers;
	private final Identifier id;

	public EmiCompostingRecipe(EmiIngredient stack, net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt layers, Identifier id) {
		this.stack = stack;
		this.layers = layers;
		this.id = id;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.COMPOSTING;
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
		return List.of(BONE_MEAL);
	}

	@Override
	public int getDisplayWidth() {
		return 238;
	}

	@Override
	public int getDisplayHeight() {
		return 52;
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(stack,0,18);
        widgets.addTexture(EmiTexture.EMPTY_ARROW,190,19);
        widgets.addSlot(BONE_MEAL,220,18).recipeContext(this);
        var profile=dev.emi.emi.compost.CompostProfiles.get(layers);
        var empty=profile.flatMap(value -> value.levels().get(0));
        var filled=profile.flatMap(value -> value.levels().get(1));
        widgets.addText(net.minecraft.network.chat.Component.translatableWithFallback("emi.compost.empty","Empty: %s",dev.emi.emi.compost.CompostText.describe(empty,0)),25,4,-1,true);
        widgets.addText(net.minecraft.network.chat.Component.translatableWithFallback("emi.compost.level","Level %s: %s",1,dev.emi.emi.compost.CompostText.describe(filled,1)),25,19,-1,true);
        widgets.addText(net.minecraft.network.chat.Component.translatableWithFallback("emi.compost.total","7 layers → bone meal"),25,34,-1,true);
        var tooltip=new java.util.ArrayList<net.minecraft.network.chat.Component>();
        for(int fill=0;fill<7;fill++) {
            int current=fill;
            tooltip.add(net.minecraft.network.chat.Component.translatableWithFallback("emi.compost.level","Level %s: %s",fill,
                dev.emi.emi.compost.CompostText.describe(profile.flatMap(value -> value.levels().get(current)),fill)));
        }
        widgets.addTooltip(tooltip.stream().map(net.minecraft.network.chat.Component::getVisualOrderText)
            .map(net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent::create).toList(),25,0,190,48);
    }
}
