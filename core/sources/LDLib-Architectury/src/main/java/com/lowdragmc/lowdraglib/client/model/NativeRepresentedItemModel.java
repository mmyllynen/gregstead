package com.lowdragmc.lowdraglib.client.model;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.*;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import java.util.function.Supplier;

/** Native extraction for the item represented by a configured block-state renderer. */
public final class NativeRepresentedItemModel implements ItemModel {
    private final Supplier<ItemStack> represented;
    public NativeRepresentedItemModel(Supplier<ItemStack> represented) { this.represented = represented; }
    public void update(ItemStackRenderState output, ItemStack host, ItemModelResolver resolver, ItemDisplayContext context,
            ClientLevel level, ItemOwner owner, int seed) {
        boolean previous = com.lowdragmc.lowdraglib.client.renderer.NativeItemModelProvider.DISABLED.get();
        com.lowdragmc.lowdraglib.client.renderer.NativeItemModelProvider.DISABLED.set(true);
        try { resolver.appendItemLayers(output, represented.get(), context, level, owner, seed); }
        finally { com.lowdragmc.lowdraglib.client.renderer.NativeItemModelProvider.DISABLED.set(previous); }
    }
}
