package com.lowdragmc.lowdraglib.transfer.item.wrapper;

import com.lowdragmc.lowdraglib.transfer.item.IItemHandlerModifiable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import java.util.Objects;

/** Container slot API backed by the correct native ownership-aware wrapper. */
public class InvWrapper implements IItemHandlerModifiable, com.lowdragmc.lowdraglib.transfer.item.NativeItemView {
    @Override public ResourceHandler<ItemResource> nativeView() { return handler; }

    private final Container inv;
    protected final ResourceHandler<ItemResource> handler;
    public InvWrapper(Container inv) {
        this.inv=Objects.requireNonNull(inv);
        handler=inv instanceof Inventory inventory ? PlayerInventoryWrapper.of(inventory) : VanillaContainerWrapper.of(inv);
    }
    public Container getInv() { return inv; }
    @Override public int getSlots() { return inv.getContainerSize(); }
    protected void checkSlot(int slot) { Objects.checkIndex(slot,getSlots()); }
    @Override public ItemStack getStackInSlot(int slot) { checkSlot(slot);return inv.getItem(slot); }
    @Override public void setStackInSlot(int slot, ItemStack stack) {
        checkSlot(slot);
        if (Transaction.getLifecycle()==Transaction.Lifecycle.OPEN || Transaction.getLifecycle()==Transaction.Lifecycle.CLOSING)
            throw new IllegalStateException("Direct container edits cannot bypass a transfer transaction");
        inv.setItem(slot,Objects.requireNonNull(stack)); inv.setChanged();
    }
    @Override public int getSlotLimit(int slot) { checkSlot(slot);return inv.getMaxStackSize(); }
    @Override public boolean isItemValid(int slot, ItemStack stack) { checkSlot(slot);return inv.canPlaceItem(slot,stack); }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        checkSlot(slot);if (stack.isEmpty())return stack;
        try(Transaction tx=open()) {
            int inserted=handler.insert(slot,ItemResource.of(stack),stack.getCount(),tx);
            ItemStack remainder=stack.copyWithCount(stack.getCount()-inserted);
            if(!simulate)tx.commit(); return remainder;
        }
    }
    @Override public ItemStack extractItem(int slot,int amount,boolean simulate) {
        checkSlot(slot);ItemResource resource=handler.getResource(slot);
        if(amount<=0 || resource.isEmpty())return ItemStack.EMPTY;
        try(Transaction tx=open()) {
            ItemStack result=resource.toStack(handler.extract(slot,resource,amount,tx));
            if(!simulate)tx.commit();return result;
        }
    }
    @SuppressWarnings("deprecation")
    private static Transaction open() {return Transaction.open(Transaction.getCurrentOpenedTransaction());}
}
