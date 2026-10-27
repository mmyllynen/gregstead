package com.lowdragmc.lowdraglib.transfer.item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.*;
import java.util.Objects;
import java.util.function.Function;
/** Native insertion-only network entrance; destinations and routing journals share its transaction. */
public final class ItemRoutingView implements ResourceHandler<ItemResource> {
    private final Function<ItemStack,ItemStack> route;
    public ItemRoutingView(Function<ItemStack,ItemStack> route){this.route=Objects.requireNonNull(route);}
    private void check(int index){Objects.checkIndex(index,1);}
    public int size(){return 1;}
    public ItemResource getResource(int index){check(index);return ItemResource.EMPTY;}
    public long getAmountAsLong(int index){check(index);return 0;}
    public long getCapacityAsLong(int index,ItemResource resource){check(index);return 64;}
    public boolean isValid(int index,ItemResource resource){check(index);return !resource.isEmpty();}
    public int insert(int index,ItemResource resource,int amount,TransactionContext transaction){
        check(index);TransferPreconditions.checkNonEmptyNonNegative(resource,amount);if(amount==0)return 0;
        try(var routing=Transaction.open(transaction)) {
            ItemStack remaining=route.apply(resource.toStack(amount));
            int inserted=amount-remaining.getCount();
            if(inserted<0 || inserted>amount || !remaining.isEmpty() && !resource.matches(remaining))
                throw new IllegalStateException("Invalid item routing remainder");
            routing.commit();return inserted;
        }
    }
    public int extract(int index,ItemResource resource,int amount,TransactionContext transaction){
        check(index);TransferPreconditions.checkNonEmptyNonNegative(resource,amount);return 0;
    }
}
