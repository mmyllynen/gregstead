package com.lowdragmc.lowdraglib.core.mixins;

import com.lowdragmc.lowdraglib.client.model.forge.LDLRendererModel;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.resources.model.ModelBaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SingleVariant.Unbaked.class)
public abstract class RendererBlockStateBakeMixin {
    @Shadow public abstract Variant variant();
    @Inject(method="bake", at=@At("HEAD"), cancellable=true, require=1)
    private void ldlib$renderer(ModelBaker baker, CallbackInfoReturnable<BlockStateModel> callback) {
        var model = baker.getModel(variant().modelLocation());
        while (model != null) {
            if (model.wrapped() instanceof LDLRendererModel) {
                callback.setReturnValue(new LDLRendererModel.RendererBakedModel());
                return;
            }
            if (model.wrapped().geometry() != null) return;
            model = model.parent();
        }
    }
}
