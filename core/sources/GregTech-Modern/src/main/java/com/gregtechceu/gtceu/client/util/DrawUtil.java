package com.gregtechceu.gtceu.client.util;

import com.lowdragmc.lowdraglib.transfer.client.GuiQuadState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;

public class DrawUtil {

    /**
     * Fills a rectangle with a gradient color from colorFrom to colorTo at the specified z-level using the given render
     * type and coordinates as the boundaries.
     *
     * @param y2         the y-coordinate of the second corner of the rectangle.
     * @param x2         the x-coordinate of the second corner of the rectangle.
     * @param y1         the y-coordinate of the first corner of the rectangle.
     * @param x1         the x-coordinate of the first corner of the rectangle.
     * @param renderType the render type to use.
     * @param z          the z-level of the rectangle.
     * @param colorTo    the ending color of the gradient.
     * @param colorFrom  the starting color of the gradient.
     */
    public static void fillHorizontalGradient(GuiGraphicsExtractor graphics, RenderPipeline pipeline, int x1, int y1, int x2,
                                              int y2, int colorFrom, int colorTo, int z) {
        graphics.submitGuiElementRenderState(horizontalGradientState(new Matrix3x2f(graphics.pose()),
                graphics.peekScissorStack(), pipeline, x1, y1, x2, y2, colorFrom, colorTo, z));
    }

    public static GuiQuadState horizontalGradientState(Matrix3x2f pose,
            net.minecraft.client.gui.navigation.ScreenRectangle scissor, RenderPipeline pipeline,
            int x1, int y1, int x2, int y2, int colorFrom, int colorTo, int z) {
        return new GuiQuadState(pipeline, net.minecraft.client.gui.render.TextureSetup.noTexture(), pose,
                new GuiQuadState.Vertex[] {
                    new GuiQuadState.Vertex(x1, y1, 0, 0, colorFrom),
                    new GuiQuadState.Vertex(x1, y2, 0, 0, colorFrom),
                    new GuiQuadState.Vertex(x2, y2, 0, 0, colorTo),
                    new GuiQuadState.Vertex(x2, y1, 0, 0, colorTo)
                }, z, false, scissor);
    }

    /**
     * Converts an (A)RGB integer color into an array of floats, for use in GL calls
     * 
     * @return float[]{R, G, B, A}
     */
    public static float[] floats(int argb) {
        return new float[] {
                (float) (argb >> 16 & 255) / 255.0F,
                (float) (argb >> 8 & 255) / 255.0F,
                (float) (argb & 255) / 255.0F,
                (float) (argb >> 24 & 255) / 255.0F
        };
    }
}
