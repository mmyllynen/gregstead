package com.lowdragmc.lowdraglib.misc;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import com.lowdragmc.lowdraglib.transfer.item.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class ItemStackTransfer extends ItemStackHandler implements IContentChangeAware {
    @Getter
    @Setter
    private Runnable OnContentsChanged = Runnables.doNothing();

    @Setter
    private Function<ItemStack, Boolean> filter;

    public ItemStackTransfer() {
        this(1);
    }

    public ItemStackTransfer(int size) {
        super(size);
    }

    public ItemStackTransfer(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    public ItemStackTransfer(ItemStack stack) {
        this(NonNullList.of(ItemStack.EMPTY, stack));
    }

    public void setStackInSlot(int slot, @Nonnull ItemStack stack, boolean notify) {
        validateSlotIndex(slot);
        requireDirectEdit();
        this.stacks.set(slot, stack);
        if (notify) {
            onContentsChanged(slot);
        }
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return filter == null || filter.apply(stack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        OnContentsChanged.run();
    }

    public ItemStackTransfer copy() {
        var copiedStack = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++) {
            copiedStack.set(i, stacks.get(i).copy());
        }
        var copied = new ItemStackTransfer(copiedStack);
        copied.setFilter(filter);
        return copied;
    }
}
