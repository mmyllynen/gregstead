package com.lowdragmc.lowdraglib.transfer.menu;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Shared original ghost-slot operation: edits filter contents without consuming held items. */
public final class PhantomSlotOperations {
    private PhantomSlotOperations() {}
    public static ItemStack click(Slot slot, int button, ContainerInput input, ItemStack held, int maximum,
                                  Predicate<ItemStack> validator, BiPredicate<ItemStack, ItemStack> equal) {
        var previous = slot.getItem().isEmpty() ? ItemStack.EMPTY : slot.getItem().copy();
        if (button == 2) fill(slot, ItemStack.EMPTY, button, maximum, validator);
        else if (button == 0 || button == 1) {
            var current = slot.getItem();
            if (current.isEmpty()) {
                if (!held.isEmpty()) fill(slot, held, button, maximum, validator);
            } else if (held.isEmpty()) adjust(slot, button, input, maximum);
            else {
                if (!equal.test(current, held)) adjust(slot, button, input, maximum);
                fill(slot, held, button, maximum, validator);
            }
        } else if (button == 5 && !slot.hasItem()) fill(slot, held, button, maximum, validator);
        return previous;
    }
    private static void adjust(Slot slot, int button, ContainerInput input, int maximum) {
        var stack = slot.getItem();
        int count = input == ContainerInput.QUICK_MOVE
                ? (button == 0 ? (stack.getCount() + 1) / 2 : stack.getCount() * 2)
                : (button == 0 ? stack.getCount() - 1 : stack.getCount() + 1);
        stack.setCount(Math.min(maximum, Math.min(slot.getMaxStackSize(), count)));
        slot.set(stack);
    }
    private static void fill(Slot slot, ItemStack held, int button, int maximum, Predicate<ItemStack> validator) {
        if (held.isEmpty()) { slot.set(ItemStack.EMPTY); return; }
        int count = Math.min(maximum, Math.min(slot.getMaxStackSize(), button == 0 ? held.getCount() : 1));
        var phantom = held.copyWithCount(count);
        if (validator.test(phantom)) slot.set(phantom);
    }
}
