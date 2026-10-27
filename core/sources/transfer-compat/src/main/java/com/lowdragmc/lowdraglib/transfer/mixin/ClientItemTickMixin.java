package com.lowdragmc.lowdraglib.transfer.mixin;
import com.lowdragmc.lowdraglib.transfer.lifecycle.ItemTickBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemStack.class)
public abstract class ClientItemTickMixin {
    @Inject(method="inventoryTick",at=@At("TAIL"))
    private void legacyClientTick(Level level, Entity owner, @Nullable EquipmentSlot slot, CallbackInfo callback) {
        if(level.isClientSide())ItemTickBridge.dispatch((ItemStack)(Object)this,level,owner,slot);
    }
}
