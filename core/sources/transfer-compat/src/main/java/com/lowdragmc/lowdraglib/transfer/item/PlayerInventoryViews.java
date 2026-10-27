package com.lowdragmc.lowdraglib.transfer.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import java.util.AbstractList;
import java.util.Objects;

/** Live legacy-sized equipment lists backed by native inventory equipment slots. */
public final class PlayerInventoryViews {
    private PlayerInventoryViews() {}
    public static NonNullList<ItemStack> armor(Inventory inventory) { return new EquipmentView(inventory, 36, 4); }
    public static NonNullList<ItemStack> offhand(Inventory inventory) { return new EquipmentView(inventory, Inventory.SLOT_OFFHAND, 1); }
    private static final class EquipmentView extends NonNullList<ItemStack> {
        private EquipmentView(Inventory inventory, int first, int count) {
            super(new AbstractList<>() {
                @Override public int size() { return count; }
                @Override public ItemStack get(int index) {
                    return inventory.getItem(first + Objects.checkIndex(index, count));
                }
                @Override public ItemStack set(int index, ItemStack item) {
                    int slot = first + Objects.checkIndex(index, count);
                    ItemStack previous = inventory.getItem(slot);
                    inventory.setItem(slot, item);
                    return previous;
                }
            }, ItemStack.EMPTY);
        }
    }
}
