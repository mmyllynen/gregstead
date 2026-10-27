package com.lowdragmc.lowdraglib.client.scene;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;

/** Paired scene activation, without overwriting the parent's projection backup slot. */
public final class NativeSceneCameraScope implements AutoCloseable {
    private final GpuBufferSlice previousProjection;
    private final ProjectionType previousType;
    private final ProjectionMatrixBuffer projection;
    private boolean closed;
    public NativeSceneCameraScope(NativeSceneCamera camera) {
        previousProjection=RenderSystem.getProjectionMatrixBuffer();
        previousType=RenderSystem.getProjectionType();
        projection=new ProjectionMatrixBuffer("LDLib scene camera scope");
        var stack=RenderSystem.getModelViewStack();
        stack.pushMatrix();
        try {
            stack.set(camera.view());
            RenderSystem.setProjectionMatrix(projection.getBuffer(camera.projection()),camera.type());
        } catch(RuntimeException|Error error) {
            stack.popMatrix();projection.close();throw error;
        }
    }
    @Override public void close() {
        if(closed)return;
        closed=true;
        RenderSystem.setProjectionMatrix(previousProjection,previousType);
        RenderSystem.getModelViewStack().popMatrix();
        projection.close();
    }
}
