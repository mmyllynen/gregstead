package dev.emi.emi.screen.tooltip;

import dev.emi.emi.mixin.accessor.DrawContextAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public final class NativeTooltipQueue {
    private NativeTooltipQueue() {}
    public static void queue(GuiGraphicsExtractor graphics,Font font,List<ClientTooltipComponent> lines,int x,int y,
            ClientTooltipPositioner positioner,ItemStack stack) {
        if(lines.isEmpty())return;
        var access=(DrawContextAccessor)graphics;
        var previous=access.emi$tooltipStack();
        var captured=stack.copy();
        try {
            access.emi$tooltipStack(captured);
            access.emi$queueTooltip(font,new ArrayList<>(lines),x,Math.max(y,16),positioner,
                captured.get(DataComponents.TOOLTIP_STYLE),true,true);
        } finally { access.emi$tooltipStack(previous); }
    }
}
