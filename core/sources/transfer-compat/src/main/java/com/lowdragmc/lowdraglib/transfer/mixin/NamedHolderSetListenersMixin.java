package com.lowdragmc.lowdraglib.transfer.mixin;

import net.minecraft.core.HolderSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Native parallel registry decoding may subscribe while tags notify their existing listeners. */
@Mixin(HolderSet.Named.class)
public abstract class NamedHolderSetListenersMixin {
    @Shadow @Final @Mutable private List<Runnable> invalidationCallbacks;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gtport$snapshotListeners(CallbackInfo callback) {
        invalidationCallbacks = new CopyOnWriteArrayList<>(invalidationCallbacks);
    }
}
