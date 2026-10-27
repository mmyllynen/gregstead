package com.gregtechceu.gtceu.client.renderer.machine.impl;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import java.util.*;
/** Frozen original tank quad vertices; submission reads no fluid/world/model state. */
public record NativeTankVertices(List<Vertex> vertices) {
    public record Vertex(float x,float y,float z,int color,float u,float v,int overlay,int light,Direction normal) {}
    public NativeTankVertices { vertices=List.copyOf(vertices); }
    public static NativeTankVertices capture(NativeQuantumTankShape shape,TextureAtlasSprite sprite,int color,int light,int overlay) {
        var b=shape.bounds();float x0=(float)b.minX,y0=(float)b.minY,z0=(float)b.minZ,x1=(float)b.maxX,y1=(float)b.maxY,z1=(float)b.maxZ;
        var result=new ArrayList<Vertex>();
        for(Direction side:Direction.values()) {
            if(!shape.faces().contains(side))continue;
            float[][] positions=switch(side) {
                case UP->new float[][]{{x0,y1,z0},{x0,y1,z1},{x1,y1,z1},{x1,y1,z0}};
                case DOWN->new float[][]{{x0,y0,z0},{x1,y0,z0},{x1,y0,z1},{x0,y0,z1}};
                case NORTH->new float[][]{{x0,y0,z0},{x0,y1,z0},{x1,y1,z0},{x1,y0,z0}};
                case SOUTH->new float[][]{{x0,y0,z1},{x1,y0,z1},{x1,y1,z1},{x0,y1,z1}};
                case WEST->new float[][]{{x0,y0,z0},{x0,y0,z1},{x0,y1,z1},{x0,y1,z0}};
                case EAST->new float[][]{{x1,y0,z0},{x1,y1,z0},{x1,y1,z1},{x1,y0,z1}};
            };
            for(int i=0;i<4;i++)result.add(new Vertex(positions[i][0],positions[i][1],positions[i][2],color,
                    i==0||i==3?sprite.getU0():sprite.getU1(),i<2?sprite.getV1():sprite.getV0(),overlay,light,side));
        }
        return new NativeTankVertices(result);
    }
    public void submit(PoseStack pose,SubmitNodeCollector collector,RenderType renderType) {
        collector.submitCustomGeometry(pose,renderType,(matrix,buffer)->{
            for(var v:vertices)buffer.addVertex(matrix,v.x(),v.y(),v.z()).setColor(v.color()).setUv(v.u(),v.v())
                    .setOverlay(v.overlay()).setLight(v.light()).setNormal(matrix,v.normal().getStepX(),v.normal().getStepY(),v.normal().getStepZ());
        });
    }
}
