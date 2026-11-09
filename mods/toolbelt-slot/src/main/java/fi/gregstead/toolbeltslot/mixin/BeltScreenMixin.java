package fi.gregstead.toolbeltslot.mixin;

import com.tiviacz.travelertoolbelt.client.screens.BeltScreen;
import com.tiviacz.travelertoolbelt.config.TravelerToolBeltConfig;
import com.tiviacz.travelertoolbelt.platform.Platform;
import com.tiviacz.travelertoolbelt.platform.PlatformClient;
import fi.gregstead.toolbeltslot.BeltScreenAccess;
import fi.gregstead.toolbeltslot.BeltSession;
import fi.gregstead.toolbeltslot.ToolbeltSlot;
import fi.gregstead.toolbeltslot.ToolCycle;
import com.tiviacz.travelertoolbelt.network.ServerboundSwapToolPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BeltScreen.class, remap = false)
public abstract class BeltScreenMixin extends Screen implements BeltScreenAccess {
    @Unique private BeltSession gregstead$session;
    @Unique private KeyMapping gregstead$opener;

    protected BeltScreenMixin(Component title) { super(title); }

    @Override
    public void gregstead$configure(BeltSession session, KeyMapping opener) {
        gregstead$session = session;
        gregstead$opener = opener;
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void gregstead$pinSelection(GuiGraphicsExtractor graphics, int x, int y, float delta, CallbackInfo ci) {
        if (gregstead$session != null) ToolbeltSlot.synchronize(gregstead$session);
    }

    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE",
        target = "Lcom/tiviacz/travelertoolbelt/platform/Platform;isKeyDown(Lnet/minecraft/client/KeyMapping;)Z"))
    private boolean gregstead$holdOpeningKey(KeyMapping upstream) {
        return Platform.isKeyDown(gregstead$opener == null ? upstream : gregstead$opener);
    }

    @Redirect(method = {"extractRenderState", "mouseClicked"}, at = @At(value = "INVOKE",
        target = "Lcom/tiviacz/travelertoolbelt/platform/PlatformClient;sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"))
    private void gregstead$selectBeforeSwap(CustomPacketPayload payload) {
        if (gregstead$session == null) PlatformClient.sendToServer(payload);
        else {
            if (payload instanceof ServerboundSwapToolPacket swap) ToolCycle.manualSwap(gregstead$session, swap);
            ToolbeltSlot.swap(gregstead$session, () -> PlatformClient.sendToServer(payload));
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void gregstead$blockHotbarScroll(double x, double y, double horizontal, double vertical,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (gregstead$session != null) cir.setReturnValue(true);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void gregstead$toggleOpeningKey(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (gregstead$opener != null && !TravelerToolBeltConfig.CLIENT.holdToOpen.get()
            && gregstead$opener.matches(event)) {
            onClose();
            cir.setReturnValue(true);
        }
    }
}
