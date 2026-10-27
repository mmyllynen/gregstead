package com.lowdragmc.lowdraglib.transfer.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
public final class GuiItems {
    private GuiItems() {}
    public static void drawScaled(GuiGraphicsExtractor graphics, ItemStack item, float x, float y,
                                  float width, float height, int color, String amountText) {
        if (width <= 0 || height <= 0) return;
        graphics.pose().pushMatrix();
        try {
            graphics.pose().scale(width / 16f, height / 16f);
            graphics.pose().translate(x * 16 / width, y * 16 / height);
            draw(graphics, item, 0, 0, color, amountText);
        } finally {
            graphics.pose().popMatrix();
        }
    }
    public static void draw(GuiGraphicsExtractor graphics, ItemStack item, int x, int y, int color, String amountText) {
        draw(graphics, item, x, y, color, amountText, 0);
    }
    public static void draw(GuiGraphicsExtractor graphics, ItemStack item, int x, int y, int color, String amountText, int decorationOffset) {
        GuiColorScope.run(color, () -> {
            graphics.item(item, x, y);
            graphics.itemDecorations(Minecraft.getInstance().font, item, x + decorationOffset, y, amountText);
        });
    }
}
