package dev.emi.emi.recipe;

import com.lowdragmc.lowdraglib.transfer.client.FluidVisuals;
import com.lowdragmc.lowdraglib.transfer.client.GuiPrimitives;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.neoforge.fluids.FluidStack;

/** Native queued fluid sprite with the original sixteen-pixel tile crop and opaque vertex tint. */
public final class NativeFluidSprite {
    private NativeFluidSprite() {}
    public static void draw(GuiGraphicsExtractor graphics,FluidStack stack,int x,int y,int xOff,int yOff,int width,int height,float depth) {
        if(width<=0 || height<=0)return;
        var sprite=FluidVisuals.stillSprite(stack);
        if(sprite==null)return;
        draw(graphics, sprite, FluidVisuals.color(stack), x, y, xOff, yOff, width, height, depth);
    }
    public static void draw(GuiGraphicsExtractor graphics, net.minecraft.client.renderer.texture.TextureAtlasSprite sprite,
                            int tint,int x,int y,int xOff,int yOff,int width,int height,float depth) {
        if(sprite==null || width<=0 || height<=0)return;
        // The original immediate helper used RGB/256, then packed float channels to bytes; alpha was one.
        int color=0xff000000 | (((tint>>>16)&255)*255/256)<<16 | (((tint>>>8)&255)*255/256)<<8 | (tint&255)*255/256;
        GuiPrimitives.textured(graphics,sprite.atlasLocation(),x,y,x+width,y+height,
            sprite.getU(xOff/16f),sprite.getV(yOff/16f),sprite.getU((xOff+width)/16f),sprite.getV((yOff+height)/16f),depth,color);
    }
}
