package com.gregtechceu.gtceu.api.misc.forge;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.transfer.fluid.*;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import java.util.function.*;
/** Native whole-container fluid component handler; partial fills/drains are rejected. */
public class FilteredFluidHandlerItemStackSimple extends ItemAccessFluidHandler {
    protected final Predicate<FluidStack> filter;
    public FilteredFluidHandlerItemStackSimple(Supplier<DataComponentType<SimpleFluidContent>> component,ItemAccess access,int capacity,Predicate<FluidStack> filter){
        super(access,component.get(),capacity);this.filter=filter;
    }
    @Override public boolean isValid(int index,FluidResource resource){return super.isValid(index,resource) && filter.test(resource.toStack(capacity));}
    @Override protected ItemResource update(ItemResource container,int index,FluidResource resource,int amount){
        return amount==0 || amount==capacity?super.update(container,index,resource,amount):ItemResource.EMPTY;
    }
}
