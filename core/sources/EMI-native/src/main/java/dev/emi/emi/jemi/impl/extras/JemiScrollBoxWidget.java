package dev.emi.emi.jemi.impl.extras;

/** Actual native scroll box owns contents, clipping, visibility, input and drag lifecycle. */
public final class JemiScrollBoxWidget extends mezz.jei.library.gui.widgets.ScrollBoxRecipeWidget {
    public JemiScrollBoxWidget(int x, int y, int width, int height) {
        super(width, height, x, y);
    }
}
