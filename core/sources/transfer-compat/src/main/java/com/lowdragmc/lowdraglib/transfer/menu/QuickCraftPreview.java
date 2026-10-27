package com.lowdragmc.lowdraglib.transfer.menu;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Detached slot display matching native quick-craft distribution. */
public final class QuickCraftPreview {
    private QuickCraftPreview() {}

    public static ItemStack stack(Slot slot, ItemStack carried, int selectedSlots, int type) {
        if (carried.isEmpty() || selectedSlots <= 1
                || !AbstractContainerMenu.canItemQuickReplace(slot, carried, true)) {
            return slot.getItem().copy();
        }
        int existing = slot.getItem().getCount();
        int limit = Math.min(carried.getMaxStackSize(), slot.getMaxStackSize(carried));
        int count = AbstractContainerMenu.getQuickCraftPlaceCount(selectedSlots, type, carried) + existing;
        return carried.copyWithCount(Math.min(count, limit));
    }
}
