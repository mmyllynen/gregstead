package dev.emi.emi.runtime;

import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import java.util.Optional;

/** Explicit queued write masks for legacy highlight and recipe-preview alpha composition. */
public final class NativeGuiComposition {
    public static final RenderPipeline HIGHLIGHT = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("emi", "pipeline/highlight"))
        .withColorTargetState(new ColorTargetState(Optional.of(BlendFunction.TRANSLUCENT),
            ColorTargetState.DEFAULT.format(), ColorTargetState.WRITE_COLOR)).build();
    public static final RenderPipeline ALPHA_TEXTURE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("emi", "pipeline/recipe_alpha"))
        .withColorTargetState(new ColorTargetState(Optional.empty(),
            ColorTargetState.DEFAULT.format(), ColorTargetState.WRITE_ALPHA)).build();
    private static final ThreadLocal<Boolean> ALPHA = ThreadLocal.withInitial(() -> false);
    private NativeGuiComposition() {}
    public static RenderPipeline texturePipeline() {
        return ALPHA.get() ? ALPHA_TEXTURE : RenderPipelines.GUI_TEXTURED;
    }
    public static void alphaMask(GuiGraphicsExtractor graphics, Runnable draw) {
        boolean previous = ALPHA.get();
        graphics.nextStratum();
        ALPHA.set(true);
        try { draw.run(); } finally { ALPHA.set(previous); }
    }
    public static void highlight(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.nextStratum();
        graphics.fill(HIGHLIGHT, x, y, x + width, y + height, 0x80ffffff);
    }
}
