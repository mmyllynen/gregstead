package com.gregtechceu.gtceu.core.mixins.client;

import com.tterrag.registrate.util.RegistrateItemColors;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(CuboidItemModelWrapper.class)
public class ItemTintLayersMixin {
    @Shadow @Final private ItemQuads itemQuads;
    @Shadow @Final private java.util.List<net.minecraft.client.color.item.ItemTintSource> tints;
    @Inject(method="update",at=@At("RETURN"),require=1)
    private void gtceu$registeredTint(ItemStackRenderState output,ItemStack stack,
            net.minecraft.client.renderer.item.ItemModelResolver resolver,net.minecraft.world.item.ItemDisplayContext context,
            net.minecraft.client.multiplayer.ClientLevel level,net.minecraft.world.entity.ItemOwner owner,int seed,CallbackInfo callback,
            @Local ItemStackRenderState.LayerRenderState layer) {
        if (tints.stream().anyMatch(net.neoforged.neoforge.client.color.item.FluidContentsTint.class::isInstance)) return;
        var color=RegistrateItemColors.get(stack.getItem());if(color==null)return;
        int maximum=-1;for(var quad:itemQuads.all())maximum=Math.max(maximum,quad.materialInfo().tintIndex());
        var tints=layer.tintLayers();tints.clear();
        for(int index=0;index<=maximum;index++)tints.add(color.getColor(stack,index)|0xff000000);
    }
}
