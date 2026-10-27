package com.lowdragmc.lowdraglib.transfer.mixin;

import com.lowdragmc.lowdraglib.transfer.client.GuiColorScope;
import com.lowdragmc.lowdraglib.transfer.client.GuiModulatedState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiRenderer.class)
public abstract class GuiAtlasBlitColorMixin {
    @Redirect(method = "submitBlitFromItemAtlas", at = @At(value = "NEW", target = "net/minecraft/client/renderer/state/gui/BlitRenderState"))
    private BlitRenderState gttransfercompat$modulate(RenderPipeline pipeline, TextureSetup texture, Matrix3x2fc pose,
            int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color,
            ScreenRectangle scissor, ScreenRectangle bounds, GuiItemRenderState item, GuiItemAtlas.SlotView slot) {
        int modulation = GuiColorScope.premultiplied(((GuiModulatedState) (Object) item).gttransfercompat$color());
        return new BlitRenderState(pipeline, texture, pose, x0, y0, x1, y1, u0, u1, v0, v1,
                ARGB.multiply(color, modulation), scissor, bounds);
    }
}
