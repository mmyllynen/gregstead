package com.lowdragmc.lowdraglib.client.renderer;

import com.lowdragmc.lowdraglib.client.renderer.impl.BlockStateRenderer;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import com.lowdragmc.lowdraglib.client.renderer.impl.UIResourceRenderer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.world.item.ItemStack;

/** Client model selection kept outside the common persisted renderer interface. */
public final class NativeRendererItemModels {
    private NativeRendererItemModels() {}
    public static ItemModel get(IRenderer renderer, ItemStack stack) {
        if (renderer instanceof UIResourceRenderer resource) return resource.getNativeItemModel(stack);
        if (renderer instanceof IModelRenderer model) return model.getNativeItemModel(stack);
        if (renderer instanceof BlockStateRenderer block) return block.getNativeItemModel(stack);
        return com.lowdragmc.lowdraglib.client.model.NativeRendererItemModel.of(renderer);
    }
}
