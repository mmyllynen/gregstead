package fi.gregstead.toolbeltslot.mixin;

import com.tiviacz.travelertoolbelt.handlers.LoaderClientHandler;
import fi.gregstead.toolbeltslot.ToolbeltSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LoaderClientHandler.class, remap = false)
public abstract class ToolbeltKeysMixin {
    @Inject(method = "tickClient", at = @At("HEAD"), cancellable = true)
    private static void gregstead$openBoundSlot(CallbackInfo ci) {
        if (ToolbeltSlot.tick()) ci.cancel();
    }
}
