package com.lowdragmc.lowdraglib.transfer.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

/** Upstream capsule distance and unclamped axial color mix in pre-pose GUI units. */
public final class GuiRoundLineState implements GuiElementRenderState {
    public static final VertexFormat FORMAT = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT).addAttribute("Color", GpuFormat.RGBA8_UNORM)
            .addAttribute("UV0", GpuFormat.RG32_FLOAT).addAttribute("Segment", GpuFormat.RG32_FLOAT)
            .addAttribute("LineRadius", GpuFormat.R32_FLOAT).addAttribute("SecondColor", GpuFormat.RGBA32_FLOAT).build();
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("gttransfercompat", "pipeline/round_line"))
            .withVertexShader(Identifier.fromNamespaceAndPath("gttransfercompat", "core/round_line"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("gttransfercompat", "core/round_line"))
            .withVertexBinding(0, FORMAT).build();
    private final Matrix3x2f pose;
    private final float x0, y0, x1, y1, width, extent;
    private final int color, secondColor;
    private final ScreenRectangle scissor, bounds;
    public GuiRoundLineState(Matrix3x2f pose, ScreenRectangle scissor, float x0, float y0, float x1, float y1,
                              float width, int color, int secondColor) {
        this.pose = new Matrix3x2f(pose); this.scissor = scissor;
        this.x0 = x0; this.y0 = y0; this.x1 = x1; this.y1 = y1;
        this.width = width; this.color = color; this.secondColor = secondColor; this.extent = Math.max(width + 2, 0);
        float left = Math.min(x0, x1) - extent, top = Math.min(y0, y1) - extent;
        float right = Math.max(x0, x1) + extent, bottom = Math.max(y0, y1) + extent;
        var area = new ScreenRectangle((int) Math.floor(left), (int) Math.floor(top),
                (int) Math.ceil(right) - (int) Math.floor(left), (int) Math.ceil(bottom) - (int) Math.floor(top))
                .transformMaxBounds(this.pose);
        this.bounds = scissor == null ? area : scissor.intersection(area);
    }
    @Override public void buildVertices(VertexConsumer consumer) {
        float left = Math.min(x0, x1) - extent, top = Math.min(y0, y1) - extent;
        float right = Math.max(x0, x1) + extent, bottom = Math.max(y0, y1) + extent;
        emit(consumer, left, top); emit(consumer, left, bottom); emit(consumer, right, bottom); emit(consumer, right, top);
    }
    private void emit(VertexConsumer consumer, float x, float y) {
        var point = pose.transformPosition(x, y, new Vector2f());
        consumer.addVertex(point.x, point.y, 0).setColor(color).setUv(x - x0, y - y0)
                .misc(FORMAT.getElement("Segment"), Float.floatToRawIntBits(x1 - x0), Float.floatToRawIntBits(y1 - y0))
                .misc(FORMAT.getElement("LineRadius"), Float.floatToRawIntBits(width))
                .misc(FORMAT.getElement("SecondColor"), Float.floatToRawIntBits(((secondColor >>> 16) & 255) / 255f),
                        Float.floatToRawIntBits(((secondColor >>> 8) & 255) / 255f), Float.floatToRawIntBits((secondColor & 255) / 255f),
                        Float.floatToRawIntBits(((secondColor >>> 24) & 255) / 255f));
    }
    @Override public RenderPipeline pipeline() { return PIPELINE; }
    @Override public TextureSetup textureSetup() { return TextureSetup.noTexture(); }
    @Override public ScreenRectangle scissorArea() { return scissor; }
    @Override public ScreenRectangle bounds() { return bounds; }
}
