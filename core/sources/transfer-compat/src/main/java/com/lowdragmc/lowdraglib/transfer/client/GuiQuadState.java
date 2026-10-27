package com.lowdragmc.lowdraglib.transfer.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

/** Immutable extraction snapshot; geometry is emitted only by the native GUI renderer. */
public final class GuiQuadState implements GuiElementRenderState {
    public record Vertex(float x, float y, float u, float v, int color) {}
    private final RenderPipeline pipeline;
    private final TextureSetup texture;
    private final Matrix3x2f pose;
    private final Vertex[] vertices;
    private final float depth;
    private final boolean textured;
    private final ScreenRectangle scissor;
    private final ScreenRectangle bounds;
    public GuiQuadState(RenderPipeline pipeline, TextureSetup texture, Matrix3x2f pose, Vertex[] vertices,
                        float depth, boolean textured, ScreenRectangle scissor) {
        if (vertices.length != 4) throw new IllegalArgumentException("Expected four quad vertices");
        this.pipeline = pipeline;
        this.texture = texture;
        this.pose = new Matrix3x2f(pose);
        this.vertices = vertices.clone();
        this.depth = depth;
        this.textured = textured;
        this.scissor = scissor;
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        for (var vertex : vertices) {
            var point = this.pose.transformPosition(vertex.x(), vertex.y(), new Vector2f());
            minX = Math.min(minX, point.x); minY = Math.min(minY, point.y);
            maxX = Math.max(maxX, point.x); maxY = Math.max(maxY, point.y);
        }
        var transformed = new ScreenRectangle((int) Math.floor(minX), (int) Math.floor(minY),
                (int) Math.ceil(maxX) - (int) Math.floor(minX), (int) Math.ceil(maxY) - (int) Math.floor(minY));
        this.bounds = scissor == null ? transformed : scissor.intersection(transformed);
    }
    @Override public void buildVertices(VertexConsumer consumer) {
        for (var vertex : vertices) {
            var point = pose.transformPosition(vertex.x(), vertex.y(), new Vector2f());
            var output = consumer.addVertex(point.x, point.y, depth).setColor(vertex.color());
            if (textured) output.setUv(vertex.u(), vertex.v());
        }
    }
    @Override public RenderPipeline pipeline() { return pipeline; }
    @Override public TextureSetup textureSetup() { return texture; }
    @Override public ScreenRectangle scissorArea() { return scissor; }
    @Override public ScreenRectangle bounds() { return bounds; }
}
