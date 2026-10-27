package com.gregtechceu.gtceu.core.mixins.kubejs;

import com.gregtechceu.gtceu.core.IResourceLocationExtensions;

import net.minecraft.resources.Identifier;

import dev.latvian.mods.kubejs.util.KubeIdentifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = KubeIdentifier.class)
public abstract class KubeIdentifierMixin {

    @Inject(at = @At("HEAD"), method = "wrap", cancellable = true)
    private static void wrap(Object args, CallbackInfoReturnable<KubeIdentifier> cir) {
        if (args instanceof String stringArg) {
            if (!stringArg.contains(":")) {
                var loc = Identifier.fromNamespaceAndPath("kubejs", stringArg);
                ((IResourceLocationExtensions) (Object) loc).gtm$setImplicit(true);
                cir.setReturnValue(new KubeIdentifier(loc));
            }
        }
    }
}
