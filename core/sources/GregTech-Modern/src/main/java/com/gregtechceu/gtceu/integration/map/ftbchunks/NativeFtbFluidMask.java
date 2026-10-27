package com.gregtechceu.gtceu.integration.map.ftbchunks;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.pipeline.*;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

/** Independently interpolated fluid UV, ARGB tint and map-mask UV in the native GUI queue. */
public record NativeFtbFluidMask(TextureSetup textureSetup, Matrix3x2f pose, float centerX, float centerY,
                                 float width, float rotation, float mapSize, float u0, float u1,
                                 float v0, float v1, int color, boolean square,
                                 ScreenRectangle scissorArea) implements GuiElementRenderState {
    public static final VertexFormat FORMAT = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT).addAttribute("UV0", GpuFormat.RG32_FLOAT)
            .addAttribute("Color", GpuFormat.RGBA8_UNORM).addAttribute("MaskUV", GpuFormat.RG32_FLOAT).build();
    public static final RenderPipeline CIRCLE = pipeline(false);
    public static final RenderPipeline SQUARE = pipeline(true);
    public NativeFtbFluidMask { pose = new Matrix3x2f(pose); }
    private static RenderPipeline pipeline(boolean square) {
        var builder = RenderPipeline.builder().withLocation(Identifier.parse("gtceu:pipeline/ftb_fluid_" + (square ? "square" : "circle")))
                .withVertexShader(Identifier.parse("gtceu:core/ftb_fluid_mask"))
                .withFragmentShader(Identifier.parse("gtceu:core/ftb_fluid_mask"))
                .withBindGroupLayout(BindGroupLayouts.PROJECTION).withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER1).withVertexBinding(0, FORMAT)
                .withPrimitiveTopology(PrimitiveTopology.QUADS).withCull(false).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT));
        if (square) builder.withShaderDefine("SQUARE_MASK");
        return builder.build();
    }
    @Override public RenderPipeline pipeline() { return square ? SQUARE : CIRCLE; }
    @Override public ScreenRectangle bounds() {
        float radius = width * 0.707107F;
        return new ScreenRectangle((int)Math.floor(centerX-radius),(int)Math.floor(centerY-radius),
                (int)Math.ceil(radius*2)+1,(int)Math.ceil(radius*2)+1).transformMaxBounds(pose);
    }
    @Override public void buildVertices(VertexConsumer output) {
        float[] xs = {-0.5F,-0.5F,0.5F,0.5F}, ys = {-0.5F,0.5F,0.5F,-0.5F};
        float cos=(float)Math.cos(rotation), sin=(float)Math.sin(rotation);
        for(int i=0;i<4;i++) {
            float x=centerX+width*(cos*xs[i]-sin*ys[i]), y=centerY+width*(sin*xs[i]+cos*ys[i]);
            var point=pose.transformPosition(x,y,new Vector2f());
            output.addVertex(point.x,point.y,0).setUv(i<2?u0:u1,i==0||i==3?v0:v1).setColor(color)
                    .misc(FORMAT.getElement("MaskUV"),Float.floatToRawIntBits((cos*x+sin*y)/mapSize+0.5F),Float.floatToRawIntBits((-sin*x+cos*y)/mapSize+0.5F));
        }
    }
}
