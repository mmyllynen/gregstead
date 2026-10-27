package com.lowdragmc.lowdraglib.transfer.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Vector4f;

/** Original signed-thickness rounded-frame SDF expressed as native per-vertex data instead of GL uniforms. */
public final class GuiRoundedFrameState implements GuiElementRenderState {
    public static final VertexFormat FORMAT = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT).addAttribute("Color", GpuFormat.RGBA8_UNORM)
            .addAttribute("UV0", GpuFormat.RG32_FLOAT).addAttribute("ShapeSize", GpuFormat.RG32_FLOAT)
            .addAttribute("ShapeRadii", GpuFormat.RGBA32_FLOAT).addAttribute("FrameRadii", GpuFormat.RGBA32_FLOAT)
            .addAttribute("ShapeThickness", GpuFormat.R32_FLOAT).addAttribute("ShapeBlur", GpuFormat.R32_FLOAT).build();
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("gttransfercompat", "pipeline/rounded_frame"))
            .withVertexShader(net.minecraft.resources.Identifier.fromNamespaceAndPath("gttransfercompat", "core/rounded_frame")).withFragmentShader(net.minecraft.resources.Identifier.fromNamespaceAndPath("gttransfercompat", "core/rounded_frame"))
            .withVertexBinding(0, FORMAT).build();
    private final float left, top, right, bottom, scale;
    private final Vector4f radii, frameRadii;
    private final float thickness;
    private final int color;
    private final ScreenRectangle scissor, bounds;
    public GuiRoundedFrameState(float left, float top, float right, float bottom, Vector4f radii, Vector4f frameRadii, float thickness, int color,
                               float scale, ScreenRectangle scissor) {
        if (!(scale > 0)) throw new IllegalArgumentException("GUI scale must be positive");
        this.left = left; this.top = top; this.right = right; this.bottom = bottom;
        this.radii = new Vector4f(radii); this.frameRadii = new Vector4f(frameRadii); this.thickness = thickness; this.color = color; this.scale = scale; this.scissor = scissor;
        float blur = Math.max(thickness, 0) + 2 / scale;
        var area = new ScreenRectangle((int) Math.floor(left - blur), (int) Math.floor(top - blur),
                (int) Math.ceil(right + blur) - (int) Math.floor(left - blur),
                (int) Math.ceil(bottom + blur) - (int) Math.floor(top - blur));
        this.bounds = scissor == null ? area : scissor.intersection(area);
    }
    @Override public void buildVertices(VertexConsumer consumer) {
        float halfWidth = (right - left) * scale / 2, halfHeight = (bottom - top) * scale / 2;
        float padding = Math.max(thickness, 0) + 2 / scale;
        emit(consumer, left - padding, top - padding, -halfWidth - padding * scale, halfHeight + padding * scale, halfWidth, halfHeight);
        emit(consumer, left - padding, bottom + padding, -halfWidth - padding * scale, -halfHeight - padding * scale, halfWidth, halfHeight);
        emit(consumer, right + padding, bottom + padding, halfWidth + padding * scale, -halfHeight - padding * scale, halfWidth, halfHeight);
        emit(consumer, right + padding, top - padding, halfWidth + padding * scale, halfHeight + padding * scale, halfWidth, halfHeight);
    }
    private void emit(VertexConsumer consumer, float x, float y, float localX, float localY, float halfWidth, float halfHeight) {
        consumer.addVertex(x, y, 0).setColor(color).setUv(localX, localY)
                .misc(FORMAT.getElement("ShapeSize"), Float.floatToRawIntBits(halfWidth), Float.floatToRawIntBits(halfHeight))
                .misc(FORMAT.getElement("ShapeRadii"), Float.floatToRawIntBits(radii.x * scale), Float.floatToRawIntBits(radii.y * scale),
                        Float.floatToRawIntBits(radii.z * scale), Float.floatToRawIntBits(radii.w * scale))
                .misc(FORMAT.getElement("FrameRadii"), Float.floatToRawIntBits(frameRadii.x * scale), Float.floatToRawIntBits(frameRadii.y * scale),
                        Float.floatToRawIntBits(frameRadii.z * scale), Float.floatToRawIntBits(frameRadii.w * scale))
                .misc(FORMAT.getElement("ShapeThickness"), Float.floatToRawIntBits(thickness * scale))
                .misc(FORMAT.getElement("ShapeBlur"), Float.floatToRawIntBits(2));
    }
    @Override public RenderPipeline pipeline() { return PIPELINE; }
    @Override public TextureSetup textureSetup() { return TextureSetup.noTexture(); }
    @Override public ScreenRectangle scissorArea() { return scissor; }
    @Override public ScreenRectangle bounds() { return bounds; }
}
