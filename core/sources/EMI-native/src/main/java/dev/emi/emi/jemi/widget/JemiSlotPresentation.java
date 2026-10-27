package dev.emi.emi.jemi.widget;

import dev.emi.emi.api.widget.Bounds;
import mezz.jei.api.gui.builder.ITooltipBuilder;

public interface JemiSlotPresentation {
    Bounds contentBounds();
    void buildTooltip(ITooltipBuilder tooltip);
}
