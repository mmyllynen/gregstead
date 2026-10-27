package com.lowdragmc.lowdraglib.transfer.item;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
public final class NativeItemViews {
    private NativeItemViews() {}
    @SuppressWarnings("unchecked")
    public static ResourceHandler<ItemResource> require(IItemHandler handler) {
        if(handler instanceof NativeItemView view)return view.nativeView();
        if(handler instanceof ResourceHandler<?> nativeHandler)return (ResourceHandler<ItemResource>)nativeHandler;
        throw new IllegalArgumentException("Item handler lacks transaction-aware native view: "+handler.getClass().getName());
    }
}
