package com.lowdragmc.lowdraglib.client.renderer;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Author: KilaBash
 * Date: 2022/04/21
 * Description: 
 */
public interface IItemRendererProvider extends NativeItemModelProvider {
    
    /** Prevents recursive native item-model provider extraction. */
    ThreadLocal<Boolean> disabled = NativeItemModelProvider.DISABLED;

    /**
     * Get the renderer for the item stack.
     * @return return null if the item stack does not have a renderer.
     */
    @Nullable
    IRenderer getRenderer(ItemStack stack);
    @Override default net.minecraft.client.renderer.item.ItemModel nativeItemModel(ItemStack stack) {
        var renderer=getRenderer(stack);return renderer==null?null:NativeRendererItemModels.get(renderer, stack);
    }
}
