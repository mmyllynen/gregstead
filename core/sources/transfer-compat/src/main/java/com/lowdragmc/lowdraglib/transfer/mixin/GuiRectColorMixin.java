package com.lowdragmc.lowdraglib.transfer.mixin;
import com.lowdragmc.lowdraglib.transfer.client.GuiColorScope;
import net.minecraft.client.renderer.state.gui.ColoredRectangleRenderState;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ColoredRectangleRenderState.class)
public abstract class GuiRectColorMixin {
    @Shadow @Final @Mutable private int col1;
    @Shadow @Final @Mutable private int col2;
    @Inject(method = "<init>(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2fc;IIIIIILnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V", at = @At("RETURN"))
    private void gttransfercompat$capture(CallbackInfo ci) {
        col1 = ARGB.multiply(col1, GuiColorScope.current());
        col2 = ARGB.multiply(col2, GuiColorScope.current());
    }
}
