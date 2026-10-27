package com.lowdragmc.lowdraglib.core.mixins;

import com.lowdragmc.lowdraglib.client.model.NativeMaterialUsage;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Native material queries replace the removed function-based model bake overload. */
@Mixin(MaterialBaker.class)
public abstract class ModelBakerImplMixin {
    @Inject(method="<init>",at=@At("RETURN"))
    private void ldlib$resetMaterials(CallbackInfo ci){NativeMaterialUsage.clear();}
    @Inject(method="get",at=@At("HEAD"))
    private void ldlib$recordMaterial(Material material,ModelDebugName name,CallbackInfoReturnable<Material.Baked> cir) {
        NativeMaterialUsage.record(material,name);
    }
}
