package com.lowdragmc.lowdraglib.transfer.mixin;

import com.lowdragmc.lowdraglib.transfer.client.GuiColorScope;
import com.lowdragmc.lowdraglib.transfer.client.GuiModulatedState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PictureInPictureRenderer.class)
public abstract class GuiPipBlitColorMixin {
    @Redirect(method = "blitTexture", at = @At(value = "NEW", target = "net/minecraft/client/renderer/state/gui/BlitRenderState"))
    private BlitRenderState gttransfercompat$modulate(RenderPipeline pipeline, TextureSetup texture, Matrix3x2fc pose,
            int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color,
            ScreenRectangle scissor, ScreenRectangle bounds, PictureInPictureRenderState item, GuiRenderState state) {
        int modulation = item instanceof GuiModulatedState tinted ? GuiColorScope.premultiplied(tinted.gttransfercompat$color()) : -1;
        return new BlitRenderState(pipeline, texture, pose, x0, y0, x1, y1, u0, u1, v0, v1,
                ARGB.multiply(color, modulation), scissor, bounds);
    }
}
