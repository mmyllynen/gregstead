package com.gregtechceu.gtceu.api.transfer.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import com.lowdragmc.lowdraglib.transfer.fluid.IFluidHandler;
import com.lowdragmc.lowdraglib.transfer.fluid.NativeFluidViews;
import com.lowdragmc.lowdraglib.transfer.fluid.NativeFluidView;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ModifiableFluidHandlerWrapper implements IFluidHandlerModifiable, NativeFluidView {

    private IFluidHandler handler;

    @Override
    public ResourceHandler<FluidResource> nativeView() {
        // This wrapper delegates all transfer operations. Keep the underlying native
        // direction/filter gates and rollback owner rather than creating a second view.
        return NativeFluidViews.require(handler);
    }

    @Override
    public void setFluidInTank(int tank, FluidStack fluidStack) {
        if (handler instanceof IFluidHandlerModifiable modifiable) {
            modifiable.setFluidInTank(tank, fluidStack);
            return;
        }
        var fluid = handler.getFluidInTank(tank);
        var canDrain = handler.drain(fluid, FluidAction.SIMULATE);
        if (!canDrain.isEmpty()) {
            drain(canDrain, FluidAction.EXECUTE);
            fill(fluidStack, FluidAction.EXECUTE);
        }
    }

    @Override
    public int getTanks() {
        return handler.getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return handler.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return handler.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return handler.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return handler.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return handler.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return handler.drain(maxDrain, action);
    }
}
