package com.lowdragmc.lowdraglib.client.scene;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;
import java.util.function.BiFunction;

/** Native world AO/tints/culling captured into reusable scene vertices before submission. */
public final class NativeSceneBlockGeometry {
    private record Vertex(float x,float y,float z,int color,float u,float v,int overlay,int light,float nx,float ny,float nz) {}
    private final Map<ChunkSectionLayer,List<Vertex>> layers;
    private final Map<ChunkSectionLayer,RenderType> renderTypes;
    private NativeSceneBlockGeometry(Map<ChunkSectionLayer,Capture> captures) {
        var frozen=new EnumMap<ChunkSectionLayer,List<Vertex>>(ChunkSectionLayer.class);
        captures.forEach((layer,capture)->frozen.put(layer,capture.vertices.stream().map(MutableVertex::freeze).toList()));
        layers=Collections.unmodifiableMap(frozen);
        var types=new EnumMap<ChunkSectionLayer,RenderType>(ChunkSectionLayer.class);
        frozen.keySet().forEach(layer -> types.put(layer,renderType(layer)));
        renderTypes=Collections.unmodifiableMap(types);
    }
    private NativeSceneBlockGeometry(Map<ChunkSectionLayer,List<Vertex>> layers,Map<ChunkSectionLayer,RenderType> renderTypes) {
        this.layers=layers;this.renderTypes=Collections.unmodifiableMap(renderTypes);
    }
    public NativeSceneBlockGeometry withRenderTypes(java.util.function.UnaryOperator<RenderType> mapper) {
        var types=new EnumMap<ChunkSectionLayer,RenderType>(ChunkSectionLayer.class);
        renderTypes.forEach((layer,type) -> types.put(layer,Objects.requireNonNull(mapper.apply(type))));
        return new NativeSceneBlockGeometry(layers,types);
    }
    public static NativeSceneBlockGeometry extract(BlockAndTintGetter world,BlockPos pos,BlockState state,
            BlockStateModel model,boolean ao,boolean cull,BiFunction<ChunkSectionLayer,VertexConsumer,VertexConsumer> wrappers) {
        return extract(world,pos,state,model,ao,cull,wrappers,java.util.function.UnaryOperator.identity());
    }
    public static NativeSceneBlockGeometry extract(BlockAndTintGetter world,BlockPos pos,BlockState state,
            BlockStateModel model,boolean ao,boolean cull,BiFunction<ChunkSectionLayer,VertexConsumer,VertexConsumer> wrappers,
            java.util.function.UnaryOperator<VertexConsumer> blockTransform) {
        var captures=new EnumMap<ChunkSectionLayer,Capture>(ChunkSectionLayer.class);
        var outputs=new EnumMap<ChunkSectionLayer,VertexConsumer>(ChunkSectionLayer.class);
        java.util.function.Function<ChunkSectionLayer,VertexConsumer> outputForLayer=layer->outputs.computeIfAbsent(layer,key->{
            var capture=new Capture();captures.put(key,capture);
            return wrappers==null?capture:Objects.requireNonNull(wrappers.apply(key,capture));
        });
        if(state.getRenderShape()==net.minecraft.world.level.block.RenderShape.MODEL)
            new ModelBlockRenderer(ao,cull,Minecraft.getInstance().getBlockColors()).tesselateBlock((x,y,z,quad,instance)->
                    blockTransform.apply(outputForLayer.apply(quad.materialInfo().layer())).putBlockBakedQuad(x,y,z,quad,instance),
                    pos.getX(),pos.getY(),pos.getZ(),world,pos,state,model,state.getSeed(pos));
        if(!state.getFluidState().isEmpty()) {
            new FluidRenderer(Minecraft.getInstance().getModelManager().getFluidStateModelSet()).tesselate(world,pos,layer-> {
                var normalizer=new NativeSceneVertexConsumer(outputForLayer.apply(layer));
                normalizer.addOffset(pos.getX()-(pos.getX()&15),pos.getY()-(pos.getY()&15),pos.getZ()-(pos.getZ()&15));
                return normalizer;
            },state,state.getFluidState());
        }
        return new NativeSceneBlockGeometry(captures);
    }
    public static RenderType renderType(ChunkSectionLayer layer) {
        return switch(layer) {
            case SOLID->RenderTypes.solidMovingBlock();
            case CUTOUT->RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT->RenderTypes.translucentMovingBlock();
        };
    }
    public void submit(PoseStack pose,SubmitNodeCollector collector) {
        layers.forEach((layer,vertices)-> {
            if(vertices.isEmpty())return;
            collector.submitCustomGeometry(pose,renderTypes.get(layer),(matrix,buffer)-> {
                for(var vertex:vertices)buffer.addVertex(matrix,vertex.x,vertex.y,vertex.z).setColor(vertex.color)
                        .setUv(vertex.u,vertex.v).setOverlay(vertex.overlay).setLight(vertex.light)
                        .setNormal(matrix,vertex.nx,vertex.ny,vertex.nz);
            });
        });
    }
    private static final class MutableVertex {
        float x,y,z,u,v,nx,ny,nz;int color=-1,overlay,light;
        Vertex freeze(){return new Vertex(x,y,z,color,u,v,overlay,light,nx,ny,nz);}
    }
    private static final class Capture implements VertexConsumer {
        final List<MutableVertex> vertices=new ArrayList<>();MutableVertex current;
        @Override public VertexConsumer addVertex(float x,float y,float z){current=new MutableVertex();current.x=x;current.y=y;current.z=z;vertices.add(current);return this;}
        @Override public VertexConsumer setColor(int r,int g,int b,int a){current.color=ARGB.color(a,r,g,b);return this;}
        @Override public VertexConsumer setColor(int color){current.color=color;return this;}
        @Override public VertexConsumer setUv(float u,float v){current.u=u;current.v=v;return this;}
        @Override public VertexConsumer setUv1(int u,int v){current.overlay=u|(v<<16);return this;}
        @Override public VertexConsumer setUv2(int u,int v){current.light=u|(v<<16);return this;}
        @Override public VertexConsumer setNormal(float x,float y,float z){current.nx=x;current.ny=y;current.nz=z;return this;}
        @Override public VertexConsumer setUv3(float u,float v){throw new UnsupportedOperationException("Block geometry has no UV3");}
        @Override public VertexConsumer setLineWidth(float width){throw new UnsupportedOperationException("Block geometry has no line width");}
    }
}
