package com.lowdragmc.lowdraglib.transfer.client;

import net.minecraft.util.ARGB;

/** Extraction-local modulation, captured by render states and restored even on failure. */
public final class GuiColorScope {
    private static final ThreadLocal<Integer> COLOR = ThreadLocal.withInitial(() -> -1);
    private GuiColorScope() {}
    public static int current() { return COLOR.get(); }
    public static void run(int color, Runnable action) {
        int previous = current();
        COLOR.set(ARGB.multiply(previous, color));
        try { action.run(); } finally { COLOR.set(previous); }
    }
    static void captured(int color, Runnable action) {
        int previous = current();
        COLOR.set(color);
        try { action.run(); } finally { COLOR.set(previous); }
    }
    public static int premultiplied(int color) {
        int alpha = color >>> 24;
        return alpha << 24 | (((color >>> 16) & 255) * alpha / 255) << 16
                | (((color >>> 8) & 255) * alpha / 255) << 8 | (color & 255) * alpha / 255;
    }
}
