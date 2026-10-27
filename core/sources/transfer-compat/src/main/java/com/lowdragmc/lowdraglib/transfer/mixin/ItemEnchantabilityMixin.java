package com.lowdragmc.lowdraglib.transfer.mixin;
import com.lowdragmc.lowdraglib.transfer.enchantment.LegacyEnchantable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ItemStack.class)
public abstract class ItemEnchantabilityMixin {
    @Inject(method="isEnchantable",at=@At("HEAD"),cancellable=true)
    private void legacyEnchantable(CallbackInfoReturnable<Boolean> callback) {
        var stack=(ItemStack)(Object)this;
        if(!stack.getComponentsPatch().isPatched(DataComponents.ENCHANTABLE) && stack.getItem() instanceof LegacyEnchantable source)
            callback.setReturnValue(source.isEnchantable(stack) && !stack.isEnchanted());
    }
}
