package com.lowdragmc.lowdraglib.client.scene;

import com.lowdragmc.lowdraglib.transfer.client.GuiQuadState;
import com.lowdragmc.lowdraglib.transfer.client.GuiPrimitives;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix3x2f;
import java.util.*;

/** Retains the three original two-point strips, including triangles connecting their end sections. */
public final class NativeGraphEdges {
    private NativeGraphEdges() {}
    public static void draw(GuiGraphicsExtractor graphics,Vec2 output,Vec2 input,int start,int end) {
        for(var state:states(new Matrix3x2f(graphics.pose()),graphics.peekScissorStack(),output,input,start,end))
            graphics.submitGuiElementRenderState(state);
    }
    public static List<GuiQuadState> states(Matrix3x2f pose,ScreenRectangle scissor,Vec2 output,Vec2 input,int start,int end) {
        var a=output.add(new Vec2(20,0));var b=input.add(new Vec2(-20,0));
        var strip=new ArrayList<GuiQuadState.Vertex>();
        segment(strip,output,a,start,start);segment(strip,a,b,start,end);segment(strip,b,input,end,end);
        var states=new ArrayList<GuiQuadState>();
        for(int i=0;i+2<strip.size();i++)states.add(GuiPrimitives.triangleState(pose,scissor,
                strip.get(i+i%2),strip.get(i+1-i%2),strip.get(i+2)));
        return List.copyOf(states);
    }
    private static void segment(List<GuiQuadState.Vertex> output,Vec2 a,Vec2 b,int start,int end) {
        float dx=b.x-a.x,dy=b.y-a.y,length=(float)Math.hypot(dx,dy);
        if(length==0)return;
        float nx=dy/length*0.75f,ny=-dx/length*0.75f;
        output.add(v(a.x+nx,a.y+ny,start));output.add(v(a.x-nx,a.y-ny,midpoint(start,end)));
        output.add(v(b.x+nx,b.y+ny,end));output.add(v(b.x-nx,b.y-ny,end));
    }
    private static GuiQuadState.Vertex v(float x,float y,int color){return new GuiQuadState.Vertex(x,y,0,0,color);}
    private static int midpoint(int a,int b){int result=0;for(int s:new int[]{0,8,16,24})result|=((((a>>>s)&255)+((b>>>s)&255))/2)<<s;return result;}
}
