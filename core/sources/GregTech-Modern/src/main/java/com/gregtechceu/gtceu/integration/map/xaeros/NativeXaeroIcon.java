package com.gregtechceu.gtceu.integration.map.xaeros;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import xaero.lib.client.graphics.util.ImmediateRenderUtil;

/** Per-vertex material tint uses Xaero's native immediate render target and texture bindings. */
public final class NativeXaeroIcon {
    private NativeXaeroIcon() {}
    public static void draw(PoseStack pose,TextureAtlasSprite sprite,int x,int y,int size,int argb) {
        var texture=Minecraft.getInstance().getTextureManager().getTexture(sprite.atlasLocation());
        var buffer=ImmediateRenderUtil.begin(PrimitiveTopology.QUADS,DefaultVertexFormat.POSITION_TEX_COLOR);
        var matrix=pose.last().pose();
        buffer.addVertex(matrix,x,y+size,0).setUv(sprite.getU0(),sprite.getV1()).setColor(argb);
        buffer.addVertex(matrix,x+size,y+size,0).setUv(sprite.getU1(),sprite.getV1()).setColor(argb);
        buffer.addVertex(matrix,x+size,y,0).setUv(sprite.getU1(),sprite.getV0()).setColor(argb);
        buffer.addVertex(matrix,x,y,0).setUv(sprite.getU0(),sprite.getV0()).setColor(argb);
        ImmediateRenderUtil.drawImmediateMeshData(buffer.buildOrThrow(),RenderPipelines.GUI_TEXTURED,
                new PreparedRenderType.Texture("Sampler0",texture.getTextureView(),texture.getSampler()));
    }
}
