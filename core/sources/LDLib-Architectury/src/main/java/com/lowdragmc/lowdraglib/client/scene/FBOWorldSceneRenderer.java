package com.lowdragmc.lowdraglib.client.scene;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import javax.annotation.Nonnull;

/** Explicit native offscreen scene target, presented through deferred GUI geometry. */
public class FBOWorldSceneRenderer extends WorldSceneRenderer {
    private int resolutionWidth,resolutionHeight;
    private final NativeSceneTarget fbo;
    private boolean matchGuiResolution;
    public FBOWorldSceneRenderer matchGuiResolution() { matchGuiResolution = true; return this; }
    public FBOWorldSceneRenderer(Level world,int width,int height) {
        super(world);fbo=new NativeSceneTarget(width,height);resolutionWidth=width;resolutionHeight=height;
    }
    public FBOWorldSceneRenderer(Level world,RenderTarget target) {
        super(world);fbo=new NativeSceneTarget(target);
        resolutionWidth=target.getColorTextureView().getWidth(0);resolutionHeight=target.getColorTextureView().getHeight(0);
    }
    @Override protected RenderTarget particleRenderTarget(){return fbo.get();}
    public int getResolutionWidth(){return resolutionWidth;}
    public int getResolutionHeight(){return resolutionHeight;}
    public void setFBOSize(int width,int height){fbo.resize(width,height);resolutionWidth=width;resolutionHeight=height;}
    public BlockHitResult screenPos2BlockPosFace(int mouseX,int mouseY) {
        return super.screenPos2BlockPosFace(mouseX,mouseY,0,0,resolutionWidth,resolutionHeight);
    }
    public Vector3f blockPos2ScreenPos(BlockPos pos,boolean depth) {
        return super.blockPos2ScreenPos(pos,depth,0,0,resolutionWidth,resolutionHeight);
    }
    @Override public void render(@Nonnull GuiGraphicsExtractor graphics,float x,float y,float width,float height,int mouseX,int mouseY) {
        if(width<=0 || height<=0)return;
        if (matchGuiResolution) {
            var pose = graphics.pose();
            double scale = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScale();
            setFBOSize(Math.max(1,(int)Math.ceil(width*scale*Math.hypot(pose.m00(),pose.m01()))),
                    Math.max(1,(int)Math.ceil(height*scale*Math.hypot(pose.m10(),pose.m11()))));
        }
        var mouse=new Matrix3x2f(graphics.pose()).invert().transformPosition(mouseX,mouseY,new Vector2f());
        super.render(new PoseStack(),0,0,resolutionWidth,resolutionHeight,
                (int)(resolutionWidth*(mouse.x-x)/width),(int)(resolutionHeight*(1-(mouse.y-y)/height)));
        fbo.draw(graphics,x,y,width,height);
    }
    public void releaseFBO(){fbo.close();}
}
