package dev.emi.emi.jemi.widget;

import dev.emi.emi.api.widget.Bounds;

/** Hover padding extends input bounds without changing the drawn ingredient's bounds. */
public record NativeHoverPadding(int top, int bottom, int left, int right) {
    public static final NativeHoverPadding NONE = new NativeHoverPadding(0,0,0,0);
    public NativeHoverPadding {
        if(top<0 || bottom<0 || left<0 || right<0) throw new IllegalArgumentException("Hover padding must be non-negative");
    }
    public Bounds expand(Bounds content) {
        return new Bounds(Math.subtractExact(content.x(), left), Math.subtractExact(content.y(), top),
            Math.addExact(Math.addExact(content.width(),left),right),
            Math.addExact(Math.addExact(content.height(),top),bottom));
    }
}
