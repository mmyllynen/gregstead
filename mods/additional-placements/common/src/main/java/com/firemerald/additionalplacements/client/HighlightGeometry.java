package com.firemerald.additionalplacements.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.rendertype.RenderType;

/** Extracts immutable placement-preview vertices before the deferred render pass. */
public final class HighlightGeometry {
    public record Vertex(float x, float y, float z, int color, float nx, float ny, float nz, float width) {
        public void emit(VertexConsumer target, PoseStack.Pose pose) {
            target.addVertex(pose, x, y, z).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(width);
        }
    }
    private final Map<RenderType, Recorder> buffers = new LinkedHashMap<>();
    public VertexConsumer buffer(RenderType type) { return buffers.computeIfAbsent(type, ignored -> new Recorder()); }
    public Map<RenderType, List<Vertex>> snapshot() {
        Map<RenderType, List<Vertex>> snapshot = new LinkedHashMap<>();
        buffers.forEach((type, recorder) -> { recorder.finish(); snapshot.put(type, List.copyOf(recorder.vertices)); });
        return Map.copyOf(snapshot);
    }
    private static final class Recorder implements VertexConsumer {
        final List<Vertex> vertices = new ArrayList<>();
        boolean pending;
        float x, y, z, nx, ny, nz, width = 1;
        int color = -1;
        void finish() {
            if (pending) vertices.add(new Vertex(x, y, z, color, nx, ny, nz, width));
            pending = false;
        }
        public VertexConsumer addVertex(float x, float y, float z) { finish(); this.x=x; this.y=y; this.z=z; pending=true; return this; }
        public VertexConsumer setColor(int r,int g,int b,int a) { return setColor((a<<24)|(r<<16)|(g<<8)|b); }
        public VertexConsumer setColor(int color) { this.color=color; return this; }
        public VertexConsumer setNormal(float x,float y,float z) { nx=x; ny=y; nz=z; return this; }
        public VertexConsumer setLineWidth(float width) { this.width=width; return this; }
        public VertexConsumer setUv(float u,float v) { return this; }
        public VertexConsumer setUv1(int u,int v) { return this; }
        public VertexConsumer setUv2(int u,int v) { return this; }
        public VertexConsumer setUv3(float u,float v) { return this; }
    }
}
