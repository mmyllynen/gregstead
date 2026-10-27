package com.lowdragmc.lowdraglib.integration.jei;

import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;

import java.util.Optional;

public class ModularForegroundRecipeWidget implements IRecipeWidget {
    public final ModularWrapper<?> modular;

    public ModularForegroundRecipeWidget(ModularWrapper<?> modular) {
        this.modular = modular;
    }

    @Override
    public ScreenPosition getPosition() {
        return new ScreenPosition(0, 0);
    }

    @Override
    public void drawWidget(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();

        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(-modular.getLeft(), -modular.getTop());


            com.lowdragmc.lowdraglib.transfer.client.GuiOverlays.render(graphics, () -> modular.modularUI.mainGroup.drawInForeground(graphics, (int) mouseX, (int) mouseY, partialTick));
            modular.modularUI.mainGroup.drawOverlay(graphics, (int) mouseX, (int) mouseY, partialTick);

            // do not draw tooltips here, do it from recipe viewer.
            if (modular.isShouldRenderTooltips() && modular.tooltipTexts != null && !modular.tooltipTexts.isEmpty()) {
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, modular.tooltipTexts, Optional.ofNullable(modular.tooltipComponent), modular.tooltipStack, (int) mouseX, (int) mouseY);
            }


        } finally {
            graphics.pose().popMatrix();
        }
    }
}
