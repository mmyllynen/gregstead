package com.gregtechceu.gtceu.integration.curios;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import java.util.function.ToLongBiFunction;

/** Charges equipped stacks directly; ItemResource snapshots cannot retain mutable energy components. */
public final class NativeCuriosCharging {
    private NativeCuriosCharging() {}

    public static long charge(Iterable<? extends IDynamicStackHandler> inventories, long remaining,
                              ToLongBiFunction<Long, ItemStack> charger) {
        for (var inventory : inventories) {
            for (int slot = 0; slot < inventory.getSlots() && remaining > 0; slot++) {
                var stack = inventory.getStackInSlot(slot);
                long charged = charger.applyAsLong(remaining, stack);
                if (charged > 0) {
                    inventory.setStackInSlot(slot, stack);
                    remaining -= charged;
                }
            }
            if (remaining <= 0) break;
        }
        return remaining;
    }
}
