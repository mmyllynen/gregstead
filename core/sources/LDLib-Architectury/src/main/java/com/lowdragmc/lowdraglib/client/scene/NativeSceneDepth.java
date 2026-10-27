package com.lowdragmc.lowdraglib.client.scene;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

/** Explicit target readback for existing OpenGL scene picking, independent of the current draw FBO. */
public final class NativeSceneDepth {
    private NativeSceneDepth() {}
    private static final java.util.Map<RenderTarget, Sample> samples = new java.util.WeakHashMap<>();
    private static final class Sample {
        Object texture;
        int x, y;
        float depth;
        boolean pending;
        boolean valid;
    }

    /** Native GPU readback is asynchronous; reuse the last completed sample at the same target pixel. */
    private static float readNative(RenderTarget target, int x, int y) {
        var depth = target.getDepthTexture();
        if (depth == null) throw new IllegalArgumentException("Scene picking requires a depth texture");
        if (depth.getFormat() != com.mojang.renderpearl.api.GpuFormat.D32_FLOAT)
            throw new IllegalArgumentException("Scene picking requires D32_FLOAT depth");
        var sample = samples.computeIfAbsent(target, ignored -> new Sample());
        boolean matches = sample.texture == depth && sample.x == x && sample.y == y;
        float completed = matches && sample.valid ? sample.depth : 0;
        if (!sample.pending) {
            sample.texture = depth; sample.x = x; sample.y = y; sample.pending = true;
            if (!matches) sample.valid = false;
            var device = com.mojang.blaze3d.systems.RenderSystem.getDevice();
            var buffer = device.createBuffer(() -> "LDLib scene depth pixel", 9, 4);
            try {
                device.createCommandEncoder().copyTextureToBuffer(depth, buffer, 0, () -> {
                    try (buffer; var mapped = buffer.map(true, false)) {
                        sample.depth = mapped.data().order(java.nio.ByteOrder.nativeOrder()).getFloat(0);
                        sample.valid = true;
                    } finally { sample.pending = false; }
                }, 0, x, y, 1, 1);
            } catch (RuntimeException | Error failure) {
                sample.pending = false; buffer.close(); throw failure;
            }
        }
        return completed;
    }
    public static float read(RenderTarget target,int x,int y) {
        if(x<0 || y<0 || x>=target.getColorTextureView().getWidth(0) || y>=target.getColorTextureView().getHeight(0))return 0;
        if(!(target.getColorTexture() instanceof GlTexture color) || !(target.getDepthTexture() instanceof GlTexture depth))
            return readNative(target, x, y);
        int previous=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING),fbo=GL30.glGenFramebuffers();
        int[] packNames={GL11.GL_PACK_ALIGNMENT,GL11.GL_PACK_ROW_LENGTH,GL11.GL_PACK_SKIP_PIXELS,GL11.GL_PACK_SKIP_ROWS};
        int[] packValues=new int[packNames.length];
        for(int i=0;i<packNames.length;i++)packValues[i]=GL11.glGetInteger(packNames[i]);
        int packBuffer=GL11.glGetInteger(org.lwjgl.opengl.GL21.GL_PIXEL_PACK_BUFFER_BINDING);
        try(MemoryStack stack=MemoryStack.stackPush()) {
            org.lwjgl.opengl.GL15.glBindBuffer(org.lwjgl.opengl.GL21.GL_PIXEL_PACK_BUFFER,0);
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT,4);
            for(int i=1;i<packNames.length;i++)GL11.glPixelStorei(packNames[i],0);
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,fbo);
            GL30.glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,color.glId(),0);
            GL30.glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER,GL30.GL_DEPTH_ATTACHMENT,GL11.GL_TEXTURE_2D,depth.glId(),0);
            if(GL30.glCheckFramebufferStatus(GL30.GL_READ_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)
                throw new IllegalStateException("Scene picking framebuffer is incomplete");
            var pixel=stack.mallocFloat(1);
            GL11.glReadPixels(x,y,1,1,GL11.GL_DEPTH_COMPONENT,GL11.GL_FLOAT,pixel);
            return pixel.get(0);
        } finally {
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,previous);
            GL30.glDeleteFramebuffers(fbo);
            for(int i=0;i<packNames.length;i++)GL11.glPixelStorei(packNames[i],packValues[i]);
            org.lwjgl.opengl.GL15.glBindBuffer(org.lwjgl.opengl.GL21.GL_PIXEL_PACK_BUFFER,packBuffer);
        }
    }
}
