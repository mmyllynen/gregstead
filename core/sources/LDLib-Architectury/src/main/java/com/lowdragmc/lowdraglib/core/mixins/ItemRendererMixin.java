package com.lowdragmc.lowdraglib.core.mixins;

import com.lowdragmc.lowdraglib.client.renderer.NativeItemModelProvider;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelResolver.class)
public abstract class ItemRendererMixin {
    @Shadow private ClientItem.Properties getItemProperties(Identifier id){throw new AssertionError();}
    @Inject(method="appendItemLayers",at=@At("HEAD"),cancellable=true,require=1)
    private void ldlib$extractNativeItem(ItemStackRenderState output,ItemStack stack,ItemDisplayContext context,
            Level level,ItemOwner owner,int seed,CallbackInfo callback) {
        if(NativeItemModelProvider.DISABLED.get() || !(stack.getItem() instanceof NativeItemModelProvider provider))return;
        var model=provider.nativeItemModel(stack);if(model==null)return;
        var id=stack.get(DataComponents.ITEM_MODEL);
        if(id!=null)output.setOversizedInGui(getItemProperties(id).oversizedInGui());
        boolean previous=NativeItemModelProvider.DISABLED.get();
        NativeItemModelProvider.DISABLED.set(true);
        try {model.update(output,stack,(ItemModelResolver)(Object)this,context,level instanceof ClientLevel client?client:null,owner,seed);}
        finally {NativeItemModelProvider.DISABLED.set(previous);}
        callback.cancel();
    }
}
