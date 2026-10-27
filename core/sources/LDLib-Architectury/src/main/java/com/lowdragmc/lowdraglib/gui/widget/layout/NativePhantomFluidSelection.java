package com.lowdragmc.lowdraglib.gui.widget.layout;

import com.lowdragmc.lowdraglib.transfer.fluid.IFluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;

/** Same phantom selection replacement for local widgets and received server actions. */
public final class NativePhantomFluidSelection {
    private NativePhantomFluidSelection() {}
    public static void replace(IFluidHandler handler, int tank, FluidStack selection) {
        handler.drain(handler.getTankCapacity(tank), IFluidHandler.FluidAction.EXECUTE);
        if (selection != null && !selection.isEmpty()) handler.fill(selection.copy(), IFluidHandler.FluidAction.EXECUTE);
    }
}
