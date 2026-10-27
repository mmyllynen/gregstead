package com.lowdragmc.lowdraglib.transfer.fluid;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.Nullable;
/** Detached legacy filter probes; contained fluid also means simulated extraction, not a raw storage read. */
public final class FluidItemProbe {
    private FluidItemProbe() {}
    public static @Nullable FluidStack extractable(ItemStack input,int maximum) {
        var fluids=handler(input);
        return fluids==null?null:fluids.drain(maximum,IFluidHandler.FluidAction.SIMULATE);
    }
    public static final class Container {
        private final ItemStacksResourceHandler slot;
        private final IFluidHandler fluids;
        private Container(ItemStacksResourceHandler slot,IFluidHandler fluids) {
            this.slot=slot;this.fluids=fluids;
        }
        public IFluidHandler fluids(){return fluids;}
        public ItemStack result(){return slot.getResource(0).toStack(slot.getAmountAsInt(0));}
    }
    public static @Nullable Container container(ItemStack input) {
        if(input.isEmpty())return null;
        var stacks=NonNullList.of(ItemStack.EMPTY,input.copyWithCount(1));
        var slot=new ItemStacksResourceHandler(stacks);
        var fluids=ItemAccess.forHandlerIndexStrict(slot,0).getCapability(Capabilities.Fluid.ITEM);
        return fluids==null?null:new Container(slot,ResourceFluidHandler.of(fluids));
    }
    public static @Nullable IFluidHandler handler(ItemStack input) {
        var container=container(input);
        return container==null?null:container.fluids();
    }
    public static FluidStack contained(ItemStack input) {
        var fluid=extractable(input,Integer.MAX_VALUE);
        return fluid==null?FluidStack.EMPTY:fluid;
    }
}
