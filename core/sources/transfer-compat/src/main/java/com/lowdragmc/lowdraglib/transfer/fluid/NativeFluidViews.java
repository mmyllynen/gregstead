package com.lowdragmc.lowdraglib.transfer.fluid;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
public final class NativeFluidViews {
    private NativeFluidViews() {}
    @SuppressWarnings("unchecked")
    public static ResourceHandler<FluidResource> require(IFluidHandler handler) {
        if(handler instanceof NativeFluidView view)return view.nativeView();
        if(handler instanceof ResourceHandler<?> resource)return (ResourceHandler<FluidResource>)resource;
        throw new IllegalArgumentException("Fluid handler has no transaction-aware native view: "+handler.getClass().getName());
    }
}
