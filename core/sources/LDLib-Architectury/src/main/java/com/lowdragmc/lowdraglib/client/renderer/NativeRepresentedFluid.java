package com.lowdragmc.lowdraglib.client.renderer;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Native fluid tessellation captured before deferred BER submission. */
public final class NativeRepresentedFluid {
    private record Vertex(float x,float y,float z,int color,float u,float v,int overlay,int light,float nx,float ny,float nz) {}
    private final List<Vertex> vertices;
    private NativeRepresentedFluid(List<Vertex> vertices) { this.vertices=List.copyOf(vertices); }
    public static NativeRepresentedFluid extract(BlockAndTintGetter level,BlockPos pos,BlockState state) {
        List<Vertex> vertices=new ArrayList<>();
        if (!state.getFluidState().isEmpty()) {
            var capture=new Capture(vertices,pos);
            new FluidRenderer(Minecraft.getInstance().getModelManager().getFluidStateModelSet())
                    .tesselate(level,pos,layer->capture,state,state.getFluidState());
        }
        return new NativeRepresentedFluid(vertices);
    }
    public void submit(PoseStack pose,SubmitNodeCollector collector) {
        if(vertices.isEmpty())return;
        collector.submitCustomGeometry(pose,RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS),(matrix,buffer)-> {
            for(var vertex:vertices) buffer.addVertex(matrix,vertex.x,vertex.y,vertex.z).setColor(vertex.color)
                    .setUv(vertex.u,vertex.v).setOverlay(vertex.overlay).setLight(vertex.light)
                    .setNormal(matrix,vertex.nx,vertex.ny,vertex.nz);
        });
    }
    private static final class Capture implements VertexConsumer {
        private final List<Vertex> vertices;
        private final int x,y,z;
        Capture(List<Vertex> vertices,BlockPos pos) { this.vertices=vertices;x=pos.getX()&15;y=pos.getY()&15;z=pos.getZ()&15; }
        @Override public void addVertex(float px,float py,float pz,int color,float u,float v,int overlay,int light,float nx,float ny,float nz) {
            vertices.add(new Vertex(px-x,py-y,pz-z,color,u,v,overlay,light,nx,ny,nz));
        }
        private UnsupportedOperationException unexpected() { return new UnsupportedOperationException("FluidRenderer must emit complete native vertices"); }
        public VertexConsumer addVertex(float x,float y,float z){throw unexpected();}
        public VertexConsumer setColor(int r,int g,int b,int a){throw unexpected();}
        public VertexConsumer setColor(int color){throw unexpected();}
        public VertexConsumer setUv(float u,float v){throw unexpected();}
        public VertexConsumer setUv1(int u,int v){throw unexpected();}
        public VertexConsumer setUv2(int u,int v){throw unexpected();}
        public VertexConsumer setUv3(float u,float v){throw unexpected();}
        public VertexConsumer setNormal(float x,float y,float z){throw unexpected();}
        public VertexConsumer setLineWidth(float width){throw unexpected();}
    }
}
