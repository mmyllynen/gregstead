package com.lowdragmc.lowdraglib.transfer.fluid;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.*;
import java.util.*;
import java.util.function.*;
/** Transactional long fluid storage over an existing machine state, with deferred commit notification. */
public final class LongFluidView implements ResourceHandler<FluidResource> {
    public record State(FluidResource resource,long amount) {}
    private final Supplier<State> read;private final Consumer<State> write;private final LongSupplier capacity;
    private final Predicate<FluidResource> filter;private final BooleanSupplier voiding;private final Runnable changed;
    private final SnapshotJournal<State> journal=new SnapshotJournal<>() {
        protected State createSnapshot(){return read.get();}
        protected void revertToSnapshot(State state){write.accept(state);}
        protected void onRootCommit(State original){if(!original.equals(read.get()))changed.run();}
    };
    public LongFluidView(Supplier<State> read,Consumer<State> write,LongSupplier capacity,Predicate<FluidResource> filter,BooleanSupplier voiding,Runnable changed){
        this.read=read;this.write=write;this.capacity=capacity;this.filter=filter;this.voiding=voiding;this.changed=changed;
    }
    private void check(int index){Objects.checkIndex(index,1);}
    public int size(){return 1;}
    public FluidResource getResource(int index){check(index);var state=read.get();return state.amount()>0?state.resource():FluidResource.EMPTY;}
    public long getAmountAsLong(int index){check(index);return read.get().amount();}
    public long getCapacityAsLong(int index,FluidResource resource){check(index);return capacity.getAsLong();}
    public boolean isValid(int index,FluidResource resource){check(index);return !resource.isEmpty() && filter.test(resource);}
    public int insert(int index,FluidResource resource,int amount,TransactionContext transaction){
        check(index);TransferPreconditions.checkNonEmptyNonNegative(resource,amount);var state=read.get();
        if(amount==0 || !isValid(index,resource) || state.amount()>0 && !state.resource().equals(resource))return 0;
        long free=Math.max(0,capacity.getAsLong()-state.amount());int accepted=voiding.getAsBoolean()?amount:(int)Math.min(amount,free);
        long stored=state.amount()+Math.min(accepted,free);
        if(accepted>0 && stored!=state.amount()){journal.updateSnapshots(transaction);write.accept(new State(resource,stored));}
        return accepted;
    }
    public int extract(int index,FluidResource resource,int amount,TransactionContext transaction){
        check(index);TransferPreconditions.checkNonEmptyNonNegative(resource,amount);var state=read.get();
        if(amount==0 || !state.resource().equals(resource))return 0;int extracted=(int)Math.min(amount,state.amount());
        if(extracted>0){journal.updateSnapshots(transaction);long remainder=state.amount()-extracted;write.accept(new State(remainder==0?FluidResource.EMPTY:resource,remainder));}
        return extracted;
    }
}
