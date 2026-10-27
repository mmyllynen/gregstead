package com.lowdragmc.lowdraglib.integration.rei;

import com.lowdragmc.lowdraglib.integration.jei.ModularWrapper;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ModularForegroundRenderWidget extends Widget {
    public final ModularWrapper<?> modular;

    public ModularForegroundRenderWidget(ModularWrapper<?> modular) {
        this.modular = modular;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(-modular.getLeft(), -modular.getTop());


            com.lowdragmc.lowdraglib.transfer.client.GuiOverlays.render(graphics, () -> modular.modularUI.mainGroup.drawInForeground(graphics, mouseX, mouseY, partialTick));
            modular.modularUI.mainGroup.drawOverlay(graphics, mouseX, mouseY, partialTick);

            // do not draw tooltips here, do it from recipe viewer.
            if (modular.isShouldRenderTooltips() && modular.tooltipTexts != null && !modular.tooltipTexts.isEmpty()) {
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, modular.tooltipTexts, Optional.ofNullable(modular.tooltipComponent), modular.tooltipStack, mouseX, mouseY);
            }



        } finally {
            graphics.pose().popMatrix();
        }

    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }
}
