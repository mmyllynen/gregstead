package com.gregtechceu.gtceu.integration.rei.oreprocessing;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.integration.xei.widgets.GTOreByProductWidget;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.integration.rei.ModularDisplay;

import net.minecraft.resources.Identifier;

import java.util.Optional;

public class GTOreProcessingDisplay extends ModularDisplay<WidgetGroup> {

    private final Material material;

    public GTOreProcessingDisplay(Material material) {
        super(() -> new GTOreByProductWidget(material), GTOreProcessingDisplayCategory.CATEGORY);
        this.material = material;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return Optional.of(material.getResourceLocation());
    }
}
