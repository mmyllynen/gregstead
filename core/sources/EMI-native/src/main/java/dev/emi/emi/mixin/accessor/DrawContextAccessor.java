package dev.emi.emi.mixin.accessor;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiGraphicsExtractor.class)
public interface DrawContextAccessor {
    @Accessor("tooltipStack") ItemStack emi$tooltipStack();
    @Accessor("tooltipStack") void emi$tooltipStack(ItemStack stack);
    @Invoker("setTooltipForNextFrameInternal")
    void emi$queueTooltip(Font font,List<ClientTooltipComponent> lines,int x,int y,ClientTooltipPositioner positioner,
        Identifier style,boolean replace,boolean extraSpace);
}
