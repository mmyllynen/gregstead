package com.gregtechceu.gtceu.common.data;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import com.lowdragmc.lowdraglib.transfer.item.IItemHandler;
import java.util.Locale;

public final class NativePlaceholderValues {
    private NativePlaceholderValues() {}
    public static int countItems(String id, IItemHandler inventory) {
        if (inventory == null) return 0;
        int count = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(id)) count += stack.getCount();
        }
        return count;
    }
    public static ChatFormatting formatting(String name) {
        if (name == null) return null;
        String normalized = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        for (var value : ChatFormatting.values())
            if (value.name().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "").equals(normalized)) return value;
        return null;
    }
}
