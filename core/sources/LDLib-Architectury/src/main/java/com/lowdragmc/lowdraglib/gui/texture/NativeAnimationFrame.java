package com.lowdragmc.lowdraglib.gui.texture;

import com.lowdragmc.lowdraglib.transfer.client.GuiQuadState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import org.joml.Matrix3x2f;

/** Sprite-sheet cell geometry retaining the original row-major UVs and tint. */
public final class NativeAnimationFrame {
    private NativeAnimationFrame() {}
    public static void draw(GuiGraphicsExtractor graphics, Identifier texture, int cellSize, int frame,
                            float x, float y, float width, float height, int color) {
        var image = Minecraft.getInstance().getTextureManager().getTexture(texture);
        graphics.submitGuiElementRenderState(state(new Matrix3x2f(graphics.pose()), graphics.peekScissorStack(),
                TextureSetup.singleTexture(image.getTextureView(), image.getSampler()), cellSize, frame, x, y, width, height, color));
    }
    public static GuiQuadState state(Matrix3x2f pose, ScreenRectangle scissor, TextureSetup texture,
                                     int cellSize, int frame, float x, float y, float width, float height, int color) {
        float cell = 1f / cellSize;
        float u = (frame % cellSize) * cell, v = (frame / cellSize) * cell;
        return new GuiQuadState(RenderPipelines.GUI_TEXTURED, texture, pose, new GuiQuadState.Vertex[] {
                new GuiQuadState.Vertex(x, y + height, u, v + cell, color),
                new GuiQuadState.Vertex(x + width, y + height, u + cell, v + cell, color),
                new GuiQuadState.Vertex(x + width, y, u + cell, v, color),
                new GuiQuadState.Vertex(x, y, u, v, color)
        }, 0, true, scissor);
    }
}
