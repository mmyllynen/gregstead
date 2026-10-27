package com.gregtechceu.gtceu.api.misc.forge;
import com.gregtechceu.gtceu.api.item.datacomponents.LargeFluidContent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import java.util.Objects;
/** Long component storage with integer-sized native operations and atomic backing item exchange. */
public class NativeQuantumFluidItemHandler implements ResourceHandler<FluidResource> {
    protected final ItemAccess access;private final Item item;
    protected final long capacity;private final DataComponentType<LargeFluidContent> component;
    public NativeQuantumFluidItemHandler(ItemAccess access,long capacity,DataComponentType<LargeFluidContent> component){
        if(capacity<0)throw new IllegalArgumentException("Negative quantum capacity");
        this.access=access;this.item=access.getResource().getItem();this.capacity=capacity;this.component=component;
    }
    private void check(int index){Objects.checkIndex(index,1);}
    private boolean available(){return access.getAmount()==1 && access.getResource().is(item);}
    private LargeFluidContent content(){return available()?access.getResource().getOrDefault(component,LargeFluidContent.EMPTY):LargeFluidContent.EMPTY;}
    public int size(){return 1;}
    public FluidResource getResource(int index){check(index);var content=content();return content.amount()>0?FluidResource.of(content.stored()):FluidResource.EMPTY;}
    public long getAmountAsLong(int index){check(index);var content=content();return content.stored().isEmpty()?0:content.amount();}
    public long getCapacityAsLong(int index,FluidResource resource){check(index);return capacity;}
    public boolean isValid(int index,FluidResource resource){check(index);return available();}
    private int exchange(FluidResource resource,long amount,int changed,TransactionContext transaction){
        ItemResource replacement=amount==0?access.getResource().without(component):access.getResource().with(component,
                new LargeFluidContent(resource.toStack(1),amount));
        return access.exchange(replacement,1,transaction)==1?changed:0;
    }
    public int insert(int index,FluidResource resource,int amount,TransactionContext transaction){
        check(index);TransferPreconditions.checkNonEmptyNonNegative(resource,amount);if(!available())return 0;
        var existing=getResource(0);if(!existing.isEmpty() && !existing.equals(resource))return 0;
        long held=getAmountAsLong(0);int accepted=(int)Math.min(amount,Math.max(0,capacity-held));
        return accepted==0?0:exchange(resource,held+accepted,accepted,transaction);
    }
    public int extract(int index,FluidResource resource,int amount,TransactionContext transaction){
        check(index);TransferPreconditions.checkNonEmptyNonNegative(resource,amount);if(!available() || !resource.equals(getResource(0)))return 0;
        long held=getAmountAsLong(0);int extracted=(int)Math.min(amount,held);
        return extracted==0?0:exchange(resource,held-extracted,extracted,transaction);
    }
}
