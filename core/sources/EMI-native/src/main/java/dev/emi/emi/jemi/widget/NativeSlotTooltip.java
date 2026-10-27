package dev.emi.emi.jemi.widget;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public final class NativeSlotTooltip {
    private NativeSlotTooltip() {}
    public static <T> void add(ITooltipBuilder tooltip, ITypedIngredient<T> ingredient, IIngredientRenderer<T> renderer) {
        var client=Minecraft.getInstance();
        tooltip.setIngredient(ingredient);
        renderer.getTooltip(tooltip,ingredient.getIngredient(),Item.TooltipContext.of(client.level),client.player,
            client.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);
    }
}
