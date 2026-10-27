package com.gregtechceu.gtceu.integration.map.journeymap;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

/** Builds a separately owned marker image from borrowed native atlas sprites, in ARGB throughout. */
public final class NativeOreMarkerImage {
    private NativeOreMarkerImage() {}
    public static NativeImage create(TextureAtlasSprite base, int color,
                                     @Nullable TextureAtlasSprite secondary, int secondaryColor) {
        var result = new NativeImage(NativeImage.Format.RGBA, base.contents().width(), base.contents().height(), false);
        try {
            for (int x = 0; x < result.getWidth(); x++) {
                for (int y = 0; y < result.getHeight(); y++) {
                    int pixel = ARGB.multiply(base.getPixelARGB(0, x, y), color);
                    if (secondary != null) pixel = ARGB.alphaBlend(pixel,
                            ARGB.multiply(secondary.getPixelARGB(0, x, y), secondaryColor));
                    result.setPixel(x, y, ARGB.alpha(pixel) == 0 ? pixel : ARGB.opaque(pixel));
                }
            }
            return result;
        } catch (RuntimeException | Error failure) {
            result.close();
            throw failure;
        }
    }
}
