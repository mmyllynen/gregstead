package com.gregtechceu.gtceu.client.renderer;
import com.mojang.renderpearl.api.pipeline.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;
import java.util.concurrent.ConcurrentHashMap;
/** Native world pipelines retain original topology, alpha blending and no-cull policies. */
public final class GTRenderTypes {
    private GTRenderTypes() {}
    public static final RenderPipeline LIGHT_RING_PIPELINE=RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.parse("gtceu:pipeline/light_ring"))
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP).withDepthStencilState(DepthStencilState.DEFAULT).withCull(false).build();
    public static final RenderPipeline MONITOR_PIPELINE=LIGHT_RING_PIPELINE.toBuilder()
            .withLocation(Identifier.parse("gtceu:pipeline/central_monitor")).withPrimitiveTopology(PrimitiveTopology.QUADS).build();
    public static final RenderPipeline GUI_TEXTURE_PIPELINE=RenderPipeline.builder(RenderPipelines.WORLD_TEXT_SNIPPET)
            .withLocation(Identifier.parse("gtceu:pipeline/gui_texture"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withCull(true).build();
    public static final RenderPipeline HIGHLIGHT_TEXTURE_PIPELINE=GUI_TEXTURE_PIPELINE.toBuilder()
            .withLocation(Identifier.parse("gtceu:pipeline/highlight_texture"))
            .withDepthStencilState(java.util.Optional.empty()).withCull(false).build();
    private static final java.util.Map<Identifier,RenderType> HIGHLIGHTS=new ConcurrentHashMap<>();
    public static RenderType highlightTexture(Identifier id){return HIGHLIGHTS.computeIfAbsent(id,key->RenderType.create("gtceu_highlight_"+key,
            RenderSetup.builder(HIGHLIGHT_TEXTURE_PIPELINE).withTexture("Sampler0",key).useLightmap().createRenderSetup()));}
    private static final RenderType LIGHT_RING=RenderType.create("gtceu_light_ring",RenderSetup.builder(LIGHT_RING_PIPELINE).createRenderSetup());
    private static final RenderType MONITOR=RenderType.create("gtceu_central_monitor",RenderSetup.builder(MONITOR_PIPELINE).createRenderSetup());
    private static final java.util.Map<Identifier,RenderType> TEXTURES=new ConcurrentHashMap<>();
    public static RenderType getLightRing(){return LIGHT_RING;}
    public static RenderType getMonitor(){return MONITOR;}
    public static RenderType guiTexture(Identifier id){return TEXTURES.computeIfAbsent(id,key->RenderType.create("gtceu_gui_"+key,
            RenderSetup.builder(GUI_TEXTURE_PIPELINE).withTexture("Sampler0",key).useLightmap().sortOnUpload().createRenderSetup()));}
}
