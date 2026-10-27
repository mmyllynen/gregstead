package com.lowdragmc.lowdraglib.transfer.mixin;
import com.lowdragmc.lowdraglib.transfer.tooltip.LegacyBlockTooltip;
import com.lowdragmc.lowdraglib.transfer.tooltip.LegacyTooltips;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Item.class)
public abstract class BlockTooltipMixin {
    @Inject(method="appendHoverText",at=@At("HEAD"))
    private void legacyBlockTooltip(ItemStack stack,Item.TooltipContext context,TooltipDisplay display,Consumer<Component> output,TooltipFlag flag,CallbackInfo callback) {
        if((Object)this instanceof BlockItem item && item.getBlock() instanceof LegacyBlockTooltip block)
            LegacyTooltips.emit(output,lines->block.appendBlockTooltip(stack,context,lines,flag));
    }
}
