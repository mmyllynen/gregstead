package com.gregtechceu.gtceu.api.misc.forge;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import net.neoforged.neoforge.transfer.access.ItemAccess;
public class QuantumFluidHandlerItemStack extends NativeQuantumFluidItemHandler {
    public QuantumFluidHandlerItemStack(ItemAccess access,long capacity){super(access,capacity,GTDataComponents.LARGE_FLUID_CONTENT.get());}
}
