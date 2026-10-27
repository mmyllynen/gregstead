package com.lowdragmc.lowdraglib.transfer.fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import java.util.function.Predicate;
/** Read-only direction/filter gates; underlying native children own rollback state. */
public final class DirectionalFluidView implements ResourceHandler<FluidResource> {
    private final java.util.function.Supplier<ResourceHandler<FluidResource>> delegate;
    private final Predicate<FluidStack> valid,insert,extract;
    public DirectionalFluidView(ResourceHandler<FluidResource> delegate,Predicate<FluidStack> valid,Predicate<FluidStack> insert,Predicate<FluidStack> extract){this(()->delegate,valid,insert,extract);}
    public DirectionalFluidView(java.util.function.Supplier<ResourceHandler<FluidResource>> delegate,Predicate<FluidStack> valid,Predicate<FluidStack> insert,Predicate<FluidStack> extract){this.delegate=delegate;this.valid=valid;this.insert=insert;this.extract=extract;}
    public int size(){return delegate.get().size();}
    public FluidResource getResource(int index){return delegate.get().getResource(index);}
    public long getAmountAsLong(int index){return delegate.get().getAmountAsLong(index);}
    public long getCapacityAsLong(int index,FluidResource resource){return delegate.get().getCapacityAsLong(index,resource);}
    public boolean isValid(int index,FluidResource resource){java.util.Objects.checkIndex(index,size());return !resource.isEmpty() && valid.test(resource.toStack(1)) && delegate.get().isValid(index,resource);}
    private boolean allowed(Predicate<FluidStack> predicate,FluidResource resource,int amount){if(amount<0)throw new IllegalArgumentException("Negative transfer amount");return amount>0 && !resource.isEmpty() && predicate.test(resource.toStack(amount));}
    public int insert(int index,FluidResource resource,int amount,TransactionContext transaction){java.util.Objects.checkIndex(index,size());return allowed(insert,resource,amount)?delegate.get().insert(index,resource,amount,transaction):0;}
    public int extract(int index,FluidResource resource,int amount,TransactionContext transaction){java.util.Objects.checkIndex(index,size());return allowed(extract,resource,amount)?delegate.get().extract(index,resource,amount,transaction):0;}
    public int insert(FluidResource resource,int amount,TransactionContext transaction){return allowed(insert,resource,amount)?delegate.get().insert(resource,amount,transaction):0;}
    public int extract(FluidResource resource,int amount,TransactionContext transaction){return allowed(extract,resource,amount)?delegate.get().extract(resource,amount,transaction):0;}
}
