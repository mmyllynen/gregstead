package com.gregtechceu.gtceu.core.mixins.ftbchunks;

import com.gregtechceu.gtceu.integration.map.ftbchunks.NativeFtbFluidMask;
import com.gregtechceu.gtceu.integration.map.ftbchunks.veins.fluid.FluidVeinIcon;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftbchunks.api.client.icon.*;
import dev.ftb.mods.ftbchunks.api.client.minimap.MinimapRenderContext;
import dev.ftb.mods.ftbchunks.client.minimap.layers.IconLayerRenderer;
import dev.ftb.mods.ftbchunks.client.minimap.layers.TerrainLayerRenderer;
import dev.ftb.mods.ftbchunks.config.FTBChunksClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value=IconLayerRenderer.class,remap=false)
public class FTBChunksClientMixin {
    @Unique private final ThreadLocal<Matrix3x2f> gtceu$mapPose = new ThreadLocal<>();
    @WrapMethod(method="extractLayer")
    private void gtceu$layer(GuiGraphicsExtractor graphics, Matrix3x2fStack pose, MinimapRenderContext ctx, Operation<Void> original) {
        var previous=gtceu$mapPose.get();
        try { gtceu$mapPose.set(new Matrix3x2f(pose)); original.call(graphics,pose,ctx); }
        finally { if(previous==null)gtceu$mapPose.remove();else gtceu$mapPose.set(previous); }
    }
    @WrapOperation(method="extractLayer",at=@At(value="INVOKE",target="Ldev/ftb/mods/ftbchunks/api/client/icon/MapIcon;draw(Ldev/ftb/mods/ftbchunks/api/client/icon/MapType;Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIZI)V"))
    private void gtceu$fluid(MapIcon icon, MapType type, GuiGraphicsExtractor graphics, int x,int y,int w,int h,
                            boolean outside,int alpha,Operation<Void> original,@Local(argsOnly=true) MinimapRenderContext ctx) {
        if(!(icon instanceof FluidVeinIcon fluid)) { original.call(icon,type,graphics,x,y,w,h,outside,alpha);return; }
        if(!fluid.isEnabled())return;
        var mc=Minecraft.getInstance();
        var sprite=mc.getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS,
                com.lowdragmc.lowdraglib.transfer.client.FluidVisuals.stillTexture(fluid.getFluidInfo().fluid())));
        var texture=mc.getTextureManager().getTexture(sprite.atlasLocation());
        var mask=mc.getTextureManager().getTexture(TerrainLayerRenderer.CIRCLE_MASK);
        var textures=TextureSetup.doubleTexture(texture.getTextureView(),texture.getSampler(),mask.getTextureView(),mask.getSampler());
        var position=fluid.getPos(ctx.deltaTracker().getGameTimeDeltaPartialTick(false));
        float rotation=ctx.rotation()+(float)Math.PI;
        float dx=(float)(position.x-ctx.playerPos().x)*ctx.scale()*ctx.zoom();
        float dz=(float)(position.z-ctx.playerPos().z)*ctx.scale()*ctx.zoom();
        float cos=(float)Math.cos(rotation),sin=(float)Math.sin(rotation);
        graphics.submitGuiElementRenderState(new NativeFtbFluidMask(textures,gtceu$mapPose.get(),
                cos*dx-sin*dz,sin*dx+cos*dz,16*ctx.scale()*ctx.zoom(),rotation,ctx.size(),
                sprite.getU0(),sprite.getU1(),sprite.getV0(),sprite.getV1(),0xc8000000|(fluid.getColor()&0xffffff),
                FTBChunksClientConfig.SQUARE_MINIMAP.get(),graphics.peekScissorStack()));
    }
}
