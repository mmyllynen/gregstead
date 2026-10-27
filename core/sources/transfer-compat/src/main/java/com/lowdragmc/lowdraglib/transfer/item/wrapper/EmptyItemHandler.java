package com.lowdragmc.lowdraglib.transfer.item.wrapper;

import com.lowdragmc.lowdraglib.transfer.item.IItemHandlerModifiable;
import net.minecraft.world.item.ItemStack;

/** The deliberate zero-slot view used by recipe displays and cover inventories that have no items. */
public final class EmptyItemHandler implements IItemHandlerModifiable, com.lowdragmc.lowdraglib.transfer.item.NativeItemView {
    @Override public net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> nativeView() {
        return net.neoforged.neoforge.transfer.EmptyResourceHandler.instance();
    }

    public static final EmptyItemHandler INSTANCE=new EmptyItemHandler();
    private EmptyItemHandler() {}
    @Override public int getSlots(){return 0;}
    @Override public ItemStack getStackInSlot(int slot){return ItemStack.EMPTY;}
    @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return stack;}
    @Override public ItemStack extractItem(int slot,int amount,boolean simulate){return ItemStack.EMPTY;}
    @Override public int getSlotLimit(int slot){return 0;}
    @Override public boolean isItemValid(int slot,ItemStack stack){return false;}
    @Override public void setStackInSlot(int slot,ItemStack stack){throw new UnsupportedOperationException("Zero-slot inventory");}
}
