package dev.emi.emi.runtime;

import com.lowdragmc.lowdraglib.transfer.client.GuiColorScope;
import java.util.WeakHashMap;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;

/** Tint shared by wrappers of one extraction frame, without changing global render state. */
public final class NativeGuiTint {
    private static final WeakHashMap<GuiGraphicsExtractor, Integer> COLORS = new WeakHashMap<>();
    private NativeGuiTint() {}

    public static void set(GuiGraphicsExtractor graphics, float r, float g, float b, float a) {
        int color = ARGB.colorFromFloat(a, r, g, b);
        if (color == -1) COLORS.remove(graphics); else COLORS.put(graphics, color);
    }

    public static int current(GuiGraphicsExtractor graphics) {
        return COLORS.getOrDefault(graphics, -1);
    }

    public static void extract(GuiGraphicsExtractor graphics, Runnable action) {
        GuiColorScope.run(current(graphics), action);
    }
    public static void texture(GuiGraphicsExtractor graphics, net.minecraft.resources.Identifier texture,
                               int x, int y, int width, int height, float u, float v,
                               int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        graphics.blit(NativeGuiComposition.texturePipeline(),
            texture, x, y, u, v, width, height, regionWidth, regionHeight,
            textureWidth, textureHeight, current(graphics));
    }

}
