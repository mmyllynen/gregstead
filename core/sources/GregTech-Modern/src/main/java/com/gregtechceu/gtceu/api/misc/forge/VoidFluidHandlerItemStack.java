package com.gregtechceu.gtceu.api.misc.forge;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.minecraft.world.item.Item;

/** Native item fluid disposal; each operation accepts at most the configured capacity. */
public final class VoidFluidHandlerItemStack implements ResourceHandler<FluidResource> {
    private final ItemAccess access;
    private final Item item;
    private final int capacity;
    public VoidFluidHandlerItemStack(ItemAccess access) { this(access,Integer.MAX_VALUE); }
    public VoidFluidHandlerItemStack(ItemAccess access,int capacity) {
        if(capacity<0)throw new IllegalArgumentException("Negative disposal capacity");
        this.access=access;this.item=access.getResource().getItem();this.capacity=capacity;
    }
    private void check(int index){java.util.Objects.checkIndex(index,1);}
    public int size(){return 1;}
    public FluidResource getResource(int index){check(index);return FluidResource.EMPTY;}
    public long getAmountAsLong(int index){check(index);return 0;}
    public long getCapacityAsLong(int index,FluidResource resource){check(index);return capacity;}
    public boolean isValid(int index,FluidResource resource){check(index);return access.getAmount()==1 && access.getResource().is(item);}
    public int insert(int index,FluidResource resource,int amount,TransactionContext transaction) {
        check(index);TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        return isValid(index,resource)?Math.min(capacity,amount):0;
    }
    public int extract(int index,FluidResource resource,int amount,TransactionContext transaction) {
        check(index);TransferPreconditions.checkNonEmptyNonNegative(resource,amount);return 0;
    }
}
