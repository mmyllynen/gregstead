package com.lowdragmc.lowdraglib.transfer.item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
/** Live native delegate and gates. Extraction filters receive the actual extractable count. */
public final class DirectionalItemView implements ResourceHandler<ItemResource> {
    private final Supplier<ResourceHandler<ItemResource>> delegate;
    private final Predicate<ItemStack> valid,input,output;
    public DirectionalItemView(ResourceHandler<ItemResource> delegate,Predicate<ItemStack> valid,Predicate<ItemStack> input,Predicate<ItemStack> output) {
        this(()->delegate,valid,input,output);
    }
    public DirectionalItemView(Supplier<ResourceHandler<ItemResource>> delegate,Predicate<ItemStack> valid,Predicate<ItemStack> input,Predicate<ItemStack> output) {
        this.delegate=delegate;this.valid=valid;this.input=input;this.output=output;
    }
    public int size(){return delegate.get().size();}
    public ItemResource getResource(int index){return delegate.get().getResource(index);}
    public long getAmountAsLong(int index){return delegate.get().getAmountAsLong(index);}
    public long getCapacityAsLong(int index,ItemResource resource){return delegate.get().getCapacityAsLong(index,resource);}
    public boolean isValid(int index,ItemResource resource){var child=delegate.get();java.util.Objects.checkIndex(index,child.size());return !resource.isEmpty() && valid.test(resource.toStack(1)) && child.isValid(index,resource);}
    public int insert(int index,ItemResource resource,int amount,TransactionContext transaction) {
        var child=delegate.get();java.util.Objects.checkIndex(index,child.size());TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        return amount>0 && input.test(resource.toStack(amount))?child.insert(index,resource,amount,transaction):0;
    }
    public int extract(int index,ItemResource resource,int amount,TransactionContext transaction) {
        var child=delegate.get();java.util.Objects.checkIndex(index,child.size());TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(amount==0)return 0;
        int extracted;
        try(var probe=Transaction.open(transaction)){extracted=child.extract(index,resource,amount,probe);}
        return extracted>0 && output.test(resource.toStack(extracted))?child.extract(index,resource,extracted,transaction):0;
    }
}
