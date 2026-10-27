package com.lowdragmc.lowdraglib.transfer.fluid;

import net.minecraft.world.item.ItemStack;

/** Internal container contract used by GregTech canning recipes; native item capabilities require ItemAccess. */
public interface IFluidHandlerItem extends IFluidHandler {
    ItemStack getContainer();
}
