package dev.emi.emi.screen.widget;

import com.lowdragmc.lowdraglib.transfer.client.GuiColorScope;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Extract the original search spin, tint and highlight in a scope that cannot leak into later widgets. */
public final class NativeSearchEffect {
    private NativeSearchEffect() {}

    public static void extract(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
            float radians, int tint, boolean highlight, Runnable field) {
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(x + width / 2, y + height / 2);
            graphics.pose().rotate(radians);
            graphics.pose().translate(-(x + width / 2), -(y + height / 2));
            GuiColorScope.run(tint, () -> {
                field.run();
                if (highlight) {
                    int border = 0xffeeee00;
                    graphics.fill(x - 1, y - 1, x + width + 1, y, border);
                    graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, border);
                    graphics.fill(x - 1, y - 1, x, y + height + 1, border);
                    graphics.fill(x + width, y - 1, x + width + 1, y + height + 1, border);
                }
            });
        } finally {
            graphics.pose().popMatrix();
        }
    }
}
