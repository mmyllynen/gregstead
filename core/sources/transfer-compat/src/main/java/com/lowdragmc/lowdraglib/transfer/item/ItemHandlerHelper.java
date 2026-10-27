package com.lowdragmc.lowdraglib.transfer.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Internal slot helpers; every insertion goes through the target's own validation/IO rules. */
public final class ItemHandlerHelper {
    private ItemHandlerHelper() {}
    public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
        return !a.isEmpty() && !b.isEmpty() && ItemStack.isSameItemSameComponents(a,b);
    }
    public static ItemStack copyStackWithSize(ItemStack stack,int size) {return stack.copyWithCount(size);}
    public static ItemStack insertItem(IItemHandler target,ItemStack stack,boolean simulate) {
        if(target==null || stack.isEmpty())return stack;
        ItemStack remainder=stack;
        for(int slot=0;slot<target.getSlots() && !remainder.isEmpty();slot++) remainder=target.insertItem(slot,remainder,simulate);
        return remainder;
    }
    public static ItemStack insertItemStacked(IItemHandler target,ItemStack stack,boolean simulate) {
        if(target==null || stack.isEmpty())return stack;
        if(!stack.isStackable())return insertItem(target,stack,simulate);
        ItemStack remainder=stack;
        for(int slot=0;slot<target.getSlots() && !remainder.isEmpty();slot++) {
            if(canItemStacksStack(target.getStackInSlot(slot),remainder)) remainder=target.insertItem(slot,remainder,simulate);
        }
        for(int slot=0;slot<target.getSlots() && !remainder.isEmpty();slot++) {
            if(target.getStackInSlot(slot).isEmpty()) remainder=target.insertItem(slot,remainder,simulate);
        }
        return remainder;
    }
    public static void giveItemToPlayer(Player player,ItemStack stack) {giveItemToPlayer(player,stack,-1);}
    public static void giveItemToPlayer(Player player,ItemStack stack,int preferredSlot) {
        if(stack.isEmpty())return;
        var inventory=PlayerInventoryWrapper.of(player);
        var main=inventory.getMainSlots();ItemResource resource=ItemResource.of(stack);
        try(Transaction tx=open()) {
            int remaining=stack.getCount();
            if(preferredSlot>=0 && preferredSlot<main.size()) remaining-=main.insert(preferredSlot,resource,remaining,tx);
            if(remaining>0) inventory.placeItemBackInInventory(resource,remaining,tx);
            tx.commit();
        }
    }
    @SuppressWarnings("deprecation")
    private static Transaction open(){return Transaction.open(Transaction.getCurrentOpenedTransaction());}
}
