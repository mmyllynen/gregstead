package com.lowdragmc.lowdraglib.transfer.mixin;
import com.lowdragmc.lowdraglib.transfer.name.DynamicDescription;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Item.class)
public abstract class ItemDescriptionMixin {
    @Inject(method="getDescriptionId",at=@At("HEAD"),cancellable=true)
    private void dynamicDescription(CallbackInfoReturnable<String> callback) {
        if ((Object)this instanceof DynamicDescription source) callback.setReturnValue(source.dynamicDescriptionId());
    }
}
