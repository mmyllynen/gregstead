package com.lowdragmc.lowdraglib.client.shader.management;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.backend.opengl.*;
import com.lowdragmc.lowdraglib.client.shader.uniform.IUniformCallback;
import org.lwjgl.opengl.*;

/** Original GUI shader vertex inputs rendered unblended into a native target before GUI composition. */
public final class NativeGuiShaderDraw {
    private NativeGuiShaderDraw() {}
    public static void render(RenderTarget target,ShaderProgram program,int color,IUniformCallback uniforms) {
        if(!(target.getColorTexture() instanceof GlTexture texture))throw new UnsupportedOperationException("Raw GUI shaders require OpenGL");
        int draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int vao=GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING),buffer=GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        int[] viewport=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);
        boolean depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),scissor=GL11.glIsEnabled(GL11.GL_SCISSOR_TEST),blend=GL11.glIsEnabled(GL11.GL_BLEND);
        var masks=org.lwjgl.BufferUtils.createByteBuffer(4);GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK,masks);
        int fbo=GL30.glGenFramebuffers(),ownVao=GL30.glGenVertexArrays(),vbo=GL15.glGenBuffers();
        try {
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,texture.glId(),0);
            if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("GUI shader framebuffer incomplete");
            GlStateManager._viewport(0,0,target.width,target.height);GlStateManager._disableDepthTest();GlStateManager._disableScissorTest();GlStateManager._disableBlend(0);GlStateManager._colorMask(15);
            program.use(uniforms);
            float r=((color>>>16)&255)/255f,g=((color>>>8)&255)/255f,b=(color&255)/255f,a=(color>>>24)/255f;
            GlStateManager._glBindVertexArray(ownVao);GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER,vbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER,new float[]{
                    -1,1,0,0,1,r,g,b,a, -1,-1,0,0,0,r,g,b,a,
                    1,1,0,1,1,r,g,b,a, 1,-1,0,1,0,r,g,b,a},GL15.GL_STREAM_DRAW);
            attribute(program,"Position",3,0);attribute(program,"UV",2,12);attribute(program,"c",4,20);
            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP,0,4);
        } finally {
            program.release();GlStateManager._glBindVertexArray(vao);GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER,buffer);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);
            GlStateManager._viewport(viewport[0],viewport[1],viewport[2],viewport[3]);
            if(depth)GlStateManager._enableDepthTest();else GlStateManager._disableDepthTest();
            if(scissor)GlStateManager._enableScissorTest();else GlStateManager._disableScissorTest();
            if(blend)GlStateManager._enableBlend(0);else GlStateManager._disableBlend(0);
            GlStateManager._colorMask((masks.get(0)!=0?1:0)|(masks.get(1)!=0?2:0)|(masks.get(2)!=0?4:0)|(masks.get(3)!=0?8:0));
            GL15.glDeleteBuffers(vbo);GL30.glDeleteVertexArrays(ownVao);GL30.glDeleteFramebuffers(fbo);
        }
    }
    private static void attribute(ShaderProgram program,String name,int count,long offset) {
        int id=GL20.glGetAttribLocation(program.programId,name);
        if(id<0){if(name.equals("Position"))throw new IllegalStateException("GUI shader position missing");return;}
        GL20.glEnableVertexAttribArray(id);GL20.glVertexAttribPointer(id,count,GL11.GL_FLOAT,false,36,offset);
    }
}
