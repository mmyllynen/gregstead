package com.lowdragmc.lowdraglib.transfer.fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
/** Ordered native children, validated before any mutation; journals remain owned by each child. */
public final class CombinedFluidView implements ResourceHandler<FluidResource> {
    private final List<ResourceHandler<FluidResource>> children;
    private final Predicate<FluidStack> filter;
    private final java.util.function.BooleanSupplier allowSameFluids;
    public CombinedFluidView(IFluidHandler[] handlers,Predicate<FluidStack> filter) {
        this(handlers,filter,()->true);
    }
    public CombinedFluidView(IFluidHandler[] handlers,Predicate<FluidStack> filter,java.util.function.BooleanSupplier allowSameFluids) {
        this.allowSameFluids=Objects.requireNonNull(allowSameFluids);
        this.children=java.util.Arrays.stream(handlers).map(NativeFluidViews::require).toList();
        this.filter=Objects.requireNonNull(filter);
    }
    @Override public int size(){int size=0;for(var child:children)size=Math.addExact(size,child.size());return size;}
    private record Slot(ResourceHandler<FluidResource> handler,int index){}
    private Slot slot(int index){Objects.checkIndex(index,size());for(var child:children){if(index<child.size())return new Slot(child,index);index-=child.size();}throw new AssertionError();}
    @Override public FluidResource getResource(int index){var slot=slot(index);return slot.handler.getResource(slot.index);}
    @Override public long getAmountAsLong(int index){var slot=slot(index);return slot.handler.getAmountAsLong(slot.index);}
    @Override public long getCapacityAsLong(int index,FluidResource resource){var slot=slot(index);return slot.handler.getCapacityAsLong(slot.index,resource);}
    @Override public boolean isValid(int index,FluidResource resource){var slot=slot(index);return !resource.isEmpty() && filter.test(resource.toStack(1)) && slot.handler.isValid(slot.index,resource);}
    private boolean allowed(FluidResource resource,int amount){Objects.requireNonNull(resource);if(amount<0)throw new IllegalArgumentException("Negative transfer amount");return amount>0 && !resource.isEmpty() && filter.test(resource.toStack(amount));}
    @Override public int insert(int index,FluidResource resource,int amount,TransactionContext transaction) {
        var slot=slot(index);
        if(!allowed(resource,amount))return 0;
        int existing=allowSameFluids.getAsBoolean()?-1:existing(resource);
        if(existing>=0 && existing!=index)return 0;
        return slot.handler.insert(slot.index,resource,amount,transaction);
    }
    @Override public int extract(int index,FluidResource resource,int amount,TransactionContext transaction){var slot=slot(index);return allowed(resource,amount)?slot.handler.extract(slot.index,resource,amount,transaction):0;}
    @Override public int insert(FluidResource resource,int amount,TransactionContext transaction){
        if(!allowed(resource,amount))return 0;
        if(!allowSameFluids.getAsBoolean()) {
            int existing=existing(resource);
            if(existing>=0){var slot=slot(existing);return slot.handler.insert(slot.index,resource,amount,transaction);}
            for(int index=0;index<size();index++) {
                var slot=slot(index);int inserted=slot.handler.insert(slot.index,resource,amount,transaction);
                if(inserted>0)return inserted;
            }
            return 0;
        }
        int inserted=0;
        for(var child:children){inserted+=child.insert(resource,amount-inserted,transaction);if(inserted==amount)break;}return inserted;
    }
    private int existing(FluidResource resource) {
        for(int index=0;index<size();index++)if(getAmountAsLong(index)>0 && getResource(index).equals(resource))return index;
        return -1;
    }
    @Override public int extract(FluidResource resource,int amount,TransactionContext transaction){
        if(!allowed(resource,amount))return 0;int extracted=0;
        for(var child:children){extracted+=child.extract(resource,amount-extracted,transaction);if(extracted==amount)break;}return extracted;
    }
}
