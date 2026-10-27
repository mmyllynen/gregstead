package com.lowdragmc.lowdraglib.transfer.tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;
public interface LegacyBlockTooltip {
    void appendBlockTooltip(ItemStack stack,Item.TooltipContext context,List<Component> lines,TooltipFlag flag);
}
