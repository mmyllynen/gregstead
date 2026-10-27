package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.common.data.GTItems;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.math.Position;
import com.lowdragmc.lowdraglib.math.Size;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public class CoverConfigurator implements IFancyConfigurator {

    protected final ICoverable coverable;
    // runtime
    @Nullable
    protected final Direction side;
    @Nullable
    protected final CoverBehavior coverBehavior;

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.cover_setting.title");
    }

    @Override
    public IGuiTexture getIcon() {
        return new ItemStackTexture(GTItems.ITEM_FILTER.get());
    }

    @Override
    public Widget createConfigurator() {
        WidgetGroup group = new WidgetGroup(Position.of(0, 0));
        if (side != null) {
            if (coverable.getCoverAtSide(side) instanceof IUICover iuiCover) {
                Widget coverConfigurator = iuiCover.createUIWidget();
                coverConfigurator.setBackground(GuiTextures.BACKGROUND);
                coverConfigurator.setSelfPosition(Position.of(4, -4));
                group.addWidget(coverConfigurator);
                group.setSize(Size.of(Math.max(120, coverConfigurator.getSize().width + 8),
                        Math.max(80, 80 + coverConfigurator.getSize().height)));
            }
        }
        return group;
    }
}
