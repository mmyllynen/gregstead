package com.lowdragmc.lowdraglib.client.scene;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import org.joml.Matrix4f;
import java.util.Optional;
import java.util.OptionalDouble;

/** Owned native feature execution for scene targets, including quad and model particle submissions. */
public final class NativeSceneFeaturePass implements AutoCloseable {
    private final RenderBuffers buffers;
    private final FeatureRenderDispatcher dispatcher;
    private final ProjectionMatrixBuffer projection;
    private final FogRenderer fog;
    private boolean closed;

    public NativeSceneFeaturePass() {
        var mc=Minecraft.getInstance();
        buffers=new RenderBuffers(0);
        dispatcher=new FeatureRenderDispatcher(buffers,mc.getModelManager(),mc.getAtlasManager(),mc.font,new GameRenderState());
        projection=new ProjectionMatrixBuffer("LDLib scene");
        var previousFog=RenderSystem.getShaderFog();
        try { fog=new FogRenderer(); }
        finally { RenderSystem.setShaderFog(previousFog); }
    }
    public static void clearDepth(RenderTarget target) {
        if (target.getDepthTexture() == null) throw new IllegalArgumentException("Scene clear requires a depth target");
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(target.getDepthTexture(), 0);
    }
    public static void clear(RenderTarget target, org.joml.Vector4f color, RenderPass.RenderArea area) {
        if(target.getDepthTexture()==null)throw new IllegalArgumentException("Scene clear requires a depth target");
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                target.getColorTexture(),color,target.getDepthTexture(),0,
                area.x(),area.y(),area.width(),area.height(),0);
    }
    public void render(SubmitNodeStorage submits, RenderTarget target, Matrix4f projectionMatrix,
            ProjectionType projectionType, Matrix4f modelView, RenderPass.RenderArea area) {
        if(closed)throw new IllegalStateException("Scene feature pass is closed");
        if(target.getDepthTextureView()==null)throw new IllegalArgumentException("Scene feature rendering requires a depth target");
        var previousProjection=RenderSystem.getProjectionMatrixBuffer();
        var previousType=RenderSystem.getProjectionType();
        var previousFog=RenderSystem.getShaderFog();
        var stack=RenderSystem.getModelViewStack();
        stack.pushMatrix();
        try {
            stack.set(modelView);
            RenderSystem.setProjectionMatrix(projection.getBuffer(new Matrix4f(projectionMatrix)),projectionType);
            RenderSystem.setShaderFog(fog.getBuffer(FogRenderer.FogMode.NONE));
            try(var frame=dispatcher.prepareFrame(submits);
                var pass=RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        ()->"LDLib scene features",target.getColorTextureView(),Optional.empty(),
                        target.getDepthTextureView(),OptionalDouble.empty(),area)) {
                pass.setViewport(area.x(),area.y(),area.width(),area.height());
                FeatureRenderDispatcher.renderAllFeatures(pass,frame);
            }
        } finally {
            stack.popMatrix();
            RenderSystem.setProjectionMatrix(previousProjection,previousType);
            RenderSystem.setShaderFog(previousFog);
            buffers.endFrame();
        }
    }
    @Override public void close() {
        if(closed)return;
        closed=true;
        dispatcher.close();
        buffers.close();
        buffers.fixedBufferPack().close();
        projection.close();
        fog.close();
    }
}
