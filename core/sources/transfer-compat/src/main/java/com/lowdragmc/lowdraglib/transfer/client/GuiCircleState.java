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

/** Upstream circle radius and one-GUI-unit edge evaluated before the pose transform. */
public final class GuiCircleState implements GuiElementRenderState {
    public static final VertexFormat FORMAT = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT).addAttribute("Color", GpuFormat.RGBA8_UNORM)
            .addAttribute("UV0", GpuFormat.RG32_FLOAT).addAttribute("CircleRadius", GpuFormat.R32_FLOAT).build();
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("gttransfercompat", "pipeline/circle"))
            .withVertexShader(Identifier.fromNamespaceAndPath("gttransfercompat", "core/circle"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("gttransfercompat", "core/circle"))
            .withVertexBinding(0, FORMAT).build();
    private final Matrix3x2f pose;
    private final float x, y, radius, extent;
    private final int color;
    private final ScreenRectangle scissor, bounds;
    public GuiCircleState(Matrix3x2f pose, ScreenRectangle scissor, float x, float y, float radius, int color) {
        this.pose = new Matrix3x2f(pose); this.scissor = scissor;
        this.x = x; this.y = y; this.radius = radius; this.color = color;
        this.extent = Math.max(radius + 1, 0);
        var area = new ScreenRectangle((int) Math.floor(x - extent), (int) Math.floor(y - extent),
                (int) Math.ceil(x + extent) - (int) Math.floor(x - extent),
                (int) Math.ceil(y + extent) - (int) Math.floor(y - extent)).transformMaxBounds(this.pose);
        this.bounds = scissor == null ? area : scissor.intersection(area);
    }
    @Override public void buildVertices(VertexConsumer consumer) {
        emit(consumer, -extent, -extent); emit(consumer, -extent, extent);
        emit(consumer, extent, extent); emit(consumer, extent, -extent);
    }
    private void emit(VertexConsumer consumer, float localX, float localY) {
        var point = pose.transformPosition(x + localX, y + localY, new Vector2f());
        consumer.addVertex(point.x, point.y, 0).setColor(color).setUv(localX, localY)
                .misc(FORMAT.getElement("CircleRadius"), Float.floatToRawIntBits(radius));
    }
    @Override public RenderPipeline pipeline() { return PIPELINE; }
    @Override public TextureSetup textureSetup() { return TextureSetup.noTexture(); }
    @Override public ScreenRectangle scissorArea() { return scissor; }
    @Override public ScreenRectangle bounds() { return bounds; }
}
