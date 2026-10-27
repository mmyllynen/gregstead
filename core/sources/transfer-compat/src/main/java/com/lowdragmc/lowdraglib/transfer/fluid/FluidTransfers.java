package com.lowdragmc.lowdraglib.transfer.fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
/** Component-exact tank transfer joins the caller transaction and rolls back any partial extraction. */
public final class FluidTransfers {
    private FluidTransfers() {}
    public static FluidStack move(IFluidHandler destination,IFluidHandler source,FluidStack requested,boolean execute) {
        if(requested.isEmpty() || destination==source)return FluidStack.EMPTY;
        var from=NativeFluidViews.require(source);var to=NativeFluidViews.require(destination);
        var available=source.drain(requested,IFluidHandler.FluidAction.SIMULATE);
        if(available.isEmpty())return FluidStack.EMPTY;
        int limit=Math.min(requested.getAmount(),available.getAmount());
        var identity=FluidResource.of(requested);
        if(!identity.matches(available))return FluidStack.EMPTY;
        try(var transaction=Transaction.open(Transaction.getCurrentOpenedTransaction())) {
            int accepted=to.insert(identity,limit,transaction);
            if(accepted<=0 || from.extract(identity,accepted,transaction)!=accepted)return FluidStack.EMPTY;
            if(execute)transaction.commit();
            return requested.copyWithAmount(accepted);
        }
    }
}
