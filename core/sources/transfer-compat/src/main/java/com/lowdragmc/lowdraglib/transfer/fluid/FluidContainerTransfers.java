package com.lowdragmc.lowdraglib.transfer.fluid;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
/** One-container transfer; native children and item replacement participate in the same transaction. */
public final class FluidContainerTransfers {
    private FluidContainerTransfers() {}
    public record Result(boolean success,ItemStack result){
        public boolean isSuccess(){return success;}
        public ItemStack getResult(){return result;}
    }
    public static Result tryFillContainer(ItemStack input,IFluidHandler tank,int maximum,boolean execute){return transfer(input,tank,maximum,execute,true);}
    public static Result tryEmptyContainer(ItemStack input,IFluidHandler tank,int maximum,boolean execute){return transfer(input,tank,maximum,execute,false);}
    @SuppressWarnings("deprecation")
    private static Result transfer(ItemStack input,IFluidHandler tank,int maximum,boolean execute,boolean fill){
        if(input.isEmpty() || maximum<=0)return new Result(false,ItemStack.EMPTY);
        var nativeTank=NativeFluidViews.require(tank);
        var slot=new ItemStacksResourceHandler(NonNullList.of(ItemStack.EMPTY,input.copyWithCount(1)));
        var container=ItemAccess.forHandlerIndexStrict(slot,0).getCapability(Capabilities.Fluid.ITEM);
        if(container==null)return new Result(false,ItemStack.EMPTY);
        var donor=fill?tank:ResourceFluidHandler.of(container);
        // Match legacy max-amount selection: choose the first drainable identity, not a later fallback fluid.
        var candidate=donor.drain(maximum,IFluidHandler.FluidAction.SIMULATE);
        if(candidate.isEmpty())return new Result(false,ItemStack.EMPTY);
        var identity=FluidResource.of(candidate);
        try(var transaction=Transaction.open(Transaction.getCurrentOpenedTransaction())){
            var from=fill?nativeTank:container;
            var to=fill?container:nativeTank;
            // Aggregate children before extracting bucket-sized quanta. Per-index move cannot combine small tanks.
            int inserted=to.insert(identity,candidate.getAmount(),transaction);
            if(inserted<=0 || from.extract(identity,inserted,transaction)!=inserted)
                return new Result(false,ItemStack.EMPTY);
            var result=slot.getResource(0).toStack(slot.getAmountAsInt(0));
            if(execute)transaction.commit();
            return new Result(true,result);
        }
    }
}
