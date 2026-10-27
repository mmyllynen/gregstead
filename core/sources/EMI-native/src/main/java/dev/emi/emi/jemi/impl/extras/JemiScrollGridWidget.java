package dev.emi.emi.jemi.impl.extras;

import java.util.List;
import java.util.function.Consumer;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IScrollGridWidget;

/** Native JEI grid owns visible rows, scrolling, slot placement and hovered-slot lookup. */
public final class JemiScrollGridWidget extends mezz.jei.library.gui.widgets.ScrollGridRecipeWidget {
    private final Consumer<List<IRecipeSlotDrawable>> claim;
    public JemiScrollGridWidget(List<IRecipeSlotDrawable> slots, int columns, int rows,
                               Consumer<List<IRecipeSlotDrawable>> claim) {
        super(area(columns, rows), columns, rows, slots);
        this.claim = claim;
        claim.accept(List.copyOf(slots));
    }
    private static mezz.jei.common.util.ImmutableRect2i area(int columns, int rows) {
        var size = calculateSize(columns, rows);
        return new mezz.jei.common.util.ImmutableRect2i(0, 0, size.width(), size.height());
    }
    @Override public JemiScrollGridWidget setSlots(List<IRecipeSlotDrawable> slots) {
        super.setSlots(slots);
        claim.accept(List.copyOf(slots));
        return this;
    }
}
