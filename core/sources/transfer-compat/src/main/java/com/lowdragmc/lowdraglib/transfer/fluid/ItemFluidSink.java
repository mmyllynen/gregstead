package com.lowdragmc.lowdraglib.transfer.fluid;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import java.util.*;
import java.util.function.Consumer;
/** Fluid disposal charges the backing tool through the same item-access transaction. */
public final class ItemFluidSink implements ResourceHandler<FluidResource> {
    private final ItemAccess access;private final Item item;private final Consumer<ItemStack> damage;
    public ItemFluidSink(ItemAccess access,Consumer<ItemStack> damage){this.access=access;this.item=access.getResource().getItem();this.damage=damage;}
    private void check(int index){Objects.checkIndex(index,1);}
    public int size(){return 1;}
    public FluidResource getResource(int index){check(index);return FluidResource.EMPTY;}
    public long getAmountAsLong(int index){check(index);return 0;}
    public long getCapacityAsLong(int index,FluidResource resource){check(index);return Integer.MAX_VALUE;}
    public boolean isValid(int index,FluidResource resource){check(index);return access.getAmount()==1 && access.getResource().is(item);}
    public int insert(int index,FluidResource resource,int amount,TransactionContext transaction){
        check(index);TransferPreconditions.checkNonEmptyNonNegative(resource,amount);if(amount==0 || !isValid(index,resource))return 0;
        var stack=access.getResource().toStack(1);damage.accept(stack);
        int exchanged=stack.isEmpty()?access.extract(access.getResource(),1,transaction):access.exchange(ItemResource.of(stack),1,transaction);
        return exchanged==1?amount:0;
    }
    public int extract(int index,FluidResource resource,int amount,TransactionContext transaction){check(index);TransferPreconditions.checkNonEmptyNonNegative(resource,amount);return 0;}
}
