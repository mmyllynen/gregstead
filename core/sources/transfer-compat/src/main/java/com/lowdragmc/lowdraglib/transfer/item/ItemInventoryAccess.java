package com.lowdragmc.lowdraglib.transfer.item;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.Nullable;

/** Native capability access retaining the actual parent slot for nested inventories. */
public final class ItemInventoryAccess {
    private ItemInventoryAccess() {}
    public static @Nullable IItemHandler entity(Entity entity) {
        var handler = entity.getCapability(Capabilities.Item.ENTITY);
        return handler == null ? null : ResourceItemHandler.of(handler);
    }
    public static @Nullable IItemHandler nested(IItemHandler parent, int slot) {
        var access = ItemAccess.forHandlerIndexStrict(NativeItemViews.require(parent), slot);
        var handler = access.getCapability(Capabilities.Item.ITEM);
        return handler == null ? null : ResourceItemHandler.of(handler);
    }
}
