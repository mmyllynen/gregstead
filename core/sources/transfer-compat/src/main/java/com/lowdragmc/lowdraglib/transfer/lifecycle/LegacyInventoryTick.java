package com.lowdragmc.lowdraglib.transfer.lifecycle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
public interface LegacyInventoryTick {
    void legacyInventoryTick(ItemStack stack, Level level, Entity owner, int slotId, boolean selected);
}
