package com.lowdragmc.lowdraglib.client.scene;

import com.lowdragmc.lowdraglib.transfer.client.GuiPrimitives;
import com.lowdragmc.lowdraglib.transfer.client.GuiQuadState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Matrix3x2f;
import java.util.*;
import java.util.function.DoubleToIntFunction;

/** Original checker cells and one-column gradient sampling, extracted as immutable GUI quads. */
public final class NativeGradientBar {
    private NativeGradientBar() {}
    public static List<GuiQuadState> states(Matrix3x2f pose,ScreenRectangle scissor,int x,int y,int width,int gray,DoubleToIntFunction color) {
        if(width<=0)return List.of();
        var states=new ArrayList<GuiQuadState>();int parity=0;
        for(int dx=0;dx<width;dx+=3)for(int dy=0;dy<15;dy+=3) {
            int cell=parity++%2==0?-1:gray;
            states.add(GuiPrimitives.gradientState(pose,scissor,x+dx,y+dy,3,3,cell,cell,true));
        }
        for(int i=0;i<width;i++) {
            int start=color.applyAsInt(i/(float)width),end=color.applyAsInt((i+1f)/width);
            states.add(GuiPrimitives.gradientState(pose,scissor,x+i,y,1,15,start,end,true));
        }
        return List.copyOf(states);
    }
    public static void draw(GuiGraphicsExtractor graphics,int x,int y,int width,int gray,DoubleToIntFunction color) {
        for(var state:states(new Matrix3x2f(graphics.pose()),graphics.peekScissorStack(),x,y,width,gray,color))
            graphics.submitGuiElementRenderState(state);
    }
}
