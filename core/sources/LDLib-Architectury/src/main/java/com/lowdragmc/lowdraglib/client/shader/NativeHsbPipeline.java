package com.lowdragmc.lowdraglib.client.shader;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.resources.Identifier;

/** Native HSB interpolation; GUI submission must supply projection and dynamic transforms. */
public final class NativeHsbPipeline {
    private NativeHsbPipeline() {}
    public static final RenderPipeline PIPELINE = RenderPipeline.builder()
            .withLocation(Identifier.parse("ldlib:pipeline/hsb_block"))
            .withVertexShader(Identifier.parse("ldlib:core/hsb_block_native"))
            .withFragmentShader(Identifier.parse("ldlib:core/hsb_block_native"))
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withVertexBinding(0, NativeShaderFormats.HSB)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .build();
}
