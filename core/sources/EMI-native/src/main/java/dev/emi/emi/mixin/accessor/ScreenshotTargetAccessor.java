package dev.emi.emi.mixin.accessor;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface ScreenshotTargetAccessor {
    @Mutable @Accessor("mainRenderTarget") void emi$screenshotTarget(RenderTarget target);
}
