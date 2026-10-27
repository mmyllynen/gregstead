package com.lowdragmc.lowdraglib.transfer.item;

import com.lowdragmc.lowdraglib.transfer.persistence.INBTSerializable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Objects;

/**
 * Native transaction-aware storage with the slot operations used by GregTech recipes.
 * Simulation runs in an aborted transaction. Native insert/extract journal each slot
 * and invoke content listeners only after a root commit, including nested transactions.
 */
public class ItemStackHandler extends ItemStacksResourceHandler implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {
    public ItemStackHandler() { this(1); }
    public ItemStackHandler(int size) { super(size); }
    public ItemStackHandler(NonNullList<ItemStack> stacks) { super(stacks); }

    @Override
    public int getSlots() { return size(); }

    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return stacks.get(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        requireDirectEdit();
        Objects.requireNonNull(stack);
        set(slot, ItemResource.of(stack), stack.getCount());
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        validateSlotIndex(slot);
        Objects.requireNonNull(stack);
        if (stack.isEmpty() || !isItemValid(slot, stack)) return stack;
        try (Transaction transaction = openSlotTransaction()) {
            int inserted = insert(slot, ItemResource.of(stack), stack.getCount(), transaction);
            ItemStack remainder = stack.copyWithCount(stack.getCount() - inserted);
            if (!simulate) transaction.commit();
            return remainder;
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        validateSlotIndex(slot);
        ItemStack current = stacks.get(slot);
        if (amount <= 0 || current.isEmpty()) return ItemStack.EMPTY;
        try (Transaction transaction = openSlotTransaction()) {
            // Preserve the slot API's limit of one ordinary stack per extraction.
            int extracted = extract(slot, ItemResource.of(current),
                    Math.min(amount, current.getMaxStackSize()), transaction);
            ItemStack result = current.copyWithCount(extracted);
            if (!simulate) transaction.commit();
            return result;
        }
    }

    @SuppressWarnings("deprecation")
    private static Transaction openSlotTransaction() {
        // Legacy simulation calls have no context parameter; join an explicit native
        // transaction if the caller already opened one, otherwise open a root.
        return Transaction.open(Transaction.getCurrentOpenedTransaction());
    }

    @Override
    public int getSlotLimit(int slot) { validateSlotIndex(slot); return 64; }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) { validateSlotIndex(slot); return true; }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return isItemValid(index, resource.toStack());
    }

    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        return resource.isEmpty() ? getSlotLimit(index) : getStackLimit(index, resource.toStack());
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousContents) { onContentsChanged(index); }
    protected void onContentsChanged(int slot) {}
    protected void onLoad() {}

    protected void validateSlotIndex(int slot) { Objects.checkIndex(slot, size()); }

    protected static void requireDirectEdit() {
        if (Transaction.getLifecycle() == Transaction.Lifecycle.OPEN ||
                Transaction.getLifecycle() == Transaction.Lifecycle.CLOSING) {
            throw new IllegalStateException("Direct inventory edits cannot bypass an open transfer transaction");
        }
    }

    public void setSize(int size) {
        requireDirectEdit();
        setStacks(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    /** Retain the original Items/Slot/Size NBT layout for GregTech saves and sync accessors. */
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag result = new CompoundTag();
        ListTag items = new ListTag();
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        for (int slot = 0; slot < size(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (stack.isEmpty()) continue;
            CompoundTag entry = (CompoundTag) ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
            entry.putInt("Slot", slot);
            items.add(entry);
        }
        result.put("Items", items);
        result.putInt("Size", size());
        return result;
    }

    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        requireDirectEdit();
        int size = tag.getIntOr("Size", size());
        if (size < 0) throw new IllegalArgumentException("Negative inventory size: " + size);
        NonNullList<ItemStack> restored = NonNullList.withSize(size, ItemStack.EMPTY);
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        for (Tag value : tag.getListOrEmpty("Items")) {
            if (!(value instanceof CompoundTag entry)) continue;
            int slot = entry.getIntOr("Slot", -1);
            if (slot >= 0 && slot < size) restored.set(slot, ItemStack.CODEC.parse(ops, entry).getOrThrow());
        }
        setStacks(restored);
        onLoad();
    }
}
