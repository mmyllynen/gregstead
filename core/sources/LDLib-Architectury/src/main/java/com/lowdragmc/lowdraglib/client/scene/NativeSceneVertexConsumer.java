package com.lowdragmc.lowdraglib.client.scene;

import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Setter;
import net.minecraft.util.ARGB;

/** Scene offsets/color modulation stay active throughout fluent native vertex calls. */
public class NativeSceneVertexConsumer implements VertexConsumer {
    protected final VertexConsumer builder;
    @Setter protected float offsetX,offsetY,offsetZ;
    protected float r=1,g=1,b=1,a=1;
    public NativeSceneVertexConsumer(VertexConsumer builder){this.builder=builder;}
    public void addOffset(float x,float y,float z){offsetX+=x;offsetY+=y;offsetZ+=z;}
    public void setColorMultiplier(float r,float g,float b,float a){this.r=r;this.g=g;this.b=b;this.a=a;}
    public void clearOffset(){offsetX=offsetY=offsetZ=0;}
    public void clearColor(){r=g=b=a=1;}
    @Override public VertexConsumer addVertex(float x,float y,float z){builder.addVertex(x+offsetX,y+offsetY,z+offsetZ);return this;}
    @Override public VertexConsumer setColor(int r,int g,int b,int a){builder.setColor((int)(r*this.r),(int)(g*this.g),(int)(b*this.b),(int)(a*this.a));return this;}
    @Override public VertexConsumer setColor(int color){return setColor(ARGB.red(color),ARGB.green(color),ARGB.blue(color),ARGB.alpha(color));}
    @Override public VertexConsumer setUv(float u,float v){builder.setUv(u,v);return this;}
    @Override public VertexConsumer setUv1(int u,int v){builder.setUv1(u,v);return this;}
    @Override public VertexConsumer setUv2(int u,int v){builder.setUv2(u,v);return this;}
    @Override public VertexConsumer setUv3(float u,float v){builder.setUv3(u,v);return this;}
    @Override public VertexConsumer setNormal(float x,float y,float z){builder.setNormal(x,y,z);return this;}
    @Override public VertexConsumer setLineWidth(float width){builder.setLineWidth(width);return this;}
    @Override public VertexConsumer misc(com.mojang.renderpearl.api.vertex.VertexFormatElement element,int... data){builder.misc(element,data);return this;}
}
