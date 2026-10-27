package com.lowdragmc.lowdraglib.transfer.fluid;

import net.neoforged.neoforge.fluids.FluidStack;

public interface IFluidHandlerModifiable extends IFluidHandler {
    void setFluidInTank(int tank, FluidStack stack);
    default boolean supportsFill(int tank) { return true; }
    default boolean supportsDrain(int tank) { return true; }
}
