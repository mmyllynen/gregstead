package com.lowdragmc.lowdraglib.gui.util;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.client.utils.RenderBufferUtils;
import com.lowdragmc.lowdraglib.utils.FluidHelper;
import com.lowdragmc.lowdraglib.utils.ColorUtils;
import com.lowdragmc.lowdraglib.math.Position;
import com.lowdragmc.lowdraglib.math.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class DrawerHelper {

    public static void init() {
        com.lowdragmc.lowdraglib.transfer.client.GuiBuiltInPipelines.initialize();
    }

    public static void drawFluidTexture(@Nonnull GuiGraphicsExtractor graphics, float xCoord, float yCoord, TextureAtlasSprite textureSprite, float maskTop, float maskRight, float zLevel, int fluidColor) {
        drawFluidTexture(graphics,xCoord,yCoord,textureSprite,maskTop,maskRight,zLevel,fluidColor,net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED);
    }
    public static void drawFluidTexture(@Nonnull GuiGraphicsExtractor graphics,float xCoord,float yCoord,TextureAtlasSprite textureSprite,
            float maskTop,float maskRight,float zLevel,int fluidColor,com.mojang.renderpearl.api.pipeline.RenderPipeline pipeline) {
        float uMin = textureSprite.getU0();
        float uMax = textureSprite.getU1();
        float vMin = textureSprite.getV0();
        float vMax = textureSprite.getV1();
        uMax = uMax - maskRight / 16f * (uMax - uMin);
        vMax = vMax - maskTop / 16f * (vMax - vMin);

        com.lowdragmc.lowdraglib.transfer.client.GuiPrimitives.textured(graphics, pipeline, textureSprite.atlasLocation(),
                xCoord, yCoord + maskTop, xCoord + 16 - maskRight, yCoord + 16,
                uMin, vMin, uMax, vMax, zLevel, fluidColor);
    }

    public static void drawFluidForGui(@Nonnull GuiGraphicsExtractor graphics, FluidStack contents, float startX, float startY, float widthT, float heightT) {
        drawFluidForGui(graphics,contents,startX,startY,widthT,heightT,net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED);
    }
    public static void drawFluidForGui(@Nonnull GuiGraphicsExtractor graphics,FluidStack contents,float startX,float startY,float widthT,float heightT,
            com.mojang.renderpearl.api.pipeline.RenderPipeline pipeline) {
        TextureAtlasSprite fluidStillSprite = FluidHelper.getStillTexture(contents);
        if (fluidStillSprite == null) {
            fluidStillSprite = graphics.getSprite(new net.minecraft.client.resources.model.sprite.SpriteId(
                    net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()));
            if (Platform.isDevEnv()) {
                LDLib.LOGGER.error("Missing fluid texture for fluid: " + contents.getHoverName().getString());
            }
        }
        int fluidColor = FluidHelper.getColor(contents) | 0xff000000;

        final int xTileCount = (int) (widthT / 16);
        final float xRemainder = widthT - xTileCount * 16;
        final int yTileCount = (int) (heightT / 16);
        final float yRemainder = heightT - yTileCount * 16;

        final float yStart = startY + heightT;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                float width = xTile == xTileCount ? xRemainder : 16;
                float height = yTile == yTileCount ? yRemainder : 16;
                float x = startX + xTile * 16;
                float y = yStart - (yTile + 1) * 16;
                if (width > 0 && height > 0) {
                    float maskTop = 16 - height;
                    float maskRight = 16 - width;
                    drawFluidTexture(graphics, x, y, fluidStillSprite, maskTop, maskRight, 0, fluidColor,pipeline);
                }
            }
        }
    }

    public static void drawBorder(@Nonnull GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color, int border) {
        drawSolidRect(graphics, x - border, y - border, width + 2 * border, border, color);
        drawSolidRect(graphics, x - border, y + height, width + 2 * border, border, color);
        drawSolidRect(graphics, x - border, y, border, height, color);
        drawSolidRect(graphics, x + width, y, border, height, color);
    }

    public static void drawStringSized(@Nonnull GuiGraphicsExtractor graphics, String text, float x, float y, int color, boolean dropShadow, float scale, boolean center) {
        graphics.pose().pushMatrix();
        Font fontRenderer = Minecraft.getInstance().font;
        double scaledTextWidth = center ? fontRenderer.width(text) * scale : 0.0;
        graphics.pose().translate((float) (x - scaledTextWidth / 2.0), y);
        graphics.pose().scale(scale, scale);
        com.lowdragmc.lowdraglib.transfer.client.GuiText.drawString(graphics, fontRenderer, text, 0, 0, color, dropShadow);
        graphics.pose().popMatrix();
    }

    public static void drawStringFixedCorner(@Nonnull GuiGraphicsExtractor graphics, String text, float x, float y, int color, boolean dropShadow, float scale) {
        Font fontRenderer = Minecraft.getInstance().font;
        float scaledWidth = fontRenderer.width(text) * scale;
        float scaledHeight = fontRenderer.lineHeight * scale;
        drawStringSized(graphics, text, x - scaledWidth, y - scaledHeight, color, dropShadow, scale, false);
    }

    public static void drawText(@Nonnull GuiGraphicsExtractor graphics, String text, float x, float y, float scale, int color) {
        drawText(graphics, text, x, y, scale, color, false);
    }

    public static void drawText(@Nonnull GuiGraphicsExtractor graphics, String text, float x, float y, float scale, int color, boolean shadow) {
        Font fontRenderer = Minecraft.getInstance().font;
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
        float sf = 1 / scale;
        com.lowdragmc.lowdraglib.transfer.client.GuiText.drawString(graphics, fontRenderer, text, (int) (x * sf), (int) (y * sf), color, shadow);
        graphics.pose().popMatrix();
    }

    public static void drawItemStack(@Nonnull GuiGraphicsExtractor graphics, ItemStack itemStack, int x, int y, int color, @Nullable String altTxt) {
        com.lowdragmc.lowdraglib.transfer.client.GuiItems.draw(graphics, itemStack, x, y, color, altTxt);
    }

    public static List<Component> getItemToolTip(ItemStack itemStack) {
        Minecraft mc = Minecraft.getInstance();
        return Screen.getTooltipFromItem(mc, itemStack);
    }

    public static void drawSolidRect(@Nonnull GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
    }

    public static void drawSolidRect(@Nonnull GuiGraphicsExtractor graphics, Rect rect, int color) {
        drawSolidRect(graphics, rect.left, rect.up, rect.right - rect.left, rect.down - rect.up, color);
    }

    public static void drawRectShadow(@Nonnull GuiGraphicsExtractor graphics, int x, int y, int width, int height, int distance) {
        drawGradientRect(graphics, x + distance, y + height, width - distance, distance, 0x4f000000, 0, false);
        drawGradientRect(graphics, x + width, y + distance, distance, height - distance, 0x4f000000, 0, true);

        com.lowdragmc.lowdraglib.transfer.client.GuiPrimitives.shadowCorner(graphics,
                x + width, y + height, distance, 0x4f000000);
    }

    public static void drawGradientRect(@Nonnull GuiGraphicsExtractor graphics, int x, int y, int width, int height, int startColor, int endColor) {
        drawGradientRect(graphics, x, y, width, height, startColor, endColor, false);
    }

    public static void drawGradientRect(@Nonnull GuiGraphicsExtractor graphics, float x, float y, float width, float height, int startColor, int endColor, boolean horizontal) {
        com.lowdragmc.lowdraglib.transfer.client.GuiPrimitives.gradient(graphics, x, y, width, height,
                startColor, endColor, horizontal);
    }

    public static void drawLines(@Nonnull GuiGraphicsExtractor graphics, List<Vec2> Vec2s, int startColor, int endColor, float width) {
        com.lowdragmc.lowdraglib.transfer.client.GuiPrimitives.lines(graphics, Vec2s, startColor, endColor, width);
    }

    public static void drawTextureRect(@Nonnull GuiGraphicsExtractor graphics, Identifier texture,
                                       float x, float y, float width, float height) {
        com.lowdragmc.lowdraglib.transfer.client.GuiPrimitives.textured(graphics, texture,
                x, y, x + width, y + height, 0, 0, 1, 1, 0, -1);
    }

    public static void drawRound(@Nonnull GuiGraphicsExtractor graphics, int color, float radius, Position centerPos) {
        com.lowdragmc.lowdraglib.client.shader.NativeScreenEffects.draw(graphics,com.lowdragmc.lowdraglib.client.shader.Shaders.ROUND_F,uniform->{uniform.fillRGBAColor("Color",color);uniform.glUniform1F("StepLength",1f);uniform.glUniform1F("Radius",radius);uniform.glUniform2F("CenterPos",centerPos.x,centerPos.y);});
    }

    public static void drawPanelBg(@Nonnull GuiGraphicsExtractor graphics) {
        com.lowdragmc.lowdraglib.client.shader.NativeScreenEffects.draw(graphics,com.lowdragmc.lowdraglib.client.shader.Shaders.PANEL_BG_F,uniform->{uniform.glUniform1F("Density",5);uniform.glUniform1F("SquareSize",.1f);float bg=20f/255f,square=40f/255f;uniform.glUniform4F("BgColor",bg,bg,bg,.95f);uniform.glUniform4F("SquareColor",square,square,square,.95f);});
    }

    public static void drawRoundBox(@Nonnull GuiGraphicsExtractor graphics, Rect square, Vector4f radius, int color) {
        com.lowdragmc.lowdraglib.client.shader.NativeScreenEffects.draw(graphics,com.lowdragmc.lowdraglib.client.shader.Shaders.ROUND_BOX_F,uniform->{
            float scale=screenBoxUniforms(graphics,square,uniform);
            uniform.glUniform4F("RoundRadius",radius.x*scale,radius.y*scale,radius.z*scale,radius.w*scale);
            uniform.fillRGBAColor("Color",color);uniform.glUniform1F("Blur",2);
        });
    }
    public static void drawProgressRoundBox(@Nonnull GuiGraphicsExtractor graphics, Rect square, Vector4f radius, int color1, int color2, float progress) {
        com.lowdragmc.lowdraglib.client.shader.NativeScreenEffects.draw(graphics,com.lowdragmc.lowdraglib.client.shader.Shaders.PROGRESS_ROUND_BOX_F,uniform->{
            uniform.glUniform4F("SquareVertex",square.left,square.up,square.right,square.down);
            uniform.glUniform4F("RoundRadius",radius.x,radius.y,radius.z,radius.w);
            uniform.fillRGBAColor("Color1",color1);uniform.fillRGBAColor("Color2",color2);
            uniform.glUniform1F("Blur",2);uniform.glUniform1F("Progress",progress);
        });
    }
    public static void drawFrameRoundBox(@Nonnull GuiGraphicsExtractor graphics, Rect square, float thickness, Vector4f radius1, Vector4f radius2, int color) {
        com.lowdragmc.lowdraglib.client.shader.NativeScreenEffects.draw(graphics,com.lowdragmc.lowdraglib.client.shader.Shaders.FRAME_ROUND_BOX_F,uniform->{
            float scale=screenBoxUniforms(graphics,square,uniform);
            uniform.glUniform4F("RoundRadius1",radius1.x*scale,radius1.y*scale,radius1.z*scale,radius1.w*scale);
            uniform.glUniform4F("RoundRadius2",radius2.x*scale,radius2.y*scale,radius2.z*scale,radius2.w*scale);
            uniform.glUniform1F("Thickness",thickness*scale);uniform.fillRGBAColor("Color",color);uniform.glUniform1F("Blur",2);
        });
    }
    private static float screenBoxUniforms(GuiGraphicsExtractor graphics,Rect square,com.lowdragmc.lowdraglib.client.shader.uniform.UniformCache uniform) {
        uniform.glUniformMatrix4F("PoseStack",new org.joml.Matrix4f());
        var pose=graphics.pose();var first=pose.transformPosition(square.left-.25f,square.up-.25f,new org.joml.Vector2f());
        var second=pose.transformPosition(square.right-.25f,square.down-.25f,new org.joml.Vector2f());
        uniform.glUniform4F("SquareVertex",first.x,first.y,second.x,second.y);
        return pose.m00()+pose.m10();
    }
    public static void drawRoundLine(@Nonnull GuiGraphicsExtractor graphics, Position begin, Position end, int width, int color1, int color2) {
        com.lowdragmc.lowdraglib.client.shader.NativeScreenEffects.draw(graphics,com.lowdragmc.lowdraglib.client.shader.Shaders.ROUND_LINE_F,uniform->{
            uniform.glUniform1F("Width",width);uniform.glUniform2F("Point1",begin.x,begin.y);uniform.glUniform2F("Point2",end.x,end.y);
            uniform.fillRGBAColor("Color1",color1);uniform.fillRGBAColor("Color2",color2);uniform.glUniform1F("Blur",2);
        });
    }

    public static void drawTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, List<Component> tooltipTexts, ItemStack tooltipStack, @Nullable TooltipComponent tooltipComponent, Font tooltipFont) {
        graphics.setTooltipForNextFrame(tooltipFont, tooltipTexts, Optional.ofNullable(tooltipComponent), tooltipStack, mouseX, mouseY);
    }

    public static ClientTooltipComponent getClientTooltipComponent(TooltipComponent component) {
        return ClientTooltipComponent.create(component);
    }
}
