package dev.emi.emi.runtime;

import java.util.List;
import java.util.function.Consumer;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import dev.emi.emi.mixin.accessor.ScreenshotTargetAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.*;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

/** Native GUI renderer into an owned target; caller owns the returned texture until readback completes. */
public final class NativeRecipeScreenshot {
    private NativeRecipeScreenshot() {}
    public static TextureTarget render(int width, int height, int scale, Consumer<GuiGraphicsExtractor> extract) {
        if(width<=0 || height<=0 || scale<=0) throw new IllegalArgumentException("Screenshot dimensions must be positive");
        var client=Minecraft.getInstance();
        var target=new TextureTarget("EMI recipe screenshot",Math.multiplyExact(width,scale),Math.multiplyExact(height,scale),
            GpuFormat.RGBA8_UNORM,GpuFormat.D32_FLOAT);
        var game=client.gameRenderer;
        var previousTarget=game.mainRenderTarget();
        var window=game.gameRenderState().windowRenderState;
        int previousWidth=window.width,previousHeight=window.height,previousScale=window.guiScale;
        var projection=RenderSystem.getProjectionMatrixBuffer();
        var projectionType=RenderSystem.getProjectionType();
        boolean success=false;
        try {
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                target.getColorTexture(),new org.joml.Vector4f(0,0,0,0),target.getDepthTexture(),0);
            ((ScreenshotTargetAccessor)game).emi$screenshotTarget(target);
            window.width=target.width;window.height=target.height;window.guiScale=scale;
            var states=new GuiRenderState();
            try(var renderer=new GuiRenderer(states,game.featureRenderDispatcher(),
                    net.neoforged.neoforge.client.ClientHooks.gatherPictureInPictureRenderers(List.of(
                        new net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration<>(net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState.class, () -> new GuiEntityRenderer(client.getEntityRenderDispatcher())),
                        new net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration<>(net.minecraft.client.renderer.state.gui.pip.GuiSkinRenderState.class, GuiSkinRenderer::new),
                        new net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration<>(net.minecraft.client.renderer.state.gui.pip.GuiBookModelRenderState.class, GuiBookModelRenderer::new),
                        new net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration<>(net.minecraft.client.renderer.state.gui.pip.GuiBannerResultRenderState.class, () -> new GuiBannerResultRenderer(client.getAtlasManager())),
                        new net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration<>(net.minecraft.client.renderer.state.gui.pip.GuiProfilerChartRenderState.class, GuiProfilerChartRenderer::new))))) {
                extract.accept(new GuiGraphicsExtractor(client,states,0,0));
                renderer.render();
                renderer.endFrame();
            }
            success=true;
            return target;
        } finally {
            ((ScreenshotTargetAccessor)game).emi$screenshotTarget(previousTarget);
            window.width=previousWidth;window.height=previousHeight;window.guiScale=previousScale;
            RenderSystem.setProjectionMatrix(projection,projectionType);
            if(!success)target.destroyBuffers();
        }
    }
    public static void capture(com.mojang.blaze3d.pipeline.RenderTarget target,
                               Consumer<com.mojang.blaze3d.platform.NativeImage> callback) {
        int width=target.width,height=target.height;
        var texture=target.getColorTexture();
        var buffer=RenderSystem.getDevice().createBuffer(() -> "EMI RGBA readback",9,(long)width*height*texture.getFormat().blockSize());
        try {
            RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(texture,buffer,0L,() -> {
                try(var read=buffer.map(true,false)) {
                    var image=new com.mojang.blaze3d.platform.NativeImage(width,height,false);
                    boolean transferred=false;
                    try {
                        for(int y=0;y<height;y++) for(int x=0;x<width;x++)
                            image.setPixelABGR(x,height-y-1,read.data().getInt((x+y*width)*texture.getFormat().blockSize()));
                        callback.accept(image);
                        transferred=true;
                    } finally { if(!transferred)image.close(); }
                } finally { buffer.close(); }
            },0);
        } catch(RuntimeException failure) { buffer.close();throw failure; }
    }

}
