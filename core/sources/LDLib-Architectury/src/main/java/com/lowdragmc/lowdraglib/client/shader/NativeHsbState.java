package com.lowdragmc.lowdraglib.client.shader;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

/** Immutable HSV corners: conversion to RGB happens after interpolation in the fragment shader. */
public final class NativeHsbState implements GuiElementRenderState {
    public enum Mode { H, S, B }
    public record Vertex(float x, float y, float h, float s, float b, float a) {}
    private final Matrix3x2f pose;
    private final Vertex[] vertices;
    private final ScreenRectangle scissor, bounds;

    public NativeHsbState(Matrix3x2f pose, ScreenRectangle scissor, Vertex[] vertices) {
        if (vertices.length != 4) throw new IllegalArgumentException("Expected four HSB vertices");
        this.pose = new Matrix3x2f(pose);
        this.vertices = vertices.clone();
        this.scissor = scissor;
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        for (var vertex : this.vertices) {
            var point = this.pose.transformPosition(vertex.x(), vertex.y(), new Vector2f());
            minX = Math.min(minX, point.x); minY = Math.min(minY, point.y);
            maxX = Math.max(maxX, point.x); maxY = Math.max(maxY, point.y);
        }
        var area = new ScreenRectangle((int)Math.floor(minX), (int)Math.floor(minY),
                (int)Math.ceil(maxX)-(int)Math.floor(minX), (int)Math.ceil(maxY)-(int)Math.floor(minY));
        bounds = scissor == null ? area : scissor.intersection(area);
    }

    public static NativeHsbState main(Matrix3x2f pose, ScreenRectangle scissor,
            Mode mode, float x, float y, float width, float height, float h, float s, float b) {
        return new NativeHsbState(pose, scissor, switch (mode) {
            case H -> new Vertex[] { v(x,y,h,0,1), v(x,y+height,h,0,0),
                    v(x+width,y+height,h,1,0), v(x+width,y,h,1,1) };
            case S -> new Vertex[] { v(x,y,0,s,1), v(x,y+height,0,s,0),
                    v(x+width,y+height,360,s,0), v(x+width,y,360,s,1) };
            case B -> new Vertex[] { v(x,y,0,1,b), v(x,y+height,0,0,b),
                    v(x+width,y+height,360,0,b), v(x+width,y,360,1,b) };
        });
    }
    public static NativeHsbState slider(Matrix3x2f pose, ScreenRectangle scissor,
            Mode mode, float x, float y, float width, float height, float h, float s, float b) {
        float topH = mode == Mode.H ? 360 : h, bottomH = mode == Mode.H ? 0 : h;
        float topS = mode == Mode.H || mode == Mode.S ? 1 : s;
        float bottomS = mode == Mode.H ? 1 : mode == Mode.S ? 0 : s;
        float topB = mode == Mode.H || mode == Mode.B ? 1 : b;
        float bottomB = mode == Mode.H ? 1 : mode == Mode.B ? 0 : b;
        return new NativeHsbState(pose, scissor, new Vertex[] {
                v(x,y+height,bottomH,bottomS,bottomB), v(x+width,y+height,bottomH,bottomS,bottomB),
                v(x+width,y,topH,topS,topB), v(x,y,topH,topS,topB) });
    }
    private static Vertex v(float x, float y, float h, float s, float b) { return new Vertex(x,y,h,s,b,1); }
    public static void submit(GuiGraphicsExtractor graphics, Mode mode, int x, int y, int width, int height,
            int gap, int barWidth, float h, float s, float b) {
        graphics.submitGuiElementRenderState(main(new Matrix3x2f(graphics.pose()), graphics.peekScissorStack(),
                mode,x,y,width,height,h,s,b));
        graphics.submitGuiElementRenderState(slider(new Matrix3x2f(graphics.pose()), graphics.peekScissorStack(),
                mode,x+width+gap,y,barWidth,height,h,s,b));
    }
    @Override public void buildVertices(VertexConsumer consumer) {
        for (var v : vertices) {
            var point = pose.transformPosition(v.x(), v.y(), new Vector2f());
            consumer.addVertex(point.x, point.y, 0).misc(NativeShaderFormats.HSB.getElement("HSB_ALPHA"),
                    Float.floatToRawIntBits(v.h()), Float.floatToRawIntBits(v.s()),
                    Float.floatToRawIntBits(v.b()), Float.floatToRawIntBits(v.a()));
        }
    }
    @Override public RenderPipeline pipeline() { return NativeHsbPipeline.PIPELINE; }
    @Override public TextureSetup textureSetup() { return TextureSetup.noTexture(); }
    @Override public ScreenRectangle scissorArea() { return scissor; }
    @Override public ScreenRectangle bounds() { return bounds; }
}
