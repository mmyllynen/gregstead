package com.lowdragmc.lowdraglib.transfer.mixin;

import com.lowdragmc.lowdraglib.transfer.client.GuiModulatedState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.pip.OversizedItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OversizedItemRenderState.class)
public abstract class GuiOversizedColorMixin implements GuiModulatedState {
    @Shadow public abstract GuiItemRenderState guiItemRenderState();
    @Override public int gttransfercompat$color() { return ((GuiModulatedState) (Object) guiItemRenderState()).gttransfercompat$color(); }
}
