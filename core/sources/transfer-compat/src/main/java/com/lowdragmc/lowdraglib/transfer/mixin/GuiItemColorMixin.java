package com.lowdragmc.lowdraglib.transfer.mixin;

import com.lowdragmc.lowdraglib.transfer.client.GuiColorScope;
import com.lowdragmc.lowdraglib.transfer.client.GuiModulatedState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiItemRenderState.class)
public abstract class GuiItemColorMixin implements GuiModulatedState {
    @Unique private int gttransfercompat$modulation;
    @Inject(method = "<init>", at = @At("RETURN"))
    private void gttransfercompat$capture(CallbackInfo ci) { gttransfercompat$modulation = GuiColorScope.current(); }
    @Override public int gttransfercompat$color() { return gttransfercompat$modulation; }
}
