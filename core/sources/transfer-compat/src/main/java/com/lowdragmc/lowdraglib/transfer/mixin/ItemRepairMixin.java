package com.lowdragmc.lowdraglib.transfer.mixin;
import com.lowdragmc.lowdraglib.transfer.repair.LegacyRepair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ItemStack.class)
public abstract class ItemRepairMixin {
    @Inject(method="isValidRepairItem",at=@At("HEAD"),cancellable=true)
    private void legacyRepair(ItemStack candidate,CallbackInfoReturnable<Boolean> callback) {
        var stack=(ItemStack)(Object)this;
        if(!stack.getComponentsPatch().isPatched(DataComponents.REPAIRABLE) && stack.getItem() instanceof LegacyRepair source)
            callback.setReturnValue(source.isValidRepairItem(stack,candidate));
    }
}
