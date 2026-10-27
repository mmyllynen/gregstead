package com.lowdragmc.lowdraglib.client.utils;

import com.lowdragmc.lowdraglib.client.shader.management.ShaderManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.backend.opengl.*;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.*;

public class ShaderUtils {
    private static com.lowdragmc.lowdraglib.client.shader.management.NativeShaderTextureProgram nativeBlit;
    static {com.lowdragmc.lowdraglib.client.shader.Shaders.addReloadListener(()->{if(nativeBlit!=null){nativeBlit.close();nativeBlit=null;}});}

    public static void fastBlit(RenderTarget from,RenderTarget to) {
        RenderSystem.assertOnRenderThread();
        if(!(from.getColorTexture() instanceof GlTexture texture)) {
            if(from==to)throw new IllegalArgumentException("Cannot sample and render the same framebuffer");
            if(nativeBlit==null) {
                var fragment=ShaderManager.getInstance().resourceFragment(Identifier.parse("ldlib:shaders/core/fast_blit.fsh"));
                nativeBlit=new com.lowdragmc.lowdraglib.client.shader.management.NativeShaderTextureProgram(
                        fragment.source,false,new com.mojang.renderpearl.api.pipeline.BlendFunction(
                                com.mojang.renderpearl.api.pipeline.BlendFactor.SRC_ALPHA,
                                com.mojang.renderpearl.api.pipeline.BlendFactor.ONE_MINUS_SRC_ALPHA,
                                com.mojang.renderpearl.api.pipeline.BlendFactor.ONE,
                                com.mojang.renderpearl.api.pipeline.BlendFactor.ZERO));
            }
            nativeBlit.render(to,-1,cache->{},java.util.Map.of("DiffuseSampler",from.getColorTextureView()));
            return;
        }
        boolean blend=GL11.glIsEnabled(GL11.GL_BLEND);
        int srcRgb=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dstRgb=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int srcAlpha=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),dstAlpha=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        int rgbEquation=GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB),alphaEquation=GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA);
        java.nio.ByteBuffer masks=org.lwjgl.BufferUtils.createByteBuffer(4);GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK,masks);
        int mask=0;for(int i=0;i<4;i++)if(masks.get(i)!=0)mask|=1<<i;
        try {
            GlStateManager._colorMask(15);GlStateManager._enableBlend(0);
            GlStateManager._blendEquationSeparate(GL14.GL_FUNC_ADD,GL14.GL_FUNC_ADD);
            GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA,GL11.GL_ONE,GL11.GL_ZERO);
            var manager=ShaderManager.getInstance();
            var fragment=manager.resourceFragment(Identifier.parse("ldlib:shaders/core/fast_blit.fsh"));
            manager.renderFullImageInFramebuffer(to,fragment,null,null,program->program.bindTexture("DiffuseSampler",texture.glId()));
        } finally {
            GlStateManager._colorMask(mask);
            GlStateManager._blendEquationSeparate(rgbEquation,alphaEquation);
            GlStateManager._blendFuncSeparate(srcRgb,dstRgb,srcAlpha,dstAlpha);
            if(blend)GlStateManager._enableBlend(0);else GlStateManager._disableBlend(0);
        }
    }
    public static void warpGLDebugLabel(String message,Runnable block) {
        if(GL.getCapabilities().GL_KHR_debug && !net.neoforged.fml.loading.FMLEnvironment.isProduction()) {
            GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION,0,message);
            try {block.run();} finally {GL43.glPopDebugGroup();}
        } else block.run();
    }
}
