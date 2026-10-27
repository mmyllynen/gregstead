package com.lowdragmc.lowdraglib.client.model;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.*;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import java.util.List;

/** Reload-aware native item-state extraction from LD model cache. */
public final class NativeCachedItemModel implements ItemModel {
    private final NativeModelCache cache;
    private final List<ItemTintSource> tints;
    private NativeModelCache.Item generation;
    private CuboidItemModelWrapper delegate;
    public NativeCachedItemModel(NativeModelCache cache,List<ItemTintSource> tints) {
        this.cache=cache;this.tints=List.copyOf(tints);
    }
    @Override public void update(ItemStackRenderState output,ItemStack stack,ItemModelResolver resolver,
            ItemDisplayContext context,ClientLevel level,ItemOwner owner,int seed) {
        var item=cache.item();
        if(item!=generation) {
            generation=item;
            delegate=new CuboidItemModelWrapper(tints,item.geometry(),
                    new ModelRenderProperties(item.guiLight().lightLikeBlock(),item.particle(),item.transforms()),new Matrix4f());
        }
        delegate.update(output,stack,resolver,context,level,owner,seed);
    }
}
