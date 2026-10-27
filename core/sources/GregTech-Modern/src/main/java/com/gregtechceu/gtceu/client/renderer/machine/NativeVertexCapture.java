package com.gregtechceu.gtceu.client.renderer.machine;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import java.util.*;
/** Converts immediate native vertices into an immutable deferred geometry submission. */
public final class NativeVertexCapture implements VertexConsumer {
    public record Vertex(float x,float y,float z,int color,float u,float v,int overlay,int light,float nx,float ny,float nz,float lineWidth) {}
    private static final class Pending {
        float x,y,z,u,v,nx,ny,nz,lineWidth;int color=-1,overlay,light;
        Vertex freeze(){return new Vertex(x,y,z,color,u,v,overlay,light,nx,ny,nz,lineWidth);}
    }
    private final List<Pending> vertices=new ArrayList<>();private Pending current;
    private Pending current(){return Objects.requireNonNull(current,"Vertex attributes require a vertex");}
    public VertexConsumer addVertex(float x,float y,float z){current=new Pending();current.x=x;current.y=y;current.z=z;vertices.add(current);return this;}
    public VertexConsumer setColor(int r,int g,int b,int a){return setColor(net.minecraft.util.ARGB.color(a,r,g,b));}
    public VertexConsumer setColor(int color){current().color=color;return this;}
    public VertexConsumer setUv(float u,float v){current().u=u;current().v=v;return this;}
    public VertexConsumer setUv1(int u,int v){current().overlay=u|(v<<16);return this;}
    public VertexConsumer setUv2(int u,int v){current().light=u|(v<<16);return this;}
    public VertexConsumer setNormal(float x,float y,float z){current().nx=x;current().ny=y;current().nz=z;return this;}
    public VertexConsumer setUv3(float u,float v){throw new UnsupportedOperationException("Block geometry has no UV3");}
    public VertexConsumer setLineWidth(float width){current().lineWidth=width;return this;}
    public List<Vertex> snapshot(){return vertices.stream().map(Pending::freeze).toList();}
    public com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission freeze(RenderType type) {
        var frozen=snapshot();
        return (pose,collector)->collector.submitCustomGeometry(pose,type,(matrix,buffer)-> {
            for(var v:frozen){buffer.addVertex(matrix,v.x(),v.y(),v.z()).setColor(v.color()).setUv(v.u(),v.v())
                    .setOverlay(v.overlay()).setLight(v.light()).setNormal(matrix,v.nx(),v.ny(),v.nz());
                if(v.lineWidth()!=0)buffer.setLineWidth(v.lineWidth());
            }
        });
    }
}
