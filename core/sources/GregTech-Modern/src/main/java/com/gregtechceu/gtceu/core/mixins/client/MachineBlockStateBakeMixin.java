package com.gregtechceu.gtceu.core.mixins.client;
import com.gregtechceu.gtceu.client.model.machine.UnbakedMachineModel;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.resources.model.ModelBaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(SingleVariant.Unbaked.class)
public abstract class MachineBlockStateBakeMixin {
    @Shadow public abstract Variant variant();
    @Inject(method="bake", at=@At("HEAD"), cancellable=true, require=1)
    private void gtceu$bakeMachine(ModelBaker baker, CallbackInfoReturnable<BlockStateModel> callback) {
        var variant=variant();
        var resolved=baker.getModel(variant.modelLocation());
        var machine=UnbakedMachineModel.find(resolved);
        if(machine!=null) callback.setReturnValue(machine.bakeDynamic(baker,variant.modelState().asModelState(),resolved));
    }
}
