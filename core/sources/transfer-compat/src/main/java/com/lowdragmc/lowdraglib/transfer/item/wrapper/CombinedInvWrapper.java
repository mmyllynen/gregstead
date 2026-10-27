package com.lowdragmc.lowdraglib.transfer.item.wrapper;

import com.lowdragmc.lowdraglib.transfer.item.IItemHandlerModifiable;
import net.minecraft.world.item.ItemStack;
import java.util.Objects;

/** Internal aggregate slot view; mutations retain each child's filters and native journals. */
public class CombinedInvWrapper implements IItemHandlerModifiable, com.lowdragmc.lowdraglib.transfer.item.NativeItemView {
    @Override public net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> nativeView() {
        return new net.neoforged.neoforge.transfer.CombinedResourceHandler<>(java.util.Arrays.stream(itemHandler)
                .map(com.lowdragmc.lowdraglib.transfer.item.NativeItemViews::require).toList());
    }
    protected final IItemHandlerModifiable[] itemHandler;
    protected final int[] baseIndex;
    protected final int slotCount;
    public CombinedInvWrapper(IItemHandlerModifiable... handlers) {
        itemHandler = handlers.clone(); baseIndex = new int[handlers.length];
        int count = 0;
        for (int i=0;i<handlers.length;i++) {
            count = Math.addExact(count, Objects.requireNonNull(handlers[i]).getSlots());
            baseIndex[i] = count;
        }
        slotCount = count;
    }
    protected int getIndexForSlot(int slot) {
        Objects.checkIndex(slot, slotCount);
        for (int i=0;i<baseIndex.length;i++) if (slot < baseIndex[i]) return i;
        throw new IndexOutOfBoundsException(slot);
    }
    protected IItemHandlerModifiable getHandlerFromIndex(int index) { return itemHandler[index]; }
    protected int getSlotFromIndex(int slot, int index) { return slot - (index==0 ? 0 : baseIndex[index-1]); }
    @Override public int getSlots() { return slotCount; }
    @Override public ItemStack getStackInSlot(int slot) {
        int index=getIndexForSlot(slot);return itemHandler[index].getStackInSlot(getSlotFromIndex(slot,index));
    }
    @Override public void setStackInSlot(int slot, ItemStack stack) {
        int index=getIndexForSlot(slot);itemHandler[index].setStackInSlot(getSlotFromIndex(slot,index),stack);
    }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        int index=getIndexForSlot(slot);return itemHandler[index].insertItem(getSlotFromIndex(slot,index),stack,simulate);
    }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        int index=getIndexForSlot(slot);return itemHandler[index].extractItem(getSlotFromIndex(slot,index),amount,simulate);
    }
    @Override public int getSlotLimit(int slot) {
        int index=getIndexForSlot(slot);return itemHandler[index].getSlotLimit(getSlotFromIndex(slot,index));
    }
    @Override public boolean isItemValid(int slot, ItemStack stack) {
        int index=getIndexForSlot(slot);return itemHandler[index].isItemValid(getSlotFromIndex(slot,index),stack);
    }
}
