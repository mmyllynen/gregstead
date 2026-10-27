package com.lowdragmc.lowdraglib.misc;

import net.minecraft.world.entity.player.Inventory;
import com.lowdragmc.lowdraglib.transfer.item.wrapper.InvWrapper;

public class PlayerInventoryTransfer extends InvWrapper {
    public PlayerInventoryTransfer(Inventory inv) {
        super(inv);
    }

    @Override
    public int getSlots() {
        return net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper.of((Inventory) getInv()).getMainSlots().size();
    }
}
