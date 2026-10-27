package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

public class PipeTankList implements IFluidHandlerModifiable, Iterable<CustomFluidTank>, com.lowdragmc.lowdraglib.transfer.fluid.NativeFluidView {

    private final FluidPipeBlockEntity pipe;
    private final CustomFluidTank[] tanks;
    private final Direction facing;
    private final com.lowdragmc.lowdraglib.transfer.fluid.ChannelFluidView storage;
    @Override
    public net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> nativeView() {
        return storage;
    }

    public PipeTankList(FluidPipeBlockEntity pipe, Direction facing, CustomFluidTank... fluidTanks) {
        this.tanks = fluidTanks;
        this.pipe = pipe;
        this.facing = facing;
        storage = new com.lowdragmc.lowdraglib.transfer.fluid.ChannelFluidView(tanks, () -> pipe.isBlocked(facing), channel -> {
            pipe.receivedFrom(facing);
            pipe.checkAndDestroy(tanks[channel].getFluid());
        });
    }

    @Override
    public int getTanks() {
        return tanks.length;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return tanks[tank].getFluid();
    }

    @Override
    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {
        tanks[tank].setFluid(fluidStack);
    }

    @Override
    public int getTankCapacity(int tank) {
        return tanks[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tanks[tank].isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return com.lowdragmc.lowdraglib.transfer.fluid.ResourceFluidHandler.of(storage).fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0) return FluidStack.EMPTY;
        for (CustomFluidTank tank : tanks) {
            FluidStack drained = tank.drain(maxDrain, action);
            if (!drained.isEmpty()) return drained;
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.getAmount() <= 0) return FluidStack.EMPTY;
        for (CustomFluidTank tank : tanks) {
            FluidStack drained = tank.drain(resource, action);
            if (!drained.isEmpty()) return drained;
        }
        return FluidStack.EMPTY;
    }

    @Override
    @NotNull
    public Iterator<CustomFluidTank> iterator() {
        return Arrays.stream(tanks).iterator();
    }
}
