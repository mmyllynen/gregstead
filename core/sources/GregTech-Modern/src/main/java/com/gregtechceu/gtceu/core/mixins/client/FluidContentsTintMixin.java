package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.NativeFluidItemTint;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.color.item.FluidContentsTint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidContentsTint.class)
public class FluidContentsTintMixin {
    @Inject(method="calculate", at=@At("RETURN"), cancellable=true)
    private void gtceu$fluidLayer(ItemStack stack, ClientLevel level, LivingEntity owner,
                                  CallbackInfoReturnable<Integer> result) {
        result.setReturnValue(NativeFluidItemTint.apply(stack, result.getReturnValue()));
    }
}
