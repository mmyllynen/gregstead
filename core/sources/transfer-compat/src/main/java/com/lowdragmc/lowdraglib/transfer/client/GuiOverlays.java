package com.lowdragmc.lowdraglib.transfer.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Matrix3x2f;
import java.util.ArrayList;

/** Foreground popups execute after the enclosing widget traversal with their extraction context. */
public final class GuiOverlays {
    private record Entry(Matrix3x2f pose, ScreenRectangle clip, int color, Runnable draw) {}
    private record Frame(GuiGraphicsExtractor graphics, ArrayList<Entry> entries) {}
    private static final ThreadLocal<Frame> ACTIVE = new ThreadLocal<>();
    private GuiOverlays() {}
    public static void render(GuiGraphicsExtractor graphics, Runnable foreground) {
        var current = ACTIVE.get();
        if (current != null && current.graphics() == graphics) { foreground.run(); return; }
        var frame = new Frame(graphics, new ArrayList<>());
        ACTIVE.set(frame);
        try {
            foreground.run();
            for (int i = 0; i < frame.entries().size(); i++) replay(graphics, frame.entries().get(i));
        } finally {
            if (current == null) ACTIVE.remove(); else ACTIVE.set(current);
        }
    }
    public static void popup(GuiGraphicsExtractor graphics, Runnable draw) {
        var entry = new Entry(new Matrix3x2f(graphics.pose()), graphics.peekScissorStack(), GuiColorScope.current(), draw);
        var current = ACTIVE.get();
        if (current != null && current.graphics() == graphics) current.entries().add(entry);
        else render(graphics, () -> ACTIVE.get().entries().add(entry));
    }
    private static void replay(GuiGraphicsExtractor graphics, Entry entry) {
        graphics.nextStratum();
        graphics.pose().pushMatrix();
        boolean clipped = false;
        try {
            graphics.pose().identity();
            if (entry.clip() != null) {
                var clip = entry.clip();
                graphics.enableScissor(clip.left(), clip.top(), clip.right(), clip.bottom());
                clipped = true;
            }
            graphics.pose().set(entry.pose());
            GuiColorScope.captured(entry.color(), entry.draw());
        } finally {
            if (clipped) graphics.disableScissor();
            graphics.pose().popMatrix();
        }
    }
}
