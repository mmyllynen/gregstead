package fi.neogreg.uifixes.mixin;

import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** IPN 2.3.8's NeoForge listener pushes a matrix and never pops it. */
@Pseudo
@Mixin(targets = "org.anti_ad.mc.ipnext.neoforge.ForgeEventHandler", remap = false)
public abstract class InventoryProfilesForegroundMixin {
    @Redirect(
        method = "onForegroundRender",
        at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;pushMatrix()Lorg/joml/Matrix3x2fStack;"),
        require = 1
    )
    private Matrix3x2fStack neogreg$omitUnmatchedPush(Matrix3x2fStack pose) {
        // The listener performs no matching pop or transform. Keeping the stack
        // untouched preserves the caller's scope, including Core Lib's origin.
        return pose;
    }
}
