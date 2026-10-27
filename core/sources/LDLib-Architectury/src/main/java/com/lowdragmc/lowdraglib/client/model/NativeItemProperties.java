package com.lowdragmc.lowdraglib.client.model;

import com.lowdragmc.lowdraglib.client.renderer.NativeItemModelProvider;
import com.lowdragmc.lowdraglib.core.mixins.accessor.ModelBakeryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Fresh native metadata for configured represented items, without stale baked-model caching. */
public final class NativeItemProperties {
    private NativeItemProperties() {}
    public static ItemStackRenderState extract(ItemStack stack) {
        var output = new ItemStackRenderState();
        boolean previous = NativeItemModelProvider.DISABLED.get();
        NativeItemModelProvider.DISABLED.set(true);
        try { new ItemModelResolver(Minecraft.getInstance().getModelManager()).updateForTopItem(
                output,stack,ItemDisplayContext.GUI,null,null,0); }
        finally { NativeItemModelProvider.DISABLED.set(previous); }
        return output;
    }
    public static boolean gui3d(ItemStack stack) {
        var id = stack.get(DataComponents.ITEM_MODEL);
        if (id == null) return true;
        var info = ((ModelBakeryAccessor) ModelFactory.getModelBakery()).getClientInfos().get(id);
        if (info != null && info.model() instanceof CuboidItemModelWrapper.Unbaked cuboid) {
            return NativeModelCache.gui3d(ModelFactory.getResolvedModel(cuboid.model()));
        }
        // Original built-in/special models use the regular 3D classification.
        return true;
    }
}
