package com.lowdragmc.lowdraglib.client.shader;

import com.mojang.renderpearl.api.pipeline.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;
import java.util.Optional;

/** Native editor overlay pipelines; line width is carried in each native vertex. */
public final class LDLibRenderTypes {
    private LDLibRenderTypes() {}
    public static final RenderPipeline COLOR_PIPELINE=RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Identifier.parse("ldlib:pipeline/position_color_no_depth"))
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES).withCull(true).build();
    public static final RenderPipeline LINE_PIPELINE=RenderPipelines.LINES.toBuilder()
            .withLocation(Identifier.parse("ldlib:pipeline/no_depth_lines"))
            .withDepthStencilState(Optional.empty()).withCull(false).build();
    public static final RenderPipeline GROUND_PIPELINE=RenderPipelines.GUI_TEXTURED.toBuilder()
            .withLocation(Identifier.parse("ldlib:pipeline/scene_textured_plane"))
            .withDepthStencilState(DepthStencilState.DEFAULT).withCull(false).build();
    private static final java.util.Map<Identifier,RenderType> TEXTURED_PLANES=new java.util.HashMap<>();
    public static RenderType texturedPlane(Identifier texture) {
        return TEXTURED_PLANES.computeIfAbsent(texture,id->RenderType.create("ldlib_scene_plane_"+id,
                RenderSetup.builder(GROUND_PIPELINE).withTexture("Sampler0",id).createRenderSetup()));
    }
    public static final RenderPipeline FACE_OVERLAY_PIPELINE=RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Identifier.parse("ldlib:pipeline/face_overlay"))
            .withPrimitiveTopology(PrimitiveTopology.QUADS).withCull(false).build();
    public static final RenderPipeline SELECTION_PIPELINE=FACE_OVERLAY_PIPELINE.toBuilder()
            .withLocation(Identifier.parse("ldlib:pipeline/block_selection"))
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING)).withCull(true).build();
    private static final RenderType FACE_OVERLAY=RenderType.create("ldlib_face_overlay",RenderSetup.builder(FACE_OVERLAY_PIPELINE).createRenderSetup());
    private static final RenderType SELECTION=RenderType.create("ldlib_selection",RenderSetup.builder(SELECTION_PIPELINE).createRenderSetup());
    public static RenderType faceOverlay(){return FACE_OVERLAY;}
    public static RenderType selection(){return SELECTION;}
    public static final RenderPipeline TANK_FLUID_PIPELINE=RenderPipelines.GUI_TEXTURED.toBuilder()
            .withLocation(Identifier.parse("ldlib:pipeline/tank_fluid"))
            .withColorTargetState(ColorTargetState.DEFAULT).build();
    public static final RenderPipeline TANK_HOVER_PIPELINE=RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Identifier.parse("ldlib:pipeline/tank_hover"))
            .withColorTargetState(new ColorTargetState(Optional.of(BlendFunction.TRANSLUCENT),
                    com.mojang.renderpearl.api.GpuFormat.RGBA8_UNORM,ColorTargetState.WRITE_COLOR)).build();
    private static final RenderType POSITION_COLOR_NO_DEPTH=RenderType.create("ldlib_position_color_no_depth",
            RenderSetup.builder(COLOR_PIPELINE).createRenderSetup());
    private static final RenderType NO_DEPTH_LINES=RenderType.create("ldlib_no_depth_lines",
            RenderSetup.builder(LINE_PIPELINE).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup());
    public static RenderType positionColorNoDepth(){return POSITION_COLOR_NO_DEPTH;}
    public static RenderType noDepthLines(){return NO_DEPTH_LINES;}
}
