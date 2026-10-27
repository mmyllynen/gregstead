package com.gregtechceu.gtceu.integration.rei.multipage;

import com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.integration.rei.ModularDisplay;

import net.minecraft.resources.Identifier;

import java.util.Optional;

public class MultiblockInfoDisplay extends ModularDisplay<WidgetGroup> {

    public final MultiblockMachineDefinition definition;

    public MultiblockInfoDisplay(MultiblockMachineDefinition definition) {
        super(() -> PatternPreviewWidget.getPatternWidget(definition), MultiblockInfoDisplayCategory.CATEGORY);
        this.definition = definition;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return Optional.of(definition.getId());
    }
}
