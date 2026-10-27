package com.lowdragmc.lowdraglib.transfer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Vector4f;

/** Queued floating-point textured and gradient primitives. */
public final class GuiPrimitives {
    private GuiPrimitives() {}
    public static void textured(GuiGraphicsExtractor graphics, Identifier texture, float x0, float y0, float x1, float y1,
                                float u0, float v0, float u1, float v1, float depth, int color) {
        textured(graphics,RenderPipelines.GUI_TEXTURED,texture,x0,y0,x1,y1,u0,v0,u1,v1,depth,color);
    }
    public static void textured(GuiGraphicsExtractor graphics,com.mojang.renderpearl.api.pipeline.RenderPipeline pipeline,
            Identifier texture,float x0,float y0,float x1,float y1,float u0,float v0,float u1,float v1,float depth,int color) {
        var image = Minecraft.getInstance().getTextureManager().getTexture(texture);
        graphics.submitGuiElementRenderState(new GuiQuadState(pipeline,
                TextureSetup.singleTexture(image.getTextureView(), image.getSampler()), new Matrix3x2f(graphics.pose()),
                new GuiQuadState.Vertex[] {
                        new GuiQuadState.Vertex(x0, y0, u0, v0, color), new GuiQuadState.Vertex(x0, y1, u0, v1, color),
                        new GuiQuadState.Vertex(x1, y1, u1, v1, color), new GuiQuadState.Vertex(x1, y0, u1, v0, color)
                }, depth, true, graphics.peekScissorStack()));
    }
    public static void missingTexture(GuiGraphicsExtractor graphics, float x, float y, float width, float height) {
        var atlas = net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;
        var sprite = graphics.getSprite(new net.minecraft.client.resources.model.sprite.SpriteId(atlas,
                net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation()));
        textured(graphics, atlas, x, y, x + width, y + height,
                sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), 0, -1);
    }
    public static void gradient(GuiGraphicsExtractor graphics, float x, float y, float width, float height,
                                int startColor, int endColor, boolean horizontal) {
        graphics.submitGuiElementRenderState(gradientState(new Matrix3x2f(graphics.pose()), graphics.peekScissorStack(),
                x, y, width, height, startColor, endColor, horizontal));
    }
    public static GuiQuadState gradientState(Matrix3x2f pose, net.minecraft.client.gui.navigation.ScreenRectangle scissor,
                                             float x, float y, float width, float height,
                                             int startColor, int endColor, boolean horizontal) {
        return new GuiQuadState(RenderPipelines.GUI, TextureSetup.noTexture(), pose, new GuiQuadState.Vertex[] {
                        new GuiQuadState.Vertex(x, y, 0, 0, startColor),
                        new GuiQuadState.Vertex(x, y + height, 0, 0, horizontal ? startColor : endColor),
                        new GuiQuadState.Vertex(x + width, y + height, 0, 0, endColor),
                        new GuiQuadState.Vertex(x + width, y, 0, 0, horizontal ? endColor : startColor)
                }, 0, false, scissor);
    }
    public static GuiQuadState triangleState(Matrix3x2f pose, net.minecraft.client.gui.navigation.ScreenRectangle scissor,
                                             GuiQuadState.Vertex first, GuiQuadState.Vertex second, GuiQuadState.Vertex third) {
        // The native GUI batch uses quads: a repeated first vertex makes the second triangle degenerate.
        return new GuiQuadState(RenderPipelines.GUI, TextureSetup.noTexture(), pose,
                new GuiQuadState.Vertex[] { first, second, third, first }, 0, false, scissor);
    }
    public static void shadowCorner(GuiGraphicsExtractor graphics, float x, float y, float distance, int color) {
        for (var state : shadowCornerStates(new Matrix3x2f(graphics.pose()), graphics.peekScissorStack(), x, y, distance, color)) {
            graphics.submitGuiElementRenderState(state);
        }
    }
    public static java.util.List<GuiQuadState> shadowCornerStates(Matrix3x2f pose,
            net.minecraft.client.gui.navigation.ScreenRectangle scissor, float x, float y, float distance, int color) {
        var origin = new GuiQuadState.Vertex(x, y, 0, 0, color);
        var bottom = new GuiQuadState.Vertex(x, y + distance, 0, 0, 0);
        var outer = new GuiQuadState.Vertex(x + distance, y + distance, 0, 0, 0);
        var right = new GuiQuadState.Vertex(x + distance, y, 0, 0, 0);
        return java.util.List.of(triangleState(pose, scissor, origin, bottom, outer),
                triangleState(pose, scissor, origin, outer, right));
    }
    public static void lines(GuiGraphicsExtractor graphics, java.util.List<net.minecraft.world.phys.Vec2> points,
                             int startColor, int endColor, float halfWidth) {
        for (var state : lineStates(new Matrix3x2f(graphics.pose()), graphics.peekScissorStack(), points,
                startColor, endColor, halfWidth)) graphics.submitGuiElementRenderState(state);
    }
    public static java.util.List<GuiQuadState> lineStates(Matrix3x2f pose,
            net.minecraft.client.gui.navigation.ScreenRectangle scissor, java.util.List<net.minecraft.world.phys.Vec2> source,
            int startColor, int endColor, float halfWidth) {
        var points = new java.util.ArrayList<net.minecraft.world.phys.Vec2>();
        for (var point : source) {
            if (points.isEmpty() || points.getLast().x != point.x || points.getLast().y != point.y) points.add(point);
        }
        if (points.size() < 2 || halfWidth == 0) return java.util.List.of();
        var strip = new java.util.ArrayList<GuiQuadState.Vertex>();
        org.joml.Vector3f normal = null;
        for (int i = 1; i < points.size(); i++) {
            var previous = points.get(i - 1);
            var point = points.get(i);
            normal = new org.joml.Vector3f(point.x - previous.x, point.y - previous.y, 0)
                    .rotateZ((float) (Math.PI / 2)).normalize().mul(-halfWidth);
            strip.add(new GuiQuadState.Vertex(previous.x + normal.x, previous.y + normal.y, 0, 0,
                    interpolateColor(startColor, endColor, (i - 1f) / points.size())));
            strip.add(new GuiQuadState.Vertex(previous.x - normal.x, previous.y - normal.y, 0, 0,
                    interpolateColor(startColor, endColor, i * 1f / points.size())));
        }
        var last = points.getLast();
        strip.add(new GuiQuadState.Vertex(last.x + normal.x, last.y + normal.y, 0, 0, endColor));
        strip.add(new GuiQuadState.Vertex(last.x - normal.x, last.y - normal.y, 0, 0, endColor));
        var states = new java.util.ArrayList<GuiQuadState>();
        for (int i = 0; i < strip.size() - 2; i++) {
            states.add(triangleState(pose, scissor, strip.get(i + (i % 2)), strip.get(i + 1 - (i % 2)), strip.get(i + 2)));
        }
        return java.util.List.copyOf(states);
    }
    private static int interpolateColor(int first, int second, float fraction) {
        int result = 0;
        for (int shift : new int[] { 24, 16, 8, 0 }) {
            int start = (first >>> shift) & 255;
            int end = (second >>> shift) & 255;
            result |= ((int) (start + (end - start) * fraction) & 255) << shift;
        }
        return result;
    }

    public static void roundedBox(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom,
                                   org.joml.Vector4f radii, int color) {
        float guiScale = (float) Minecraft.getInstance().getWindow().getGuiScale();
        graphics.submitGuiElementRenderState(roundedBoxState(new Matrix3x2f(graphics.pose()), graphics.peekScissorStack(),
                left, top, right, bottom, radii, color, guiScale));
    }
    public static GuiRoundedBoxState roundedBoxState(Matrix3x2f pose,
            net.minecraft.client.gui.navigation.ScreenRectangle scissor, float left, float top, float right, float bottom,
            org.joml.Vector4f radii, int color, float guiScale) {
        // Retain upstream's baked transformed endpoints, -0.25 GUI offset and +2 physical-pixel SDF center offset.
        var first = pose.transformPosition(left - 0.25f, top - 0.25f, new org.joml.Vector2f());
        var second = pose.transformPosition(right - 0.25f, bottom - 0.25f, new org.joml.Vector2f());
        float scaleX = pose.m00() + pose.m10();
        float shift = 2 / guiScale;
        return new GuiRoundedBoxState(first.x + shift, first.y + shift, second.x + shift, second.y + shift,
                new org.joml.Vector4f(radii).mul(scaleX), color, guiScale, scissor);
    }

    public static void roundedFrame(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom,
                                     Vector4f radii, Vector4f frameRadii, float thickness, int color) {
        graphics.submitGuiElementRenderState(roundedFrameState(new Matrix3x2f(graphics.pose()), graphics.peekScissorStack(),
                left, top, right, bottom, radii, frameRadii, thickness, color,
                (float) Minecraft.getInstance().getWindow().getGuiScale()));
    }
    public static GuiRoundedFrameState roundedFrameState(Matrix3x2f pose,
            net.minecraft.client.gui.navigation.ScreenRectangle scissor, float left, float top, float right, float bottom,
            Vector4f radii, Vector4f frameRadii, float thickness, int color, float guiScale) {
        var first = pose.transformPosition(left - 0.25f, top - 0.25f, new org.joml.Vector2f());
        var second = pose.transformPosition(right - 0.25f, bottom - 0.25f, new org.joml.Vector2f());
        float scaleX = pose.m00() + pose.m10();
        float shift = 2 / guiScale;
        return new GuiRoundedFrameState(first.x + shift, first.y + shift, second.x + shift, second.y + shift,
                new Vector4f(radii).mul(scaleX), new Vector4f(frameRadii).mul(scaleX), thickness * scaleX,
                color, guiScale, scissor);
    }

    public static void progressBox(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom,
                                    Vector4f radii, int firstColor, int secondColor, float progress) {
        graphics.submitGuiElementRenderState(progressBoxState(new Matrix3x2f(graphics.pose()), graphics.peekScissorStack(),
                left, top, right, bottom, radii, firstColor, secondColor, progress,
                (float) Minecraft.getInstance().getWindow().getGuiScale()));
    }
    public static GuiProgressBoxState progressBoxState(Matrix3x2f pose,
            net.minecraft.client.gui.navigation.ScreenRectangle scissor, float left, float top, float right, float bottom,
            Vector4f radii, int firstColor, int secondColor, float progress, float guiScale) {
        float shift = 2 / guiScale;
        return new GuiProgressBoxState(left + shift, top + shift, right + shift, bottom + shift,
                radii, firstColor, secondColor, progress, pose, guiScale, scissor);
    }

    public static void circle(GuiGraphicsExtractor graphics, float x, float y, float radius, int color) {
        graphics.submitGuiElementRenderState(new GuiCircleState(new Matrix3x2f(graphics.pose()),
                graphics.peekScissorStack(), x, y, radius, color));
    }

    public static void roundLine(GuiGraphicsExtractor graphics, float x0, float y0, float x1, float y1,
                                  float width, int firstColor, int secondColor) {
        graphics.submitGuiElementRenderState(new GuiRoundLineState(new Matrix3x2f(graphics.pose()),
                graphics.peekScissorStack(), x0, y0, x1, y1, width, firstColor, secondColor));
    }

    public static void panel(GuiGraphicsExtractor graphics) {
        var window = Minecraft.getInstance().getWindow();
        graphics.submitGuiElementRenderState(new GuiPanelState(new Matrix3x2f(graphics.pose()), graphics.peekScissorStack(),
                graphics.guiWidth(), graphics.guiHeight(), window.getWidth() / (float) window.getHeight()));
    }

}
