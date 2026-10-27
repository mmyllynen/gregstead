package com.lowdragmc.lowdraglib.transfer.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Objects;

/** Makes a native inventory available to existing GregTech slot-based pipe and cover logic. */
public final class ResourceItemHandler implements IItemHandler, NativeItemView {
    @Override public ResourceHandler<ItemResource> nativeView() { return handler; }

    private final ResourceHandler<ItemResource> handler;

    private ResourceItemHandler(ResourceHandler<ItemResource> handler) {
        this.handler = Objects.requireNonNull(handler);
    }

    public static IItemHandler of(ResourceHandler<ItemResource> handler) {
        return handler instanceof IItemHandler slots ? slots : new ResourceItemHandler(handler);
    }

    @Override
    public int getSlots() { return handler.size(); }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return stack;
        try (Transaction transaction = openTransaction()) {
            int inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), transaction);
            ItemStack remainder = stack.copyWithCount(stack.getCount() - inserted);
            if (!simulate) transaction.commit();
            return remainder;
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemResource resource = handler.getResource(slot);
        if (amount <= 0 || resource.isEmpty()) return ItemStack.EMPTY;
        try (Transaction transaction = openTransaction()) {
            int extracted = handler.extract(slot, resource, amount, transaction);
            ItemStack result = resource.toStack(extracted);
            if (!simulate) transaction.commit();
            return result;
        }
    }

    @SuppressWarnings("deprecation")
    private static Transaction openTransaction() {
        return Transaction.open(Transaction.getCurrentOpenedTransaction());
    }

    @Override
    public int getSlotLimit(int slot) {
        return handler.getCapacityAsInt(slot, ItemResource.EMPTY);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !stack.isEmpty() && handler.isValid(slot, ItemResource.of(stack));
    }
}
