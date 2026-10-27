package com.lowdragmc.lowdraglib.transfer.item;

import net.minecraft.world.item.ItemStack;

/** Internal slot API used by recipe simulation and UI code; external capabilities use ResourceHandler. */
public interface IItemHandler {
    int getSlots();
    ItemStack getStackInSlot(int slot);
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
    ItemStack extractItem(int slot, int amount, boolean simulate);
    int getSlotLimit(int slot);
    boolean isItemValid(int slot, ItemStack stack);
}
