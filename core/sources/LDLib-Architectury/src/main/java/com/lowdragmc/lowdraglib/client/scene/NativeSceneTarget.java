package com.lowdragmc.lowdraglib.client.scene;

import com.lowdragmc.lowdraglib.transfer.client.GuiQuadState;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.joml.Matrix3x2f;
import java.util.*;

/** Scene color/depth ownership; replaced targets survive queued GUI commands through frame completion. */
@EventBusSubscriber(modid="ldlib",value=Dist.CLIENT)
public final class NativeSceneTarget implements AutoCloseable {
    private static final List<RenderTarget> retired=new ArrayList<>();
    private RenderTarget target;
    public NativeSceneTarget(int width,int height){resize(width,height);}
    public NativeSceneTarget(RenderTarget target){
        if(target.getDepthTextureView()==null)throw new IllegalArgumentException("Scene target requires depth");
        this.target=Objects.requireNonNull(target);
    }
    public RenderTarget get(){if(target==null)throw new IllegalStateException("Scene target is released");return target;}
    public void resize(int width,int height) {
        if(width<=0 || height<=0)throw new IllegalArgumentException("Scene target dimensions must be positive");
        if(target!=null && target.getColorTextureView().getWidth(0)==width && target.getColorTextureView().getHeight(0)==height)return;
        var replacement=new TextureTarget("LDLib scene",width,height,GpuFormat.RGBA8_UNORM,GpuFormat.D32_FLOAT);
        if(target!=null)retired.add(target);target=replacement;
    }
    public void draw(GuiGraphicsExtractor graphics,float x,float y,float width,float height) {
        graphics.submitGuiElementRenderState(new GuiQuadState(RenderPipelines.GUI_TEXTURED,
                TextureSetup.singleTexture(get().getColorTextureView(),RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)),
                new Matrix3x2f(graphics.pose()),new GuiQuadState.Vertex[]{
                    new GuiQuadState.Vertex(x,y,0,1,-1),new GuiQuadState.Vertex(x,y+height,0,0,-1),
                    new GuiQuadState.Vertex(x+width,y+height,1,0,-1),new GuiQuadState.Vertex(x+width,y,1,1,-1)
                },0,true,graphics.peekScissorStack()));
    }
    @Override public void close(){if(target!=null){retired.add(target);target=null;}}
    @SubscribeEvent public static void afterFrame(RenderFrameEvent.Post event){finishFrame();}
    public static void finishFrame(){for(var old:retired)RenderSystem.queueFencedTask(old::destroyBuffers);retired.clear();}
}
