package com.lowdragmc.lowdraglib.client.shader;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.*;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

/** Original two-point triangle strip captured for native deferred GUI submission. */
public final class NativeCompassLineState implements GuiElementRenderState {
    public static final VertexFormat FORMAT = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT)
            .addAttribute("Color", GpuFormat.RGBA8_UNORM)
            .addAttribute("UV0", GpuFormat.RG32_FLOAT)
            .addAttribute("CompassTime", GpuFormat.R32_FLOAT).build();
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Identifier.parse("ldlib:pipeline/compass_line"))
            .withVertexShader(Identifier.parse("ldlib:core/compass_line_native"))
            .withFragmentShader(Identifier.parse("ldlib:core/compass_line_native"))
            .withVertexBinding(0, FORMAT)
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withCull(false).build();
    private record Vertex(float x, float y, float u, float v, int color) {}
    private final Matrix3x2f pose;
    private final ScreenRectangle scissor, bounds;
    private final Vertex[] vertices;
    private final float time;
    public NativeCompassLineState(Matrix3x2f pose, ScreenRectangle scissor,
            float x0, float y0, float x1, float y1, float width, int start, int end, float time) {
        this.pose = new Matrix3x2f(pose); this.scissor = scissor; this.time = time;
        float dx=x1-x0, dy=y1-y0, length=(float)Math.hypot(dx,dy);
        if (!(length > 0)) { vertices=new Vertex[0]; bounds=null; return; }
        float nx=dy/length*width, ny=-dx/length*width;
        vertices=new Vertex[] {
                new Vertex(x0+nx,y0+ny,0,0,start), new Vertex(x0-nx,y0-ny,0,1,midpoint(start,end)),
                new Vertex(x1+nx,y1+ny,1,0,end), new Vertex(x1-nx,y1-ny,1,1,end) };
        float minX=Float.POSITIVE_INFINITY,minY=Float.POSITIVE_INFINITY;
        float maxX=Float.NEGATIVE_INFINITY,maxY=Float.NEGATIVE_INFINITY;
        for(var vertex:vertices) {
            var point=this.pose.transformPosition(vertex.x(),vertex.y(),new Vector2f());
            minX=Math.min(minX,point.x);minY=Math.min(minY,point.y);
            maxX=Math.max(maxX,point.x);maxY=Math.max(maxY,point.y);
        }
        var area=new ScreenRectangle((int)Math.floor(minX),(int)Math.floor(minY),
                (int)Math.ceil(maxX)-(int)Math.floor(minX),(int)Math.ceil(maxY)-(int)Math.floor(minY));
        bounds=scissor==null?area:scissor.intersection(area);
    }
    private static int midpoint(int first,int second) {
        int result=0;
        for(int shift:new int[]{0,8,16,24}) result|=((((first>>>shift)&255)+((second>>>shift)&255))/2)<<shift;
        return result;
    }
    public static void submit(GuiGraphicsExtractor graphics,float x0,float y0,float x1,float y1,
            float width,int start,int end,float time) {
        graphics.submitGuiElementRenderState(new NativeCompassLineState(new Matrix3x2f(graphics.pose()),
                graphics.peekScissorStack(),x0,y0,x1,y1,width,start,end,time));
    }
    @Override public void buildVertices(VertexConsumer consumer) {
        if(vertices.length==0)return;
        // Degenerate fourth vertices retain exactly the strip's two original triangles.
        for(int index:new int[]{0,1,2,0,2,1,3,2}) {
            var vertex=vertices[index];var point=pose.transformPosition(vertex.x(),vertex.y(),new Vector2f());
            consumer.addVertex(point.x,point.y,0).setColor(vertex.color()).setUv(vertex.u(),vertex.v())
                    .misc(FORMAT.getElement("CompassTime"),Float.floatToRawIntBits(time));
        }
    }
    @Override public RenderPipeline pipeline(){return PIPELINE;}
    @Override public TextureSetup textureSetup(){return TextureSetup.noTexture();}
    @Override public ScreenRectangle scissorArea(){return scissor;}
    @Override public ScreenRectangle bounds(){return bounds;}
}
