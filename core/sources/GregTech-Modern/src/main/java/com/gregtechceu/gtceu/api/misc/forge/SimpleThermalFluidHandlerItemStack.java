package com.gregtechceu.gtceu.api.misc.forge;


import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import org.jetbrains.annotations.NotNull;

public class SimpleThermalFluidHandlerItemStack extends ThermalFluidHandlerItemStack {

    public SimpleThermalFluidHandlerItemStack(@NotNull ItemAccess container, int capacity, int maxFluidTemperature,
                                              boolean gasProof, boolean acidProof, boolean cryoProof,
                                              boolean plasmaProof) {
        super(container,capacity,maxFluidTemperature,gasProof,acidProof,cryoProof,plasmaProof);
    }
    @Override protected ItemResource update(ItemResource container,int index,FluidResource resource,int amount) {
        return amount==0 || amount==capacity?super.update(container,index,resource,amount):ItemResource.EMPTY;
    }
}
