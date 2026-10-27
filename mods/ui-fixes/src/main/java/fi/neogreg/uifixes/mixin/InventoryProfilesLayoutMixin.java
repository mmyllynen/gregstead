package fi.neogreg.uifixes.mixin;

import fi.neogreg.uifixes.ContainerLayoutAccess;
import org.anti_ad.mc.ipnext.gui.inject.SortingButtonCollectionWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "org.anti_ad.mc.ipnext.gui.inject.SortingButtonCollectionWidget", remap = false)
public abstract class InventoryProfilesLayoutMixin {
    @Inject(method = "postForegroundRender", at = @At(value = "INVOKE",
        target = "Lorg/anti_ad/mc/ipnext/gui/inject/base/InsertableWidget;render(Lorg/anti_ad/mc/common/gui/NativeContext;IIF)V"), require = 1)
    private void neogreg$layoutAfterVanillaHints(CallbackInfo ci) {
        // IPN re-hints and updates parent bounds first; our layout must run afterwards.
        var screen = ((SortingButtonCollectionWidget) (Object) this).getScreen();
        if (screen instanceof ContainerLayoutAccess access) access.neogreg$refreshButtonLayout();
    }
}
