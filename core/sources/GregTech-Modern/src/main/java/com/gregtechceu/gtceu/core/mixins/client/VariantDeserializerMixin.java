package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.util.VariantRotationHelpers;
import net.minecraft.client.renderer.block.dispatch.Variant;
import com.mojang.serialization.MapCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Variant.SimpleModelState.class)
public class VariantDeserializerMixin {
    @Shadow @Final @Mutable public static MapCodec<Variant.SimpleModelState> MAP_CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"), require = 1)
    private static void gtceu$acceptLegacyZRotation(CallbackInfo callback) {
        MAP_CODEC = VariantRotationHelpers.legacyCompatibleStateCodec();
    }
}
