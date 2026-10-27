package com.lowdragmc.lowdraglib.transfer.tooltip;

import com.lowdragmc.lowdraglib.transfer.mixin.ItemPropertiesComponentsAccessor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;

/** Hides native attribute rows while retaining modifiers and other tooltip choices. */
public final class AttributeTooltips {
    private AttributeTooltips() {}
    public static void hide(ItemStack stack) {
        stack.set(DataComponents.TOOLTIP_DISPLAY,stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY,TooltipDisplay.DEFAULT)
                .withHidden(DataComponents.ATTRIBUTE_MODIFIERS,true));
    }
    public static void hide(Item.Properties properties) {
        var accessor=(ItemPropertiesComponentsAccessor)properties;
        accessor.gtport$initializer(accessor.gtport$initializer().andThen((components,context,key) -> {
            var display=components.build().getOrDefault(DataComponents.TOOLTIP_DISPLAY,TooltipDisplay.DEFAULT);
            components.set(DataComponents.TOOLTIP_DISPLAY,display.withHidden(DataComponents.ATTRIBUTE_MODIFIERS,true));
        }));
    }
}
