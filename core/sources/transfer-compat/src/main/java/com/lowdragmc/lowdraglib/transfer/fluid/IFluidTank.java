package com.lowdragmc.lowdraglib.transfer.fluid;

import net.neoforged.neoforge.fluids.FluidStack;

public interface IFluidTank {
    FluidStack getFluid();
    int getFluidAmount();
    int getCapacity();
    boolean isFluidValid(FluidStack stack);
    int fill(FluidStack resource, IFluidHandler.FluidAction action);
    FluidStack drain(int maxDrain, IFluidHandler.FluidAction action);
}
