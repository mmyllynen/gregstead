package com.lowdragmc.lowdraglib.transfer.mixin;
import com.lowdragmc.lowdraglib.transfer.name.DynamicItemName;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ItemStack.class)
public abstract class ItemNameMixin {
    @Inject(method="getItemName",at=@At("HEAD"),cancellable=true)
    private void dynamicItemName(CallbackInfoReturnable<Component> callback) {
        var stack=(ItemStack)(Object)this;
        if (stack.getItem() instanceof DynamicItemName source && !stack.getComponentsPatch().isPatched(DataComponents.ITEM_NAME))
            callback.setReturnValue(source.dynamicItemName(stack));
    }
}
