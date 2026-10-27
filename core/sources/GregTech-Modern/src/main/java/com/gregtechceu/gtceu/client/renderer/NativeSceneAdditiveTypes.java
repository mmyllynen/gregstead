package com.gregtechceu.gtceu.client.renderer;

import com.mojang.renderpearl.api.pipeline.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

/** Neighbor blocks retain their native alpha-test/light/depth pipeline with ONE + ONE blending. */
public final class NativeSceneAdditiveTypes {
    private NativeSceneAdditiveTypes() {}
    private static RenderType create(String name,RenderPipeline base) {
        var pipeline=base.toBuilder().withLocation(Identifier.parse("gtceu:pipeline/scene_additive_"+name))
                .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.ONE,BlendFactor.ONE))).build();
        var setup=RenderSetup.builder(pipeline).useLightmap().affectsCrumbling()
                .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                .withTexture("Sampler0",TextureAtlas.LOCATION_BLOCKS, () ->
                        com.mojang.blaze3d.systems.RenderSystem.getSamplerCache().getSampler(
                                com.mojang.renderpearl.api.textures.AddressMode.CLAMP_TO_EDGE,
                                com.mojang.renderpearl.api.textures.AddressMode.CLAMP_TO_EDGE,
                                com.mojang.renderpearl.api.textures.FilterMode.LINEAR,
                                com.mojang.renderpearl.api.textures.FilterMode.NEAREST,true));
        if(name.equals("translucent"))setup.sortOnUpload();
        return RenderType.create("gtceu_scene_additive_"+name,setup.createRenderSetup());
    }
    private static final RenderType SOLID=create("solid",RenderPipelines.SOLID_BLOCK);
    private static final RenderType CUTOUT=create("cutout",RenderPipelines.CUTOUT_BLOCK);
    private static final RenderType TRANSLUCENT=create("translucent",RenderPipelines.TRANSLUCENT_BLOCK);
    public static RenderType map(RenderType original) {
        if(original==RenderTypes.solidMovingBlock())return SOLID;
        if(original==RenderTypes.cutoutMovingBlock())return CUTOUT;
        if(original==RenderTypes.translucentMovingBlock())return TRANSLUCENT;
        return original;
    }
}
