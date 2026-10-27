package com.lowdragmc.lowdraglib.client.shader.management;

import com.lowdragmc.lowdraglib.client.shader.uniform.IUniformCallback;
import com.lowdragmc.lowdraglib.math.PositionedRect;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.backend.opengl.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.*;
import java.util.*;
import java.util.function.Consumer;

/** Owned fullscreen GL shader rendering into native render-target textures. */
public class ShaderManager {
    private static final ShaderManager INSTANCE=new ShaderManager();
    public static ShaderManager getInstance(){return INSTANCE;}
    public static boolean allowedShader(){return true;}
    private final Map<Shader,ShaderProgram> programs=new IdentityHashMap<>();
    private Shader imageVertex;
    private final Map<Identifier,Shader> resourceFragments=new HashMap<>();
    private static TextureTarget TEMP_TARGET;
    private PositionedRect viewPort;
    private ShaderManager() {com.lowdragmc.lowdraglib.client.shader.Shaders.addReloadListener(this::reload);}
    public static TextureTarget getTempTarget() {
        if(TEMP_TARGET==null) {
            TEMP_TARGET=new TextureTarget("ldlib shader temporary",1024,1024,GpuFormat.RGBA8_UNORM,null);
            if (TEMP_TARGET.getColorTexture() instanceof GlTexture texture) {
            int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            try {
                GlStateManager._bindTexture(texture.glId());
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);
            } finally { GlStateManager._bindTexture(previous); }
            }
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(TEMP_TARGET.getColorTexture(),new org.joml.Vector4f());
        }
        return TEMP_TARGET;
    }
    public void reload() {
        programs.forEach((shader,program)->{program.delete();shader.deleteShader();});programs.clear();
        resourceFragments.values().forEach(Shader::deleteShader);resourceFragments.clear();
        if(imageVertex!=null){imageVertex.deleteShader();imageVertex=null;}
    }
    public Shader resourceFragment(Identifier id) {
        return resourceFragments.computeIfAbsent(id,key->{
            try {return Shader.loadSource(Shader.ShaderType.FRAGMENT,key);}
            catch(java.io.IOException exception){throw new IllegalStateException("Missing fragment resource "+key,exception);}
        });
    }
    public void setViewPort(PositionedRect viewport){viewPort=viewport;}
    public boolean hasViewPort(){return viewPort!=null;}
    public void clearViewPort(){viewPort=null;}
    public RenderTarget renderFullImageInFramebuffer(RenderTarget target,Shader fragment,IUniformCallback callback,Consumer<ShaderProgram> created) {
        return renderFullImageInFramebuffer(target,fragment,callback,created,null);
    }
    public RenderTarget renderFullImageInFramebuffer(RenderTarget target,Shader fragment,IUniformCallback callback,
            Consumer<ShaderProgram> created,Consumer<ShaderProgram> configure) {
        if(target==null || fragment==null || fragment.shaderType!=Shader.ShaderType.FRAGMENT)return target;
        RenderSystem.assertOnRenderThread();
        if(!(target.getColorTexture() instanceof GlTexture texture)) {
            ShaderProgram nativeProgram=programs.get(fragment);
            if(nativeProgram==null) {
                nativeProgram=ShaderProgram.nativeFramebuffer(fragment);
                try {if(created!=null)created.accept(nativeProgram);}
                catch(RuntimeException|Error failure){nativeProgram.delete();throw failure;}
                programs.put(fragment,nativeProgram);
            }
            if(configure!=null)configure.accept(nativeProgram);
            nativeProgram.renderNative(target,cache->{
                var mc=Minecraft.getInstance();
                float time=mc.player!=null?(mc.player.tickCount+mc.getDeltaTracker().getGameTimeDeltaTicks())/20f:System.currentTimeMillis()/1000f;
                cache.glUniform1F("iTime",time);cache.glUniform2F("iResolution",target.width,target.height);
                if(callback!=null)callback.apply(cache);
            });
            return target;
        }
        ShaderProgram program=programs.get(fragment);
        if(program==null) {
            if(imageVertex==null) {
                try {imageVertex=Shader.loadShader(Shader.ShaderType.VERTEX,Identifier.parse("ldlib:shaders/image.vsh"));}
                catch(java.io.IOException exception){throw new IllegalStateException("Missing original image vertex shader",exception);}
            }
            program=new ShaderProgram();
            try {program.attach(imageVertex).attach(fragment);if(created!=null)created.accept(program);}
            catch(RuntimeException|Error error){program.delete();throw error;}
            programs.put(fragment,program);
        }
        int draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int vao=GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING),buffer=GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        int[] viewport=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);
        boolean depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),scissor=GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        int fbo=GL30.glGenFramebuffers(),ownVao=GL30.glGenVertexArrays(),vbo=GL15.glGenBuffers();
        try {
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,texture.glId(),0);
            if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("Shader target framebuffer incomplete");
            GlStateManager._viewport(0,0,target.width,target.height);
            GlStateManager._disableDepthTest();GlStateManager._disableScissorTest();
            if(configure!=null)configure.accept(program);
            program.use(cache->{
                var mc=Minecraft.getInstance();
                float time=mc.player!=null ? (mc.player.tickCount+mc.getDeltaTracker().getGameTimeDeltaTicks())/20f : System.currentTimeMillis()/1000f;
                cache.glUniform1F("iTime",time);cache.glUniform2F("iResolution",target.width,target.height);
                if(callback!=null)callback.apply(cache);
            });
            GlStateManager._glBindVertexArray(ownVao);GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER,vbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER,new float[]{-1,1,0,-1,-1,0,1,1,0,1,-1,0},GL15.GL_STREAM_DRAW);
            int attribute=GL20.glGetAttribLocation(program.programId,"Position");
            if(attribute<0)throw new IllegalStateException("Image shader Position attribute missing");
            GL20.glEnableVertexAttribArray(attribute);GL20.glVertexAttribPointer(attribute,3,GL11.GL_FLOAT,false,12,0L);
            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP,0,4);
        } finally {
            program.release();
            GlStateManager._glBindVertexArray(vao);GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER,buffer);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);
            if(depth)GlStateManager._enableDepthTest();else GlStateManager._disableDepthTest();
            if(scissor)GlStateManager._enableScissorTest();else GlStateManager._disableScissorTest();
            if(viewPort!=null)GlStateManager._viewport(viewPort.position.x,viewPort.position.y,viewPort.size.width,viewPort.size.height);
            else GlStateManager._viewport(viewport[0],viewport[1],viewport[2],viewport[3]);
            GL15.glDeleteBuffers(vbo);GL30.glDeleteVertexArrays(ownVao);GL30.glDeleteFramebuffers(fbo);
        }
        return target;
    }
}
