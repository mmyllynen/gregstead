package com.lowdragmc.lowdraglib.transfer.item;

import net.minecraft.world.item.ItemStack;

/** Internal direct-edit API for loading inventories and editing ghost slots. */
public interface IItemHandlerModifiable extends IItemHandler {
    void setStackInSlot(int slot, ItemStack stack);
}
