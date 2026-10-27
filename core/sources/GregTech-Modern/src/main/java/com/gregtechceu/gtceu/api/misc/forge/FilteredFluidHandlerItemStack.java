package com.gregtechceu.gtceu.api.misc.forge;

import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import java.util.function.Predicate;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;

/** Partial-capacity component tank backed by the capability's native owning item access. */
public class FilteredFluidHandlerItemStack extends ItemAccessFluidHandler {
    private final Predicate<FluidStack> filter;
    public FilteredFluidHandlerItemStack(ItemAccess access,int capacity,Predicate<FluidStack> filter) {
        super(access,GTDataComponents.FLUID_CONTENT.get(),capacity);this.filter=filter;
    }
    @Override public boolean isValid(int index,FluidResource resource) {
        return super.isValid(index,resource) && filter.test(resource.toStack(1));
    }
}
