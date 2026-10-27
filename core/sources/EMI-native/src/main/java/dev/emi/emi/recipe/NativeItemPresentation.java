package dev.emi.emi.recipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import java.util.List;

public final class NativeItemPresentation {
    private NativeItemPresentation() {}
    public static <T> T component(Item item,DataComponentPatch patch,DataComponentType<? extends T> type) {
        return patch.get(item.components(),type);
    }
    public static void icon(GuiGraphicsExtractor graphics,ItemStack stack,int x,int y) {
        graphics.fakeItem(stack,x,y);
        // EMI draws its long quantity separately; native durability/cooldown decorations remain.
        graphics.itemDecorations(Minecraft.getInstance().font,stack,x,y,"");
    }
    public static List<Component> tooltip(ItemStack stack) {
        var client=Minecraft.getInstance();
        if(client.isSameThread())return stack.getTooltipLines(Item.TooltipContext.of(client.level),client.player,TooltipFlag.NORMAL);
        var context=client.level==null?Item.TooltipContext.EMPTY:Item.TooltipContext.of(client.level.registryAccess());
        return stack.getTooltipLines(context,null,TooltipFlag.NORMAL);
    }
}
