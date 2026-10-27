package com.gregtechceu.gtceu.integration.rei.circuit;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.integration.xei.widgets.GTProgrammedCircuitWidget;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.integration.rei.IGui2Renderer;
import com.lowdragmc.lowdraglib.integration.rei.ModularDisplay;
import com.lowdragmc.lowdraglib.integration.rei.ModularUIDisplayCategory;
import com.lowdragmc.lowdraglib.math.Size;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import lombok.Getter;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;

import java.util.Optional;

public class GTProgrammedCircuitCategory extends
                                         ModularUIDisplayCategory<GTProgrammedCircuitCategory.GTProgrammedCircuitDisplay> {

    private static final Identifier ID = GTCEu.id("programmed_circuit");
    public static final CategoryIdentifier<GTProgrammedCircuitDisplay> CATEGORY = CategoryIdentifier.of(ID);

    @Getter
    private final Renderer icon;
    @Getter
    private final Size size;

    public GTProgrammedCircuitCategory() {
        this.icon = IGui2Renderer.toDrawable(new ItemStackTexture(GTItems.PROGRAMMED_CIRCUIT.asItem()));
        this.size = Size.of(158, 80);
    }

    @Override
    public CategoryIdentifier<? extends GTProgrammedCircuitDisplay> getCategoryIdentifier() {
        return CATEGORY;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.programmed_circuit");
    }

    @Override
    public int getDisplayHeight() {
        return getSize().height;
    }

    @Override
    public int getDisplayWidth(GTProgrammedCircuitDisplay display) {
        return getSize().width;
    }

    public static class GTProgrammedCircuitDisplay extends ModularDisplay<WidgetGroup> {

        public GTProgrammedCircuitDisplay() {
            super(GTProgrammedCircuitWidget::new, GTProgrammedCircuitCategory.CATEGORY);
        }

        @Override
        public Optional<Identifier> getDisplayLocation() {
            return Optional.of(ID);
        }
    }
}
