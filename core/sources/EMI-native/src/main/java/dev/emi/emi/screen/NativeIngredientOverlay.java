package dev.emi.emi.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Foreground states for the quantity and four-pixel ingredient badges. */
public final class NativeIngredientOverlay {
    private static final Identifier WIDGETS = Identifier.fromNamespaceAndPath("emi", "textures/gui/widgets.png");

    private NativeIngredientOverlay() {}

    public static void amount(GuiGraphicsExtractor graphics, int x, int y, Component amount) {
        var font = Minecraft.getInstance().font;
        graphics.nextStratum();
        graphics.text(font, amount, x + 17 - Math.min(14, font.width(amount)), y + 9, -1, true);
    }

    public static void badge(GuiGraphicsExtractor graphics, int x, int y, int u) {
        graphics.nextStratum();
        graphics.blit(RenderPipelines.GUI_TEXTURED, WIDGETS, x, y, u, 252, 4, 4, 256, 256);
    }
    public static void microGlyph(GuiGraphicsExtractor graphics, Identifier texture, int x, int y,
                                  int u, int v, int width, int color) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, 7, 256, 256, color);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v + 7, width, 7, 256, 256);
    }

}
