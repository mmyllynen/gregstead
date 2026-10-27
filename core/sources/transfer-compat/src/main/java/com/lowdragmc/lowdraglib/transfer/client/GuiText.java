package com.lowdragmc.lowdraglib.transfer.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** Queues native text while retaining legacy RGB colors and returned cursor position. */
public final class GuiText {
    private GuiText() {}
    private static int color(int color) { return (color & 0xff000000) == 0 ? color | 0xff000000 : color; }
    /** Resolve every style, including hover-only text, using native fractional glyph advances in visual sequence order. */
    public static net.minecraft.network.chat.Style styleAtWidth(Font font, FormattedCharSequence text, float width) {
        if (width < 0) return null;
        float[] remaining = {width};
        net.minecraft.network.chat.Style[] found = {null};
        text.accept((index, style, codepoint) -> {
            remaining[0] -= font.getSplitter().stringWidth(FormattedCharSequence.codepoint(codepoint, style));
            if (remaining[0] < 0) {
                found[0] = style;
                return false;
            }
            return true;
        });
        return found[0];
    }
    public static int drawString(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color) {
        return drawString(graphics, font, text, x, y, color, true);
    }
    public static int drawString(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color, boolean shadow) {
        if (text == null) return 0;
        graphics.text(font, text, x, y, color(color), shadow);
        return (int) (x + font.getSplitter().stringWidth(text)) + (shadow ? 1 : 0);
    }
    public static int drawString(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color) {
        return drawString(graphics, font, text, x, y, color, true);
    }
    public static int drawString(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color, boolean shadow) {
        graphics.text(font, text, x, y, color(color), shadow);
        return (int) (x + font.getSplitter().stringWidth(text)) + (shadow ? 1 : 0);
    }
    public static int drawString(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        return drawString(graphics, font, text, x, y, color, true);
    }
    public static int drawString(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        graphics.text(font, text, x, y, color(color), shadow);
        return (int) (x + font.getSplitter().stringWidth(text)) + (shadow ? 1 : 0);
    }
    public static void centered(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color) {
        graphics.centeredText(font, text, x, y, color(color));
    }
    public static void centered(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color) {
        graphics.centeredText(font, text, x, y, color(color));
    }
    public static void centered(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        graphics.centeredText(font, text, x, y, color(color));
    }
}
