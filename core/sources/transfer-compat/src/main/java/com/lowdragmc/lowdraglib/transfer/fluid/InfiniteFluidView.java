package com.lowdragmc.lowdraglib.transfer.fluid;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import java.util.*;
import java.util.function.*;
/** Creative fluid identity is stable; transfer returns at most the requested per-operation amount. */
public final class InfiniteFluidView implements ResourceHandler<FluidResource> {
    private final Supplier<FluidResource> fluid;private final IntSupplier perCycle;
    public InfiniteFluidView(Supplier<FluidResource> fluid,IntSupplier perCycle){this.fluid=fluid;this.perCycle=perCycle;}
    private void check(int index){Objects.checkIndex(index,1);}
    public int size(){return 1;}
    public FluidResource getResource(int index){check(index);return fluid.get();}
    public long getAmountAsLong(int index){check(index);return fluid.get().isEmpty()?0:perCycle.getAsInt();}
    public long getCapacityAsLong(int index,FluidResource resource){check(index);return Long.MAX_VALUE;}
    public boolean isValid(int index,FluidResource resource){check(index);return !resource.isEmpty() && resource.equals(fluid.get());}
    public int insert(int index,FluidResource resource,int amount,TransactionContext tx){check(index);TransferPreconditions.checkNonEmptyNonNegative(resource,amount);return isValid(index,resource)?amount:0;}
    public int extract(int index,FluidResource resource,int amount,TransactionContext tx){check(index);TransferPreconditions.checkNonEmptyNonNegative(resource,amount);return isValid(index,resource)?Math.min(amount,Math.max(0,perCycle.getAsInt())):0;}
}
