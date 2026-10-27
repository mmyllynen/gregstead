package com.gregtechceu.gtceu.integration.map.ftbchunks.veins.fluid;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import dev.ftb.mods.ftbchunks.client.gui.map.RegionMapPanel;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import lombok.Getter;

public class FluidVeinWidget extends Widget {

    @Getter
    private final FluidVeinIcon mapIcon;

    public FluidVeinWidget(RegionMapPanel p, FluidVeinIcon mapIcon) {
        super(p);
        this.mapIcon = mapIcon;
    }

    @Override
    public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        if (!shouldDraw()) {
            return;
        }
        dev.ftb.mods.ftblibrary.client.icon.IconHelper.renderIcon(
                mapIcon.getIcon(isMouseOver() ? 230 : 205, isMouseOver()), graphics, x, y, w, h);
    }
}
