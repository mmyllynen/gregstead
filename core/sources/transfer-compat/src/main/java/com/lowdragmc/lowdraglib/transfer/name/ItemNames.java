package com.lowdragmc.lowdraglib.transfer.name;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
public final class ItemNames {
    private ItemNames() {}
    public static String descriptionId(ItemStack stack) {
        return stack.getItem() instanceof DynamicStackDescription source
                ? source.dynamicStackDescriptionId(stack) : stack.getItem().getDescriptionId();
    }
    public static Component fallbackName(ItemStack stack) {
        String key=descriptionId(stack);
        if (!key.equals(stack.getItem().getDescriptionId())) return Component.translatable(key);
        return stack.getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
    }
}
