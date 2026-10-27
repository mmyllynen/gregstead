package com.lowdragmc.lowdraglib.transfer.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Missing distillation outlet intentionally discards its fraction, with no persistent state. */
public final class DiscardFluidHandler implements IFluidHandler, ResourceHandler<FluidResource> {
    public static final DiscardFluidHandler INSTANCE = new DiscardFluidHandler();
    private DiscardFluidHandler() {}
    private void check(int index) { java.util.Objects.checkIndex(index,1); }
    public int getTanks(){return 1;}
    public FluidStack getFluidInTank(int index){check(index);return FluidStack.EMPTY;}
    public int getTankCapacity(int index){check(index);return Integer.MAX_VALUE;}
    public boolean isFluidValid(int index,FluidStack resource){check(index);return true;}
    public int fill(FluidStack resource,FluidAction action){return resource.getAmount();}
    public FluidStack drain(FluidStack resource,FluidAction action){return FluidStack.EMPTY;}
    public FluidStack drain(int maximum,FluidAction action){return FluidStack.EMPTY;}
    public int size(){return 1;}
    public FluidResource getResource(int index){check(index);return FluidResource.EMPTY;}
    public long getAmountAsLong(int index){check(index);return 0;}
    public long getCapacityAsLong(int index,FluidResource resource){check(index);return Integer.MAX_VALUE;}
    public boolean isValid(int index,FluidResource resource){check(index);return true;}
    public int insert(int index,FluidResource resource,int amount,TransactionContext transaction){
        check(index);TransferPreconditions.checkNonEmptyNonNegative(resource,amount);return amount;
    }
    public int extract(int index,FluidResource resource,int amount,TransactionContext transaction){
        check(index);TransferPreconditions.checkNonEmptyNonNegative(resource,amount);return 0;
    }
}
