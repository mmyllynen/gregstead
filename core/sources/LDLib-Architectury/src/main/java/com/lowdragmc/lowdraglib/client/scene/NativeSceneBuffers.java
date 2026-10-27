package com.lowdragmc.lowdraglib.client.scene;

import com.mojang.blaze3d.vertex.*;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import java.util.*;
import java.util.function.Consumer;

/** Captures already transformed editor vertices and attributes before native feature preparation. */
public final class NativeSceneBuffers {
    private final Map<RenderType,Capture> buffers=new LinkedHashMap<>();
    private final List<Consumer<SubmitNodeCollector>> models=new ArrayList<>();
    private static final net.minecraft.client.renderer.block.model.BlockDisplayContext BLOCK_CONTEXT=
            net.minecraft.client.renderer.block.model.BlockDisplayContext.create();
    public void submitBlock(net.minecraft.world.level.block.state.BlockState block,PoseStack pose) {
        var state=new net.minecraft.client.renderer.block.BlockModelRenderState();
        net.minecraft.client.Minecraft.getInstance().getBlockModelResolver().update(state,block,BLOCK_CONTEXT);
        var snapshot=pose.last().copy();
        models.add(collector->{var stack=new PoseStack();stack.last().set(snapshot);
            state.submitMultiLayer(stack,collector,net.minecraft.util.LightCoordsUtil.FULL_BRIGHT,
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0);});
    }
    public void clear(){buffers.clear();models.clear();}
    public VertexConsumer getBuffer(RenderType type){return buffers.computeIfAbsent(type,key->new Capture());}
    public void submit(SubmitNodeCollector collector) {
        models.forEach(command->command.accept(collector));models.clear();
        buffers.forEach((type,capture)-> {
            var commands=List.copyOf(capture.commands);
            if(capture.vertices==0)return;
            collector.submitCustomGeometry(new PoseStack(),type,(pose,consumer)->commands.forEach(command->command.accept(consumer)));
        });
        buffers.clear();
    }
    private static final class Capture implements VertexConsumer {
        final List<Consumer<VertexConsumer>> commands=new ArrayList<>();int vertices;
        public VertexConsumer addVertex(float x,float y,float z){commands.add(out->{out.addVertex(x,y,z);out.setLineWidth(3);});vertices++;return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){commands.add(out->out.setColor(r,g,b,a));return this;}
        public VertexConsumer setColor(int color){commands.add(out->out.setColor(color));return this;}
        public VertexConsumer setUv(float u,float v){commands.add(out->out.setUv(u,v));return this;}
        public VertexConsumer setUv1(int u,int v){commands.add(out->out.setUv1(u,v));return this;}
        public VertexConsumer setUv2(int u,int v){commands.add(out->out.setUv2(u,v));return this;}
        public VertexConsumer setUv3(float u,float v){commands.add(out->out.setUv3(u,v));return this;}
        public VertexConsumer setNormal(float x,float y,float z){commands.add(out->out.setNormal(x,y,z));return this;}
        public VertexConsumer setLineWidth(float width){commands.add(out->out.setLineWidth(width));return this;}
        public VertexConsumer misc(VertexFormatElement element,int... data){var copy=data.clone();commands.add(out->out.misc(element,copy.clone()));return this;}
    }
}
