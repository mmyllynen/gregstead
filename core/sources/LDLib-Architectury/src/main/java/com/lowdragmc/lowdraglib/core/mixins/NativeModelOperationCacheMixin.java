package com.lowdragmc.lowdraglib.core.mixins;

import com.lowdragmc.lowdraglib.client.model.NativeModelOperationCache;
import net.minecraft.client.resources.model.ModelBaker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Map;

@Mixin(targets = "net.minecraft.client.resources.model.ModelBakery$ModelBakerImpl")
public abstract class NativeModelOperationCacheMixin {
    @Shadow @Final @Mutable
    private Map<ModelBaker.SharedOperationKey<Object>, Object> operationCache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ldlib$allowNestedModelOperations(CallbackInfo ci) {
        operationCache = new NativeModelOperationCache<>();
    }
}
