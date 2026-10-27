package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.model.pipe.UnbakedPipeModel;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.resources.model.ModelBaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SingleVariant.Unbaked.class)
public abstract class PipeBlockStateBakeMixin {
    @Shadow public abstract Variant variant();
    @Inject(method="bake",at=@At("HEAD"),cancellable=true,require=1)
    private void gtceu$bakeDynamicPipe(ModelBaker baker,CallbackInfoReturnable<BlockStateModel> callback) {
        var variant=variant();var resolved=baker.getModel(variant.modelLocation());
        var pipe=UnbakedPipeModel.find(resolved);
        if(pipe!=null)callback.setReturnValue(pipe.bakeDynamic(baker,variant.modelState().asModelState(),resolved));
    }
}
