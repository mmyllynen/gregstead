package com.lowdragmc.lowdraglib.client.scene;
import com.lowdragmc.lowdraglib.client.shader.LDLibRenderTypes;
import com.lowdragmc.lowdraglib.transfer.client.GuiQuadState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import org.joml.Matrix3x2f;
public final class NativeTankHover {
    private NativeTankHover() {}
    public static void draw(GuiGraphicsExtractor graphics,int x,int y,int width,int height) {
        draw(graphics, x, y, width, height, 0x80ffffff);
    }
    public static void draw(GuiGraphicsExtractor graphics,int x,int y,int width,int height,int color) {
        graphics.submitGuiElementRenderState(new GuiQuadState(LDLibRenderTypes.TANK_HOVER_PIPELINE,TextureSetup.noTexture(),
                new Matrix3x2f(graphics.pose()),new GuiQuadState.Vertex[]{
                    new GuiQuadState.Vertex(x,y,0,0,color),new GuiQuadState.Vertex(x,y+height,0,0,color),
                    new GuiQuadState.Vertex(x+width,y+height,0,0,color),new GuiQuadState.Vertex(x+width,y,0,0,color)},0,false,graphics.peekScissorStack()));
    }
}
