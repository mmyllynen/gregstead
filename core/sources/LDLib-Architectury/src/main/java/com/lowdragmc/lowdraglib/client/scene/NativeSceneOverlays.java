package com.lowdragmc.lowdraglib.client.scene;

import com.lowdragmc.lowdraglib.client.shader.LDLibRenderTypes;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;

/** Native snapshots of the original face border and additive selected-block cube. */
public final class NativeSceneOverlays {
    private NativeSceneOverlays() {}
    public static void facing(NativeSceneBuffers buffers,PoseStack pose,BlockPos pos,Direction face,int color,int inner) {
        pose.pushPose();
        try {
            pose.translate(pos.getX()+0.5+face.getStepX()*0.5,pos.getY()+0.5+face.getStepY()*0.5,pos.getZ()+0.5+face.getStepZ()*0.5);
            switch(face) {
                case UP->{pose.scale(1,-1,1);pose.rotate(new Quaternionf().rotateX((float)Math.PI/2));}
                case DOWN->{pose.scale(1,-1,1);pose.rotate(new Quaternionf().rotateX(-(float)Math.PI/2));}
                case EAST->{pose.scale(-1,-1,-1);pose.rotate(new Quaternionf().rotateY(-(float)Math.PI/2));}
                case WEST->{pose.scale(-1,-1,-1);pose.rotate(new Quaternionf().rotateY((float)Math.PI/2));}
                case NORTH->pose.scale(-1,-1,-1);
                case SOUTH->{pose.scale(-1,-1,-1);pose.rotate(new Quaternionf().rotateY((float)Math.PI));}
            }
            pose.scale(1f/16,1f/16,0);pose.translate(-8,-8,0);
            int x=1+inner*2,y=x,w=14-4*inner,h=w;
            rect(buffers,pose,x-1,y-1,w+2,1,color);rect(buffers,pose,x-1,y+h,w+2,1,color);
            rect(buffers,pose,x-1,y,1,h,color);rect(buffers,pose,x+w,y,1,h,color);
        } finally {pose.popPose();}
    }
    private static void rect(NativeSceneBuffers buffers,PoseStack pose,float x,float y,float w,float h,int color) {
        var out=buffers.getBuffer(LDLibRenderTypes.faceOverlay());var m=pose.last().pose();
        out.addVertex(m,x+w,y+h,0).setColor(color);out.addVertex(m,x+w,y,0).setColor(color);
        out.addVertex(m,x,y,0).setColor(color);out.addVertex(m,x,y+h,0).setColor(color);
    }
    public static void selection(NativeSceneBuffers buffers,PoseStack pose,BlockPos pos,float r,float g,float b,float scale) {
        pose.pushPose();
        try {
            pose.translate(pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5);pose.scale(scale,scale,scale);
            RenderUtils.renderCubeFace(pose,buffers.getBuffer(LDLibRenderTypes.selection()),-0.5f,-0.5f,-0.5f,0.5f,0.5f,0.5f,r,g,b,1);
        } finally {pose.popPose();}
    }
}
