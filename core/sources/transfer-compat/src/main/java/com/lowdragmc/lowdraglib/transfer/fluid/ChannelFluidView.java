package com.lowdragmc.lowdraglib.transfer.fluid;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.*;
import java.util.*;
import java.util.function.*;

/** One channel per fluid identity, with original reverse-match/first-empty selection. */
public final class ChannelFluidView implements ResourceHandler<FluidResource> {
    private final List<ResourceHandler<FluidResource>> tanks;
    private final BooleanSupplier blocked;
    private final IntConsumer inserted;
    private final List<Integer> pending = new ArrayList<>();
    private final SnapshotJournal<List<Integer>> journal = new SnapshotJournal<>() {
        protected List<Integer> createSnapshot(){return List.copyOf(pending);}
        protected void revertToSnapshot(List<Integer> saved){pending.clear();pending.addAll(saved);}
        protected void onRootCommit(List<Integer> original){
            var committed=List.copyOf(pending);pending.clear();
            for(int channel:committed)inserted.accept(channel);
        }
    };
    public ChannelFluidView(IFluidHandler[] tanks,BooleanSupplier blocked,IntConsumer inserted){
        this.tanks=Arrays.stream(tanks).map(NativeFluidViews::require).toList();
        for(var tank:this.tanks)if(tank.size()!=1)throw new IllegalArgumentException("Each pipe channel must have one tank");
        this.blocked=Objects.requireNonNull(blocked);this.inserted=Objects.requireNonNull(inserted);
    }
    public int size(){return tanks.size();}
    private ResourceHandler<FluidResource> tank(int index){return tanks.get(index);}
    public FluidResource getResource(int index){return tank(index).getResource(0);}
    public long getAmountAsLong(int index){return tank(index).getAmountAsLong(0);}
    public long getCapacityAsLong(int index,FluidResource resource){return tank(index).getCapacityAsLong(0,resource);}
    public boolean isValid(int index,FluidResource resource){return tank(index).isValid(0,resource);}
    private int channel(FluidResource resource){
        int empty=-1;
        for(int index=size()-1;index>=0;index--){
            if(getAmountAsLong(index)==0)empty=index;
            else if(getResource(index).equals(resource))return index;
        }
        return empty;
    }
    public int insert(int index,FluidResource resource,int amount,TransactionContext transaction){
        Objects.checkIndex(index,size());TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(amount==0 || blocked.getAsBoolean() || channel(resource)!=index)return 0;
        int accepted=tank(index).insert(0,resource,amount,transaction);
        if(accepted>0){journal.updateSnapshots(transaction);pending.add(index);}
        return accepted;
    }
    public int insert(FluidResource resource,int amount,TransactionContext transaction){
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        int channel=channel(resource);return channel<0?0:insert(channel,resource,amount,transaction);
    }
    public int extract(int index,FluidResource resource,int amount,TransactionContext transaction){
        return tank(index).extract(0,resource,amount,transaction);
    }
}
