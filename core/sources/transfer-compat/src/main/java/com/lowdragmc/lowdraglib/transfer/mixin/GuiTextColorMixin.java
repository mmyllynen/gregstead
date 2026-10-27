package com.lowdragmc.lowdraglib.transfer.mixin;
import com.lowdragmc.lowdraglib.transfer.client.GuiColorScope;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GuiTextRenderState.class)
public abstract class GuiTextColorMixin {
    @Shadow @Final @Mutable private int color;
    @Shadow @Final @Mutable private int backgroundColor;
    @Inject(method = "<init>", at = @At("RETURN"))
    private void gttransfercompat$capture(CallbackInfo ci) {
        color = ARGB.multiply(color, GuiColorScope.current());
        backgroundColor = ARGB.multiply(backgroundColor, GuiColorScope.current());
    }
}
