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

/** Original full-screen, pre-pose NDC checker pattern with physical viewport aspect. */
public final class GuiPanelState implements GuiElementRenderState {
    public static final VertexFormat FORMAT = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT).addAttribute("UV0", GpuFormat.RG32_FLOAT)
            .addAttribute("PanelAspect", GpuFormat.R32_FLOAT).build();
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("gttransfercompat", "pipeline/panel"))
            .withVertexShader(Identifier.fromNamespaceAndPath("gttransfercompat", "core/panel"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("gttransfercompat", "core/panel"))
            .withVertexBinding(0, FORMAT).build();
    private final Matrix3x2f pose;
    private final float width, height, aspect;
    private final ScreenRectangle scissor, bounds;
    public GuiPanelState(Matrix3x2f pose, ScreenRectangle scissor, float width, float height, float aspect) {
        this.pose = new Matrix3x2f(pose); this.scissor = scissor;
        this.width = width; this.height = height; this.aspect = aspect;
        var area = new ScreenRectangle(0, 0, (int) Math.ceil(width), (int) Math.ceil(height)).transformMaxBounds(this.pose);
        this.bounds = scissor == null ? area : scissor.intersection(area);
    }
    @Override public void buildVertices(VertexConsumer consumer) {
        emit(consumer, 0, 0, -1, 1); emit(consumer, 0, height, -1, -1);
        emit(consumer, width, height, 1, -1); emit(consumer, width, 0, 1, 1);
    }
    private void emit(VertexConsumer consumer, float x, float y, float u, float v) {
        var point = pose.transformPosition(x, y, new Vector2f());
        consumer.addVertex(point.x, point.y, 0).setUv(u, v)
                .misc(FORMAT.getElement("PanelAspect"), Float.floatToRawIntBits(aspect));
    }
    @Override public RenderPipeline pipeline() { return PIPELINE; }
    @Override public TextureSetup textureSetup() { return TextureSetup.noTexture(); }
    @Override public ScreenRectangle scissorArea() { return scissor; }
    @Override public ScreenRectangle bounds() { return bounds; }
}
