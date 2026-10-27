package com.gregtechceu.gtceu.integration.jade;

import com.lowdragmc.lowdraglib.transfer.client.GuiPrimitives;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import snownee.jade.api.ui.*;
import snownee.jade.api.view.ProgressView;
import snownee.jade.impl.ui.StyledElement;
import java.util.Optional;

/** Native view payload keeps GT's explicit fill colors and gray framed bars. */
public final class NativeJadeProgress {
    private NativeJadeProgress() {}
    public static void add(snownee.jade.api.ITooltip tooltip, float progress, Component text, int color, boolean viewGroup) {
        tooltip.add(create(progress, text, color, viewGroup));
    }
    public static void addItem(snownee.jade.api.ITooltip tooltip, net.minecraft.world.item.ItemStack stack) {
        tooltip.add(JadeUI.smallItem(stack));
    }
    public static void appendItem(snownee.jade.api.ITooltip tooltip, net.minecraft.world.item.ItemStack stack) {
        tooltip.append(JadeUI.smallItem(stack));
    }
    public static Element create(float progress, Component text, int color, boolean viewGroup) {
        var box = new BoxStyle(Optional.empty(), ColorPalette.DEFAULT,
                viewGroup ? Optional.of(new int[]{2,2,2,2}) : Optional.empty(), 1,
                Optional.empty(), Optional.empty(), false) {
            @Override public void render(GuiGraphicsExtractor graphics, StyledElement element,
                    float x, float y, float width, float height, float alpha) {
                float borderWidth = viewGroup ? 0.75f : 1;
                if (viewGroup) rect(graphics, x + borderWidth, y + borderWidth,
                        width - 2 * borderWidth, height - 2 * borderWidth, faded(0x44444444, alpha));
                int border = faded(0xff555555, alpha);
                rect(graphics, x, y, width, borderWidth, border);
                rect(graphics, x, y + height - borderWidth, width, borderWidth, border);
                rect(graphics, x, y + borderWidth, borderWidth, height - 2 * borderWidth, border);
                rect(graphics, x + width - borderWidth, y + borderWidth, borderWidth, height - 2 * borderWidth, border);
            }
        };
        var view = new ProgressView(ProgressView.Part.of(progress, color), text, JadeUI.progressStyle(), box);
        return JadeUI.progress(view);
    }
    private static int faded(int color, float alpha) { return ARGB.color(Math.round(ARGB.alpha(color) * alpha), color & 0xffffff); }
    private static void rect(GuiGraphicsExtractor graphics, float x, float y, float w, float h, int color) {
        GuiPrimitives.gradient(graphics, x, y, w, h, color, color, false);
    }
}
