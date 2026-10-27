package com.lowdragmc.lowdraglib.integration.emi;

import com.lowdragmc.lowdraglib.integration.jei.ModularWrapper;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Optional;

public class ModularForegroundRenderWidget extends Widget {
    public final ModularWrapper<?> modular;

    public ModularForegroundRenderWidget(ModularWrapper<?> modular) {
        this.modular = modular;
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(0, 0, modular.getWidget().getSize().width, modular.getWidget().getSize().height);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.nextStratum();
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(-modular.getLeft(), -modular.getTop());
            com.lowdragmc.lowdraglib.transfer.client.GuiOverlays.render(graphics,
                () -> modular.modularUI.mainGroup.drawInForeground(graphics, mouseX, mouseY, partialTick));
            modular.modularUI.mainGroup.drawOverlay(graphics, mouseX, mouseY, partialTick);
            if (modular.isShouldRenderTooltips() && modular.tooltipTexts != null && !modular.tooltipTexts.isEmpty()) {
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, modular.tooltipTexts,
                    Optional.ofNullable(modular.tooltipComponent), mouseX, mouseY);
            }
        } finally { graphics.pose().popMatrix(); }
    }
}
