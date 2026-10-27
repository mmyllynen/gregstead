package com.lowdragmc.lowdraglib.transfer.lifecycle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
public final class ItemTickBridge {
    private ItemTickBridge() {}
    public static void dispatch(ItemStack stack, Level level, Entity owner, @Nullable EquipmentSlot equipmentSlot) {
        if (stack.getItem() instanceof LegacyInventoryTick ticker)
            ticker.legacyInventoryTick(stack, level, owner, slotId(stack, owner, equipmentSlot), equipmentSlot==EquipmentSlot.MAINHAND);
    }
    public static int slotId(ItemStack stack, Entity owner, @Nullable EquipmentSlot equipmentSlot) {
        if (owner instanceof Player player) {
            var inventory=player.getInventory();
            if (equipmentSlot==EquipmentSlot.MAINHAND) return inventory.getSelectedSlot();
            if (equipmentSlot!=null) {
                for(var entry:Inventory.EQUIPMENT_SLOT_MAPPING.int2ObjectEntrySet())
                    if(entry.getValue()==equipmentSlot)return entry.getIntKey();
            }
            for(int i=0;i<inventory.getNonEquipmentItems().size();i++)
                if(inventory.getItem(i)==stack)return i;
        }
        return -1;
    }
}
