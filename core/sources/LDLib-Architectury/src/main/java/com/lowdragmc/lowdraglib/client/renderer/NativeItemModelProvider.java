package com.lowdragmc.lowdraglib.client.renderer;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface NativeItemModelProvider {
    ThreadLocal<Boolean> DISABLED=ThreadLocal.withInitial(()->false);
    @Nullable ItemModel nativeItemModel(ItemStack stack);
}
