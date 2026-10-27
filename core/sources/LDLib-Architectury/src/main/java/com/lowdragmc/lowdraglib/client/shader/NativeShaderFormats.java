package com.lowdragmc.lowdraglib.client.shader;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;

/** Preserve HSB floats as floats; converting them to packed RGB would change interpolation. */
public final class NativeShaderFormats {
    private NativeShaderFormats() {}
    public static final VertexFormat HSB = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT)
            .addAttribute("HSB_ALPHA", GpuFormat.RGBA32_FLOAT)
            .build();
}
